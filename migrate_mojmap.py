#!/usr/bin/env python3
"""One-shot Yarn -> Mojang-official (26.1) identifier migration for src/.

Translates import FQNs and renamed simple class names across all Java sources.
Method-level renames (e.g. getCachedState->getBlockState) are NOT handled here —
they surface as the next round of compile errors and are fixed by hand.

Run:  python3 migrate_mojmap.py     (idempotent-ish; re-running is safe)
"""
import os, re, glob

# yarn FQN -> mojmap FQN. Simple-name change (last segment differs) also triggers
# a whole-word token replacement across the file body.
MAP = {
 # block
 "net.minecraft.block.AbstractBlock": "net.minecraft.world.level.block.state.BlockBehaviour",
 "net.minecraft.block.Block": "net.minecraft.world.level.block.Block",
 "net.minecraft.block.BlockEntityProvider": "net.minecraft.world.level.block.EntityBlock",
 "net.minecraft.block.BlockSetType": "net.minecraft.world.level.block.state.properties.BlockSetType",
 "net.minecraft.block.BlockState": "net.minecraft.world.level.block.state.BlockState",
 "net.minecraft.block.Blocks": "net.minecraft.world.level.block.Blocks",
 "net.minecraft.block.FenceBlock": "net.minecraft.world.level.block.FenceBlock",
 "net.minecraft.block.FenceGateBlock": "net.minecraft.world.level.block.FenceGateBlock",
 "net.minecraft.block.Fertilizable": "net.minecraft.world.level.block.BonemealableBlock",
 "net.minecraft.block.FlowerBlock": "net.minecraft.world.level.block.FlowerBlock",
 "net.minecraft.block.LeavesBlock": "net.minecraft.world.level.block.LeavesBlock",
 "net.minecraft.block.PillarBlock": "net.minecraft.world.level.block.RotatedPillarBlock",
 "net.minecraft.block.SaplingBlock": "net.minecraft.world.level.block.SaplingBlock",
 "net.minecraft.block.SaplingGenerator": "net.minecraft.world.level.block.grower.TreeGrower",
 "net.minecraft.block.SlabBlock": "net.minecraft.world.level.block.SlabBlock",
 "net.minecraft.block.StairsBlock": "net.minecraft.world.level.block.StairBlock",
 "net.minecraft.block.TrapdoorBlock": "net.minecraft.world.level.block.TrapDoorBlock",
 "net.minecraft.block.WoodType": "net.minecraft.world.level.block.state.properties.WoodType",
 "net.minecraft.block.entity.BlockEntity": "net.minecraft.world.level.block.entity.BlockEntity",
 "net.minecraft.block.entity.BlockEntityTicker": "net.minecraft.world.level.block.entity.BlockEntityTicker",
 "net.minecraft.block.entity.BlockEntityType": "net.minecraft.world.level.block.entity.BlockEntityType",
 "net.minecraft.block.BlockSoundGroup": "net.minecraft.world.level.block.SoundType",
 # client
 "net.minecraft.client.gui.DrawContext": "net.minecraft.client.gui.GuiGraphics",
 "net.minecraft.client.gui.screen.ingame.HandledScreen": "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen",
 "net.minecraft.client.gui.screen.ingame.HandledScreens": "net.minecraft.client.gui.screens.MenuScreens",
 "net.minecraft.client.gui.tooltip.Tooltip": "net.minecraft.client.gui.components.Tooltip",
 "net.minecraft.client.gui.widget.ButtonWidget": "net.minecraft.client.gui.components.Button",
 "net.minecraft.client.render.RenderLayer": "net.minecraft.client.renderer.rendertype.RenderType",
 "net.minecraft.client.resource.language.I18n": "net.minecraft.client.resources.language.I18n",
 # component
 "net.minecraft.component.ComponentType": "net.minecraft.core.component.DataComponentType",
 "net.minecraft.component.DataComponentTypes": "net.minecraft.core.component.DataComponents",
 "net.minecraft.component.type.NbtComponent": "net.minecraft.world.item.component.CustomData",
 # entity
 "net.minecraft.entity.Entity": "net.minecraft.world.entity.Entity",
 "net.minecraft.entity.EntityType": "net.minecraft.world.entity.EntityType",
 "net.minecraft.entity.ExperienceOrbEntity": "net.minecraft.world.entity.ExperienceOrb",
 "net.minecraft.entity.ItemEntity": "net.minecraft.world.entity.item.ItemEntity",
 "net.minecraft.entity.LightningEntity": "net.minecraft.world.entity.LightningBolt",
 "net.minecraft.entity.LivingEntity": "net.minecraft.world.entity.LivingEntity",
 "net.minecraft.entity.damage.DamageSource": "net.minecraft.world.damagesource.DamageSource",
 "net.minecraft.entity.effect.StatusEffects": "net.minecraft.world.effect.MobEffects",
 "net.minecraft.entity.player.PlayerEntity": "net.minecraft.world.entity.player.Player",
 "net.minecraft.entity.player.PlayerInventory": "net.minecraft.world.entity.player.Inventory",
 # inventory
 "net.minecraft.inventory.Inventories": "net.minecraft.world.ContainerHelper",
 "net.minecraft.inventory.Inventory": "net.minecraft.world.Container",
 "net.minecraft.inventory.SidedInventory": "net.minecraft.world.WorldlyContainer",
 "net.minecraft.inventory.SimpleInventory": "net.minecraft.world.SimpleContainer",
 # item
 "net.minecraft.item.AxeItem": "net.minecraft.world.item.AxeItem",
 "net.minecraft.item.BlockItem": "net.minecraft.world.item.BlockItem",
 "net.minecraft.item.DyeItem": "net.minecraft.world.item.DyeItem",
 "net.minecraft.item.HoeItem": "net.minecraft.world.item.HoeItem",
 "net.minecraft.item.Item": "net.minecraft.world.item.Item",
 "net.minecraft.item.ItemGroup": "net.minecraft.world.item.CreativeModeTab",
 "net.minecraft.item.ItemPlacementContext": "net.minecraft.world.item.context.BlockPlaceContext",
 "net.minecraft.item.ItemStack": "net.minecraft.world.item.ItemStack",
 "net.minecraft.item.ItemUsageContext": "net.minecraft.world.item.context.UseOnContext",
 "net.minecraft.item.PickaxeItem": "net.minecraft.world.item.PickaxeItem",
 "net.minecraft.item.ShovelItem": "net.minecraft.world.item.ShovelItem",
 "net.minecraft.item.SwordItem": "net.minecraft.world.item.SwordItem",
 "net.minecraft.item.ToolMaterial": "net.minecraft.world.item.ToolMaterial",
 "net.minecraft.item.consume.UseAction": "net.minecraft.world.item.ItemUseAnimation",
 "net.minecraft.item.tooltip.TooltipType": "net.minecraft.world.item.TooltipFlag",
 # nbt (package stable, class renamed)
 "net.minecraft.nbt.NbtCompound": "net.minecraft.nbt.CompoundTag",
 "net.minecraft.nbt.NbtElement": "net.minecraft.nbt.Tag",
 "net.minecraft.nbt.NbtList": "net.minecraft.nbt.ListTag",
 # network
 "net.minecraft.network.RegistryByteBuf": "net.minecraft.network.RegistryFriendlyByteBuf",
 "net.minecraft.network.codec.PacketCodec": "net.minecraft.network.codec.StreamCodec",
 "net.minecraft.network.codec.PacketCodecs": "net.minecraft.network.codec.ByteBufCodecs",
 # recipe
 "net.minecraft.recipe.CraftingRecipe": "net.minecraft.world.item.crafting.CraftingRecipe",
 "net.minecraft.recipe.Ingredient": "net.minecraft.world.item.crafting.Ingredient",
 "net.minecraft.recipe.Recipe": "net.minecraft.world.item.crafting.Recipe",
 "net.minecraft.recipe.RecipeEntry": "net.minecraft.world.item.crafting.RecipeHolder",
 "net.minecraft.recipe.RecipeSerializer": "net.minecraft.world.item.crafting.RecipeSerializer",
 "net.minecraft.recipe.RecipeType": "net.minecraft.world.item.crafting.RecipeType",
 "net.minecraft.recipe.SingleStackRecipe": "net.minecraft.world.item.crafting.SingleItemRecipe",
 "net.minecraft.recipe.SmeltingRecipe": "net.minecraft.world.item.crafting.SmeltingRecipe",
 "net.minecraft.recipe.book.RecipeBookCategories": "net.minecraft.world.item.crafting.RecipeBookCategories",
 "net.minecraft.recipe.book.RecipeBookCategory": "net.minecraft.world.item.crafting.RecipeBookCategory",
 "net.minecraft.recipe.input.CraftingRecipeInput": "net.minecraft.world.item.crafting.CraftingInput",
 "net.minecraft.recipe.input.SingleStackRecipeInput": "net.minecraft.world.item.crafting.SingleRecipeInput",
 # registry
 "net.minecraft.registry.Registries": "net.minecraft.core.registries.BuiltInRegistries",
 "net.minecraft.registry.Registry": "net.minecraft.core.Registry",
 "net.minecraft.registry.RegistryKey": "net.minecraft.resources.ResourceKey",
 "net.minecraft.registry.RegistryKeys": "net.minecraft.core.registries.Registries",
 "net.minecraft.registry.RegistryWrapper": "net.minecraft.core.HolderLookup",
 "net.minecraft.registry.entry.RegistryEntry": "net.minecraft.core.Holder",
 "net.minecraft.registry.tag.BlockTags": "net.minecraft.tags.BlockTags",
 "net.minecraft.registry.tag.TagKey": "net.minecraft.tags.TagKey",
 # resource
 "net.minecraft.resource.Resource": "net.minecraft.server.packs.resources.Resource",
 "net.minecraft.resource.ResourceManager": "net.minecraft.server.packs.resources.ResourceManager",
 "net.minecraft.resource.ResourceType": "net.minecraft.server.packs.PackType",
 "net.minecraft.resource.featuretoggle.FeatureFlags": "net.minecraft.world.flag.FeatureFlags",
 # screen / menu
 "net.minecraft.screen.ArrayPropertyDelegate": "net.minecraft.world.inventory.SimpleContainerData",
 "net.minecraft.screen.GenericContainerScreenHandler": "net.minecraft.world.inventory.ChestMenu",
 "net.minecraft.screen.NamedScreenHandlerFactory": "net.minecraft.world.MenuProvider",
 "net.minecraft.screen.PropertyDelegate": "net.minecraft.world.inventory.ContainerData",
 "net.minecraft.screen.ScreenHandler": "net.minecraft.world.inventory.AbstractContainerMenu",
 "net.minecraft.screen.ScreenHandlerType": "net.minecraft.world.inventory.MenuType",
 "net.minecraft.screen.SimpleNamedScreenHandlerFactory": "net.minecraft.world.SimpleMenuProvider",
 "net.minecraft.screen.slot.Slot": "net.minecraft.world.inventory.Slot",
 "net.minecraft.screen.slot.SlotActionType": "net.minecraft.world.inventory.ClickType",
 # server
 "net.minecraft.server.MinecraftServer": "net.minecraft.server.MinecraftServer",
 "net.minecraft.server.network.ServerPlayerEntity": "net.minecraft.server.level.ServerPlayer",
 "net.minecraft.server.world.ServerWorld": "net.minecraft.server.level.ServerLevel",
 # sound
 "net.minecraft.sound.SoundCategory": "net.minecraft.sounds.SoundSource",
 "net.minecraft.sound.SoundEvent": "net.minecraft.sounds.SoundEvent",
 # state
 "net.minecraft.state.StateManager": "net.minecraft.world.level.block.state.StateDefinition",
 "net.minecraft.state.property.Properties": "net.minecraft.world.level.block.state.properties.BlockStateProperties",
 # text
 "net.minecraft.text.Text": "net.minecraft.network.chat.Component",
 # util
 "net.minecraft.util.ActionResult": "net.minecraft.world.InteractionResult",
 "net.minecraft.util.DyeColor": "net.minecraft.world.item.DyeColor",
 "net.minecraft.util.Formatting": "net.minecraft.ChatFormatting",
 "net.minecraft.util.Hand": "net.minecraft.world.InteractionHand",
 "net.minecraft.util.Identifier": "net.minecraft.resources.Identifier",  # 26.1 keeps the name "Identifier"
 "net.minecraft.util.ItemScatterer": "net.minecraft.world.Containers",
 "net.minecraft.util.JsonHelper": "net.minecraft.util.GsonHelper",
 "net.minecraft.util.collection.DefaultedList": "net.minecraft.core.NonNullList",
 "net.minecraft.util.hit.BlockHitResult": "net.minecraft.world.phys.BlockHitResult",
 "net.minecraft.util.math.BlockPos": "net.minecraft.core.BlockPos",
 "net.minecraft.util.math.Box": "net.minecraft.world.phys.AABB",
 "net.minecraft.util.math.Direction": "net.minecraft.core.Direction",
 "net.minecraft.util.math.GlobalPos": "net.minecraft.core.GlobalPos",
 "net.minecraft.util.math.Vec3d": "net.minecraft.world.phys.Vec3",
 "net.minecraft.util.math.random.Random": "net.minecraft.util.RandomSource",
 # world
 "net.minecraft.world.BlockView": "net.minecraft.world.level.BlockGetter",
 "net.minecraft.world.PersistentState": "net.minecraft.world.level.saveddata.SavedData",
 "net.minecraft.world.World": "net.minecraft.world.level.Level",
 "net.minecraft.world.biome.BiomeKeys": "net.minecraft.world.level.biome.Biomes",
 "net.minecraft.world.gen.GenerationStep": "net.minecraft.world.level.levelgen.GenerationStep",
 "net.minecraft.world.gen.feature.ConfiguredFeature": "net.minecraft.world.level.levelgen.feature.ConfiguredFeature",
 "net.minecraft.world.gen.feature.PlacedFeature": "net.minecraft.world.level.levelgen.placement.PlacedFeature",
}


def simple(fqn):
    return fqn.rsplit(".", 1)[1]


def main():
    files = glob.glob("src/**/*.java", recursive=True)
    # token renames where the simple class name changed
    renames = {simple(y): simple(m) for y, m in MAP.items() if simple(y) != simple(m)}
    # single-pass body replacement (longest source first) so e.g. PlayerInventory is
    # matched before Inventory and no rename cascades into another's output.
    body_re = re.compile(
        r"(?<![\w.])(" + "|".join(re.escape(k) for k in sorted(renames, key=len, reverse=True)) + r")(?![\w])")
    changed = 0
    for f in files:
        txt = open(f, encoding="utf-8").read()
        orig = txt
        for y, m in MAP.items():            # import FQN swaps (unique, no cascade)
            txt = re.sub(r"(import\s+(?:static\s+)?)" + re.escape(y) + r"\b", r"\1" + m, txt)
        txt = body_re.sub(lambda mo: renames[mo.group(1)], txt)   # body simple-name swaps
        if txt != orig:
            open(f, "w", encoding="utf-8").write(txt)
            changed += 1
    print(f"migrated {changed}/{len(files)} files; {len(renames)} simple-name renames")


if __name__ == "__main__":
    main()
