package com.austinv11.peripheralsplusplus.items;

import com.austinv11.collectiveframework.minecraft.utils.NBTHelper;
import com.austinv11.peripheralsplusplus.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class ItemBlockTurtle extends ItemBlock {
	
	private Random rng = new Random();
	
	public ItemBlockTurtle(Block block) {
		super(block);
		setRegistryName(ModBlocks.TURTLE.getRegistryName());
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (NBTHelper.hasTag(item, "desc")) {
            String description = I18n.translateToLocal("peripheralsplusone.description.turtle." +
                    NBTHelper.getInt(item, "desc"));
            tooltip.add(description);
        } else {
            NBTHelper.setInteger(item, "desc", MathHelper.getRandomIntegerInRange(rng, 1, 10));
            addInformation(item, player, tooltip, advanced);
        }
    }
}
