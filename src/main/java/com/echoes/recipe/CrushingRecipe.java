package com.echoes.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

/**
 * One ore → dust crushing recipe. Extends {@link SingleItemRecipe}, which
 * provides matches/craft/ingredient placement for single-input recipes; we add
 * {@code energy} (RU consumed) and {@code processingTime} (ticks).
 *
 * <p>Data-driven (data/echoes/recipe/compressor/*.json) with vanilla Ingredient
 * syntax, so recipes accept item tags from other mods.
 */
public class CrushingRecipe extends SingleItemRecipe {
    private final int energy;
    private final int processingTime;
    private final ItemStack secondary;       // optional byproduct (EMPTY if none)
    private final float secondaryChance;      // 0..1 roll per craft

    public CrushingRecipe(Ingredient input, ItemStack result, int energy, int processingTime,
                          ItemStack secondary, float secondaryChance) {
        super("", input, result);
        this.energy = energy;
        this.processingTime = processingTime;
        this.secondary = secondary;
        this.secondaryChance = secondaryChance;
    }

    public int energy() { return energy; }
    public int processingTime() { return processingTime; }
    public ItemStack secondary() { return secondary; }
    public float secondaryChance() { return secondaryChance; }

    /** Widen the base's protected result() so the block entity & serializer can read it. */
    @Override public ItemStack result() { return super.result(); }

    @Override public RecipeSerializer<? extends SingleItemRecipe> getSerializer() { return ModRecipes.CRUSHING_SERIALIZER; }
    @Override public RecipeType<? extends SingleItemRecipe> getType() { return ModRecipes.CRUSHING_TYPE; }
    @Override public RecipeBookCategory getRecipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {
        public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::ingredient),
                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(CrushingRecipe::result),
                Codec.INT.optionalFieldOf("energy", 200).forGetter(CrushingRecipe::energy),
                Codec.INT.optionalFieldOf("processingTime", 120).forGetter(CrushingRecipe::processingTime),
                ItemStack.VALIDATED_CODEC.optionalFieldOf("secondary", ItemStack.EMPTY).forGetter(CrushingRecipe::secondary),
                Codec.FLOAT.optionalFieldOf("secondaryChance", 0.0f).forGetter(CrushingRecipe::secondaryChance)
        ).apply(builder, CrushingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> PACKET_CODEC =
                StreamCodec.tuple(
                        Ingredient.PACKET_CODEC, CrushingRecipe::ingredient,
                        ItemStack.PACKET_CODEC, CrushingRecipe::result,
                        ByteBufCodecs.VAR_INT, CrushingRecipe::energy,
                        ByteBufCodecs.VAR_INT, CrushingRecipe::processingTime,
                        ItemStack.OPTIONAL_PACKET_CODEC, CrushingRecipe::secondary,
                        ByteBufCodecs.FLOAT, CrushingRecipe::secondaryChance,
                        CrushingRecipe::new);

        @Override public MapCodec<CrushingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> packetCodec() { return PACKET_CODEC; }
    }
}
