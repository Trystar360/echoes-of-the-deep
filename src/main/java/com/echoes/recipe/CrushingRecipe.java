package com.echoes.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import org.jetbrains.annotations.Nullable;

/**
 * One ore → dust crushing recipe. Single input, single primary output, plus an
 * optional rolled byproduct; carries {@code energy} (RU consumed) and
 * {@code processingTime} (ticks).
 *
 * <p>26.1: implements {@link Recipe} directly (the old {@code SingleItemRecipe}
 * base now uses {@code ItemStackTemplate}/{@code CommonInfo}). Data-driven
 * (data/echoes/recipe/compressor/*.json) with vanilla Ingredient syntax, so recipes
 * accept item tags from other mods.
 */
public class CrushingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int energy;
    private final int processingTime;
    private final ItemStack secondary;        // optional byproduct (EMPTY if none)
    private final float secondaryChance;       // 0..1 roll per craft
    @Nullable private PlacementInfo placementInfo;

    public CrushingRecipe(Ingredient input, ItemStack result, int energy, int processingTime,
                          ItemStack secondary, float secondaryChance) {
        this.input = input;
        this.result = result;
        this.energy = energy;
        this.processingTime = processingTime;
        this.secondary = secondary;
        this.secondaryChance = secondaryChance;
    }

    public Ingredient ingredient() { return input; }
    public ItemStack result() { return result; }
    public int energy() { return energy; }
    public int processingTime() { return processingTime; }
    public ItemStack secondary() { return secondary; }
    public float secondaryChance() { return secondaryChance; }

    @Override public boolean matches(SingleRecipeInput in, Level level) { return input.test(in.item()); }
    @Override public ItemStack assemble(SingleRecipeInput in) { return result.copy(); }
    @Override public String group() { return ""; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override public PlacementInfo placementInfo() {
        if (placementInfo == null) placementInfo = PlacementInfo.create(input);
        return placementInfo;
    }

    @Override public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() { return ModRecipes.CRUSHING_SERIALIZER; }
    @Override public RecipeType<? extends Recipe<SingleRecipeInput>> getType() { return ModRecipes.CRUSHING_TYPE; }

    public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(CrushingRecipe::result),
            Codec.INT.optionalFieldOf("energy", 200).forGetter(CrushingRecipe::energy),
            Codec.INT.optionalFieldOf("processingTime", 120).forGetter(CrushingRecipe::processingTime),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary", ItemStack.EMPTY).forGetter(CrushingRecipe::secondary),
            Codec.FLOAT.optionalFieldOf("secondaryChance", 0.0f).forGetter(CrushingRecipe::secondaryChance)
    ).apply(b, CrushingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrushingRecipe::ingredient,
            ItemStack.STREAM_CODEC, CrushingRecipe::result,
            ByteBufCodecs.VAR_INT, CrushingRecipe::energy,
            ByteBufCodecs.VAR_INT, CrushingRecipe::processingTime,
            ItemStack.OPTIONAL_STREAM_CODEC, CrushingRecipe::secondary,
            ByteBufCodecs.FLOAT, CrushingRecipe::secondaryChance,
            CrushingRecipe::new);
}
