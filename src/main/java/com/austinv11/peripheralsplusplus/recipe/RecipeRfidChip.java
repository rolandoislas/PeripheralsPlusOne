package com.austinv11.peripheralsplusplus.recipe;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidTag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RecipeRfidChip implements IRecipe {
    private ResourceLocation group;
    private ResourceLocation name;

    public RecipeRfidChip(ResourceLocation group) {
        super();
        this.group = group;
    }

    public RecipeRfidChip() {
        super();
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        ItemStack blankItem = getBlankItem(inv);
        if (blankItem.isEmpty())
            return false;
        ItemStack rfidChip = getRfidChip(inv);
        return !rfidChip.isEmpty();
    }

    @Nonnull
    private ItemStack getRfidChip(InventoryCrafting inv) {
        ItemStack rfidChip = ItemStack.EMPTY;
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++) {
            ItemStack itemStack = inv.getStackInSlot(itemIndex).copy();
            if (itemStack.isEmpty())
                continue;
            if (itemStack.isItemEqual(new ItemStack(ModItems.RFID_CHIP))) {
                if (!rfidChip.isEmpty())
                    return ItemStack.EMPTY;
                rfidChip = itemStack;
            }
        }
        return rfidChip;
    }

    @Nonnull
    private ItemStack getBlankItem(InventoryCrafting inv) {
        ItemStack blankItem = ItemStack.EMPTY;
        for (int itemIndex = 0; itemIndex < inv.getSizeInventory(); itemIndex++) {
            ItemStack itemStack = inv.getStackInSlot(itemIndex).copy();
            if (itemStack.isEmpty() || itemStack.isItemEqual(new ItemStack(ModItems.RFID_CHIP)))
                continue;
            if (!RfidTag.hasTag(itemStack)) {
                if (!blankItem.isEmpty())
                    return ItemStack.EMPTY;
                blankItem = itemStack;
            }
        }
        return blankItem;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack rfidChip = getRfidChip(inv);
        ItemStack blankItem = getBlankItem(inv);
        RfidTag rfidTag = new RfidTag(rfidChip);
        RfidTag.addTag(blankItem, rfidTag);
        if (blankItem.isItemEqual(new ItemStack(ModItems.PLASTIC_CARD)))
            blankItem.setTranslatableName(blankItem.getUnlocalizedName() +
                    (Math.random() < .5 ? ".name_rfid" : ".name_nfc"));
        return blankItem;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.RFID_CHIP);
    }

    @Override
    public IRecipe setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }

    @Override
    public String getGroup() {
        return group.toString();
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getPlayer().isCreative())
            return;
        ItemStack item = event.getPlayer().getHeldItem(event.getHand());
        if (RfidTag.hasTag(item)) {
            ItemStack chip = RfidTag.createChip(new RfidTag(item));
            EntityItem chipEntity = new EntityItem(
                    event.getWorld(),
                    event.getBlockSnapshot().getPos().getX(),
                    event.getBlockSnapshot().getPos().getY(),
                    event.getBlockSnapshot().getPos().getZ(),
                    chip
            );
            event.getWorld().spawnEntity(chipEntity);
        }
    }
}
