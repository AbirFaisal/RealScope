//! GPUI front end: ribbon, waveform canvas and a control panel.

use std::sync::mpsc;
use std::time::{Duration, Instant};

use gpui::{
    App, Application, Bounds, ClickEvent, Context, Div, ElementId, Hsla, IntoElement, PathBuilder, Pixels, Render,
    SharedString, Stateful, Task, TextRun, TitlebarOptions, Window, WindowBounds, WindowOptions, canvas, div, fill,
    point, prelude::*, px, rgb, size,
};

use crate::acquire::{self, Command, Event, Frame};
use crate::decode::{self, BAUDS, DataFormat, Kind, Parity, Protocol, Row, UartLines};
use crate::fft::{self, DB_PER_DIV, FFT_H_DIVS, FftSettings, FftUnit, FftWindow, Spectrum};
use crate::math::{self, MathOp, MathTrace};
use crate::measure::{self, Group, Measurements};
use crate::persist::{self, Saved};
use crate::phosphor::{self, Persistence, Phosphor, Run, TRACES};
use crate::settings::{ChannelSettings, EXT, RunMode, Settings, Slope, Sweep, TriggerMode};
use crate::tables::{
    self, ADC_MAX, COUNTS_PER_DIV, H_DIVS, MATH_VDIV_V, POS0_MAX, TDIV_MAX_IDX, TDIV_MIN_IDX, V_DIVS, VDIV_MV,
};

/// CH1, CH2 and the math trace.
const TRACE_COLORS: [u32; TRACES] = [0x42d4f5, 0x42f578, 0xd27dff];
const TRACE_NAMES: [&str; TRACES] = ["CH1", "CH2", "Math"];
/// Index of the math trace in per-trace arrays.
const MATH: usize = 2;
/// Trigger marker colour when triggering from the external input.
const EXT_TRIGGER_COLOR: u32 = 0xbbbbbb;

/// Width of the label column in the channel/trigger control rows.
const STEPPER_LABEL_W: f32 = 80.;

/// How often the measurements panel refreshes; faster than this is unreadable.
const MEASURE_INTERVAL: Duration = Duration::from_millis(250);

/// Space around the graticule for axis labels: CH1 volts left, CH2 volts right, time below.
const MARGIN_SIDE: f32 = 56.;
const MARGIN_TOP: f32 = 6.;
const MARGIN_BOTTOM: f32 = 20.;
const LABEL_SIZE: f32 = 11.;
const LABEL_GAP: f32 = 4.;
/// Height of one protocol-decode bus row under the graticule, and the gap above it.
const BUS_ROW_H: f32 = 20.;
const BUS_ROW_GAP: f32 = 3.;
/// Most lines shown in the decoded-frames list.
const DECODE_LIST_MAX: usize = 500;

pub fn run() {
    Application::new().run(|cx: &mut App| {
        let bounds = Bounds::centered(None, size(px(1280.), px(720.)), cx);
        cx.open_window(
            WindowOptions {
                window_bounds: Some(WindowBounds::Windowed(bounds)),
                titlebar: Some(TitlebarOptions { title: Some("RealScope".into()), ..Default::default() }),
                ..Default::default()
            },
            |_, cx| cx.new(ScopeApp::new),
        )
        .expect("opening window");
        cx.on_window_closed(|cx| cx.quit()).detach();
        cx.activate(true);
    });
}

struct ScopeApp {
    settings: Settings,
    settings_tx: mpsc::Sender<Command>,
    frame: Option<Frame>,
    status: String,
    device: Option<String>,
    /// Digital-phosphor display: accumulate captures instead of drawing the latest one.
    phosphor_on: bool,
    persistence: Persistence,
    phosphor: Phosphor,
    last_frame_at: Option<Instant>,
    /// Measurements panel, refreshed at a readable rate rather than every frame.
    measurements_on: bool,
    /// CH1, CH2, Math.
    measurements: [Option<Measurements>; TRACES],
    measured_at: Option<Instant>,
    /// Auto-set is running on the acquisition thread.
    autosetting: bool,
    _poll: Task<()>,
}

/// What the canvas draws for the waveforms: CH1, CH2, Math, `None`/empty when hidden.
enum Trace {
    Line([Option<Vec<i8>>; TRACES]),
    Phosphor([Vec<Run>; TRACES]),
}

/// Everything the spectrum pane draws.
struct SpectrumView {
    spectrum: Option<Spectrum>,
    /// The source sat at the screen edge, so the levels are a bound.
    clipped: bool,
    /// Shown instead of a curve.
    message: Option<&'static str>,
    settings: FftSettings,
    /// Source V/div, which is the Vrms/div of the linear scale.
    source_vpd: f64,
    hz_per_div: f64,
}

/// The math trace of a capture under the given settings (`None` when math is off).
fn math_of(frame: &Frame, s: &Settings) -> Option<MathTrace> {
    math::compute(frame.samples.each_ref().map(|s| s.as_deref()), s)
}

impl ScopeApp {
    fn new(cx: &mut Context<Self>) -> Self {
        let (settings_tx, settings_rx) = mpsc::channel();
        let (event_tx, event_rx) = mpsc::channel();
        let saved = persist::load();
        acquire::spawn(saved.scope, settings_rx, event_tx);

        // Drain worker events once per display frame; only the newest waveform is kept.
        let poll = cx.spawn(async move |this, cx| {
            loop {
                cx.background_executor().timer(Duration::from_millis(16)).await;
                let events: Vec<Event> = event_rx.try_iter().collect();
                if events.is_empty() {
                    continue;
                }
                let alive = this.update(cx, |this, cx| {
                    for e in events {
                        this.handle(e);
                    }
                    cx.notify();
                });
                if alive.is_err() {
                    break;
                }
            }
        });

        ScopeApp {
            settings: saved.scope,
            settings_tx,
            frame: None,
            status: "Searching for scope…".into(),
            device: None,
            phosphor_on: saved.phosphor_on,
            persistence: saved.persistence,
            phosphor: Phosphor::default(),
            last_frame_at: None,
            measurements_on: saved.measurements_on,
            measurements: [None, None, None],
            measured_at: None,
            autosetting: false,
            _poll: poll,
        }
    }

    fn handle(&mut self, event: Event) {
        match event {
            Event::Status(s) => self.status = s,
            Event::Connected { serial, version } => {
                self.status = "Connected".into();
                self.device = Some(format!("{serial} ({version})"));
            }
            Event::Frame(f) => {
                let now = Instant::now();
                if self.phosphor_on {
                    if let Some(last) = self.last_frame_at {
                        self.phosphor.decay(self.persistence.decay_factor(now - last));
                    }
                    for (ch, samples) in f.samples.iter().enumerate() {
                        if let Some(samples) = samples.as_deref().filter(|_| self.settings.ch[ch].on) {
                            self.phosphor.accumulate(ch, samples);
                        }
                    }
                    if let Some(t) = math_of(&f, &self.settings) {
                        self.phosphor.accumulate(MATH, &t.samples);
                    }
                }
                self.last_frame_at = Some(now);
                if self.measured_at.is_none_or(|t| now - t >= MEASURE_INTERVAL) {
                    self.update_measurements(&f);
                    self.measured_at = Some(now);
                }
                self.frame = Some(f);
            }
            Event::SingleDone => {
                // Hold the captured trace. A Single pressed again since the capture
                // has re-armed the worker already, but is rare enough to ignore.
                if self.settings.run == RunMode::Single {
                    self.settings.run = RunMode::Stopped;
                    let _ = self.settings_tx.send(Command::Apply(self.settings));
                }
            }
            Event::AutosetDone { mut settings, summary } => {
                self.autosetting = false;
                // The channel scales changed, so re-pick the math scale too.
                if settings.math.on {
                    settings.math.vdiv_idx = math::default_vdiv(&settings.ch);
                }
                if settings.fft.on {
                    settings.fft.ref_dbv = fft::default_ref_dbv(settings.trace_scale(settings.fft.source).volts_per_div);
                }
                self.settings = settings;
                // Echo back, so a control clicked while auto-set ran can't leave
                // the worker on different settings from the UI.
                let _ = self.settings_tx.send(Command::Apply(settings));
                self.phosphor.clear();
                self.measured_at = None;
                self.status = summary;
                self.save();
            }
            Event::Disconnected(e) => {
                self.autosetting = false;
                self.status = e;
                self.device = None;
                self.frame = None;
                self.phosphor.clear();
                self.measurements = [None, None, None];
            }
        }
    }

