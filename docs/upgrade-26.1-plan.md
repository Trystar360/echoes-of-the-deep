# Upgrade plan — Minecraft 1.21.4 → 26.1.2

> **Status: plan only.** This documents *how* to port Octaves of the One to Minecraft
> **26.1.2** (the Fabric 26.1 line). It is not a small version bump — 26.1 is the largest
> toolchain change in Fabric's history. Treat this as a scoped project on its own branch.

## TL;DR — why this is big

Minecraft **26.1** (the calendar-versioned successor to the 1.21.x line) is **the first
unobfuscated Minecraft release**. Consequences that hit this mod directly:

1. **Yarn is gone.** Fabric moved to **Mojang official mappings**. Every Yarn type/method
   name in our ~100 Java files changes (`ServerWorld`→`ServerLevel`, `PlayerEntity`→
   `Player`, `Identifier`→`ResourceLocation`, `ItemStack`/`Block`/`Item` mostly stable,
   etc.), and our **mixin method descriptors** must be rewritten in Mojmap.
2. **New toolchain:** Loom **1.15**, Gradle **9.4.0**, Fabric Loader **0.18.4**, **Java 25**,
   IntelliJ **2025.3+**. The Loom plugin id and several Gradle configs change.
3. **No old deps work.** "No mods for 1.21.11 or older work on 26.1, even as a compile-only
   dependency." Our soft deps **Team Reborn Energy** (`4.0.1`) and **Trinkets** (`3.10.0`)
   need 26.1 builds or must be dropped/feature-gated.
4. **Accumulated API churn** across the ~7 versions between 1.21.4 and 26.1 (1.21.5–1.21.11):
   data-driven item models, recipe `MapCodec`/`StreamCodec`, automatic render layers,
   creative-tab event renames, delayed `ItemStack` creation, and more.

