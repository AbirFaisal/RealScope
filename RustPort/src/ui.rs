//! GPUI front end: waveform canvas plus a control panel.

use std::sync::mpsc;
use std::time::Duration;

use gpui::{
    App, Application, Bounds, ClickEvent, Context, ElementId, Hsla, IntoElement, PathBuilder, Pixels, Render,
    SharedString, Task, TitlebarOptions, Window, WindowBounds, WindowOptions, canvas, div, fill, point, prelude::*,
    px, rgb, size,
};

use crate::acquire::{self, Event, Frame};
use crate::settings::{ChannelSettings, Settings, Sweep};
use crate::tables::{
    self, ADC_MAX, COUNTS_PER_DIV, H_DIVS, POS0_MAX, TDIV_MAX_IDX, TDIV_MIN_IDX, V_DIVS, VDIV_MV,
};

const CH_COLORS: [u32; 2] = [0xf5d742, 0x42d4f5];

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
    _poll: Task<()>,
}

impl ScopeApp {
    fn new(cx: &mut Context<Self>) -> Self {
        let (settings_tx, settings_rx) = mpsc::channel();
        let (event_tx, event_rx) = mpsc::channel();
        acquire::spawn(settings_rx, event_tx);

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
            settings: Settings::default(),
            settings_tx,
            frame: None,
            status: "Searching for scope…".into(),
            device: None,
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
            Event::Frame(f) => self.frame = Some(f),
            Event::Disconnected(e) => {
                self.status = e;
                self.device = None;
                self.frame = None;
            }
        }
    }

    fn update_settings(&mut self, cx: &mut Context<Self>, f: &dyn Fn(&mut Settings)) {
        f(&mut self.settings);
        let _ = self.settings_tx.send(self.settings);
        cx.notify();
    }

    fn button(
        &self,
        id: String,
        label: impl Into<SharedString>,
        active: bool,
        cx: &mut Context<Self>,
        f: impl Fn(&mut Settings) + 'static,
    ) -> impl IntoElement {
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
            .on_click(cx.listener(move |this, _: &ClickEvent, _, cx| this.update_settings(cx, &f)))
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
            .child(div().w(px(70.)).text_color(rgb(0x999999)).child(label.to_owned()))
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
                tables::vdiv_label(ch.vdiv_idx),
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
    }

    fn controls(&self, cx: &mut Context<Self>) -> impl IntoElement {
        let s = self.settings;
        let src = s.trigger.source;
        // Trigger level steps by 1/5 division of the source channel.
        let level_step = move |s: &Settings| s.ch[s.trigger.source].volts_per_div() / 5.0;
        let level_limit = move |s: &Settings| {
            let ch = &s.ch[s.trigger.source];
            (ADC_MAX - ch.pos0.min(ADC_MAX)) as f64 * ch.volts_per_div() / COUNTS_PER_DIV as f64
        };
        div()
            .w(px(280.))
            .flex()
            .flex_col()
            .gap_2()
            .p_2()
            .bg(rgb(0x181818))
            .child(self.button(
                "run".into(),
                if s.running { "■ Stop" } else { "▶ Run" },
                s.running,
                cx,
                |s| s.running = !s.running,
            ))
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
                    .child("Trigger (rising edge)")
                    .child(
                        div()
                            .flex()
                            .gap_1()
                            .child(self.button("trg-ch1".into(), "CH1", src == 0, cx, |s| s.trigger.source = 0))
                            .child(self.button("trg-ch2".into(), "CH2", src == 1, cx, |s| s.trigger.source = 1))
                            .child(div().flex_1())
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
                    .child(self.stepper(
                        "trg-level",
                        "Level",
                        tables::format_si(s.trigger.level_v, "V"),
                        cx,
                        move |s| {
                            let lim = level_limit(s);
                            s.trigger.level_v = (s.trigger.level_v - level_step(s)).max(-lim);
                        },
                        move |s| {
                            let lim = level_limit(s);
                            s.trigger.level_v = (s.trigger.level_v + level_step(s)).min(lim);
                        },
                    )),
            )
    }

    fn status_line(&self) -> impl IntoElement {
        let s = &self.settings;
        let trig = match &self.frame {
            Some(f) if f.triggered => "Trig'd",
            Some(_) => "Auto",
            None => "—",
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
        let frame = self.frame.clone();
        let settings = self.settings;
        div()
            .flex()
            .size_full()
            .bg(rgb(0x101010))
            .text_color(rgb(0xdddddd))
            .text_sm()
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
                            .child(canvas(|_, _, _| (), move |b, _, window, _| paint_scope(b, frame.as_ref(), &settings, window)).size_full()),
                    )
                    .child(self.status_line()),
            )
            .child(self.controls(cx))
    }
}

fn paint_scope(bounds: Bounds<Pixels>, frame: Option<&Frame>, settings: &Settings, window: &mut Window) {
    let x0 = f32::from(bounds.origin.x);
    let y0 = f32::from(bounds.origin.y);
    let w = f32::from(bounds.size.width);
    let h = f32::from(bounds.size.height);
    let full = 2.0 * ADC_MAX as f32;
    // ADC code -> screen y: +125 at the top, -125 at the bottom.
    let y_of = |code: f32| y0 + h / 2.0 - code / full * h;

    window.paint_quad(fill(bounds, rgb(0x000000)));

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
    let ty = y_of(settings.trigger_level_code() as f32);
    let tc = rgb(CH_COLORS[settings.trigger.source]);
    window.paint_quad(fill(Bounds::new(point(px(x0 + w - 8.), px(ty - 4.)), size(px(8.), px(8.))), tc));
    window.paint_quad(fill(Bounds::new(point(px(x0 + w / 2. - 4.), px(y0)), size(px(8.), px(6.))), tc));

    let Some(frame) = frame else { return };
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
