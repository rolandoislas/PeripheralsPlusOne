package com.austinv11.peripheralsplusplus.items;

import com.austinv11.peripheralsplusplus.capabilities.rfid.CapabilityRfid;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagHolder;
import com.austinv11.peripheralsplusplus.reference.Reference;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFork extends ItemPPP {
    public ItemFork() {
        super();
        this.setRegistryName(Reference.MOD_ID, "fork");
        this.setUnlocalizedName("fork");
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        String description = I18n.format("peripheralsplusone.description.fork");
        tooltip.add(description);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (attacker.world.isRemote)
            return true;
        chipEntity(target, attacker);
        return true;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target,
                                            EnumHand hand) {
        if (player.world.isRemote)
            return true;
        chipEntity(target, player);
        return true;
    }

    private void chipEntity(EntityLivingBase target, EntityLivingBase attacker) {
        RfidTagHolder tagHolder = target.getCapability(CapabilityRfid.INSTANCE, null);
        if (tagHolder == null)
            return;
        // Prod and damage the target
        tagHolder.setProdded(true);
        target.attackEntityFrom(new DamageSource(Reference.MOD_ID + ".fork"), 1);
        // Drop an existing tag
        if (!tagHolder.getTag().isEmpty()) {
            EntityItem tagItem = new EntityItem(target.world, target.posX, target.posY, target.posZ,
                    tagHolder.getTag());
            tagHolder.setTag(ItemStack.EMPTY);
            target.world.spawnEntity(tagItem);
        }
    }
}