Sources: [Fabric for 26.1](https://fabricmc.net/2026/03/14/261.html) ·
[Porting to 26.1](https://docs.fabricmc.net/develop/porting/) ·
[Fabric versions](https://samolego.github.io/fabric-versions/fabric.html).

## Strategy

Recommended **two-phase, big-bang** port on a dedicated `feat/mc-26.1` branch (keep
`main` on 1.21.4 as the stable/LTS line until 26.1 is shipping):

- **Phase A — migrate Yarn → Mojmap on 1.21.4 first** (the porting guide requires this
  *before* bumping the version). Use Loom's `migrateMappings`/IntelliJ migration map to
  re-name the codebase to official mappings while still on 1.21.4, confirm it builds, commit.
- **Phase B — jump straight to 26.1.2** and fix the compile errors against the new API
  (don't step through every intermediate version — they aren't the target). Lean on the
  [Fabric API 26.1 porting guide] and NeoForge's Migration Primer for vanilla changes.

Pin to a specific **26.1.2** toolchain — confirm exact strings on
<https://fabricmc.net/develop> (Fabric API build for 26.1.2, latest Loader ≥ 0.18.4).

## Work breakdown

### 1. Build & toolchain (`build.gradle`, `gradle.properties`, wrapper)
- `./gradlew wrapper --gradle-version 9.4.0`.
- Plugin id `id 'fabric-loom' version '1.9.2'` → `id 'net.fabricmc.fabric-loom' version '1.15.x'`.
- **Remove** the `mappings "net.fabricmc:yarn:…:v2"` line entirely.
- Dependency configs: `modImplementation`→`implementation`, `modCompileOnly`→`compileOnly`
  (our TR-Energy line), `modApi`→`api` (n/a here); `remapJar`→`jar` if referenced.
- Java toolchain **21 → 25** (build.gradle `JavaLanguageVersion.of(25)`,
  `sourceCompatibility`/`targetCompatibility`, and `fabric.mod.json` `"java": ">=25"`).
- `gradle.properties` pins: `minecraft_version=26.1.2`, `loader_version=0.18.4` (or latest),
  `fabric_version=<26.1.2 build>`, drop `yarn_mappings`; update `tr_energy_version` /
  `trinkets_version` to 26.1 builds (or remove).
- CI: bump `actions/setup-java` to **25** in `.github/workflows/release.yml`; re-verify
  `build_showcase.sh` (the dev server now needs Java 25). Pages/Wiki workflows unaffected.

### 2. Mappings rename (whole codebase) — Phase A
- Run the Yarn→Mojmap migration; expect every file under `src/main/java` and
  `src/client/java` to change imports/types. High-touch spots:
  - `Identifier` → `ResourceLocation` (used in nearly every registry class).
  - `ServerWorld`/`World`, `PlayerEntity`, `RegistryEntry`→`Holder`, `SoundCategory`→
    `SoundSource`, `BlockPos`/`ItemStack` (mostly stable).
- **Mixins** (`mixin/ServerWorldMixin`, `mixin/LivingEntityMixin`): rewrite `method =`
  targets in Mojmap. The fragile one is `ServerWorldMixin.playSound` — its descriptor
  `(Lnet/minecraft/entity/player/PlayerEntity;DDD…RegistryEntry;…SoundCategory;FFJ)V`
  becomes the Mojmap `ServerLevel#playSound(... Player; ... Holder<SoundEvent>; ...
  SoundSource; ...)` signature. Verify `onDeath`/`handleFallDamage` still exist/are named
  the same; vanilla may have refactored fall handling.

### 3. Registration & core APIs
- **Data components** (`registry/ModComponents`): `ComponentType`→`DataComponentType`,
  `PacketCodecs`→`ByteBufCodecs`, builder API may have shifted; re-verify
  `DataComponentType.builder().persistent(codec).networkSynchronized(streamCodec)`.
- **Creative tab** (`registry/ModItemGroups`): `ItemGroup`→`CreativeModeTab`,
  `ItemGroupEvents`→`CreativeModeTabEvents`; **`ItemStack` creation is delayed to world
  load** → tab entries and any eager `new ItemStack(...)` (3 sites: the tab icon, the
  Transmutation Table withdraw/condense outputs, Octave Star) may need `ItemStackTemplate`.
- **Recipes** (`recipe/CrushingRecipe`, `ModRecipes`): recipe serializers are now
  `MapCodec` + `StreamCodec`; port the codec/packet-codec pair accordingly.
- **Block entities / screens** (`registry/ModBlockEntities`, `ModScreens`): verify
  `BlockEntityType.Builder`, `ScreenHandlerType`, and `ExtendedScreenHandlerType` +
  `BlockPos.PACKET_CODEC` (the `ConfigScreenHandler` opener) survive the rename.

### 4. Client (`src/client/java`)
- **Render layers** are now **auto-assigned from sprite properties** —
  `BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout()/getCutoutMipped(), …)` in
  `EchoesClient` is likely **removed**; move the lumewood sapling/leaves/lumebloom/etc.
  cutout hints into their block/model definitions instead.
- **GUI drawing**: `DrawContext.drawTexture(RenderLayer::getGuiTextured, …)` in the four
  screen classes changed signature across 1.21.x — re-check against 26.1.
- **Screen registration**: `HandledScreens.register(...)` may be renamed; re-wire all five.
- Color providers: if any are added later, `ColorProviderRegistry`→`BlockColorRegistry`.

### 5. Fabric API module changes
- Removed modules `fabric-convention-tags-v1` and `fabric-loot-api-v2` — audit usage (our
  item tags live in `data/echoes/tags/item`; loot tables are plain JSON, so likely fine,
  but confirm nothing imports the removed APIs).
- Fabric API **no longer provides the `fabric` mod id** — don't depend on it by that id.
- **Transfer API** (`wireless/WirelessNetworkManager`: `Storage<ItemVariant>`,
  `FluidVariant`, `StorageUtil`, `Transaction`, `FluidConstants`) — re-verify package/method
  names; this is a core subsystem, test wireless item/fluid/RU transfer end-to-end.

### 6. Data & resource packs (formats + content)
- **`pack_format`** bumps for both resource and data packs — update the mod's internal pack
  meta and `showcase/datapack/pack.mcmeta` (currently `61`) to the 26.1.2 value (read it
  from a vanilla pack on 26.1; the gen scripts `gen_advancements.py`/`gen_showcase.py`
  hard-code formats).
- **Item models**: 1.21.5 moved item models to the data-driven `assets/<ns>/items/`
  definition format (already partially present) — re-verify every item renders; the
  procedural `gen_textures.py` output and model JSON may need regeneration.
- **Advancements** (`data/echoes/advancement/great_work/…`): the `minecraft:inventory_changed`
  **item predicate** format changed with the component rework — re-validate the 24 nodes and
  the two item tags load without error on 26.1.
- **Showcase datapack**: re-run `build_showcase.sh` on 26.1 and re-verify the sign NBT
  (4 JSON-string `messages`), `item_frame` summon, and gamerules still parse; repackage the
  save and re-attach to the release.
- Worldgen JSON (`data/echoes/worldgen/…`) and `light_values.json` — likely stable, but
  re-validate the reload listeners and biome modifications.

### 7. Compatibility / soft deps
- **Team Reborn Energy**: needs a 26.1 build of the energy API; if unavailable, keep the
  bridge `isModLoaded`-gated (already is) and `compileOnly` against whatever 26.1 build
  exists, else temporarily drop the compat module.
- **Trinkets**: only a *suggested* dep (planned Resonant Ring, not implemented) — safe to
  leave out until a 26.1 build exists.

## Verification checklist (per phase)
- `./gradlew build` green on Java 25.
- `runClient`: every block/item renders; all five GUIs open; flight + tools work.
- `runServer`: registries populate, mixins apply (ambient capture + fall immunity), recipes
  parse, advancements load, worldgen attaches.
- Re-run the **headless showcase build** (`build_showcase.sh`) with zero command errors and
  repackage the save.
- Wireless transfer (items/fluids/RU), the transmutation economy, and the energy network
  distribution behave as before.

## Risks & estimate
- **Largest risk:** the mappings switch + mixin descriptor rewrite (compile-time, but
  tedious) and any vanilla refactor of `playSound`/fall damage that breaks our injection
  points. Mitigation: do Phase A in isolation and keep mixins minimal.
- **Medium:** Transfer API and data-component/recipe-codec renames; render-layer relocation.
- **Dependency blocker:** if no 26.1 Team Reborn Energy exists yet, ship 26.1 without the
  energy bridge and restore it when the dep updates.
- **Rough effort:** ~2–4 focused days — ~1 day Phase A (mojmap), ~1–2 days Phase B
  (API fixes + client), ~0.5 day data/pack + showcase re-verify, buffer for deps.

## Open items to confirm before starting
- Exact **Fabric API** version string for **26.1.2** and the matching **Loader**/**Loom**
  point releases (from <https://fabricmc.net/develop>).
- The **resource/data `pack_format`** numbers for 26.1.2.
- Availability of **26.1 Team Reborn Energy** (and, later, Trinkets).
- Whether to keep a maintained **1.21.4 LTS branch** alongside 26.1.
