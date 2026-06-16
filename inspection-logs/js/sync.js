/*
 * Sync engine. Push dirty records up, pull remote changes down, last-write-wins.
 * Triggered after writes (debounced), when the network returns, and on a timer.
 * When no cloud is configured the app is simply local-only and fully offline.
 */
const Sync = {
  state: "idle",        // idle | syncing | error
  lastError: null,
  lastSyncedAt: null,
  _statusFns: [],
  _debounce: null,
  _rerun: false,

  onStatus(fn) { this._statusFns.push(fn); },

  status() {
    if (!Remote.isConfigured()) return { mode: "local", pending: 0 };
    if (typeof navigator !== "undefined" && navigator.onLine === false)
      return { mode: "offline", pending: Store.dirtyLogs().length };
    return { mode: this.state, pending: Store.dirtyLogs().length, error: this.lastError, at: this.lastSyncedAt };
  },
  _emit() { const s = this.status(); this._statusFns.forEach((f) => { try { f(s); } catch (e) {} }); },

  schedule() {
    clearTimeout(this._debounce);
    this._debounce = setTimeout(() => this.run(), 500);
  },

  async run() {
    if (!Remote.isConfigured() || (typeof navigator !== "undefined" && navigator.onLine === false)) {
      this._emit();
      return;
    }
    if (this.state === "syncing") { this._rerun = true; return; }
    this.state = "syncing"; this.lastError = null; this._emit();
    try {
      // 1) push everything dirty (includes tombstones)
      const dirty = Store.dirtyLogs();
      if (dirty.length) {
        await Remote.push(dirty);
        await Store.markClean(dirty.map((l) => l.id), new Date().toISOString());
      }
      // 2) pull changes since our last successful pull
      const since = await IDB.getMeta("lastPulledAt");
      const remote = await Remote.pull(since);
      if (remote.length) {
        await Store.applyRemote(remote);
        const maxUpdated = remote.reduce((m, r) => ((r.updatedAt || "") > m ? r.updatedAt : m), since || "");
        if (maxUpdated) await IDB.setMeta("lastPulledAt", maxUpdated);
      }
      this.state = "idle";
      this.lastSyncedAt = new Date().toISOString();
    } catch (e) {
      console.error("Sync error", e);
      this.state = "error";
      this.lastError = e.message || String(e);
    }
    this._emit();
    if (this._rerun) { this._rerun = false; this.schedule(); }
  },

  init() {
    Remote.load();
    if (typeof window !== "undefined") {
      window.addEventListener("online", () => { this._emit(); this.run(); });
      window.addEventListener("offline", () => this._emit());
    }
    this.run();
    setInterval(() => this.run(), 60000);  // periodic catch-up
  },
};
if (typeof window !== "undefined") window.Sync = Sync;
