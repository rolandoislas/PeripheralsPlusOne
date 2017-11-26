package com.austinv11.peripheralsplusplus.integration.jei.recipe;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMagReaderWriter;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeMagCard implements IRecipeWrapper {
    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(new ItemStack(ModItems.PLASTIC_CARD));
        inputs.add(new ItemStack(Items.IRON_INGOT));
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, TileEntityMagReaderWriter.createMagCard());
    }
}
