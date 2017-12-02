package com.austinv11.peripheralsplusplus.items;

import com.austinv11.peripheralsplusplus.capabilities.rfid.CapabilityRfid;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagHolder;
import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.reference.Reference;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ItemRfidChip extends ItemPPP {
    public ItemRfidChip() {
        super();
        this.setRegistryName(Reference.MOD_ID, "rfid_chip");
        this.setUnlocalizedName("rfid_chip");
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack item, EntityPlayer player, EntityLivingBase target,
                                            EnumHand hand) {
        if (player.world.isRemote)
            return true;
        // Get capability
        RfidTagHolder tagHolder = target.getCapability(CapabilityRfid.INSTANCE, null);
        if (tagHolder == null)
            return false;
        // Check item is valid
        if (!item.isItemEqual(new ItemStack(ModItems.RFID_CHIP)) || item.getCount() < 1)
            return false;
        // Check the entity has been prodded
        if (!tagHolder.hasBeenProdded())
            return false;
        // Add tag
        if (tagHolder.getTag().isEmpty()) {
            ItemStack tag = item.copy();
            tag.setCount(1);
            tagHolder.setTag(tag);
            tagHolder.setProdded(false);
            item.shrink(1);
        }
        return true;
    }
}
