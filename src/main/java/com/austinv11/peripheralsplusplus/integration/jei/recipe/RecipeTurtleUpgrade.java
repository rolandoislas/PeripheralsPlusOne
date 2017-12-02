package com.austinv11.peripheralsplusplus.integration.jei.recipe;

import com.austinv11.peripheralsplusplus.utils.TurtleUtil;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeTurtleUpgrade implements IRecipeWrapper {
    private final boolean advanced;
    private final ITurtleUpgrade upgrade;
    private final boolean left;

    public RecipeTurtleUpgrade(ITurtleUpgrade upgrade, boolean advanced, boolean left) {
        this.upgrade = upgrade;
        this.advanced = advanced;
        this.left = left;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int emptySlot = 0; emptySlot < 3; emptySlot++)
            inputs.add(ItemStack.EMPTY);
        ItemStack turtle = TurtleUtil.getTurtle(advanced);
        inputs.add(left ? upgrade.getCraftingItem() : turtle);
        inputs.add(left ? turtle : upgrade.getCraftingItem());
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class,
                TurtleUtil.getTurtle(advanced, left ? upgrade : null, left ? null : upgrade));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
