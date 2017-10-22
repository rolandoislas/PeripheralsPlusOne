package com.austinv11.peripheralsplusplus.blocks;

import com.austinv11.peripheralsplusplus.init.ModBlocks;
import com.austinv11.peripheralsplusplus.recipe.ContainedPeripheral;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPeripheralContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockPeripheralContainer extends BlockPppBase implements ITileEntityProvider {

	public BlockPeripheralContainer() {
		super();
		this.setRegistryName(Reference.MOD_ID, "peripheral_container");
		this.setUnlocalizedName("peripheral_container");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPeripheralContainer();
	}

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
                                ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        String key = ModBlocks.PERIPHERAL_CONTAINER.getRegistryName().toString();
        if (tag == null || !tag.hasKey(key))
            return;
        NBTBase peripheralsBase = tag.getTag(key);
        if (!(peripheralsBase instanceof NBTTagList))
            return;
        NBTTagList peripherals = (NBTTagList) peripheralsBase;
        for (int peripheralIndex = 0; peripheralIndex < peripherals.tagCount(); peripheralIndex++) {
            NBTBase peripheralBase = peripherals.get(peripheralIndex);
            if (!(peripheralBase instanceof NBTTagCompound))
                continue;
            ContainedPeripheral peripheral = new ContainedPeripheral((NBTTagCompound) peripheralBase);
            TileEntity container = world.getTileEntity(pos);
            if (container != null && container instanceof TileEntityPeripheralContainer)
                ((TileEntityPeripheralContainer)container).addPeripheral(peripheral);
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        return new ArrayList<>();
    }
}
