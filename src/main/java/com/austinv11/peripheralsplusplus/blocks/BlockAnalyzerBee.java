package com.austinv11.peripheralsplusplus.blocks;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.peripheralsplusplus.init.ModBlocks;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAnalyzerBee;
import com.austinv11.peripheralsplusplus.tiles.TileEntityModNotLoaded;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class BlockAnalyzerBee extends BlockAnalyzer {
	
	public BlockAnalyzerBee() {
		super();
		this.setRegistryName(Reference.MOD_ID, "analyzer_bee");
		this.setUnlocalizedName("analyzer_bee");
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return Loader.isModLoaded(ModIds.FORESTRY) ? new TileEntityAnalyzerBee() :
				new TileEntityModNotLoaded(ModIds.FORESTRY);
	}

	public Block getBlock(){
		return ModBlocks.ANALYZER_BEE;
	}
}
