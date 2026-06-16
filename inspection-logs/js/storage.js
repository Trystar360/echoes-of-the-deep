/*
 * Offline-first store. The UI reads synchronously from an in-memory cache;
 * every write persists to IndexedDB, marks the record "dirty", and nudges the
 * sync engine. Remote changes are merged back in via applyRemote().
 *
 * A log record:
 *   { id, templateId, templateName, meta, results, notes,
 *     createdAt, updatedAt, deleted, dirty, syncedAt }
 */
const LEGACY_KEY = "eotd_inspection_logs_v1";

const Store = {
  _cache: [],
  _listeners: [],

  onChange(fn) { this._listeners.push(fn); },
  _emit() { this._listeners.forEach((f) => { try { f(); } catch (e) { console.error(e); } }); },

  async init() {
    await this._migrateLegacy();
    this._cache = await IDB.getAllLogs();
  },

  async _migrateLegacy() {
    try {
      const raw = localStorage.getItem(LEGACY_KEY);
      if (!raw) return;
      const list = JSON.parse(raw);
      if (Array.isArray(list) && list.length) {
        const now = new Date().toISOString();
        await IDB.putLogs(list.map((l) =>
          Object.assign({ deleted: false, dirty: true, updatedAt: l.updatedAt || l.createdAt || now }, l)));
      }
      localStorage.removeItem(LEGACY_KEY);
    } catch (e) { console.error("Legacy migration failed", e); }
  },

  /* ---- synchronous reads (from cache) ---- */
  all() {
    return this._cache
      .filter((l) => !l.deleted)
      .sort((a, b) => (b.updatedAt || b.createdAt || "").localeCompare(a.updatedAt || a.createdAt || ""));
  },
  get(id) {
    const l = this._cache.find((l) => l.id === id);
    return l && !l.deleted ? l : null;
  },

  /* ---- writes (local-first, then sync) ---- */
  async save(log) {
    log.updatedAt = new Date().toISOString();
    log.dirty = true;
    if (log.deleted === undefined) log.deleted = false;
    this._upsert(log);
    await IDB.putLog(log);
    this._emit();
    if (window.Sync) Sync.schedule();
    return log;
  },

  async remove(id) {
    const l = this._cache.find((l) => l.id === id);
    if (!l) return;
    l.deleted = true;            // tombstone so the delete can sync
    l.dirty = true;
    l.updatedAt = new Date().toISOString();
    await IDB.putLog(l);
    this._emit();
    if (window.Sync) Sync.schedule();
  },

  async importMerge(list) {
    if (!Array.isArray(list)) throw new Error("Import must be an array of logs");
    const now = new Date().toISOString();
    const norm = list.map((l) =>
      Object.assign({ deleted: false }, l, { dirty: true, updatedAt: l.updatedAt || l.createdAt || now }));
    norm.forEach((l) => this._upsert(l));
    await IDB.putLogs(norm);
    this._emit();
    if (window.Sync) Sync.schedule();
    return norm.length;
  },

  /* ---- sync engine helpers ---- */
  dirtyLogs() { return this._cache.filter((l) => l.dirty); },

  async markClean(ids, syncedAt) {
    const set = new Set(ids);
    const touched = this._cache.filter((l) => set.has(l.id));
    touched.forEach((l) => { l.dirty = false; l.syncedAt = syncedAt; });
    await IDB.putLogs(touched);
  },

  async applyRemote(records) {
    let changed = false;
    const toPersist = [];
    for (const r of records) {
      const local = this._cache.find((l) => l.id === r.id);
      // last-write-wins; never clobber un-synced local edits with older remote data
      if (!local || ((r.updatedAt || "") > (local.updatedAt || "") && !local.dirty)) {
        r.dirty = false;
        this._upsert(r);
        toPersist.push(r);
        changed = true;
      }
    }
    if (toPersist.length) await IDB.putLogs(toPersist);
    if (changed) this._emit();
    return changed;
  },

  _upsert(log) {
    const i = this._cache.findIndex((l) => l.id === log.id);
    if (i >= 0) this._cache[i] = log;
    else this._cache.push(log);
  },

  newId() {
    return "log_" + Date.now().toString(36) + "_" + Math.random().toString(36).slice(2, 7);
  },
};
