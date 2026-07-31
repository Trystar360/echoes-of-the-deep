# Verifier index (append-only)

## v1 — created 2026-07-31
Measures: (1) mod jar builds; (2) JUnit tests green; (3) asset integrity —
every model texture reference exists, every blockstate has a lang key;
(4) all block/item textures are 32x (32x32 or 32x(32n) animated strips);
(5) presence of the four signature systems (ProjectE transmutation,
TD conduits, MA botanicals, AE2 wireless channels).
Files: verify.sh, check_assets.py, check_textures32.py.
Baseline: first version.

## Runs
See verifier/runs/*.log — one timestamped record per execution (command,
exit code, key values), including non-checkpoint runs.
