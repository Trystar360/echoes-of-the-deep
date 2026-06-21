# MC 26.1.2 upgrade — implementation checklist

> ## ✅ COMPLETE — the port is done, builds, and runs.
>
> - **Both source sets compile** against Minecraft **26.1.2** (Mojang official mappings),
>   Fabric Loader 0.19.3, Fabric API 0.152.1, Java 25, Loom 1.17.
> - **`./gradlew clean build` is green** and produces `echoes-of-the-deep-0.2.0.jar`.
> - **The dedicated server boots to `Done`** with the mod loaded: items/blocks register,
>   both mixins apply, resources reload, and the recipe-graph Light-Value derivation runs
>   (`124 seeds + 781 from recipes = 905 valued items`).
> - Runtime caught and fixed one issue compile could not: `playSound` is declared on
>   `Level` (not `ServerLevel`), so `ServerWorldMixin` now mixes into `Level` with a
>   server-side guard.
> - The whole-codebase Yarn→Mojmap migration was driven by verifying every rename against
>   the deobf `26.1.2` jars with `javap` before applying it.

Living tracker for executing [`upgrade-26.1-plan.md`](upgrade-26.1-plan.md). Tick items as
they land. Order matters: **Phase A** (Mojmap on 1.21.4) is verifiable in the current
Java 21 / Loom 1.9.2 environment; **Phase B** (jump to 26.1.2) needs Java 25 + the 26.1
toolchain and may be gated by dependency/runtime availability here.

## Environment reconnaissance (confirmed)

A trial run on this repo established the real scope:

- ✅ **Mojmap resolves** — `mappings loom.officialMojangMappings()` downloads and the build
  runs on the current Loom 1.9.2 / Java 21 toolchain (so Phase A is doable in-repo).
- 📏 **Rename surface:** **110 Java files**, **130 unique `net.minecraft` imports**, 25
  Fabric-API imports. Switching to Mojmap with no source changes produced **100+**
  `cannot find symbol` errors (Java caps the report at 100) — package/class renames
  (`net.minecraft.util.math.BlockPos`→`net.minecraft.core.BlockPos`, `Text`→`Component`,
  `PlayerEntity`→`Player`, `ScreenHandler`→`AbstractContainerMenu`, …) **plus**
  signature-sensitive method renames (`writeNbt`→`saveAdditional`, `getCachedState`→
  `getBlockState`, `markDirty`→`setChanged`, …) and `Mixin has no targets`.
