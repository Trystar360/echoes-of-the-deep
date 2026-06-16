/*
 * Cloud adapter — Supabase REST (PostgREST). Holds the connection config and
 * translates between local log records and database rows. Swappable: anything
 * exposing isConfigured/pull/push/test can stand in (used by the test harness).
 */
const CFG_KEY = "spruce_remote_cfg";

const Remote = {
  cfg: null,

  load() {
    try { this.cfg = JSON.parse(localStorage.getItem(CFG_KEY) || "null"); }
    catch (e) { this.cfg = null; }
    return this.cfg;
  },
  save(cfg) { this.cfg = cfg; localStorage.setItem(CFG_KEY, JSON.stringify(cfg)); },
  clear() { this.cfg = null; localStorage.removeItem(CFG_KEY); },

  isConfigured() { return !!(this.cfg && this.cfg.url && this.cfg.key); },
  table() { return (this.cfg && this.cfg.table) || "inspection_logs"; },

  _headers(extra) {
    return Object.assign(
      { apikey: this.cfg.key, Authorization: "Bearer " + this.cfg.key, "Content-Type": "application/json" },
      extra || {});
  },
  _endpoint() { return this.cfg.url.replace(/\/+$/, "") + "/rest/v1/" + this.table(); },

  toRow(l) {
    return {
      id: l.id, template_id: l.templateId, template_name: l.templateName,
      meta: l.meta, results: l.results, notes: l.notes || null,
      deleted: !!l.deleted, created_at: l.createdAt, updated_at: l.updatedAt,
    };
  },
  fromRow(r) {
    return {
      id: r.id, templateId: r.template_id, templateName: r.template_name,
      meta: r.meta, results: r.results, notes: r.notes || "",
      deleted: !!r.deleted, createdAt: r.created_at, updatedAt: r.updated_at, dirty: false,
    };
  },

  async pull(sinceISO) {
    const filter = sinceISO ? `updated_at=gt.${encodeURIComponent(sinceISO)}&` : "";
    const url = `${this._endpoint()}?${filter}order=updated_at.asc&select=*`;
    const res = await fetch(url, { headers: this._headers() });
    if (!res.ok) throw new Error(`Pull failed (${res.status}): ${await res.text()}`);
    return (await res.json()).map(this.fromRow);
  },

  async push(logs) {
    if (!logs.length) return;
    const res = await fetch(this._endpoint(), {
      method: "POST",
      headers: this._headers({ Prefer: "resolution=merge-duplicates,return=minimal" }),
      body: JSON.stringify(logs.map((l) => this.toRow(l))),
    });
    if (!res.ok) throw new Error(`Push failed (${res.status}): ${await res.text()}`);
  },

  // Lightweight connectivity check — pulls "nothing" but proves auth + table.
  async test() {
    const res = await fetch(`${this._endpoint()}?select=id&limit=1`, { headers: this._headers() });
    if (!res.ok) throw new Error(`(${res.status}) ${await res.text()}`);
    return true;
  },
};
