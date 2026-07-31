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

## v2 — created 2026-07-31
Everything v1 measures, plus feature-depth checks: AE2-style autocrafting
present (FabricatorBlockEntity), item transport over the network
(wirelessItems), MA-style growth acceleration (RadiatorBlockEntity), and the
Fabricator classes being present in the shipped jar. Differs from v1 in that
it asserts not just feature-family presence but the new Fabricator feature
end-to-end (source → jar).

## v3 — created 2026-07-31 (feature/echo-bloom)
- Measures: everything in v2, PLUS MA-style resource crop acceptance — EchoBloomBlock present in src and shipped jar, echo_bloom_seeds/echo_essence registered, loot table (age-7 essence drop) and essence→radiant-dust condense recipe present.
- Diff vs v2: +4 checks (resource crop in code, seeds registered, loot+recipes, crop class in jar). Also runs v2's asset/lang checks against the 6 new echo-bloom textures (32x after pipeline upgrade).
- Latest run: PASS (see runs/*-v3-echo-bloom.log, exit 0).

## v4 — created 2026-07-31 (feature/essence-economy)
- Measures: everything in v3, PLUS ProjectE-economy integration of the crop line — echo_essence seeded at 4096 in light_values.json (consistent with the 8→2 radiant-dust ring), three essence→resource recipes (raw echocite, drumstone shard, silentite crystal), and docs/mechanics-guide.md covering all four system families.
- Diff vs v3: +3 checks (essence light value, essence resource recipes, mechanics guide coverage).
- Latest run: PASS, 19/19 (see runs/*-v4-essence-economy.log, exit 0).

## v5 — created 2026-07-31 (feature/progression)
- Measures: everything in v4, PLUS in-game progression completeness — Great Work advancements for fabricator/echo_bloom/echo_essence present with lang keys, all advancement JSON valid, mod version bumped to 0.3.0 and the 0.3.0 jar builds.
- Diff vs v4: +5 checks (version bump, advancements present, advancement lang, advancement JSON validity, 0.3.0 jar).
- Latest runs: PASS, 24/24 (see runs/*-v5-progression.log and runs/*-v5-tooltip-sync.log, both exit 0).