- ⚠️ **No safe auto-route:** Loom's `migrateMappings` task exists but reliably targets
  Yarn→Yarn, not official Mojmap. The rename is therefore manual and **all-or-nothing**
  (the source set won't compile until every file is converted) — best done with the
  IntelliJ "migrate to official mappings" map or a careful scripted pass + iteration.
- ⛔ **Phase B can't be built here:** only **Java 21** is installed; 26.1 needs **Java 25**.
  Phase B code can be written but must be compiled/run where Java 25 + the 26.1 toolchain
  (Loom 1.15, Fabric API 26.1.2) are available.

## Phase B kickoff — verified against the real 26.1.2 jars

Provisioned **JDK 25** (Temurin 25.0.3) and the 26.1.2 toolchain; the build **resolves and
compiles-attempts headlessly**. Toolchain (committed): Loom **1.17-SNAPSHOT**, Gradle
**9.5.1**, Loader **0.19.3**, Fabric API **0.152.1+26.1.2**, mappings = Loom official
(no `mappings` line). A trial source migration (`migrate_mojmap.py` + `migrate_mojmap2.py`)
was run, the surface measured, then **reverted** (kept the scripts; src stays clean) so the
branch isn't littered with half-renamed code.

**Key finding — 26.1's official names are bespoke, derive the map from the jar.** Packages
are Mojmap-style (`net.minecraft.core.BlockPos`, `net.minecraft.world.*`) but some class
names keep Mojang's own spelling. Validated all 130 mappings against
`minecraft-*-deobf-26.1.2.jar`: **124/130 correct**. Corrections/uniques:
- `Identifier` stays **`Identifier`** (`net.minecraft.resources.Identifier`) — *not*
  `ResourceLocation`. (Its `Identifier.of(ns,path)` factory likely also renamed — verify.)
- RenderType is `net.minecraft.client.renderer.rendertype.**RenderType**`.
- `Item.Properties`, `BlockBehaviour.Properties`, `HolderLookup.Provider` confirmed (block
  ctors use the inherited bare `Properties`, not `Settings`).

**Real API changes (not just renames) — own tasks:**
- ⛔ **`PickaxeItem` and `SwordItem` were removed** in the 26.1 tool rework (Axe/Hoe/Shovel
  remain). `ResonantPickaxeItem`/`ResonantSwordItem` must become `Item` (or a tool base)
  with tool/weapon **data components** instead of subclassing the removed classes.
- ⚠️ **GUI overhaul:** the `DrawContext`/`GuiGraphics` draw class and the slot click-type
  (`SlotActionType`→`ClickType`?) names changed — resolve from the client jar; affects the
  4 `*Screen` classes' `drawTexture`/`onSlotClick`.
- **Method-rename layer still pending** (the scripts do imports/classes only): e.g.
  `writeNbt`→`saveAdditional` (+signature), `getCachedState`→`getBlockState`,
  `markDirty`→`setChanged`, `getPos`→`getBlockPos`, `up()/down()`→`above()/below()`,
  `getTime`→`getGameTime`, `isChunkLoaded`→`hasChunk`, recipe `MapCodec`/`StreamCodec`,
  Fabric `InventoryStorage`/`ExtendedScreenHandlerType` API, mixin descriptors. These
  surface as the next compile round and are fixed per-file.

## Phase A — migrate Yarn → Mojang official mappings (still on 1.21.4)

- [ ] `build.gradle`: `mappings "net.fabricmc:yarn:…:v2"` → `mappings loom.officialMojangMappings()`
- [ ] `gradle.properties`: remove `yarn_mappings`
- [ ] First build to enumerate the rename surface (`./gradlew compileJava compileClientJava`)
- [ ] Bulk type renames (main + client). Known high-frequency:
  - [ ] `Identifier` → `ResourceLocation` (+ `Identifier.of(` → `ResourceLocation.fromNamespaceAndPath(` / `.parse(`)
  - [ ] `ServerWorld` → `ServerLevel`, `World` → `Level`
  - [ ] `PlayerEntity` → `Player`, `LivingEntity`/`Entity` (stable)
  - [ ] `RegistryEntry` → `Holder`, `RegistryKey` → `ResourceKey`, `Registries` → `BuiltInRegistries` (+ registry helper names)
  - [ ] `SoundCategory` → `SoundSource`, `SoundEvent` (stable)
  - [ ] `ItemStack`/`Item`/`Block`/`BlockPos`/`BlockState` (mostly stable — verify method names)
  - [ ] `Text` → `Component`, `MutableText` → `MutableComponent`
  - [ ] `ScreenHandler` → `AbstractContainerMenu`, `ScreenHandlerType` → `MenuType`, `PropertyDelegate` → `ContainerData`
  - [ ] `Inventory`/`SimpleInventory` → `Container`/`SimpleContainer`, `Slot` (verify)
  - [ ] `NbtCompound` → `CompoundTag`, `NbtList`/`NbtElement` → `ListTag`/`Tag`
  - [ ] `BlockEntity`/`BlockEntityType` (stable; verify `Builder`)
  - [ ] `ComponentType` → `DataComponentType`, `PacketCodecs` → `ByteBufCodecs`
  - [ ] `ItemGroup` → `CreativeModeTab`
- [ ] Mixins → Mojmap method targets + descriptors:
  - [ ] `ServerWorldMixin#playSound(…)` descriptor (Player/Holder<SoundEvent>/SoundSource)
  - [ ] `LivingEntityMixin#onDeath`, `#handleFallDamage` (confirm names exist in Mojmap)
- [ ] Client (`src/client/java`): `RenderLayer`, `DrawContext`, `HandledScreens`, screen classes
- [ ] `./gradlew build` green
- [ ] `./gradlew runServer` headless smoke (registries, mixins, recipes, advancements, worldgen)
- [ ] Commit "Phase A: migrate to Mojang official mappings (1.21.4)"

## Phase B — bump to Minecraft 26.1.2

### Toolchain
- [ ] `./gradlew wrapper --gradle-version 9.4.0`
- [ ] Loom plugin id → `net.fabricmc.fabric-loom` version `1.15.x`
- [ ] `gradle.properties`: `minecraft_version=26.1.2`, `loader_version=0.18.4`(or latest), `fabric_version=<26.1.2 build>`
- [ ] Dep configs: `modImplementation`→`implementation`, `modCompileOnly`→`compileOnly`; `remapJar`→`jar` if used
- [ ] Java toolchain 21 → **25** (build.gradle + `fabric.mod.json` `"java": ">=25"` + `"minecraft": "~26.1"`)
- [ ] CI: `actions/setup-java` `21` → `25` in `release.yml`; re-check `build_showcase.sh`

### Code (fix against 26.1 API)
- [ ] Data components (`ModComponents`) builder/codec API
- [ ] Creative tab (`ModItemGroups`): `ItemGroupEvents`→`CreativeModeTabEvents`; delayed `ItemStack` → `ItemStackTemplate` (3 `new ItemStack(` sites + tab entries)
- [ ] Recipes (`CrushingRecipe`, `ModRecipes`): serializer → `MapCodec` + `StreamCodec`
- [ ] Screen handlers (`ModScreens`, `*ScreenHandler`, `ExtendedScreenHandlerType`+`BlockPos` codec)
- [ ] Client render layers: remove/relocate `BlockRenderLayerMap` (now auto from sprite props)
- [ ] Client GUI: `DrawContext.drawTexture(RenderLayer::getGuiTextured,…)` signature; `HandledScreens` rename
- [ ] Fabric API: drop refs to removed `fabric-loot-api-v2` / `fabric-convention-tags-v1`; don't depend on `fabric` mod id
- [ ] Transfer API (`WirelessNetworkManager`): `Storage`/`ItemVariant`/`FluidVariant`/`StorageUtil`/`Transaction`/`FluidConstants` package+method check

### Data & resource packs
- [ ] `pack_format` (26.1.2) in mod pack meta + `gen_advancements.py` + `gen_showcase.py` + `showcase/datapack/pack.mcmeta`
- [ ] Item models: re-verify `assets/echoes/items/` definitions + regen via `gen_textures.py`
- [ ] Advancements: re-validate `inventory_changed` item-predicate format + the 2 item tags
- [ ] Showcase datapack: re-run `build_showcase.sh`, re-verify sign NBT / item_frame / gamerules; repackage save
- [ ] Worldgen JSON + `light_values.json` + reload listeners

### Dependencies
- [ ] Team Reborn Energy 26.1 build (else keep `isModLoaded`-gated / temporarily drop the compat module)
- [ ] Trinkets — leave out until a 26.1 build exists (suggested-only)

### Verify
- [ ] `./gradlew build` green on Java 25
- [ ] `runServer` headless: registries/mixins/recipes/advancements/worldgen OK
- [ ] `runClient` (where possible): rendering, all 5 GUIs, flight, tools
- [ ] Showcase headless build: zero command errors; repackage + re-attach to release
- [ ] Wireless transfer, transmutation economy, energy distribution behave as before

### Release
- [ ] Bump `mod_version`; tag a 26.1.2-compatible release; update README/wiki version references

## Confirm before Phase B
- [ ] Exact Fabric API string for 26.1.2 + matching Loader/Loom point releases (fabricmc.net/develop)
- [ ] Resource/data `pack_format` numbers for 26.1.2
- [ ] 26.1 Team Reborn Energy availability
- [ ] Java 25 toolchain available in this environment (else Phase B builds elsewhere)