    fn update_measurements(&mut self, frame: &Frame) {
        let dt = 1.0 / tables::sample_rate_hz(self.settings.tdiv_idx);
        for ch in 0..2 {
            self.measurements[ch] = frame.samples[ch].as_deref().filter(|_| self.settings.ch[ch].on).map(|samples| {
                let mut m = measure::measure(samples, &self.settings.ch[ch], dt);
                m.counter = frame.counter_hz[ch];
                m
            });
        }
        self.measurements[MATH] = math_of(frame, &self.settings).map(|t| {
            let mut m = measure::measure(&t.samples, &self.settings.math, dt);
            m.clipped |= t.sources_clipped;
            m
        });
    }

    fn measurements_panel(&self) -> impl IntoElement {
        let cols: Vec<usize> = if self.settings.math.on { vec![0, 1, MATH] } else { vec![0, 1] };
        let empty = Measurements::default();
        let tables: Vec<_> = cols.iter().map(|&i| measure::table(self.measurements[i].as_ref().unwrap_or(&empty))).collect();
        let dim = rgb(0x888888);
        let row = |label: SharedString, cells: Vec<(SharedString, gpui::Rgba)>| {
            div()
                .flex()
                .gap_1()
                .child(div().w(px(58.)).text_color(dim).child(label))
                .children(cells.into_iter().map(|(value, color)| div().flex_1().text_right().text_color(color).child(value)))
        };
        let value_color = rgb(0xdddddd);
        let headers = cols.iter().map(|&i| (TRACE_NAMES[i].into(), rgb(TRACE_COLORS[i]))).collect();
        let mut panel = div()
            .w(px(if cols.len() > 2 { 310. } else { 240. }))
            .flex()
            .flex_col()
            .gap(px(2.))
            .p_2()
            .bg(rgb(0x181818))
            .text_xs()
            .child(div().text_sm().pb_1().child("Measurements"))
            .child(row("".into(), headers));
        let mut group = None;
        for (r, &(g, label, _)) in tables[0].iter().enumerate() {
            if group != Some(g) {
                group = Some(g);
                let title = match g {
                    Group::Time => "Time",
                    Group::Voltage => "Voltage",
                };
                panel = panel.child(div().pt_2().text_color(dim).border_b_1().border_color(rgb(0x2a2a2a)).child(title));
            }
            panel = panel.child(row(label.into(), tables.iter().map(|t| (t[r].2.clone().into(), value_color)).collect()));
        }
        if cols.iter().filter_map(|&i| self.measurements[i].as_ref()).any(|m| m.clipped) {
            panel = panel.child(div().pt_2().text_color(dim).child("? = signal off screen; value is a bound"));
        }
        panel
    }

    fn update_settings(&mut self, cx: &mut Context<Self>, f: &dyn Fn(&mut Settings)) {
        f(&mut self.settings);
        let _ = self.settings_tx.send(Command::Apply(self.settings));
        // Accumulated captures were drawn under the old scales.
        self.phosphor.clear();
        // Show readings for the new settings as soon as a frame arrives.
        self.measured_at = None;
        self.save();
        cx.notify();
    }

    /// Writes the current settings and display state so the next launch starts the same way.
    fn save(&self) {
        persist::save(&Saved {
            scope: self.settings,
            measurements_on: self.measurements_on,
            phosphor_on: self.phosphor_on,
            persistence: self.persistence,
        });
    }

    fn button_base(id: String, label: impl Into<SharedString>, active: bool) -> Stateful<Div> {
        let bg = if active { rgb(0x35557f) } else { rgb(0x2a2a2a) };
        div()
            .id(ElementId::Name(id.into()))
            .px_2()
            .py_1()
            .rounded_sm()
            .bg(bg)
            .hover(|s| s.bg(rgb(0x404a58)))
            .cursor_pointer()
            .child(label.into())
    }

    /// Button that changes scope settings (sent to the acquisition thread).
    fn button(
        &self,
        id: String,
        label: impl Into<SharedString>,
        active: bool,
        cx: &mut Context<Self>,
        f: impl Fn(&mut Settings) + 'static,
    ) -> impl IntoElement {
        Self::button_base(id, label, active)
            .on_click(cx.listener(move |this, _: &ClickEvent, _, cx| this.update_settings(cx, &f)))
    }

    /// Button that changes display-only state.
    fn view_button(
        &self,
        id: String,
        label: impl Into<SharedString>,
        active: bool,
        cx: &mut Context<Self>,
        f: impl Fn(&mut ScopeApp) + 'static,
    ) -> impl IntoElement {
        Self::button_base(id, label, active).on_click(cx.listener(move |this, _: &ClickEvent, _, cx| {
            f(this);
            this.save();
            cx.notify();
        }))
    }

    /// A labelled cluster of ribbon buttons.
    fn ribbon_group(title: &str, children: Vec<gpui::AnyElement>) -> impl IntoElement {
        div()
            .flex()
            .items_center()
            .gap_1()
            .pr_3()
            .border_r_1()
            .border_color(rgb(0x333333))
            .child(div().pr_1().text_xs().text_color(rgb(0x888888)).child(title.to_owned()))
            .children(children)
    }

    fn ribbon(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let mut display = vec![
            self.view_button("measurements".into(), "Measurements", self.measurements_on, cx, |this| {
                this.measurements_on = !this.measurements_on
            })
            .into_any_element(),
            self.view_button("phosphor".into(), "Phosphor", self.phosphor_on, cx, |this| {
                this.phosphor_on = !this.phosphor_on;
                this.phosphor.clear();
            })
            .into_any_element(),
        ];
        if self.phosphor_on {
            display.push(div().w(px(8.)).into_any_element());
            for p in Persistence::ALL {
                display.push(
                    self.view_button(format!("persist-{}", p.label()), p.label(), self.persistence == p, cx, move |this| {
                        this.persistence = p
                    })
                    .into_any_element(),
                );
            }
            display.push(div().w(px(8.)).into_any_element());
            display.push(self.view_button("phosphor-clear".into(), "Clear", false, cx, |this| this.phosphor.clear()).into_any_element());
        }
        div()
            .flex()
            .items_center()
            .gap_3()
            .px_2()
            .py_1()
            .bg(rgb(0x1c1c1c))
            .border_b_1()
            .border_color(rgb(0x333333))
            .child(Self::ribbon_group("Display", display))
    }

