package com.austinv11.peripheralsplusplus.nei.recipe;

import com.austinv11.peripheralsplusplus.utils.TurtleUtil;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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
            inputs.add(null);
        ItemStack turtle = TurtleUtil.getTurtle(advanced);
        inputs.add(left ? upgrade.getCraftingItem() : turtle);
        inputs.add(left ? turtle : upgrade.getCraftingItem());
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class,
                TurtleUtil.getTurtle(advanced, left ? upgrade : null, left ? null : upgrade));
    }

    @Override
    public List getInputs() {
        return null;
    }

    @Override
    public List getOutputs() {
        return null;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return null;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

    }

    @Override
    public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {

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
