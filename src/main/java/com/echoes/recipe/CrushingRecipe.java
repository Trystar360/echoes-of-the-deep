package com.echoes.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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

import java.util.Optional;

/**
 * One ore → dust crushing recipe. Single input, single primary output, plus an
 * optional rolled byproduct; carries {@code energy} (RU consumed) and
 * {@code processingTime} (ticks).
 *
 * <p>26.1: the result and byproduct are stored as {@link ItemStackTemplate}s (deferred
 * item blueprints) like vanilla single-item recipes — decoding them with {@link
 * ItemStack#CODEC} eagerly resolves item components and fails for items whose components
 * aren't ready yet during a parallel data reload. {@link #result()} / {@link #secondary()}
 * materialise a fresh {@link ItemStack} on demand. Data-driven
 * (data/echoes/recipe/compressor/*.json) with vanilla Ingredient syntax.
 */
public class CrushingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStackTemplate result;
    private final int energy;
    private final int processingTime;
    private final Optional<ItemStackTemplate> secondary;  // absent when there is no byproduct
    private final float secondaryChance;                   // 0..1 roll per craft
    @Nullable private PlacementInfo placementInfo;

    public CrushingRecipe(Ingredient input, ItemStackTemplate result, int energy, int processingTime,
                          Optional<ItemStackTemplate> secondary, float secondaryChance) {
        this.input = input;
        this.result = result;
        this.energy = energy;
        this.processingTime = processingTime;
        this.secondary = secondary;
        this.secondaryChance = secondaryChance;
    }

    public Ingredient ingredient() { return input; }
    /** A fresh copy of the primary output. */
    public ItemStack result() { return result.create(); }
    /** A fresh copy of the byproduct, or an empty stack when there is none. */
    public ItemStack secondary() { return secondary.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY); }
    public int energy() { return energy; }
    public int processingTime() { return processingTime; }
    public float secondaryChance() { return secondaryChance; }

    // Codec getters expose the underlying templates.
    private ItemStackTemplate resultTemplate() { return result; }
    private Optional<ItemStackTemplate> secondaryTemplate() { return secondary; }

    @Override public boolean matches(SingleRecipeInput in, Level level) { return input.test(in.item()); }
    @Override public ItemStack assemble(SingleRecipeInput in) { return result.create(); }
    @Override public String group() { return ""; }
    @Override public boolean showNotification() { return false; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override public PlacementInfo placementInfo() {
        if (placementInfo == null) placementInfo = PlacementInfo.create(input);
        return placementInfo;
    }

    @Override public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() { return ModRecipes.CRUSHING_SERIALIZER; }
    @Override public RecipeType<? extends Recipe<SingleRecipeInput>> getType() { return ModRecipes.CRUSHING_TYPE; }

    public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrushingRecipe::resultTemplate),
            Codec.INT.optionalFieldOf("energy", 200).forGetter(CrushingRecipe::energy),
            Codec.INT.optionalFieldOf("processingTime", 120).forGetter(CrushingRecipe::processingTime),
            ItemStackTemplate.CODEC.optionalFieldOf("secondary").forGetter(CrushingRecipe::secondaryTemplate),
            Codec.FLOAT.optionalFieldOf("secondaryChance", 0.0f).forGetter(CrushingRecipe::secondaryChance)
    ).apply(b, CrushingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrushingRecipe::ingredient,
            ItemStackTemplate.STREAM_CODEC, CrushingRecipe::resultTemplate,
            ByteBufCodecs.VAR_INT, CrushingRecipe::energy,
            ByteBufCodecs.VAR_INT, CrushingRecipe::processingTime,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), CrushingRecipe::secondaryTemplate,
            ByteBufCodecs.FLOAT, CrushingRecipe::secondaryChance,
            CrushingRecipe::new);
}
