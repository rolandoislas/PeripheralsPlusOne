package com.austinv11.peripheralsplusplus.turtles;

import com.austinv11.collectiveframework.minecraft.utils.ModelManager;
import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityRfidReaderWriter;
import com.austinv11.peripheralsplusplus.utils.ModelUtil;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public class TurtleRfid implements ITurtleUpgrade, ModelManager.ModelRegistrar {
    @Nonnull
    @Override
    public ResourceLocation getUpgradeID() {
        return new ResourceLocation(Reference.RFID_UPGRADE);
    }

    @Override
    public int getLegacyUpgradeID() {
        return -1;
    }

    @Nonnull
    @Override
    public String getUnlocalisedAdjective() {
        return Reference.MOD_ID + ".turtle_upgrade.rfid";
    }

    @Nonnull
    @Override
    public TurtleUpgradeType getType() {
        return TurtleUpgradeType.Peripheral;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingItem() {
        return new ItemStack(ModItems.RFID_READER_WRITER);
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side) {
        return new TileEntityRfidReaderWriter();
    }

    @Nonnull
    @Override
    public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side,
                                       @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction) {
        return TurtleCommandResult.failure();
    }

    @Override
    public void update(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side) {
        IPeripheral peripheral = turtle.getPeripheral(side);
        if (peripheral instanceof TileEntityRfidReaderWriter) {
            ((TileEntityRfidReaderWriter) peripheral).setPos(turtle.getPosition());
            ((TileEntityRfidReaderWriter) peripheral).setWorldObj(turtle.getWorld());
        }
    }

    @Override
    public void registerModels(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry) {
        ModelUtil.registerTurtleUpgradeModels(modelRegistry, "turtle_rfid");
    }

    @Nonnull
    @Override
    public Pair<IBakedModel, Matrix4f> getModel(@Nullable ITurtleAccess turtle, @Nonnull TurtleSide side) {
        return ModelUtil.getTurtleUpgradeModel("turtle_rfid", side);
    }
}
