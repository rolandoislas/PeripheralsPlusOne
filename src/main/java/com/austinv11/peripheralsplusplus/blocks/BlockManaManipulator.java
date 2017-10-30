package com.austinv11.peripheralsplusplus.blocks;

import com.austinv11.peripheralsplusplus.PeripheralsPlusPlus;
import com.austinv11.peripheralsplusplus.creativetab.CreativeTabPPP;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityManaManipulator;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import vazkii.botania.api.wand.IWandable;

import javax.annotation.Nullable;

@Optional.InterfaceList(value = {
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.wand.IWandable")
})
public class BlockManaManipulator extends BlockContainerPPP implements ITileEntityProvider, IWandable {

    public BlockManaManipulator() {
        super(Material.WOOD);
        this.setRegistryName(Reference.MOD_ID, "mana_manipulator");
        this.setUnlocalizedName("mana_manipulator");
        this.setCreativeTab(CreativeTabPPP.PPP_TAB);
        this.setHardness(2);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityManaManipulator();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (!world.isRemote) {
            if (te != null)
                player.openGui(PeripheralsPlusPlus.instance, Reference.GUIs.MANA_MANIPULATOR.ordinal(), world,
                        pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        return false;
    }
}