    /// `label  ◀ value ▶`
    fn stepper(
        &self,
        id: &str,
        label: &str,
        value: String,
        cx: &mut Context<Self>,
        dec: impl Fn(&mut Settings) + 'static,
        inc: impl Fn(&mut Settings) + 'static,
    ) -> impl IntoElement {
        div()
            .flex()
            .items_center()
            .gap_1()
            .child(div().w(px(STEPPER_LABEL_W)).text_color(rgb(0x999999)).child(label.to_owned()))
            .child(self.button(format!("{id}-dec"), "◀", false, cx, dec))
            .child(div().flex_1().text_center().child(value))
            .child(self.button(format!("{id}-inc"), "▶", false, cx, inc))
    }

    fn channel_panel(&self, i: usize, cx: &mut Context<Self>) -> impl IntoElement {
        let ch = self.settings.ch[i];
        let color = rgb(TRACE_COLORS[i]);
        div()
            .flex()
            .flex_col()
            .gap_1()
            .p_2()
            .rounded_md()
            .border_1()
            .border_color(if ch.on { color } else { rgb(0x333333) })
            .child(
                div()
                    .flex()
                    .justify_between()
                    .items_center()
                    .child(div().text_color(color).child(format!("CH{}", i + 1)))
                    .child(
                        div()
                            .flex()
                            .gap_1()
                            .child(self.button(format!("ch{i}-cpl"), ch.coupling.label(), false, cx, move |s| {
                                s.ch[i].coupling = s.ch[i].coupling.next()
                            }))
                            .child(self.button(
                                format!("ch{i}-on"),
                                if ch.on { "ON" } else { "OFF" },
                                ch.on,
                                cx,
                                move |s| s.ch[i].on = !s.ch[i].on,
                            )),
                    ),
            )
            .child(self.stepper(
                &format!("ch{i}-vdiv"),
                "Volts/div",
                ch.vdiv_label(),
                cx,
                move |s| s.ch[i].vdiv_idx = s.ch[i].vdiv_idx.saturating_sub(1),
                move |s| s.ch[i].vdiv_idx = (s.ch[i].vdiv_idx + 1).min(VDIV_MV.len() - 1),
            ))
            .child(self.stepper(
                &format!("ch{i}-pos"),
                "Position",
                format!("{:+.1} div", ch.pos0 as f64 / COUNTS_PER_DIV as f64),
                cx,
                move |s| s.ch[i].pos0 = ChannelSettings::clamp_pos0(s.ch[i].pos0 - COUNTS_PER_DIV / 5),
                move |s| s.ch[i].pos0 = ChannelSettings::clamp_pos0(s.ch[i].pos0 + COUNTS_PER_DIV / 5),
            ))
            .child(
                div()
                    .flex()
                    .items_center()
                    .gap_1()
                    .child(div().w(px(STEPPER_LABEL_W)).text_color(rgb(0x999999)).child("Attenuation"))
                    .child(self.button(format!("ch{i}-x1"), "x1", ch.probe == 1, cx, move |s| s.set_probe(i, 1)))
                    .child(self.button(format!("ch{i}-x10"), "x10", ch.probe == 10, cx, move |s| s.set_probe(i, 10))),
            )
    }

    fn math_panel(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let m = self.settings.math;
        let color = rgb(TRACE_COLORS[MATH]);
        let switch_on = |s: &mut Settings| {
            s.math.on = true;
            s.math.vdiv_idx = math::default_vdiv(&s.ch);
        };
        let mut panel = div()
            .flex()
            .flex_col()
            .gap_1()
            .p_2()
            .rounded_md()
            .border_1()
            .border_color(if m.on { color } else { rgb(0x333333) })
            .child(
                div()
                    .flex()
                    .justify_between()
                    .items_center()
                    .child(div().text_color(color).child("Math"))
                    .child(self.button("math-on".into(), if m.on { "ON" } else { "OFF" }, m.on, cx, move |s| {
                        if s.math.on { s.math.on = false } else { switch_on(s) }
                    })),
            )
            .child(div().flex().gap_1().children(MathOp::ALL.map(|op| {
                // Picking an operation also switches the trace on.
                self.button(format!("math-{op:?}"), op.label(), m.on && m.op == op, cx, move |s| {
                    if !s.math.on {
                        switch_on(s);
                    }
                    s.math.op = op;
                })
                .into_any_element()
            })));
        if m.on {
            panel = panel
                .child(self.stepper(
                    "math-vdiv",
                    "Volts/div",
                    m.vdiv_label(),
                    cx,
                    |s| s.math.vdiv_idx = s.math.vdiv_idx.saturating_sub(1),
                    |s| s.math.vdiv_idx = (s.math.vdiv_idx + 1).min(MATH_VDIV_V.len() - 1),
                ))
                .child(self.stepper(
                    "math-pos",
                    "Position",
                    format!("{:+.1} div", m.pos0 as f64 / COUNTS_PER_DIV as f64),
                    cx,
                    |s| s.math.pos0 = ChannelSettings::clamp_pos0(s.math.pos0 - COUNTS_PER_DIV / 5),
                    |s| s.math.pos0 = ChannelSettings::clamp_pos0(s.math.pos0 + COUNTS_PER_DIV / 5),
                ));
        }
        panel
    }

    fn fft_panel(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let f = self.settings.fft;
        let fs = tables::sample_rate_hz(self.settings.tdiv_idx);
        let color = rgb(0xdddddd);
        // Start the display with a full-screen signal near the top line.
        let pick_ref = |s: &mut Settings| s.fft.ref_dbv = fft::default_ref_dbv(s.trace_scale(s.fft.source).volts_per_div);
        let mut panel = div()
            .flex()
            .flex_col()
            .gap_1()
            .p_2()
            .rounded_md()
            .border_1()
            .border_color(if f.on { color } else { rgb(0x333333) })
            .child(
                div()
                    .flex()
                    .justify_between()
                    .items_center()
                    .child("FFT")
                    .child(self.button("fft-on".into(), if f.on { "ON" } else { "OFF" }, f.on, cx, move |s| {
                        s.fft.on = !s.fft.on;
                        if s.fft.on {
                            pick_ref(s);
                        }
                    })),
            );
        if !f.on {
            return panel;
        }
        let row = || div().flex().gap_1();
        panel = panel
            .child(row().children((0..TRACES).map(|src| {
                self.button(format!("fft-src{src}"), TRACE_NAMES[src], f.source == src, cx, move |s| {
                    s.fft.source = src;
                    pick_ref(s);
                })
                .into_any_element()
            })))
            .child(row().children(FftWindow::ALL.map(|w| {
                self.button(format!("fft-{w:?}"), w.label(), f.window == w, cx, move |s| s.fft.window = w).into_any_element()
            })))
            .child(
                row()
                    .child(self.button("fft-dbv".into(), "dBV", f.unit == FftUnit::Dbv, cx, |s| s.fft.unit = FftUnit::Dbv))
                    .child(self.button("fft-vrms".into(), "Vrms", f.unit == FftUnit::Vrms, cx, |s| s.fft.unit = FftUnit::Vrms)),
            );
        if f.unit == FftUnit::Dbv {
            panel = panel
                .child(self.stepper(
                    "fft-dbdiv",
                    "dB/div",
                    format!("{} dB", f.db_per_div()),
                    cx,
                    |s| s.fft.db_div_idx = s.fft.db_div_idx.saturating_sub(1),
                    |s| s.fft.db_div_idx = (s.fft.db_div_idx + 1).min(DB_PER_DIV.len() - 1),
                ))
                .child(self.stepper(
                    "fft-ref",
                    "Ref level",
                    format!("{} dBV", f.ref_dbv),
                    cx,
                    |s| s.fft.ref_dbv -= s.fft.db_per_div() as i32,
                    |s| s.fft.ref_dbv += s.fft.db_per_div() as i32,
                ));
        }
        panel.child(self.stepper(
            "fft-hzdiv",
            "Hz/div",
            tables::format_si(fft::hz_per_div(fs, f.zoom), "Hz"),
            cx,
            move |s| s.fft.zoom = (s.fft.zoom + 1).min(fft::max_zoom(fs)),
            move |s| s.fft.zoom = s.fft.zoom.min(fft::max_zoom(fs)).saturating_sub(1),
        ))
    }

