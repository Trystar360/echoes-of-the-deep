# MC 26.1.2 upgrade — implementation checklist

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
