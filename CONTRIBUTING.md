# Contributing

## Translations

All in-game text lives in one file: [`src/main/resources/assets/echoes/lang/en_us.json`](src/main/resources/assets/echoes/lang/en_us.json).
It's the only language shipped today — there's no translation pipeline beyond
"add a JSON file and open a PR."

To add a language:

1. Copy `en_us.json` to `<locale>.json` in the same directory (e.g. `de_de.json` for
   German, `fr_fr.json` for French — use [Minecraft's locale codes](https://minecraft.wiki/w/Language)).
2. Translate the values only — every key (the left-hand side, e.g.
   `"block.echoes.echocite_ore"`) must stay exactly as-is; only Minecraft's client
   uses the key to look up your translation.
3. Keep any `%s` placeholders in the same relative order (they're positional).
4. It's fine to submit a partial file — Minecraft falls back to `en_us` for any key
   your file doesn't include, so a partial translation never breaks in-game text.
5. Open a PR. If you're touching an existing language file rather than adding a new
   one, a diff makes it easy to review even without speaking the language.

No build step or script needs to run for a translation PR — the lang file is loaded
directly by the game at runtime.

## Code changes

See [`README.md`](README.md#build--run) for build setup. `./gradlew build` compiles
and runs the unit test suite (`src/test`); CI runs the same on every push and PR.
