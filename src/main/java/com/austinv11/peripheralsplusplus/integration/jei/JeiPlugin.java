package com.austinv11.peripheralsplusplus.integration.jei;

import com.austinv11.peripheralsplusplus.init.ModPeripherals;
import com.austinv11.peripheralsplusplus.integration.jei.recipe.RecipeMagCard;
import com.austinv11.peripheralsplusplus.integration.jei.recipe.RecipePocketUpgrade;
import com.austinv11.peripheralsplusplus.integration.jei.recipe.RecipeRfidCard;
import com.austinv11.peripheralsplusplus.integration.jei.recipe.RecipeTurtleUpgrade;
import com.austinv11.peripheralsplusplus.items.ItemPlasticCard;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMagReaderWriter;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;

import java.util.ArrayList;

@JEIPlugin
public class JeiPlugin implements IModPlugin {
    public JeiPlugin() {
        super();
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    @Override
    public void register(IModRegistry registry) {
        ArrayList<IRecipeWrapper> recipes = new ArrayList<>();

        // Turtle upgrades
        for (ITurtleUpgrade upgrade : ModPeripherals.TURTLE_UPGRADES) {
            for (int side = 0; side < 2; side++) {
                recipes.add(new RecipeTurtleUpgrade(upgrade, true, side == 0));
                recipes.add(new RecipeTurtleUpgrade(upgrade, false, side == 0));
            }
        }
        // Pocket Upgrades
        for (IPocketUpgrade pocketUpgrade : ModPeripherals.POCKET_UPGRADES) {
            recipes.add(new RecipePocketUpgrade(pocketUpgrade, true));
            recipes.add(new RecipePocketUpgrade(pocketUpgrade, false));
        }
        // Plastic Card Variants
        recipes.add(new RecipeRfidCard(ItemPlasticCard.NAME_RFID));
        recipes.add(new RecipeRfidCard(ItemPlasticCard.NAME_NFC));
        recipes.add(new RecipeMagCard());

        registry.addRecipes(recipes);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