    fn decode_panel(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let d = self.settings.decode;
        let row = || div().flex().gap_1().items_center();
        let caption = |t: &str| div().w(px(STEPPER_LABEL_W)).text_color(rgb(0x999999)).child(t.to_owned());
        let panel = div()
            .flex()
            .flex_col()
            .gap_1()
            .p_2()
            .rounded_md()
            .border_1()
            .border_color(if d.on { rgb(0xdddddd) } else { rgb(0x333333) })
            .child(
                div()
                    .flex()
                    .justify_between()
                    .items_center()
                    .child("Decode")
                    .child(self.button("dec-on".into(), if d.on { "ON" } else { "OFF" }, d.on, cx, |s| {
                        s.decode.on = !s.decode.on
                    })),
            );
        if !d.on {
            return panel;
        }
        let mut panel = panel.child(row().children(Protocol::ALL.map(|p| {
            self.button(format!("dec-{p:?}"), p.label(), d.protocol == p, cx, move |s| s.decode.protocol = p).into_any_element()
        })));
        // `[CH1][CH2]` choice of the channel stored by `set`.
        let channel_row = |this: &Self, id: &str, title: &str, current: usize, set: fn(&mut Settings, usize), cx: &mut Context<Self>| {
            row().child(caption(title)).children((0..2).map(|c| {
                this.button(format!("{id}-{c}"), TRACE_NAMES[c], current == c, cx, move |s| set(s, c)).into_any_element()
            }))
        };
        match d.protocol {
            Protocol::Uart => {
                let u = d.uart;
                let baud_idx = BAUDS.iter().position(|&b| b == u.baud).unwrap_or(4);
                panel = panel
                    .child(row().child(caption("Lines")).children(
                        [(UartLines::Ch1, "CH1"), (UartLines::Ch2, "CH2"), (UartLines::Both, "Both")].map(|(l, name)| {
                            self.button(format!("dec-uart-{l:?}"), name, u.lines == l, cx, move |s| s.decode.uart.lines = l)
                                .into_any_element()
                        }),
                    ))
                    .child(self.stepper(
                        "dec-baud",
                        "Baud",
                        u.baud.to_string(),
                        cx,
                        move |s| s.decode.uart.baud = BAUDS[baud_idx.saturating_sub(1)],
                        move |s| s.decode.uart.baud = BAUDS[(baud_idx + 1).min(BAUDS.len() - 1)],
                    ))
                    .child(self.stepper(
                        "dec-bits",
                        "Data bits",
                        u.data_bits.to_string(),
                        cx,
                        |s| s.decode.uart.data_bits = (s.decode.uart.data_bits - 1).max(5),
                        |s| s.decode.uart.data_bits = (s.decode.uart.data_bits + 1).min(9),
                    ))
                    .child(row().child(caption("Parity")).children(
                        [(Parity::None, "None"), (Parity::Even, "Even"), (Parity::Odd, "Odd")].map(|(p, name)| {
                            self.button(format!("dec-par-{p:?}"), name, u.parity == p, cx, move |s| s.decode.uart.parity = p)
                                .into_any_element()
                        }),
                    ))
                    .child(row().child(caption("Idle level")).children([(false, "High"), (true, "Low (RS-232)")].map(
                        |(inv, name)| {
                            self.button(format!("dec-inv-{inv}"), name, u.inverted == inv, cx, move |s| {
                                s.decode.uart.inverted = inv
                            })
                            .into_any_element()
                        },
                    )));
            }
            Protocol::I2c => {
                panel = panel
                    .child(channel_row(self, "dec-scl", "SCL on", d.i2c.scl, |s, c| s.decode.i2c.scl = c, cx))
                    .child(row().child(caption("SDA on")).child(TRACE_NAMES[1 - d.i2c.scl]));
            }
            Protocol::Spi => {
                let sp = d.spi;
                panel = panel
                    .child(channel_row(self, "dec-clk", "Clock on", sp.clock, |s, c| s.decode.spi.clock = c, cx))
                    .child(row().child(caption("Data on")).child(TRACE_NAMES[1 - sp.clock]))
                    .child(self.stepper(
                        "dec-mode",
                        "Mode",
                        format!("{} ({})", sp.mode, if matches!(sp.mode, 0 | 3) { "↑" } else { "↓" }),
                        cx,
                        |s| s.decode.spi.mode = s.decode.spi.mode.saturating_sub(1),
                        |s| s.decode.spi.mode = (s.decode.spi.mode + 1).min(3),
                    ))
                    .child(self.stepper(
                        "dec-word",
                        "Word bits",
                        sp.bits.to_string(),
                        cx,
                        |s| s.decode.spi.bits = (s.decode.spi.bits - 1).max(4),
                        |s| s.decode.spi.bits = (s.decode.spi.bits + 1).min(16),
                    ))
                    .child(row().child(caption("Bit order")).children([(false, "MSB first"), (true, "LSB first")].map(
                        |(lsb, name)| {
                            self.button(format!("dec-lsb-{lsb}"), name, sp.lsb_first == lsb, cx, move |s| {
                                s.decode.spi.lsb_first = lsb
                            })
                            .into_any_element()
                        },
                    )));
            }
            Protocol::OneWire => {
                panel = panel.child(channel_row(self, "dec-1w", "Line", d.onewire.line, |s, c| s.decode.onewire.line = c, cx));
            }
        }
        panel.child(row().child(caption("Format")).children(DataFormat::ALL.map(|f| {
            self.button(format!("dec-fmt-{f:?}"), f.label(), d.format == f, cx, move |s| s.decode.format = f).into_any_element()
        })))
    }

    /// Decoded items of all rows in time order, with times relative to the trigger.
    fn decode_list(&self, rows: &[Row]) -> impl IntoElement {
        let dt = 1.0 / tables::sample_rate_hz(self.settings.tdiv_idx);
        let dim = rgb(0x888888);
        let mut entries: Vec<(f64, &str, &decode::Item)> = rows
            .iter()
            .flat_map(|r| {
                r.items
                    .iter()
                    .filter(|i| i.kind != Kind::Ack)
                    .map(move |i| ((i.start - r.len as f64 / 2.0) * dt, r.label.as_str(), i))
            })
            .collect();
        entries.sort_by(|a, b| a.0.total_cmp(&b.0));
        let mut list = div()
            .id("decode-list")
            .overflow_y_scroll()
            .w(px(250.))
            .flex()
            .flex_col()
            .gap(px(2.))
            .p_2()
            .bg(rgb(0x181818))
            .text_xs()
            .child(div().text_sm().pb_1().child(format!("Decoded {}", self.settings.decode.protocol.label())));
        for r in rows.iter().filter(|r| r.message.is_some()) {
            list = list.child(div().text_color(dim).child(format!("{}: {}", r.label, r.message.as_deref().unwrap_or(""))));
        }
        let several = rows.len() > 1;
        for (t, label, item) in entries.iter().take(DECODE_LIST_MAX) {
            let color = if item.error.is_some() { rgb(0xff8a8a) } else { rgb(0xdddddd) };
            list = list.child(
                div()
                    .flex()
                    .gap_2()
                    .child(div().w(px(64.)).flex_none().text_right().text_color(dim).child(tables::format_si(*t, "s")))
                    .when(several, |d| d.child(div().flex_none().text_color(dim).child(label.to_string())))
                    .child(div().text_color(color).child(item.describe())),
            );
        }
        if entries.len() > DECODE_LIST_MAX {
            list = list.child(div().text_color(dim).child(format!("… {} more", entries.len() - DECODE_LIST_MAX)));
        }
        list
    }

