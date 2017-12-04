package com.austinv11.peripheralsplusplus.recipe;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidTag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class RecipeRfidChip implements IRecipe {

    public RecipeRfidChip() {
        super();
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return matchesAdd(inv) || matchesRemove(inv);

    }

    /**
     * Checks if the crafting contents matches a recipe to remove a RFID chip from an item
     * @param inv crafting inventory
     * @return recipe matches
     */
    private boolean matchesRemove(InventoryCrafting inv) {
        ItemStack rfidItem = getRfidItem(inv);
        if (rfidItem == null)
            return false;
        ItemStack fork = getFork(inv);
        return fork != null && getInventorySize(inv) == 2;
    }

    /**
     * Gets the amount of items in the inventory.
     * @param inv inventory to check
     * @return amount of items in inventory
     */
    private int getInventorySize(InventoryCrafting inv) {
        int size = 0;
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++)
            if (inv.getStackInSlot(itemIndex) != null)
                size++;
        return size;
    }

    /**
     * Checks if the crafting contents matches a recipe to add a RFID chip to an item
     * @param inv crafting inventory
     * @return recipe matches
     */
    private boolean matchesAdd(InventoryCrafting inv) {
        ItemStack blankItem = getBlankItem(inv);
        if (blankItem == null)
            return false;
        ItemStack rfidChip = getRfidChip(inv);
        return rfidChip != null && getInventorySize(inv) == 2;
    }

    /**
     * @see RecipeRfidChip#getItemFromInventory(InventoryCrafting, ItemStack)
     */
    @Nullable
    private ItemStack getRfidChip(InventoryCrafting inv) {
        return getItemFromInventory(inv, new ItemStack(ModItems.RFID_CHIP));
    }

    /**
     * @see RecipeRfidChip#getItemFromInventory(InventoryCrafting, ItemStack)
     */
    @Nullable
    private ItemStack getFork(InventoryCrafting inv) {
        return getItemFromInventory(inv, new ItemStack(ModItems.FORK));
    }

    /**
     * Searches an inventory for an item. An empty item is returned if the item could not be found or there were
     * duplicates.
     * @param inv inventory to search
     * @param match item to match
     * @return matching item or empty item
     */
    @Nullable
    private ItemStack getItemFromInventory(InventoryCrafting inv, ItemStack match) {
        ItemStack found = null;
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++) {
            ItemStack itemStack = inv.getStackInSlot(itemIndex);
            if (itemStack != null)
                itemStack = itemStack.copy();
            if (itemStack == null)
                continue;
            if (itemStack.isItemEqual(match)) {
                if (found != null)
                    return null;
                found = itemStack;
            }
        }
        return found;
    }

    /**
     * Searches an inventory for an item that does not have an RFID tag. Forks and RFID chips are ignored.
     * An empty item is returned if there is more than one matching item.
     * @see RecipeRfidChip#getRfidItem(InventoryCrafting, boolean)
     * @param inv inventory to search
     * @return matching item or empty item
     */
    @Nullable
    private ItemStack getBlankItem(InventoryCrafting inv) {
        return getRfidItem(inv, true);
    }

    /**
     * Searches an inventory for an item that has an RFID tag. Forks and RFID chips are ignored.
     * An empty item is returned if there is more than one matching item.
     * @param inv inventory to search
     * @param blankItem search for an item without an RFID tag instead
     * @return matching item or empty item
     */
    @Nullable
    private ItemStack getRfidItem(InventoryCrafting inv, boolean blankItem) {
        ItemStack rfidItem = null;
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++) {
            ItemStack itemStack = inv.getStackInSlot(itemIndex);
            if (itemStack != null)
                itemStack = itemStack.copy();
            if (itemStack == null || itemStack.isItemEqual(new ItemStack(ModItems.RFID_CHIP)) ||
                    itemStack.isItemEqual(new ItemStack(ModItems.FORK)))
                continue;
            if ((!blankItem && RfidTag.hasTag(itemStack)) || (blankItem && !RfidTag.hasTag(itemStack))) {
                if (rfidItem != null)
                    return null;
                rfidItem = itemStack;
            }
        }
        return rfidItem;
    }

    /**
     * @see RecipeRfidChip#getRfidItem(InventoryCrafting, boolean)
     */
    @Nullable
    private ItemStack getRfidItem(InventoryCrafting inv) {
        return getRfidItem(inv, false);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // Add
        if (matchesAdd(inv)) {
            ItemStack rfidChip = getRfidChip(inv);
            ItemStack blankItem = getBlankItem(inv);
            RfidTag rfidTag = new RfidTag(rfidChip);
            RfidTag.addTag(blankItem, rfidTag);
            if (blankItem != null && blankItem.isItemEqual(new ItemStack(ModItems.PLASTIC_CARD)))
                blankItem.setStackDisplayName(blankItem.getUnlocalizedName() +
                        (Math.random() < .5 ? ".name_rfid" : ".name_nfc"));
            return blankItem;
        }
        // Remove
        else {
            ItemStack rfidItem = getRfidItem(inv);
            if (rfidItem != null)
                rfidItem = rfidItem.copy();
            return RfidTag.createChip(new RfidTag(rfidItem));
        }
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        // Add
        if (matchesAdd(inv))
            return ForgeHooks.defaultRecipeGetRemainingItems(inv);
        // Remove
        ItemStack[] list = new ItemStack[inv.getSizeInventory()];
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++) {
            ItemStack item = inv.getStackInSlot(itemIndex);
            if (item != null)
                item = item.copy();
            if (item != null && RfidTag.hasTag(item))
                RfidTag.removeTag(item);
            list[itemIndex] = item;
        }
        return list;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.RFID_CHIP);
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        // Drop the RFID chip when a tagged item block is placed
        if (event.getPlayer().isCreative())
            return;
        ItemStack item = event.getItemInHand();
        if (item != null && RfidTag.hasTag(item)) {
            ItemStack chip = RfidTag.createChip(new RfidTag(item));
            EntityItem chipEntity = new EntityItem(
                    event.getWorld(),
                    event.getBlockSnapshot().getPos().getX(),
                    event.getBlockSnapshot().getPos().getY(),
                    event.getBlockSnapshot().getPos().getZ(),
                    chip
            );
            event.getWorld().spawnEntityInWorld(chipEntity);
        }
    }
}
