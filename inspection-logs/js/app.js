/*
 * Spruce Lake Adventure — daily inspection logs.
 * Vanilla JS, no build step. Hash router renders into #view.
 */

const STATUSES = [
  { key: "pass", label: "Pass", mark: "✓" },
  { key: "fail", label: "Fail", mark: "✕" },
  { key: "na", label: "N/A", mark: "—" },
];

/* ---------- tiny DOM helpers ---------- */
const $ = (sel, root = document) => root.querySelector(sel);
const el = (tag, props = {}, ...kids) => {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(props)) {
    if (k === "class") node.className = v;
    else if (k === "html") node.innerHTML = v;
    else if (k.startsWith("on") && typeof v === "function") node.addEventListener(k.slice(2), v);
    else if (v !== null && v !== undefined && v !== false) node.setAttribute(k, v);
  }
  for (const kid of kids.flat()) {
    if (kid === null || kid === undefined || kid === false) continue;
    node.appendChild(typeof kid === "string" ? document.createTextNode(kid) : kid);
  }
  return node;
};
const todayISO = () => new Date().toISOString().slice(0, 10);
const fmtDate = (iso) => {
  if (!iso) return "—";
  const d = new Date(iso + "T00:00:00");
  return d.toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric", year: "numeric" });
};
const escapeHTML = (s) => String(s ?? "").replace(/[&<>"']/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

/* ---------- log stats ---------- */
function logStats(log) {
  const tpl = TEMPLATE_BY_ID[log.templateId];
  let total = 0, done = 0, pass = 0, fail = 0, na = 0;
  if (tpl) {
    tpl.sections.forEach((sec) =>
      sec.items.forEach((item) => {
        total++;
        const r = log.results[item.id];
        if (r && r.status) {
          done++;
          if (r.status === "pass") pass++;
          else if (r.status === "fail") fail++;
          else if (r.status === "na") na++;
        }
      })
    );
  }
  return { total, done, pass, fail, na, complete: total > 0 && done === total };
}

/* ====================================================================
 * Router
 * ================================================================== */
function router() {
  const view = $("#view");
  const hash = location.hash.slice(1) || "/";
  const [, route, arg] = hash.split("/");
  view.classList.remove("view-enter");
  void view.offsetWidth; // restart animation
  view.classList.add("view-enter");

  if (route === "new") renderForm(view, { templateId: arg });
  else if (route === "edit") renderForm(view, { logId: arg });
  else if (route === "log") renderDetail(view, arg);
  else if (route === "history") renderHistory(view);
  else if (route === "settings") renderSettings(view);
  else renderDashboard(view);

  $("#topbar").scrollIntoView({ behavior: "auto", block: "start" });
}
function currentRoute() { return (location.hash.slice(1) || "/").split("/")[1] || ""; }

window.addEventListener("hashchange", router);
window.addEventListener("DOMContentLoaded", async () => {
  buildChrome();
  try { await Store.init(); } catch (e) { console.error("Store init failed", e); }
  Sync.onStatus(updateSyncBadge);
  Store.onChange(() => {
    updateSyncBadge(Sync.status());
    // Re-render data views on remote/background changes, but never clobber an
    // open form the user is filling in.
    const r = currentRoute();
    if (r !== "new" && r !== "edit" && r !== "settings") router();
  });
  Sync.init();
  updateSyncBadge(Sync.status());
  router();
});

/* ====================================================================
 * App chrome (header)
 * ================================================================== */
function buildChrome() {
  $("#brand").addEventListener("click", () => (location.hash = "/"));
  const go = (id, hash) => $(id).addEventListener("click", (e) => { e.preventDefault(); location.hash = hash; });
  go("#nav-history", "/history");
  go("#nav-new", "/new");
  go("#nav-settings", "/settings");
  $("#sync-badge").addEventListener("click", () => (location.hash = "/settings"));
}

const SYNC_BADGE = {
  local:   { dot: "is-local",   label: "On device" },
  offline: { dot: "is-offline", label: "Offline" },
  syncing: { dot: "is-syncing", label: "Syncing…" },
  idle:    { dot: "is-synced",  label: "Synced" },
  error:   { dot: "is-error",   label: "Sync error" },
};
function updateSyncBadge(status) {
  const badge = $("#sync-badge");
  if (!badge) return;
  const spec = SYNC_BADGE[status.mode] || SYNC_BADGE.local;
  let label = spec.label;
  if (status.pending && (status.mode === "offline" || status.mode === "idle"))
    label = `${status.pending} pending`;
  badge.querySelector(".sync-dot").className = "sync-dot " + spec.dot;
  badge.querySelector(".sync-label").textContent = label;
  badge.title = status.mode === "error" ? (status.error || "Sync error")
    : status.mode === "local" ? "Saved on this device — tap to connect a cloud database"
    : `Cloud sync: ${label}`;
}

/* ====================================================================
 * Dashboard
 * ================================================================== */
function renderDashboard(view) {
  const logs = Store.all();
  const recent = logs.slice(0, 4);

  view.replaceChildren(
    el("section", { class: "hero" },
      el("div", { class: "hero-copy" },
        el("p", { class: "eyebrow" }, "Spruce Lake · Adventure Programming"),
        el("h1", { class: "hero-title" }, "Daily Inspection Logs"),
        el("p", { class: "hero-sub" },
          "Run every pre-use, set-up and closing check from one place. Tap through your gear, sign off, and keep a clean record for every group — before the first rider clips in."),
        el("div", { class: "hero-actions" },
          el("button", { class: "btn btn-primary", onclick: () => (location.hash = "/new") }, "Start a log"),
          el("button", { class: "btn btn-ghost", onclick: () => (location.hash = "/history") },
            `History (${logs.length})`))),
      spruceScene()),

    el("section", { class: "stack" },
      el("h2", { class: "section-head" }, "Choose a log"),
      el("div", { class: "card-grid" },
        TEMPLATES.map((tpl) => templateCard(tpl, logs)))),

    recent.length
      ? el("section", { class: "stack" },
          el("div", { class: "section-head-row" },
            el("h2", { class: "section-head" }, "Recent"),
            el("a", { class: "link", href: "#/history" }, "View all →")),
          el("div", { class: "log-list" }, recent.map(logRow)))
      : el("section", { class: "empty-hint" },
          "No logs yet — pick a checklist above to record your first inspection of the day.")
  );
}

function templateCard(tpl, logs) {
  const todays = logs.filter((l) => l.templateId === tpl.id && l.meta.date === todayISO()).length;
  return el("button",
    { class: "tpl-card", style: `--accent:${tpl.accent}`, onclick: () => (location.hash = `/new/${tpl.id}`) },
    el("span", { class: "tpl-glyph" }, tpl.glyph),
    el("span", { class: "tpl-body" },
      el("span", { class: "tpl-name" }, tpl.name),
      el("span", { class: "tpl-sub" }, tpl.subtitle),
      el("span", { class: "tpl-meta" },
        `${tpl.sections.reduce((n, s) => n + s.items.length, 0)} checks`,
        todays ? el("span", { class: "pill pill-soft" }, `${todays} today`) : null)),
    el("span", { class: "tpl-arrow" }, "→"));
}

function logRow(log) {
  const tpl = TEMPLATE_BY_ID[log.templateId];
  const st = logStats(log);
  return el("a", { class: "log-row", href: `#/log/${log.id}` },
    el("span", { class: "log-row-glyph", style: `--accent:${tpl ? tpl.accent : "#888"}` }, tpl ? tpl.glyph : "?"),
    el("span", { class: "log-row-main" },
      el("span", { class: "log-row-title" }, log.templateName),
      el("span", { class: "log-row-sub" },
        `${fmtDate(log.meta.date)} · ${log.meta.group || "No group"} · ${log.meta.participants || 0} participants`)),
    el("span", { class: `status-tag ${st.fail ? "is-fail" : st.complete ? "is-pass" : "is-partial"}` },
      st.fail ? `${st.fail} fail` : st.complete ? "Complete" : `${st.done}/${st.total}`));
}

/* ====================================================================
 * Form (new / edit)
 * ================================================================== */
function renderForm(view, { templateId, logId }) {
  let log;
  if (logId) {
    log = Store.get(logId);
    if (!log) {
      location.hash = "/";
      return;
    }
  }
  const tpl = TEMPLATE_BY_ID[(log ? log.templateId : templateId)];

  // No template chosen yet → picker
  if (!tpl) {
    view.replaceChildren(
      el("section", { class: "stack" },
        crumb("New log"),
        el("h1", { class: "page-title" }, "Which log are you running?"),
        el("div", { class: "card-grid" }, TEMPLATES.map((t) => templateCard(t, Store.all())))));
    return;
  }

  // Working copy
  const draft = log
    ? JSON.parse(JSON.stringify(log))
    : {
        id: Store.newId(),
        templateId: tpl.id,
        templateName: tpl.name,
        meta: { date: todayISO(), group: "", participants: "", inspector: "" },
        results: {},
        createdAt: new Date().toISOString(),
      };

  const setResult = (itemId, patch) => {
    draft.results[itemId] = Object.assign({ status: "", initials: draft.meta.inspector || "", note: "" }, draft.results[itemId], patch);
    refreshProgress();
  };

  /* meta fields */
  const metaInput = (key, label, attrs = {}) =>
    el("label", { class: "field" },
      el("span", { class: "field-label" }, label),
      el("input", Object.assign({
        class: "field-input",
        value: draft.meta[key] ?? "",
        oninput: (e) => { draft.meta[key] = e.target.value; },
      }, attrs)));

  const progressFill = el("span", { class: "progress-fill" });
  const progressText = el("span", { class: "progress-text" });
  function refreshProgress() {
    const st = logStatsDraft();
    const pct = st.total ? Math.round((st.done / st.total) * 100) : 0;
    progressFill.style.width = pct + "%";
    progressFill.classList.toggle("has-fail", st.fail > 0);
    progressText.textContent = `${st.done}/${st.total} checked · ${st.pass} pass · ${st.fail} fail · ${st.na} N/A`;
  }
  function logStatsDraft() {
    let total = 0, done = 0, pass = 0, fail = 0, na = 0;
    tpl.sections.forEach((s) => s.items.forEach((it) => {
      total++;
      const r = draft.results[it.id];
      if (r && r.status) { done++; r.status === "pass" && pass++; r.status === "fail" && fail++; r.status === "na" && na++; }
    }));
    return { total, done, pass, fail, na };
  }

  /* bulk action: mark every unset item Pass */
  const passAll = () => {
    tpl.sections.forEach((s) => s.items.forEach((it) => {
      const r = draft.results[it.id];
      if (!r || !r.status) setResult(it.id, { status: "pass" });
    }));
    rerenderItems();
  };

  const itemsWrap = el("div", { class: "sections" });
  function rerenderItems() {
    itemsWrap.replaceChildren(...tpl.sections.map((sec) =>
      el("div", { class: "log-section" },
        el("h3", { class: "log-section-title" }, sec.title),
        el("div", { class: "items" }, sec.items.map((item) => itemRow(item))))));
  }

  function itemRow(item) {
    const r = draft.results[item.id] || {};
    const statusBtns = el("div", { class: "status-group", role: "group", "aria-label": "Status" },
      STATUSES.map((s) =>
        el("button", {
          type: "button",
          class: "status-btn" + (r.status === s.key ? ` active is-${s.key}` : ""),
          onclick: (e) => {
            const next = r.status === s.key ? "" : s.key;
            setResult(item.id, { status: next });
            // update sibling buttons
            e.currentTarget.parentElement.querySelectorAll(".status-btn").forEach((b) =>
              b.classList.remove("active", "is-pass", "is-fail", "is-na"));
            if (next) e.currentTarget.classList.add("active", "is-" + next);
            row.classList.toggle("is-flagged", next === "fail");
          },
        }, el("span", { class: "status-mark" }, s.mark), s.label)));

    const row = el("div", { class: "item-row" + (r.status === "fail" ? " is-flagged" : "") },
      el("div", { class: "item-main" },
        el("span", { class: "item-label" }, item.label),
        item.hint ? el("span", { class: "item-hint" }, item.hint) : null),
      el("div", { class: "item-controls" },
        statusBtns,
        el("input", {
          class: "initials-input", placeholder: "INI", maxlength: "5", value: r.initials || "",
          "aria-label": "Initials",
          oninput: (e) => setResult(item.id, { initials: e.target.value.toUpperCase() }),
          onfocus: (e) => { if (!e.target.value && draft.meta.inspector) { e.target.value = draft.meta.inspector; setResult(item.id, { initials: draft.meta.inspector }); } },
        })));
    return row;
  }

  const save = (goHome) => {
    if (!draft.meta.date) draft.meta.date = todayISO();
    draft.updatedAt = new Date().toISOString();
    Store.save(draft);
    location.hash = goHome ? `/log/${draft.id}` : "/history";
  };

  view.replaceChildren(
    el("section", { class: "stack form-wrap" },
      crumb(logId ? "Edit log" : "New log"),
      el("div", { class: "form-headline", style: `--accent:${tpl.accent}` },
        el("span", { class: "form-glyph" }, tpl.glyph),
        el("div", {},
          el("h1", { class: "page-title" }, tpl.name),
          el("p", { class: "page-sub" }, tpl.subtitle))),

      el("div", { class: "meta-grid" },
        metaInput("date", "Date", { type: "date" }),
        metaInput("group", "Group name", { placeholder: "e.g. Trailblazers" }),
        metaInput("participants", "# Participants", { type: "number", min: "0", inputmode: "numeric", placeholder: "0" }),
        metaInput("inspector", "Inspector initials", { placeholder: "INI", maxlength: "5", oninput: (e) => { draft.meta.inspector = e.target.value.toUpperCase(); e.target.value = draft.meta.inspector; } })),

      el("div", { class: "progress" },
        el("div", { class: "progress-bar" }, progressFill),
        el("div", { class: "progress-row" },
          progressText,
          el("button", { type: "button", class: "btn btn-mini", onclick: passAll }, "Mark all Pass"))),

      itemsWrap,

      el("label", { class: "field" },
        el("span", { class: "field-label" }, "Notes / corrective actions"),
        el("textarea", {
          class: "field-input field-area", rows: "3",
          placeholder: "Anything flagged above, follow-ups, equipment pulled from service…",
          oninput: (e) => { draft.notes = e.target.value; },
        }, draft.notes || "")),

      el("div", { class: "form-actions" },
        el("button", { class: "btn btn-ghost", onclick: () => history.length > 1 ? history.back() : (location.hash = "/") }, "Cancel"),
        el("button", { class: "btn btn-primary", onclick: () => save(true) }, "Save log")))
  );

  rerenderItems();
  refreshProgress();
}

/* ====================================================================
 * Detail view
 * ================================================================== */
function renderDetail(view, id) {
  const log = Store.get(id);
  if (!log) {
    location.hash = "/history";
    return;
  }
  const tpl = TEMPLATE_BY_ID[log.templateId];
  const st = logStats(log);

  view.replaceChildren(
    el("section", { class: "stack detail" },
      crumb("Log detail", "history", "History"),
      el("div", { class: "form-headline", style: `--accent:${tpl ? tpl.accent : "#888"}` },
        el("span", { class: "form-glyph" }, tpl ? tpl.glyph : "?"),
        el("div", {},
          el("h1", { class: "page-title" }, log.templateName),
          el("p", { class: "page-sub" }, fmtDate(log.meta.date)))),

      el("div", { class: "detail-meta" },
        metaPill("Group", log.meta.group || "—"),
        metaPill("Participants", log.meta.participants || "0"),
        metaPill("Inspector", log.meta.inspector || "—"),
        el("span", { class: `status-tag ${st.fail ? "is-fail" : st.complete ? "is-pass" : "is-partial"}` },
          st.fail ? `${st.fail} flagged` : st.complete ? "All clear" : `${st.done}/${st.total} done`)),

      tpl ? el("div", { class: "sections" }, tpl.sections.map((sec) =>
        el("div", { class: "log-section" },
          el("h3", { class: "log-section-title" }, sec.title),
          el("div", { class: "items items-readonly" }, sec.items.map((item) => {
            const r = log.results[item.id] || {};
            return el("div", { class: "item-row item-readonly" + (r.status === "fail" ? " is-flagged" : "") },
              el("div", { class: "item-main" },
                el("span", { class: "item-label" }, item.label),
                item.hint ? el("span", { class: "item-hint" }, item.hint) : null),
              el("div", { class: "item-controls" },
                el("span", { class: `result-chip is-${r.status || "blank"}` },
                  (STATUSES.find((s) => s.key === r.status) || {}).label || "—"),
                el("span", { class: "result-initials" }, r.initials || "")));
          }))))) : null,

      log.notes ? el("div", { class: "notes-block" },
        el("h3", { class: "log-section-title" }, "Notes"),
        el("p", {}, log.notes)) : null,

      el("div", { class: "form-actions" },
        el("button", { class: "btn btn-ghost", onclick: () => location.hash = `/edit/${log.id}` }, "Edit"),
        el("button", { class: "btn btn-ghost", onclick: () => window.print() }, "Print / PDF"),
        el("button", { class: "btn btn-danger", onclick: () => {
          if (confirm("Delete this log? This cannot be undone.")) { Store.remove(log.id); location.hash = "/history"; }
        } }, "Delete")))
  );
}

function metaPill(label, value) {
  return el("span", { class: "meta-pill" },
    el("span", { class: "meta-pill-label" }, label),
    el("span", { class: "meta-pill-value" }, value));
}

/* ====================================================================
 * History
 * ================================================================== */
function renderHistory(view) {
  const logs = Store.all();

  const exportBtn = el("button", { class: "btn btn-ghost", onclick: () => {
    const blob = new Blob([JSON.stringify(logs, null, 2)], { type: "application/json" });
    const a = el("a", { href: URL.createObjectURL(blob), download: `inspection-logs-${todayISO()}.json` });
    document.body.appendChild(a); a.click(); a.remove();
  } }, "Export JSON");

  const importInput = el("input", { type: "file", accept: "application/json", class: "visually-hidden",
    onchange: (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async () => {
        try {
          const data = JSON.parse(reader.result);
          if (confirm(`Import ${Array.isArray(data) ? data.length : 0} logs? They'll be merged in and synced.`)) {
            await Store.importMerge(data); router();
          }
        } catch (err) { alert("Could not read that file: " + err.message); }
      };
      reader.readAsText(file);
    } });
  const importBtn = el("button", { class: "btn btn-ghost", onclick: () => importInput.click() }, "Import");

  view.replaceChildren(
    el("section", { class: "stack" },
      el("div", { class: "section-head-row" },
        el("h1", { class: "page-title" }, "Log history"),
        el("div", { class: "head-actions" }, importBtn, importInput, exportBtn,
          el("button", { class: "btn btn-primary", onclick: () => (location.hash = "/new") }, "New log"))),

      logs.length
        ? el("div", { class: "log-list" }, logs.map(logRow))
        : el("div", { class: "empty-hint" }, "No logs recorded yet."))
  );
}

/* ====================================================================
 * Settings — cloud sync
 * ================================================================== */
const SCHEMA_SQL = `-- Run once in your Supabase project (SQL editor)
create table if not exists inspection_logs (
  id            text primary key,
  template_id   text,
  template_name text,
  meta          jsonb,
  results       jsonb,
  notes         text,
  deleted       boolean default false,
  created_at    timestamptz,
  updated_at    timestamptz
);
create index if not exists inspection_logs_updated_at
  on inspection_logs (updated_at);

-- Row Level Security. The policy below allows the public (anon) key full
-- access — fine for a single trusted team / kiosk. Tighten for production.
alter table inspection_logs enable row level security;
create policy "team access" on inspection_logs
  for all using (true) with check (true);`;

function renderSettings(view) {
  const cfg = Remote.cfg || {};
  const status = Sync.status();

  const urlEl = el("input", { class: "field-input", type: "url", placeholder: "https://YOUR-PROJECT.supabase.co", value: cfg.url || "" });
  const keyEl = el("input", { class: "field-input", type: "password", placeholder: "anon / public API key", value: cfg.key || "" });
  const tableEl = el("input", { class: "field-input", placeholder: "inspection_logs", value: cfg.table || "inspection_logs" });
  const result = el("p", { class: "settings-result" });

  const collect = () => ({ url: urlEl.value.trim(), key: keyEl.value.trim(), table: (tableEl.value.trim() || "inspection_logs") });

  const saveConnect = () => {
    const c = collect();
    if (!c.url || !c.key) { result.textContent = "Enter both a project URL and an API key."; result.className = "settings-result is-warn"; return; }
    Remote.save(c);
    result.textContent = "Saved. Syncing…";
    result.className = "settings-result is-ok";
    Sync.run().then(() => router());
  };

  const testConn = async () => {
    const c = collect();
    if (!c.url || !c.key) { result.textContent = "Enter a URL and key first."; result.className = "settings-result is-warn"; return; }
    Remote.save(c);
    result.textContent = "Testing connection…"; result.className = "settings-result";
    try { await Remote.test(); result.textContent = "✓ Connected — table reachable."; result.className = "settings-result is-ok"; }
    catch (e) { result.textContent = "✕ " + e.message; result.className = "settings-result is-error"; }
  };

  const disconnect = () => {
    if (!confirm("Disconnect cloud sync? Logs stay on this device.")) return;
    Remote.clear(); router();
  };

  const copySchema = (e) => {
    navigator.clipboard?.writeText(SCHEMA_SQL).then(
      () => { e.target.textContent = "Copied ✓"; setTimeout(() => (e.target.textContent = "Copy SQL"), 1500); },
      () => { e.target.textContent = "Copy failed"; });
  };

  view.replaceChildren(
    el("section", { class: "stack form-wrap" },
      crumb("Settings"),
      el("h1", { class: "page-title" }, "Cloud sync"),
      el("p", { class: "page-sub" },
        "Logs always save on this device first, so the app keeps working with no signal. " +
        "Connect a Supabase database to back them up and sync across phones, tablets and staff."),

      el("div", { class: `sync-status-card is-${status.mode}` },
        el("span", { class: "sync-dot " + (SYNC_BADGE[status.mode] || SYNC_BADGE.local).dot }),
        el("div", {},
          el("strong", {}, statusHeadline(status)),
          el("span", { class: "page-sub" }, statusDetail(status)))),

      el("div", { class: "settings-form" },
        el("label", { class: "field" }, el("span", { class: "field-label" }, "Supabase project URL"), urlEl),
        el("label", { class: "field" }, el("span", { class: "field-label" }, "Anon / public API key"), keyEl),
        el("label", { class: "field" }, el("span", { class: "field-label" }, "Table name"), tableEl),
        result,
        el("div", { class: "form-actions settings-actions" },
          Remote.isConfigured() ? el("button", { class: "btn btn-danger", onclick: disconnect }, "Disconnect") : null,
          el("button", { class: "btn btn-ghost", onclick: testConn }, "Test connection"),
          el("button", { class: "btn btn-ghost", onclick: () => Sync.run() }, "Sync now"),
          el("button", { class: "btn btn-primary", onclick: saveConnect }, "Save & connect"))),

      el("details", { class: "schema-block" },
        el("summary", {}, "Database setup — one-time SQL"),
        el("p", { class: "page-sub" }, "Create a free Supabase project, then paste this into its SQL editor:"),
        el("div", { class: "schema-head" },
          el("button", { class: "btn btn-mini", onclick: copySchema }, "Copy SQL")),
        el("pre", { class: "schema-sql" }, SCHEMA_SQL),
        el("p", { class: "page-sub" },
          "Find the URL and anon key in Supabase under Project Settings → API. " +
          "The anon key is meant for browsers; keep the service-role key out of this app.")))
  );
}

function statusHeadline(s) {
  return { local: "Saved on this device", offline: "Offline", syncing: "Syncing…", idle: "Cloud sync active", error: "Sync error" }[s.mode] || "Saved on this device";
}
function statusDetail(s) {
  if (s.mode === "local") return "No cloud connected. Logs live in this browser only.";
  if (s.mode === "offline") return `${s.pending} change${s.pending === 1 ? "" : "s"} will sync when you're back online.`;
  if (s.mode === "error") return s.error || "Could not reach the database.";
  if (s.mode === "idle") return s.pending ? `${s.pending} change(s) pending.` : "Everything is backed up.";
  return "Working…";
}

/* ====================================================================
 * Bits
 * ================================================================== */
function crumb(current, backRoute = "", backLabel = "Home") {
  return el("div", { class: "crumb" },
    el("a", { class: "link", href: backRoute ? `#/${backRoute}` : "#/" }, "← " + (backRoute ? backLabel : "Home")),
    el("span", { class: "crumb-sep" }, "/"),
    el("span", { class: "crumb-current" }, current));
}

function spruceScene() {
  // A bright Spruce Lake postcard: sun, mountains, spruce treeline, lake.
  const W = 320, H = 198;
  // A spruce silhouette as a single zig-zag polygon, built from base point.
  const spruce = (bx, by, w, h, color) => {
    const pts = [
      [0, -h], [0.10 * w, -0.62 * h], [0.06 * w, -0.62 * h], [0.20 * w, -0.30 * h],
      [0.13 * w, -0.30 * h], [0.30 * w, 0], [-0.30 * w, 0], [-0.13 * w, -0.30 * h],
      [-0.20 * w, -0.30 * h], [-0.06 * w, -0.62 * h], [-0.10 * w, -0.62 * h],
    ].map(([dx, dy]) => `${(bx + dx).toFixed(1)},${(by + dy).toFixed(1)}`).join(" ");
    return `<polygon points="${pts}" fill="${color}"/>`;
  };

  const trees =
    spruce(34, 122, 30, 56, "#1f6f5c") +
    spruce(58, 122, 22, 40, "#2a8068") +
    spruce(288, 122, 32, 60, "#1c6857") +
    spruce(264, 122, 22, 42, "#2a8068") +
    spruce(150, 122, 18, 34, "#27795f");

  const svg = `
<svg viewBox="0 0 ${W} ${H}" class="hero-art-svg" role="img" aria-label="Spruce Lake landscape">
  <defs>
    <linearGradient id="sl-sky" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="#a9e9e0"/><stop offset="1" stop-color="#eafaf5"/>
    </linearGradient>
    <linearGradient id="sl-lake" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="#3f9aa6"/><stop offset="1" stop-color="#16545e"/>
    </linearGradient>
    <clipPath id="sl-frame"><rect x="0" y="0" width="${W}" height="${H}" rx="16"/></clipPath>
  </defs>
  <g clip-path="url(#sl-frame)">
    <rect x="0" y="0" width="${W}" height="122" fill="url(#sl-sky)"/>
    <circle cx="244" cy="48" r="26" fill="#ffcf6e"/>
    <circle cx="244" cy="48" r="34" fill="#ffcf6e" opacity="0.28"/>
    <path d="M0 122 L58 60 L116 122 Z" fill="#79bdba"/>
    <path d="M64 122 L150 40 L236 122 Z" fill="#579e9e"/>
    <path d="M176 122 L250 64 L320 122 Z" fill="#6cb4b0"/>
    <path d="M138 58 L150 40 L162 58 L154 53 L150 57 L146 53 Z" fill="#eafaf5"/>
    <rect x="0" y="122" width="${W}" height="76" fill="url(#sl-lake)"/>
    <g stroke="#cdf2ec" stroke-width="2.4" opacity="0.5" stroke-linecap="round">
      <line x1="28" y1="140" x2="78" y2="140"/>
      <line x1="120" y1="152" x2="196" y2="152"/>
      <line x1="214" y1="143" x2="256" y2="143"/>
      <line x1="56" y1="172" x2="138" y2="172"/>
      <line x1="180" y1="176" x2="250" y2="176"/>
    </g>
    ${trees}
  </g>
  <rect x="1" y="1" width="${W - 2}" height="${H - 2}" rx="16" fill="none" stroke="rgba(255,255,255,0.45)" stroke-width="2"/>
</svg>`;
  return el("div", { class: "hero-art", html: svg });
}
