package com.echoes.recipe;

import com.echoes.EchoesMod;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModRecipes {
    private ModRecipes() {}

    public static final RecipeType<CrushingRecipe> CRUSHING_TYPE = new RecipeType<>() {
        @Override public String toString() { return "echoes:crushing"; }
    };

    public static final CrushingRecipe.Serializer CRUSHING_SERIALIZER = new CrushingRecipe.Serializer();

    public static void register() {
        Registry.register(Registries.RECIPE_TYPE, id("crushing"), CRUSHING_TYPE);
        Registry.register(Registries.RECIPE_SERIALIZER, id("crushing"), CRUSHING_SERIALIZER);
    }

    private static Identifier id(String path) {
        return Identifier.of(EchoesMod.MOD_ID, path);
    }
}
