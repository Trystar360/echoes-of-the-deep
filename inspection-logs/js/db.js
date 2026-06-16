/*
 * Low-level IndexedDB wrapper — the on-device database.
 * One store for logs (keyed by id), one key/value store for sync metadata.
 */
const IDB = (() => {
  const NAME = "spruce_lake_logs";
  const VERSION = 1;
  let dbp = null;

  function open() {
    if (dbp) return dbp;
    dbp = new Promise((resolve, reject) => {
      const req = indexedDB.open(NAME, VERSION);
      req.onupgradeneeded = () => {
        const db = req.result;
        if (!db.objectStoreNames.contains("logs")) db.createObjectStore("logs", { keyPath: "id" });
        if (!db.objectStoreNames.contains("meta")) db.createObjectStore("meta");
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
    return dbp;
  }

  const reqp = (r) => new Promise((res, rej) => { r.onsuccess = () => res(r.result); r.onerror = () => rej(r.error); });
  const store = (name, mode) => open().then((db) => db.transaction(name, mode).objectStore(name));

  return {
    async getAllLogs() { return reqp((await store("logs", "readonly")).getAll()); },
    async putLog(log) { return reqp((await store("logs", "readwrite")).put(log)); },
    async putLogs(logs) {
      const db = await open();
      return new Promise((res, rej) => {
        const t = db.transaction("logs", "readwrite");
        logs.forEach((l) => t.objectStore("logs").put(l));
        t.oncomplete = () => res();
        t.onerror = () => rej(t.error);
      });
    },
    async getMeta(key) { return reqp((await store("meta", "readonly")).get(key)); },
    async setMeta(key, val) { return reqp((await store("meta", "readwrite")).put(val, key)); },
  };
})();
