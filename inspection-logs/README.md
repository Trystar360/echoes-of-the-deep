# Spruce Lake Adventure — Daily Inspection Logs

A creative, intuitive web app for running **daily inspection logs** for Spruce
Lake's adventure programming activities (high ropes courses, power swings, and
more). It's a static, offline-first app — no build step, no server, no account.
Open `index.html` and go.

## Design

A bright, outdoorsy interface tuned for fast use in the field:

- A consistent **proportional scale** drives the type sizes, spacing, and
  layout columns so everything lines up and feels balanced.
- **High-contrast** controls — clear Pass / Fail / N/A buttons and a live
  progress bar — so a glance tells you where you are.
- A bright "alpine daylight over clear water" palette with a warm accent.
- A hand-drawn Spruce Lake scene on the home screen for a touch of place.

## Features

- **Two starter logs**, taken straight from the field sheets:
  - **SkyTrail Daily Checklist** — Pre-Use Inspection, Activity Set Up, Activity
    Closing.
  - **Daily Indoor Power Swing Inspection Log** — harnesses, helmets, lanyard,
    rope, pulleys, release clip, test ride.
- Per-item **Pass / Fail / N/A** with inspector **initials** and a live
  completion bar. Any failed item is visibly flagged.
- "**Mark all Pass**" for fast clean runs; auto-fills initials from the
  inspector field.
- Capture **date, group name, # participants, inspector** for every log.
- **History** of every log, **print / save-as-PDF** of any log, and
  **export / import JSON** for backup or moving between devices.
- **Saved locally** in the browser (`localStorage`) — works with no signal in
  the field.

## Adding a new log type

Everything is data-driven. Add an object to `TEMPLATES` in
[`js/templates.js`](js/templates.js):

```js
{
  id: "via-ferrata",
  name: "Via Ferrata Daily Check",
  subtitle: "…",
  glyph: "🧗",
  accent: "#9bd05f",
  sections: [
    { title: "Pre-Use", items: [ { id: "cable", label: "Cable", hint: "…" } ] },
  ],
}
```

The forms, history, detail, and print views all generate themselves from it.

## Run it

Just open `inspection-logs/index.html` in a browser, or serve the folder:

```bash
cd inspection-logs
python3 -m http.server 8000
# visit http://localhost:8000
```
