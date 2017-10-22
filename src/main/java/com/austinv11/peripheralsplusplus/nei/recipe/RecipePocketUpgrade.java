package com.austinv11.peripheralsplusplus.nei.recipe;

import com.austinv11.peripheralsplusplus.utils.TurtleUtil;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class RecipePocketUpgrade implements IRecipeWrapper {
    private final IPocketUpgrade pocketUpgrade;
    private final boolean advanced;

    public RecipePocketUpgrade(IPocketUpgrade pocketUpgrade, boolean advanced) {
        this.pocketUpgrade = pocketUpgrade;
        this.advanced = advanced;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(null);
        inputs.add(pocketUpgrade.getCraftingItem());
        for (int emptySlot = 0; emptySlot < 2; emptySlot++)
            inputs.add(null);
        inputs.add(TurtleUtil.getPocket(advanced));
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, TurtleUtil.getPocket(advanced, pocketUpgrade));
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