    /// What the spectrum pane shows, computed from the held capture.
    fn spectrum_view(&self) -> SpectrumView {
        let s = &self.settings;
        let src = s.fft.source;
        let fs = tables::sample_rate_hz(s.tdiv_idx);
        let data = self.frame.as_ref().and_then(|frame| {
            if src == MATH {
                math_of(frame, s).map(|t| (t.samples, t.sources_clipped))
            } else {
                frame.samples[src].clone().map(|v| {
                    let clipped = v.iter().any(|&c| (c as i32).abs() >= ADC_MAX);
                    (v, clipped)
                })
            }
        });
        let message = if src == MATH && !s.math.on {
            Some("Turn Math on to see its spectrum")
        } else if data.is_none() {
            Some("No data")
        } else {
            None
        };
        SpectrumView {
            spectrum: data.as_ref().map(|(v, _)| fft::spectrum(v, s.trace_scale(src), fs, s.fft.window)),
            clipped: data.is_some_and(|(_, c)| c),
            message,
            settings: s.fft,
            source_vpd: s.trace_scale(src).volts_per_div,
            hz_per_div: fft::hz_per_div(fs, s.fft.zoom),
        }
    }

    fn controls(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let s = self.settings;
        div()
            .id("controls")
            .overflow_y_scroll()
            .w(px(280.))
            .flex()
            .flex_col()
            .gap_2()
            .p_2()
            .bg(rgb(0x181818))
            .child(
                div()
                    .flex()
                    .gap_1()
                    .child(self.button(
                        "run".into(),
                        if s.run == RunMode::Running { "■ Stop" } else { "▶ Run" },
                        s.run == RunMode::Running,
                        cx,
                        |s| s.run = if s.run == RunMode::Running { RunMode::Stopped } else { RunMode::Running },
                    ))
                    .child(if s.per_channel_trigger() {
                        // The per-channel trigger word has no sweep bits, so no single shot.
                        self.view_button("single".into(), "Single", false, cx, |this| {
                            this.status = "Single needs a shared trigger".into()
                        })
                        .into_any_element()
                    } else {
                        self.button("single".into(), "Single", s.run == RunMode::Single, cx, |s| s.run = RunMode::Single)
                            .into_any_element()
                    })
                    .child(self.view_button("autoset".into(), "Auto-set", self.autosetting, cx, |this| {
                        if this.autosetting {
                            return;
                        }
                        if this.device.is_none() {
                            this.status = "Auto-set needs a connected scope".into();
                            return;
                        }
                        this.autosetting = true;
                        let _ = this.settings_tx.send(Command::Autoset);
                    })),
            )
            .child(self.channel_panel(0, cx))
            .child(self.channel_panel(1, cx))
            .child(self.math_panel(cx))
            .child(self.fft_panel(cx))
            .child(self.decode_panel(cx))
            .child(
                div()
                    .flex()
                    .flex_col()
                    .gap_1()
                    .p_2()
                    .rounded_md()
                    .border_1()
                    .border_color(rgb(0x333333))
                    .child("Horizontal")
                    .child(self.stepper(
                        "tdiv",
                        "Time/div",
                        tables::tdiv_label(s.tdiv_idx),
                        cx,
                        |s| s.tdiv_idx = s.tdiv_idx.saturating_sub(1).max(TDIV_MIN_IDX),
                        |s| s.tdiv_idx = (s.tdiv_idx + 1).min(TDIV_MAX_IDX),
                    ))
                    .child(
                        div()
                            .text_color(rgb(0x999999))
                            .child(format!("Sample rate {}", tables::format_si(tables::sample_rate_hz(s.tdiv_idx), "Sa/s"))),
                    ),
            )
            .child(self.trigger_panel(cx))
    }

    fn trigger_panel(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let s = self.settings;
        let src = s.trigger.source;
        let per_channel = s.per_channel_trigger();
        let row = || div().flex().gap_1();
        let dim = rgb(0x999999);
        // Level steps by 1/5 division of the channel, within the screen.
        let nudge = |ch: usize, up: bool| {
            move |s: &mut Settings| {
                let step = s.ch[ch].volts_per_div() / 5.0;
                let (above, below) = s.level_limit(ch);
                let level =
                    if s.per_channel_trigger() { &mut s.trigger.per_channel[ch].level_v } else { &mut s.trigger.level_v };
                *level = if up { (*level + step).min(above) } else { (*level - step).max(-below) };
            }
        };
        let panel = div()
            .flex()
            .flex_col()
            .gap_1()
            .p_2()
            .rounded_md()
            .border_1()
            .border_color(rgb(0x333333))
            .child("Trigger")
            .child(
                row()
                    .child(self.button("trg-shared".into(), "Shared", !per_channel, cx, |s| {
                        s.set_trigger_mode(TriggerMode::Shared)
                    }))
                    .child(self.button("trg-per-channel".into(), "Per channel", per_channel, cx, |s| {
                        s.set_trigger_mode(TriggerMode::PerChannel)
                    })),
            );
        if per_channel {
            let mut panel = panel.child(div().text_color(dim).child("Auto sweep, each channel on its own edge"));
            for ch in 0..2 {
                let e = s.trigger.per_channel[ch];
                let color = rgb(TRACE_COLORS[ch]);
                panel = panel.child(
                    div()
                        .flex()
                        .flex_col()
                        .gap_1()
                        .pl_2()
                        .border_l_2()
                        .border_color(color)
                        .child(
                            row()
                                .items_center()
                                .child(div().w(px(40.)).text_color(color).child(TRACE_NAMES[ch]))
                                .child(self.button(format!("trg{ch}-rise"), "↑ Rise", e.slope == Slope::Rising, cx, move |s| {
                                    s.trigger.per_channel[ch].slope = Slope::Rising
                                }))
                                .child(self.button(format!("trg{ch}-fall"), "↓ Fall", e.slope == Slope::Falling, cx, move |s| {
                                    s.trigger.per_channel[ch].slope = Slope::Falling
                                })),
                        )
                        .child(self.stepper(
                            &format!("trg{ch}-level"),
                            "Level",
                            tables::format_si(e.level_v, "V"),
                            cx,
                            nudge(ch, false),
                            nudge(ch, true),
                        )),
                );
            }
            return panel;
        }
        let level_row = if let Some(c) = s.trigger_channel() {
            self.stepper("trg-level", "Level", tables::format_si(s.trigger.level_v, "V"), cx, nudge(c, false), nudge(c, true))
                .into_any_element()
        } else {
            div()
                .flex()
                .items_center()
                .gap_1()
                .child(div().w(px(STEPPER_LABEL_W)).text_color(dim).child("Level"))
                .child(div().text_color(dim).child("set by external signal"))
                .into_any_element()
        };
        panel
            .child(
                row()
                    .child(self.button("trg-auto".into(), "Auto", s.trigger.sweep == Sweep::Auto, cx, |s| {
                        s.trigger.sweep = Sweep::Auto
                    }))
                    .child(self.button("trg-normal".into(), "Normal", s.trigger.sweep == Sweep::Normal, cx, |s| {
                        s.trigger.sweep = Sweep::Normal
                    })),
            )
            .child(
                row()
                    .child(self.button("trg-ch1".into(), "CH1", src == 0, cx, |s| s.trigger.source = 0))
                    .child(self.button("trg-ch2".into(), "CH2", src == 1, cx, |s| s.trigger.source = 1))
                    .child(self.button("trg-ext".into(), "EXT", src == EXT, cx, |s| s.trigger.source = EXT)),
            )
            .child(
                row()
                    .child(self.button("trg-rise".into(), "↑ Rise", s.trigger.slope == Slope::Rising, cx, |s| {
                        s.trigger.slope = Slope::Rising
                    }))
                    .child(self.button("trg-fall".into(), "↓ Fall", s.trigger.slope == Slope::Falling, cx, |s| {
                        s.trigger.slope = Slope::Falling
                    })),
            )
            .child(level_row)
    }

