package com.austinv11.peripheralsplusplus.integration.jei.recipe;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidTag;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeRfidCard implements IRecipeWrapper {
    private final String name;

    public RecipeRfidCard(String name) {
        this.name = name;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(new ItemStack(ModItems.PLASTIC_CARD));
        inputs.add(new ItemStack(ModItems.RFID_CHIP));
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, RfidTag.createDummyCard(name));
    }
}
