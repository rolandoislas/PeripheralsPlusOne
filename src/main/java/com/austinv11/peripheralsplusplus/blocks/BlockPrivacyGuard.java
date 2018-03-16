package com.austinv11.peripheralsplusplus.blocks;

import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPrivacyGuard;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPrivacyGuard extends BlockPppBase implements ITileEntityProvider {
    public BlockPrivacyGuard() {
        super();
        this.setRegistryName(Reference.MOD_ID, "privacy_guard");
        this.setUnlocalizedName("privacy_guard");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPrivacyGuard();
    }
}
