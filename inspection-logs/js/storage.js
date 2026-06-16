/*
 * Local-first persistence. Logs live in localStorage so the app works
 * fully offline in the field — no account, no network required.
 */

const STORAGE_KEY = "eotd_inspection_logs_v1";

const Store = {
  all() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      const list = raw ? JSON.parse(raw) : [];
      return Array.isArray(list) ? list : [];
    } catch (e) {
      console.error("Could not read logs", e);
      return [];
    }
  },

  get(id) {
    return this.all().find((l) => l.id === id) || null;
  },

  save(log) {
    const list = this.all();
    const idx = list.findIndex((l) => l.id === log.id);
    if (idx >= 0) list[idx] = log;
    else list.unshift(log);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    return log;
  },

  remove(id) {
    const list = this.all().filter((l) => l.id !== id);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  },

  replaceAll(list) {
    if (!Array.isArray(list)) throw new Error("Import must be an array of logs");
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  },

  newId() {
    return "log_" + Date.now().toString(36) + "_" + Math.random().toString(36).slice(2, 7);
  },
};