    fn status_line(&self) -> impl IntoElement {
        let s = &self.settings;
        let trig = match (s.run, &self.frame) {
            (RunMode::Single, _) => "Armed",
            (RunMode::Stopped, _) => "Stopped",
            // Each channel reports its own trigger; in shared mode the scope sets both bits.
            (RunMode::Running, Some(f)) if s.per_channel_trigger() => match f.channel_triggered {
                [true, true] => "Trig'd CH1+CH2",
                [true, false] => "Trig'd CH1",
                [false, true] => "Trig'd CH2",
                [false, false] => "Auto",
            },
            (RunMode::Running, Some(f)) if f.triggered => "Trig'd",
            (RunMode::Running, Some(_)) => "Auto",
            (RunMode::Running, None) => "—",
        };
        div()
            .flex()
            .gap_4()
            .px_2()
            .text_color(rgb(0xaaaaaa))
            .child(self.device.clone().unwrap_or_else(|| "No scope".into()))
            .child(format!("{}/div", tables::tdiv_label(s.tdiv_idx)))
            .child(tables::format_si(tables::sample_rate_hz(s.tdiv_idx), "Sa/s"))
            .child(trig)
            .child(div().flex_1())
            .child(self.status.clone())
    }
}

impl Render for ScopeApp {
    fn render(&mut self, _window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        let settings = self.settings;
        let shown = [settings.ch[0].on, settings.ch[1].on, settings.math.on];
        let spectrum = settings.fft.on.then(|| self.spectrum_view());
        let decoded = settings.decode.on.then(|| {
            let f = self.frame.as_ref();
            decode::decode([0, 1].map(|i| f.and_then(|f| f.samples[i].as_deref())), &settings)
        });
        let bus = decoded.clone().unwrap_or_default();
        let trace = if self.phosphor_on {
            Trace::Phosphor(std::array::from_fn(|i| if shown[i] { self.phosphor.runs(i) } else { Vec::new() }))
        } else {
            let f = self.frame.as_ref();
            let channel = |i: usize| f.and_then(|f| f.samples[i].clone()).filter(|_| shown[i]);
            Trace::Line([channel(0), channel(1), f.and_then(|f| math_of(f, &settings)).map(|t| t.samples)])
        };
        div()
            .flex()
            .flex_col()
            .size_full()
            .bg(rgb(0x101010))
            .text_color(rgb(0xdddddd))
            .text_sm()
            .child(self.ribbon(cx))
            .child(
                div()
                    .flex_1()
                    .flex()
                    .when(self.measurements_on, |row| row.child(self.measurements_panel()))
                    .when_some(decoded.as_deref(), |row, rows| row.child(self.decode_list(rows)))
                    .child(
                        div()
                            .flex_1()
                            .flex()
                            .flex_col()
                            .gap_1()
                            .p_2()
                            .child(
                                div()
                                    .flex_1()
                                    .child(canvas(|_, _, _| (), move |b, _, window, cx| paint_scope(b, &trace, &settings, &bus, window, cx)).size_full()),
                            )
                            .when_some(spectrum, |col, view| {
                                col.child(
                                    div()
                                        .flex_1()
                                        .child(canvas(|_, _, _| (), move |b, _, window, cx| paint_spectrum(b, &view, window, cx)).size_full()),
                                )
                            })
                            .child(self.status_line()),
                    )
                    .child(self.controls(cx)),
            )
    }
}

#[derive(Clone, Copy)]
enum Align {
    Left,
    Center,
    Right,
}

/// Paints one line of text with its anchor point at `(x, y)`: `align` picks
/// which horizontal edge sits on `x`, and the text is vertically centred on `y`.
/// The result is clamped horizontally into `clip_x`.
fn paint_label(
    text: String,
    color: Hsla,
    (x, y): (f32, f32),
    align: Align,
    clip_x: (f32, f32),
    window: &mut Window,
    cx: &mut App,
) {
    let run = TextRun {
        len: text.len(),
        font: window.text_style().font(),
        color,
        background_color: None,
        underline: None,
        strikethrough: None,
    };
    let line = window.text_system().shape_line(text.into(), px(LABEL_SIZE), &[run], None);
    let width = f32::from(line.width);
    let left = match align {
        Align::Left => x,
        Align::Center => x - width / 2.,
        Align::Right => x - width,
    };
    let left = left.min(clip_x.1 - width).max(clip_x.0);
    let line_height = LABEL_SIZE * 1.3;
    let _ = line.paint(point(px(left), px(y - line_height / 2.)), px(line_height), window, cx);
}

