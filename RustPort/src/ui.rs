//! GPUI front end: ribbon, waveform canvas and a control panel.

use std::sync::mpsc;
use std::time::{Duration, Instant};

use gpui::{
    App, Application, Bounds, ClickEvent, Context, Div, ElementId, Hsla, IntoElement, PathBuilder, Pixels, Render,
    SharedString, Stateful, Task, TextRun, TitlebarOptions, Window, WindowBounds, WindowOptions, canvas, div, fill,
    point, prelude::*, px, rgb, size,
};

use crate::acquire::{self, Event, Frame};
use crate::measure::{self, Group, Measurements};
use crate::persist::{self, Saved};
use crate::phosphor::{self, Persistence, Phosphor, Run};
use crate::settings::{ChannelSettings, EXT, RunMode, Settings, Slope, Sweep};
use crate::tables::{
    self, ADC_MAX, COUNTS_PER_DIV, H_DIVS, POS0_MAX, TDIV_MAX_IDX, TDIV_MIN_IDX, V_DIVS, VDIV_MV,
};

const CH_COLORS: [u32; 2] = [0x42d4f5, 0x42f578];
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
    settings_tx: mpsc::Sender<Settings>,
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
    measurements: [Option<Measurements>; 2],
    measured_at: Option<Instant>,
    _poll: Task<()>,
}

