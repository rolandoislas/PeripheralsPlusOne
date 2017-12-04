package com.austinv11.peripheralsplusplus.creativetab;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.init.ModPeripherals;
import com.austinv11.peripheralsplusplus.items.ItemPlasticCard;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMagReaderWriter;
import com.austinv11.peripheralsplusplus.utils.TurtleUtil;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidTag;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class CreativeTabPPP {

	public static final CreativeTabs PPP_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {

		@SideOnly(Side.CLIENT)
		@Override
		public Item getTabIconItem() {
			return ModItems.CHAT_BOX;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void displayAllRelevantItems(List<ItemStack> list) {
			super.displayAllRelevantItems(list);
			// Turtle Upgrades
			for (ITurtleUpgrade upgrade : ModPeripherals.TURTLE_UPGRADES) {
				list.add(TurtleUtil.getTurtle(false, upgrade));
				list.add(TurtleUtil.getTurtle(true, upgrade));
			}
			// Pocket Upgrades
			for (IPocketUpgrade pocketUpgrade : ModPeripherals.POCKET_UPGRADES) {
				list.add(TurtleUtil.getPocket(false, pocketUpgrade));
				list.add(TurtleUtil.getPocket(true, pocketUpgrade));
			}
			// Plastic card upgrades
			list.add(RfidTag.createDummyCard(ItemPlasticCard.NAME_RFID));
			list.add(RfidTag.createDummyCard(ItemPlasticCard.NAME_NFC));
			list.add(TileEntityMagReaderWriter.createMagCard());
		}
	};
}