fn paint_scope(bounds: Bounds<Pixels>, trace: &Trace, settings: &Settings, bus: &[Row], window: &mut Window, cx: &mut App) {
    window.paint_quad(fill(bounds, rgb(0x000000)));

    // Voltage label columns: CH1 left, CH2 right. Math takes the column of a
    // hidden channel, or an extra one on the outer left when both are shown.
    let shown = [settings.ch[0].on, settings.ch[1].on, settings.math.on];
    // (right side?, columns out from the graticule)
    let math_column = if !shown[0] { (false, 0) } else if !shown[1] { (true, 0) } else { (false, 1) };
    let columns = [(false, 0), (true, 0), math_column];
    let left_margin = if shown[MATH] && math_column == (false, 1) { 2. * MARGIN_SIDE } else { MARGIN_SIDE };

    // Graticule rectangle, inset to leave room for the axis labels.
    let bx = f32::from(bounds.origin.x);
    let bw = f32::from(bounds.size.width);
    let x0 = bx + left_margin;
    let y0 = f32::from(bounds.origin.y) + MARGIN_TOP;
    let w = (bw - left_margin - MARGIN_SIDE).max(1.);
    // Decode bus rows sit between the graticule and the time labels.
    let bus_h = bus.len() as f32 * (BUS_ROW_H + BUS_ROW_GAP);
    let h = (f32::from(bounds.size.height) - MARGIN_TOP - MARGIN_BOTTOM - bus_h).max(1.);
    let full = 2.0 * ADC_MAX as f32;
    // ADC code -> screen y: +125 at the top, -125 at the bottom.
    let y_of = |code: f32| y0 + h / 2.0 - code / full * h;
    let clip = (bx, bx + bw);

    // Time axis: label every division if there is room, otherwise every 2nd or 5th.
    let div_w = w / H_DIVS as f32;
    let every = if div_w >= 56. { 1 } else if div_w >= 28. { 2 } else { 5 };
    let time_color: Hsla = rgb(0x8a8a8a).into();
    for i in (0..=H_DIVS).step_by(every) {
        let x = x0 + div_w * i as f32;
        let text = tables::format_si(tables::grid_time_s(settings.tdiv_idx, i), "s");
        paint_label(text, time_color, (x, y0 + h + bus_h + MARGIN_BOTTOM / 2.), Align::Center, clip, window, cx);
    }
    for (r, row) in bus.iter().enumerate() {
        let y = y0 + h + BUS_ROW_GAP + r as f32 * (BUS_ROW_H + BUS_ROW_GAP);
        paint_bus_row(row, settings.decode.format, (x0, w), y, clip, window, cx);
    }

    // Voltage axes: each trace has its own V/div and position, so each gets
    // its own scale in its colour.
    let volts_at = |i: usize, code: i32| if i == MATH { settings.math.code_to_volts(code) } else { settings.ch[i].code_to_volts(code) };
    for i in (0..TRACES).filter(|&i| shown[i]) {
        let color: Hsla = rgb(TRACE_COLORS[i]).into();
        let (right, k) = columns[i];
        let (x, align) = if right {
            (x0 + w + LABEL_GAP, Align::Left)
        } else {
            (x0 - LABEL_GAP - k as f32 * MARGIN_SIDE, Align::Right)
        };
        for j in 0..=V_DIVS {
            let code = ADC_MAX - COUNTS_PER_DIV * j as i32;
            let text = tables::format_si(volts_at(i, code), "V");
            paint_label(text, color, (x, y_of(code as f32)), align, clip, window, cx);
        }
    }

    let grid: Hsla = rgb(0x2a2a2a).into();
    let axis: Hsla = rgb(0x4a4a4a).into();
    for i in 0..=H_DIVS {
        let x = x0 + w * i as f32 / H_DIVS as f32;
        let c = if i == H_DIVS / 2 { axis } else { grid };
        window.paint_quad(fill(Bounds::new(point(px(x), px(y0)), size(px(1.), px(h))), c));
    }
    for i in 0..=V_DIVS {
        let y = y0 + h * i as f32 / V_DIVS as f32;
        let c = if i == V_DIVS / 2 { axis } else { grid };
        window.paint_quad(fill(Bounds::new(point(px(x0), px(y)), size(px(w), px(1.))), c));
    }

    // Channel zero markers on the left edge, trigger level on the right.
    let pos0 = [settings.ch[0].pos0, settings.ch[1].pos0, settings.math.pos0];
    for i in (0..TRACES).filter(|&i| shown[i]) {
        let y = y_of(pos0[i].clamp(-POS0_MAX.min(ADC_MAX), ADC_MAX) as f32);
        window.paint_quad(fill(Bounds::new(point(px(x0), px(y - 4.)), size(px(8.), px(8.))), rgb(TRACE_COLORS[i])));
    }
    // In per-channel mode each shown channel has its own level; the shared
    // trigger point marker at the top is then neutral.
    let per_channel = settings.per_channel_trigger();
    for ch in (0..2).filter(|&c| !per_channel || settings.ch[c].on) {
        if let Some(code) = settings.level_code(ch) {
            let ty = y_of(code as f32);
            window.paint_quad(fill(Bounds::new(point(px(x0 + w - 8.), px(ty - 4.)), size(px(8.), px(8.))), rgb(TRACE_COLORS[ch])));
        }
    }
    let tc = match settings.trigger_channel() {
        Some(c) if !per_channel => rgb(TRACE_COLORS[c]),
        _ => rgb(EXT_TRIGGER_COLOR),
    };
    window.paint_quad(fill(Bounds::new(point(px(x0 + w / 2. - 4.), px(y0)), size(px(8.), px(6.))), tc));

    match trace {
        Trace::Line(traces) => {
            for (i, samples) in traces.iter().enumerate() {
                if let Some(samples) = samples {
                    paint_trace(samples, rgb(TRACE_COLORS[i]).into(), x0, w, &y_of, window);
                }
            }
        }
        Trace::Phosphor(runs) => {
            // Each run is one quad; brightness is the trace colour's alpha.
            let col_w = w / phosphor::COLS as f32;
            for (i, runs) in runs.iter().enumerate() {
                let mut color: Hsla = rgb(TRACE_COLORS[i]).into();
                for run in runs {
                    color.a = run.level as f32 / phosphor::LEVELS as f32;
                    let top = y_of(run.code_hi() as f32 + 0.5);
                    let bottom = y_of(run.code_lo() as f32 - 0.5);
                    let x = x0 + run.col as f32 * col_w;
                    // Round widths up so neighbouring columns don't leave gaps.
                    let quad = Bounds::new(point(px(x), px(top)), size(px(col_w.ceil()), px(bottom - top)));
                    window.paint_quad(fill(quad, color));
                }
            }
        }
    }
}

/// One decode row: a box per item, aligned with the trace (same sample-to-x
/// mapping as [`paint_trace`]), labelled when the text fits.
fn paint_bus_row(row: &Row, format: DataFormat, (x0, w): (f32, f32), y: f32, clip: (f32, f32), window: &mut Window, cx: &mut App) {
    let h = BUS_ROW_H;
    let dim: Hsla = rgb(0x8a8a8a).into();
    window.paint_quad(fill(Bounds::new(point(px(x0), px(y)), size(px(w), px(h))), rgb(0x0b0b0b)));
    paint_label(row.label.clone(), dim, (x0 - LABEL_GAP, y + h / 2.), Align::Right, clip, window, cx);
    if let Some(message) = &row.message {
        paint_label(message.clone(), dim, (x0 + w / 2., y + h / 2.), Align::Center, clip, window, cx);
        return;
    }
    let n = row.len.max(2);
    let x_of = |k: f64| x0 + w * (k / (n - 1) as f64) as f32;
    for item in &row.items {
        let (mut a, mut b) = (x_of(item.start).max(x0), x_of(item.end).min(x0 + w));
        // Point events (START, STOP) get a small marker.
        if b - a < 12. && item.start == item.end {
            let c = (a + b) / 2.;
            (a, b) = (c - 6., c + 6.);
        }
        let color = match (item.error, item.kind) {
            (Some(_), _) => 0x6b2020,
            (_, Kind::Data) => 0x1d4650,
            (_, Kind::Address { .. } | Kind::Reset) => 0x5a4518,
            (_, Kind::Start | Kind::RepeatedStart | Kind::Presence) => 0x2e6b2e,
            (_, Kind::Stop) => 0x6b2e2e,
            (_, Kind::Ack) => 0x333333,
            (_, Kind::Nack) => 0x7a4a10,
        };
        let quad = Bounds::new(point(px(a), px(y + 1.)), size(px((b - a).max(1.)), px(h - 2.)));
        window.paint_quad(fill(quad, rgb(color)));
        paint_fitted(item.text(format), (a, b), y + h / 2., window, cx);
    }
}

/// Text centred between `a` and `b`, drawn only if it fits.
fn paint_fitted(text: String, (a, b): (f32, f32), y: f32, window: &mut Window, cx: &mut App) {
    let run = TextRun {
        len: text.len(),
        font: window.text_style().font(),
        color: rgb(0xeeeeee).into(),
        background_color: None,
        underline: None,
        strikethrough: None,
    };
    let line = window.text_system().shape_line(text.into(), px(LABEL_SIZE), &[run], None);
    let width = f32::from(line.width);
    if width + 4. > b - a {
        return;
    }
    let line_height = LABEL_SIZE * 1.3;
    let _ = line.paint(point(px((a + b - width) / 2.), px(y - line_height / 2.)), px(line_height), window, cx);
}