/// What the canvas draws for the waveforms.
enum Trace {
    Line(Option<Frame>),
    Phosphor([Vec<Run>; 2]),
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
            measurements: [None, None],
            measured_at: None,
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
                    let _ = self.settings_tx.send(self.settings);
                }
            }
            Event::Disconnected(e) => {
                self.status = e;
                self.device = None;
                self.frame = None;
                self.phosphor.clear();
                self.measurements = [None, None];
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
    }

    fn measurements_panel(&self) -> impl IntoElement {
        let tables = self.measurements.each_ref().map(|m| measure::table(m.as_ref().unwrap_or(&Measurements::default())));
        let dim = rgb(0x888888);
        let row = |label: SharedString, values: [SharedString; 2], colors: [gpui::Rgba; 2], label_color: gpui::Rgba| {
            div()
                .flex()
                .gap_1()
                .child(div().w(px(58.)).text_color(label_color).child(label))
                .child(div().flex_1().text_right().text_color(colors[0]).child(values[0].clone()))
                .child(div().flex_1().text_right().text_color(colors[1]).child(values[1].clone()))
        };
        let ch_colors = [rgb(CH_COLORS[0]), rgb(CH_COLORS[1])];
        let value_color = rgb(0xdddddd);
        let mut panel = div()
            .w(px(240.))
            .flex()
            .flex_col()
            .gap(px(2.))
            .p_2()
            .bg(rgb(0x181818))
            .text_xs()
            .child(div().text_sm().pb_1().child("Measurements"))
            .child(row("".into(), ["CH1".into(), "CH2".into()], ch_colors, dim));
        let mut group = None;
        for (a, b) in tables[0].iter().zip(&tables[1]) {
            if group != Some(a.0) {
                group = Some(a.0);
                let title = match a.0 {
                    Group::Time => "Time",
                    Group::Voltage => "Voltage",
                };
                panel = panel.child(div().pt_2().text_color(dim).border_b_1().border_color(rgb(0x2a2a2a)).child(title));
            }
            panel = panel.child(row(a.1.into(), [a.2.clone().into(), b.2.clone().into()], [value_color; 2], dim));
        }
        if self.measurements.iter().flatten().any(|m| m.clipped) {
            panel = panel.child(div().pt_2().text_color(dim).child("? = signal off screen; value is a bound"));
        }
        panel
    }

    fn update_settings(&mut self, cx: &mut Context<Self>, f: &dyn Fn(&mut Settings)) {
        f(&mut self.settings);
        let _ = self.settings_tx.send(self.settings);
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
        let color = rgb(CH_COLORS[i]);
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

    fn controls(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let s = self.settings;
        let src = s.trigger.source;
        // Trigger level steps by 1/5 division of the source channel, within the screen.
        let nudge_level = |up: bool| {
            move |s: &mut Settings| {
                let Some(c) = s.trigger_channel() else { return };
                let ch = &s.ch[c];
                let step = ch.volts_per_div() / 5.0;
                let lim = (ADC_MAX - ch.pos0.min(ADC_MAX)) as f64 * ch.volts_per_div() / COUNTS_PER_DIV as f64;
                s.trigger.level_v = if up { (s.trigger.level_v + step).min(lim) } else { (s.trigger.level_v - step).max(-lim) };
            }
        };
        let level_row = if s.trigger_channel().is_some() {
            self.stepper("trg-level", "Level", tables::format_si(s.trigger.level_v, "V"), cx, nudge_level(false), nudge_level(true))
                .into_any_element()
        } else {
            div()
                .flex()
                .items_center()
                .gap_1()
                .child(div().w(px(STEPPER_LABEL_W)).text_color(rgb(0x999999)).child("Level"))
                .child(div().text_color(rgb(0x999999)).child("set by external signal"))
                .into_any_element()
        };
        div()
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
                    .child(self.button("single".into(), "Single", s.run == RunMode::Single, cx, |s| s.run = RunMode::Single)),
            )
            .child(self.channel_panel(0, cx))
            .child(self.channel_panel(1, cx))
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
            .child(
                div()
                    .flex()
                    .flex_col()
                    .gap_1()
                    .p_2()
                    .rounded_md()
                    .border_1()
                    .border_color(rgb(0x333333))
                    .child("Trigger")
                    .child(
                        div()
                            .flex()
                            .gap_1()
                            .child(self.button("trg-auto".into(), "Auto", s.trigger.sweep == Sweep::Auto, cx, |s| {
                                s.trigger.sweep = Sweep::Auto
                            }))
                            .child(self.button(
                                "trg-normal".into(),
                                "Normal",
                                s.trigger.sweep == Sweep::Normal,
                                cx,
                                |s| s.trigger.sweep = Sweep::Normal,
                            )),
                    )
                    .child(
                        div()
                            .flex()
                            .gap_1()
                            .child(self.button("trg-ch1".into(), "CH1", src == 0, cx, |s| s.trigger.source = 0))
                            .child(self.button("trg-ch2".into(), "CH2", src == 1, cx, |s| s.trigger.source = 1))
                            .child(self.button("trg-ext".into(), "EXT", src == EXT, cx, |s| s.trigger.source = EXT)),
                    )
                    .child(
                        div()
                            .flex()
                            .gap_1()
                            .child(self.button("trg-rise".into(), "↑ Rise", s.trigger.slope == Slope::Rising, cx, |s| {
                                s.trigger.slope = Slope::Rising
                            }))
                            .child(self.button("trg-fall".into(), "↓ Fall", s.trigger.slope == Slope::Falling, cx, |s| {
                                s.trigger.slope = Slope::Falling
                            })),
                    )
                    .child(level_row),
            )
    }

    fn status_line(&self) -> impl IntoElement {
        let s = &self.settings;
        let trig = match (s.run, &self.frame) {
            (RunMode::Single, _) => "Armed",
            (RunMode::Stopped, _) => "Stopped",
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
        let trace = if self.phosphor_on {
            let runs = |ch: usize| if settings.ch[ch].on { self.phosphor.runs(ch) } else { Vec::new() };
            Trace::Phosphor([runs(0), runs(1)])
        } else {
            Trace::Line(self.frame.clone())
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
                                    .child(canvas(|_, _, _| (), move |b, _, window, cx| paint_scope(b, &trace, &settings, window, cx)).size_full()),
                            )
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

fn paint_scope(bounds: Bounds<Pixels>, trace: &Trace, settings: &Settings, window: &mut Window, cx: &mut App) {
    window.paint_quad(fill(bounds, rgb(0x000000)));

    // Graticule rectangle, inset to leave room for the axis labels.
    let bx = f32::from(bounds.origin.x);
    let bw = f32::from(bounds.size.width);
    let x0 = bx + MARGIN_SIDE;
    let y0 = f32::from(bounds.origin.y) + MARGIN_TOP;
    let w = (bw - 2. * MARGIN_SIDE).max(1.);
    let h = (f32::from(bounds.size.height) - MARGIN_TOP - MARGIN_BOTTOM).max(1.);
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
        paint_label(text, time_color, (x, y0 + h + MARGIN_BOTTOM / 2.), Align::Center, clip, window, cx);
    }

    // Voltage axes: each channel has its own V/div and position, so each gets
    // its own scale in its colour, CH1 on the left and CH2 on the right.
    for (i, ch) in settings.ch.iter().enumerate() {
        if !ch.on {
            continue;
        }
        let color: Hsla = rgb(CH_COLORS[i]).into();
        for j in 0..=V_DIVS {
            let code = ADC_MAX - COUNTS_PER_DIV * j as i32;
            let text = tables::format_si(ch.code_to_volts(code), "V");
            let y = y_of(code as f32);
            let (x, align) = if i == 0 { (x0 - LABEL_GAP, Align::Right) } else { (x0 + w + LABEL_GAP, Align::Left) };
            paint_label(text, color, (x, y), align, clip, window, cx);
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
    for (i, ch) in settings.ch.iter().enumerate() {
        if ch.on {
            let y = y_of(ch.pos0.clamp(-POS0_MAX.min(ADC_MAX), ADC_MAX) as f32);
            window.paint_quad(fill(Bounds::new(point(px(x0), px(y - 4.)), size(px(8.), px(8.))), rgb(CH_COLORS[i])));
        }
    }
    let tc = settings.trigger_channel().map_or(rgb(EXT_TRIGGER_COLOR), |c| rgb(CH_COLORS[c]));
    if let Some(code) = settings.trigger_level_code() {
        let ty = y_of(code as f32);
        window.paint_quad(fill(Bounds::new(point(px(x0 + w - 8.), px(ty - 4.)), size(px(8.), px(8.))), tc));
    }
    window.paint_quad(fill(Bounds::new(point(px(x0 + w / 2. - 4.), px(y0)), size(px(8.), px(6.))), tc));

    let frame = match trace {
        Trace::Line(Some(frame)) => frame,
        Trace::Line(None) => return,
        Trace::Phosphor(runs) => {
            // Each run is one quad; brightness is the channel colour's alpha.
            let col_w = w / phosphor::COLS as f32;
            for (i, runs) in runs.iter().enumerate() {
                let mut color: Hsla = rgb(CH_COLORS[i]).into();
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
            return;
        }
    };
    for (i, samples) in frame.samples.iter().enumerate() {
        let Some(samples) = samples else { continue };
        if !settings.ch[i].on || samples.is_empty() {
            continue;
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
            window.paint_path(path, rgb(CH_COLORS[i]));
        }
    }
}
