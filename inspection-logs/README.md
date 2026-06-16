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
- **History** of every log and **print / save-as-PDF** of any single log.
- **Robust export** (Export screen): filter by **log type, date range, and
  group**, see a live match count, then export as a **JSON backup**
  (re-importable), **CSV — one row per check**, **CSV — one row per log** (with
  pass/fail totals and flagged items), or a **formatted printable report**
  (Save as PDF). CSVs include a UTF-8 BOM so they open cleanly in Excel.
  **Restore from a JSON backup** merges logs back in and syncs them.
- **Offline-first storage** — logs save instantly to an on-device database
  (**IndexedDB**), so the app fully works with no signal in the field.
- **Cloud sync (optional)** — connect a **Supabase** database to back logs up
  and sync across phones, tablets and staff. A header badge shows live status
  (On device · Syncing · Synced · N pending · Offline · Error).

## Cloud sync

The app is **offline-first**: every change is written to the local IndexedDB
database first, then synced to the cloud in the background when online. A small
**sync engine** keeps an outbox of changed records, pushes them up, pulls remote
changes down, and resolves conflicts **last-write-wins** by `updated_at`
(deletes are tombstoned so they propagate too).

To enable it:

1. Create a free [Supabase](https://supabase.com) project.
2. In its SQL editor, run [`supabase-schema.sql`](supabase-schema.sql) (also
   shown, copyable, on the in-app **Settings** screen).
3. Open **Settings (⚙)** in the app and paste your **Project URL** and **anon /
   public API key** (Supabase → Project Settings → API). Hit **Test
   connection**, then **Save & connect**.

Without this step the app simply runs **local-only** — still fully functional,
just not synced. The config is stored on the device; the anon key is the
browser-safe key (keep the service-role key out of the app), and the default RLS
policy grants the anon key full table access — tighten it for wider use.

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
