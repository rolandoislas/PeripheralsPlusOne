package com.austinv11.peripheralsplusplus.pocket;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityRfidReaderWriter;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketRfid implements IPocketUpgrade {
    @Nonnull
    @Override
    public ResourceLocation getUpgradeID() {
        return new ResourceLocation(Reference.POCKET_RFID);
    }

    @Nonnull
    @Override
    public String getUnlocalisedAdjective() {
        return "peripheralsplusone.pocket_upgrade.rfid";
    }

    @Nonnull
    @Override
    public ItemStack getCraftingItem() {
        return new ItemStack(ModItems.RFID_READER_WRITER);
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral(@Nonnull IPocketAccess access) {
        return new TileEntityRfidReaderWriter();
    }

    @Override
    public void update(@Nonnull IPocketAccess access, @Nullable IPeripheral peripheral) {
        if (peripheral instanceof TileEntityRfidReaderWriter && access.getEntity() != null) {
            ((TileEntityRfidReaderWriter) peripheral).setPos(access.getEntity().getPosition());
            ((TileEntityRfidReaderWriter) peripheral).setWorldObj(access.getEntity().getEntityWorld());
        }
    }

    @Override
    public boolean onRightClick(@Nonnull World world, @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral) {
        return false;
    }
}
