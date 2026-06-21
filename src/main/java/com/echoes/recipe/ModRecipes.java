package com.echoes.recipe;

import com.echoes.EchoesMod;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class ModRecipes {
    private ModRecipes() {}

    public static final RecipeType<CrushingRecipe> CRUSHING_TYPE = new RecipeType<>() {
        @Override public String toString() { return "echoes:crushing"; }
    };

    public static final RecipeSerializer<CrushingRecipe> CRUSHING_SERIALIZER =
            new RecipeSerializer<>(CrushingRecipe.CODEC, CrushingRecipe.STREAM_CODEC);

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, id("crushing"), CRUSHING_TYPE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("crushing"), CRUSHING_SERIALIZER);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, path);
    }
}
