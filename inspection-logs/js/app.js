/*
 * Summit Logs — adventure programming daily inspection logs.
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
  else renderDashboard(view);

  $("#topbar").scrollIntoView({ behavior: "auto", block: "start" });
}
window.addEventListener("hashchange", router);
window.addEventListener("DOMContentLoaded", () => {
  buildChrome();
  router();
});

/* ====================================================================
 * App chrome (header + golden spiral mark)
 * ================================================================== */
function buildChrome() {
  $("#brand").addEventListener("click", () => (location.hash = "/"));
  $("#nav-history").addEventListener("click", (e) => {
    e.preventDefault();
    location.hash = "/history";
  });
  $("#nav-new").addEventListener("click", (e) => {
    e.preventDefault();
    location.hash = "/new";
  });
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
        el("p", { class: "eyebrow" }, "Adventure Programming"),
        el("h1", { class: "hero-title" }, "Daily Inspection Logs"),
        el("p", { class: "hero-sub" },
          "Run every pre-use, set-up and closing check from one place. Tap through your gear, sign off, and keep a clean record for every group — before the first rider clips in."),
        el("div", { class: "hero-actions" },
          el("button", { class: "btn btn-primary", onclick: () => (location.hash = "/new") }, "Start a log"),
          el("button", { class: "btn btn-ghost", onclick: () => (location.hash = "/history") },
            `History (${logs.length})`))),
      goldenSpiral()),

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
      reader.onload = () => {
        try {
          const data = JSON.parse(reader.result);
          if (confirm(`Import ${Array.isArray(data) ? data.length : 0} logs? This replaces all current logs.`)) {
            Store.replaceAll(data); router();
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
 * Bits
 * ================================================================== */
function crumb(current, backRoute = "", backLabel = "Home") {
  return el("div", { class: "crumb" },
    el("a", { class: "link", href: backRoute ? `#/${backRoute}` : "#/" }, "← " + (backRoute ? backLabel : "Home")),
    el("span", { class: "crumb-sep" }, "/"),
    el("span", { class: "crumb-current" }, current));
}

function goldenSpiral() {
  // A true logarithmic golden spiral: radius grows by φ every quarter turn.
  // Sampled as points, then auto-fit to the viewBox so the pole and the
  // outer sweep sit nicely inside the frame.
  const PHI = (1 + Math.sqrt(5)) / 2;
  const ns = "http://www.w3.org/2000/svg";
  const box = 240, pad = 18;

  const pts = [];
  const a = 1;
  for (let t = 0; t <= 1; t += 0.005) {
    const theta = t * 5.2 * Math.PI;               // ~2.6 turns
    const r = a * Math.pow(PHI, (2 * theta) / Math.PI);
    pts.push([r * Math.cos(theta), -r * Math.sin(theta)]);
  }
  // Fit points into the padded box.
  const xs = pts.map((p) => p[0]), ys = pts.map((p) => p[1]);
  const minX = Math.min(...xs), maxX = Math.max(...xs);
  const minY = Math.min(...ys), maxY = Math.max(...ys);
  const scale = (box - pad * 2) / Math.max(maxX - minX, maxY - minY);
  const ox = pad + ((box - pad * 2) - (maxX - minX) * scale) / 2 - minX * scale;
  const oy = pad + ((box - pad * 2) - (maxY - minY) * scale) / 2 - minY * scale;
  const fit = ([x, y]) => [(x * scale + ox).toFixed(2), (y * scale + oy).toFixed(2)];

  const svg = document.createElementNS(ns, "svg");
  svg.setAttribute("viewBox", `0 0 ${box} ${box}`);
  svg.setAttribute("class", "spiral");
  svg.setAttribute("aria-hidden", "true");

  // Faint golden rectangle around the sweep for texture.
  const rect = document.createElementNS(ns, "rect");
  const rw = box - pad * 2, rh = rw / PHI;
  rect.setAttribute("x", pad); rect.setAttribute("y", (box - rh) / 2);
  rect.setAttribute("width", rw); rect.setAttribute("height", rh);
  rect.setAttribute("rx", 6);
  rect.setAttribute("class", "spiral-cell");
  svg.appendChild(rect);

  const d = pts.map((p, i) => (i ? "L" : "M") + fit(p).join(",")).join(" ");
  const path = document.createElementNS(ns, "path");
  path.setAttribute("class", "spiral-arc");
  path.setAttribute("d", d);
  svg.appendChild(path);
  return svg;
}