/// Draws one record across the graticule (`x0`, width `w`) as a line.
fn paint_trace(samples: &[i8], color: Hsla, x0: f32, w: f32, y_of: &dyn Fn(f32) -> f32, window: &mut Window) {
    if samples.is_empty() {
        return;
    }
    let mut path = PathBuilder::stroke(px(1.2));
    let n = samples.len();
    let columns = (w.floor() as usize).max(1);
    if n <= columns {
        for (k, &s) in samples.iter().enumerate() {
            let p = point(px(x0 + w * k as f32 / (n - 1).max(1) as f32), px(y_of(s as f32)));
            if k == 0 { path.move_to(p) } else { path.line_to(p) }
        }
    } else {
        // Min/max decimation: two vertices per pixel column keeps edges and glitches visible.
        for c in 0..columns {
            let chunk = &samples[c * n / columns..((c + 1) * n / columns).max(c * n / columns + 1)];
            let (lo, hi) = chunk.iter().fold((i8::MAX, i8::MIN), |(lo, hi), &s| (lo.min(s), hi.max(s)));
            let x = x0 + c as f32 + 0.5;
            let a = point(px(x), px(y_of(hi as f32)));
            let b = point(px(x), px(y_of(lo as f32)));
            if c == 0 { path.move_to(a) } else { path.line_to(a) }
            path.line_to(b);
        }
    }
    if let Ok(path) = path.build() {
        window.paint_path(path, color);
    }
}

fn paint_spectrum(bounds: Bounds<Pixels>, view: &SpectrumView, window: &mut Window, cx: &mut App) {
    window.paint_quad(fill(bounds, rgb(0x000000)));
    let f = &view.settings;
    let color: Hsla = rgb(TRACE_COLORS[f.source]).into();
    let dim: Hsla = rgb(0x8a8a8a).into();

    let bx = f32::from(bounds.origin.x);
    let bw = f32::from(bounds.size.width);
    let x0 = bx + MARGIN_SIDE;
    let y0 = f32::from(bounds.origin.y) + MARGIN_TOP;
    let w = (bw - 2. * MARGIN_SIDE).max(1.);
    let h = (f32::from(bounds.size.height) - MARGIN_TOP - MARGIN_BOTTOM).max(1.);
    let clip = (bx, bx + bw);
    let div_w = w / FFT_H_DIVS as f32;
    let div_h = h / V_DIVS as f32;

    // Vrms -> screen y, clamped to the graticule.
    let y_of = |vrms: f64| -> f32 {
        let divs = match f.unit {
            FftUnit::Dbv => (f.ref_dbv as f64 - fft::to_dbv(vrms)) / f.db_per_div(),
            FftUnit::Vrms => V_DIVS as f64 - vrms / view.source_vpd,
        };
        y0 + (divs as f32 * div_h).clamp(0., h)
    };

    // Frequency labels below, level labels on the left in the source colour.
    let every = if div_w >= 56. { 1 } else { 2 };
    for i in (0..=FFT_H_DIVS).step_by(every) {
        let text = tables::format_si(i as f64 * view.hz_per_div, "Hz");
        paint_label(text, dim, (x0 + div_w * i as f32, y0 + h + MARGIN_BOTTOM / 2.), Align::Center, clip, window, cx);
    }
    for j in 0..=V_DIVS {
        let text = match f.unit {
            FftUnit::Dbv => format!("{}", f.ref_dbv as f64 - j as f64 * f.db_per_div()),
            FftUnit::Vrms => tables::format_si((V_DIVS - j) as f64 * view.source_vpd, "V"),
        };
        paint_label(text, color, (x0 - LABEL_GAP, y0 + div_h * j as f32), Align::Right, clip, window, cx);
    }

    let grid: Hsla = rgb(0x2a2a2a).into();
    for i in 0..=FFT_H_DIVS {
        window.paint_quad(fill(Bounds::new(point(px(x0 + div_w * i as f32), px(y0)), size(px(1.), px(h))), grid));
    }
    for j in 0..=V_DIVS {
        window.paint_quad(fill(Bounds::new(point(px(x0), px(y0 + div_h * j as f32)), size(px(w), px(1.))), grid));
    }

    let header_y = y0 + 10.;
    let Some(sp) = view.spectrum.as_ref().filter(|_| view.message.is_none()) else {
        let text = view.message.unwrap_or("No data").to_owned();
        paint_label(text, dim, (x0 + w / 2., y0 + h / 2.), Align::Center, clip, window, cx);
        return;
    };
    let unit = if f.unit == FftUnit::Dbv { "dBV" } else { "Vrms" };
    let header = format!(
        "FFT {} · {} · {} pt · Δf {} · {}",
        TRACE_NAMES[f.source],
        f.window.label(),
        sp.points,
        tables::format_si(sp.bin_hz, "Hz"),
        unit
    );
    paint_label(header, dim, (x0 + 6., header_y), Align::Left, clip, window, cx);

    // The curve. Zoomed out, each pixel column shows the largest bin it covers,
    // so narrow peaks survive; zoomed in, a line joins the bins.
    let hz_per_px = view.hz_per_div * FFT_H_DIVS as f64 / w as f64;
    let bins_per_px = hz_per_px / sp.bin_hz;
    let mut path = PathBuilder::stroke(px(1.2));
    let mut started = false;
    let mut point_to = |x: f32, y: f32, path: &mut PathBuilder| {
        if started { path.line_to(point(px(x), px(y))) } else { path.move_to(point(px(x), px(y))) }
        started = true;
    };
    if bins_per_px >= 1.0 {
        for c in 0..w.floor() as usize {
            let lo = (c as f64 * bins_per_px).ceil() as usize;
            let hi = (((c + 1) as f64 * bins_per_px).ceil() as usize).max(lo + 1).min(sp.vrms.len());
            if lo >= hi {
                break;
            }
            let v = sp.vrms[lo..hi].iter().copied().fold(0.0, f64::max);
            point_to(x0 + c as f32 + 0.5, y_of(v), &mut path);
        }
    } else {
        for (b, &v) in sp.vrms.iter().enumerate() {
            let x = x0 + (b as f64 / bins_per_px) as f32;
            if x > x0 + w {
                break;
            }
            point_to(x, y_of(v), &mut path);
        }
    }
    if let Ok(path) = path.build() {
        window.paint_path(path, color);
    }

    if let Some((hz, vrms)) = sp.peak() {
        let level = match f.unit {
            FftUnit::Dbv => format!("{:.1} dBV", fft::to_dbv(vrms)),
            FftUnit::Vrms => tables::format_si(vrms, "Vrms"),
        };
        let mark = if view.clipped { "?" } else { "" };
        let text = format!("Peak {} {level}{mark}", tables::format_si(hz, "Hz"));
        paint_label(text, color, (x0 + w - 6., header_y), Align::Right, clip, window, cx);
        let px_x = x0 + (hz / hz_per_px) as f32;
        if px_x <= x0 + w {
            let y = y_of(vrms);
            window.paint_quad(fill(Bounds::new(point(px(px_x - 3.), px(y - 9.)), size(px(6.), px(6.))), color));
        }
    }
}
