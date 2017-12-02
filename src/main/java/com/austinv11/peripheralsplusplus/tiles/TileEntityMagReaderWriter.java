package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.items.ItemPlasticCard;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersUtil;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityMagReaderWriter extends TileEntity implements IPlusPlusPeripheral, OpenComputersPeripheral {
    private static final int MAX_TRACKS = 3;
    private static final String MAG_TAG = String.format("%s:%s", Reference.MOD_ID, "mag_card");
    private String[] buffers;
    private List<IComputerAccess> computers;
    private Node node;

    public TileEntityMagReaderWriter() {
        buffers = new String[MAX_TRACKS];
        computers = new ArrayList<>();
        node = OpenComputersUtil.createNode(this, getType());
    }

    @Nonnull
    @Override
    public String getType() {
        return "mag_reader_writer";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[] {
                "write", // (int track_index, string data)void - sets the buffer of data to be written
                "clear" // ([int track_index])void - clears a specified or all write buffers
        };
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method,
                               @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0:
                return writeLua(arguments);
            case 1:
                return clearLua(arguments);
        }
        throw new LuaException("Unexpected error");
    }

    /**
     * Clears the write buffers
     * @param arguments optional track index to specify for clearing
     * @return nil
     */
    private Object[] clearLua(Object[] arguments) throws LuaException {
        if (arguments.length < 1) {
            buffers = new String[MAX_TRACKS];
            return new Object[0];
        }
        int trackIndex = parseTrackIndex(arguments[0], "First");
        buffers[trackIndex] = null;
        return new Object[0];
    }

    /**
     * Sets a track buffer for data to be written.
     * The data is validated against the MagTek specification.
     * See https://www.magtek.com/content/documentationfiles/d99800004.pdf
     * @param arguments object array with track index and a string of data to be written
     * @return nil
     */
    private Object[] writeLua(Object[] arguments) throws LuaException {
        if (arguments.length < 2)
            throw new LuaException("Not enough arguments");
        int trackIndex = parseTrackIndex(arguments[0], "First");
        if (!(arguments[1] instanceof String))
            throw new LuaException("Second argument expected to be a string");
        if (!isBufferValid(trackIndex, (String) arguments[1]))
            throw new LuaException("Buffer data is invalid for track");
        synchronized (this) {
            buffers[trackIndex] = (String) arguments[1];
        }
        return new Object[0];
    }

    /**
     * Checks if the data is appropriate for the track
     * @param trackIndex track index
     * @param buffer data to write
     * @return is the data compatible
     */
    private boolean isBufferValid(int trackIndex, String buffer) {
        switch (trackIndex) {
            case 0:
                return buffer.length() <= 79 && buffer.matches("[A-Za-z0-9%^?;=]*");
            case 1:
                return buffer.length() <= 40 && buffer.matches("[0-9%^?;=]*");
            case 2:
                return buffer.length() <= 107 && buffer.matches("[0-9%^?;=]*");
        }
        return false;
    }

    /**
     * Parse an object to check if it is a double, convert it to an int, then check it against the buffer index bounds
     * @param argument potential Double index
     * @return int index
     */
    private int parseTrackIndex(Object argument, String position) throws LuaException {
        if (!(argument instanceof Double))
            throw new LuaException(String.format("%s argument expected to be an integer", position));
        int trackIndex = ((Double) argument).intValue();
        if (trackIndex >= MAX_TRACKS || trackIndex < 0)
            throw new LuaException(String.format("Track index out of bounds: %d", trackIndex));
        return trackIndex;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public String[] methods() {
        return getMethodNames();
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "write":
                return writeLua(args.toArray());
            case "clear":
                return clearLua(args.toArray());
        }
        throw new NoSuchMethodException(method);
    }

    /**
     * Check the swiped item and call a swipe event if the items is a mag card
     * If the buffers are not empty, the data in them will be written
     * @param swipedItem potential mag card
     */
    public void itemSwipped(ItemStack swipedItem) {
        if (!isMagCard(swipedItem))
            return;
        // Write
        NBTTagCompound itemTag = swipedItem.getTagCompound();
        assert itemTag != null;
        NBTTagCompound magTag = itemTag.getCompoundTag(MAG_TAG);
        synchronized (this) {
            for (int bufferIndex = 0; bufferIndex < buffers.length; bufferIndex++)
                if (buffers[bufferIndex] != null)
                    magTag.setString(String.valueOf(bufferIndex), buffers[bufferIndex]);
        }
        itemTag.setTag(MAG_TAG, magTag);
        swipedItem.setTagCompound(itemTag);
        // Read
        Object[] event = new Object[]{
                magTag.getString("0"),
                magTag.getString("1"),
                magTag.getString("2")
        };
        for (IComputerAccess computer : computers)
            computer.queueEvent("mag_swipe", event);
        OpenComputersUtil.sendToReachable(node, "mag_swipe", event);
    }

    /**
     * Check if the item is a mag card
     * @param swipedItem potential mag card
     * @return is it a mag card
     */
    private boolean isMagCard(ItemStack swipedItem) {
        return swipedItem.isItemEqual(new ItemStack(ModItems.PLASTIC_CARD)) &&
                swipedItem.getTagCompound() != null &&
                swipedItem.getTagCompound().hasKey(MAG_TAG) &&
                swipedItem.stackSize == 1;
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        computers.remove(computer);
    }

    /**
     * Create a blank mag card
     * @return mag card
     */
    public static ItemStack createMagCard() {
        ItemStack magCard = new ItemStack(ModItems.PLASTIC_CARD);
        NBTTagCompound magTag = new NBTTagCompound();
        magTag.setString("0", "");
        magTag.setString("1", "");
        magTag.setString("2", "");
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(MAG_TAG, magTag);
        magCard.setTagCompound(tag);
        magCard.setStackDisplayName(ItemPlasticCard.NAME_MAG);
        return magCard;
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public Node node() {
        return node;
    }

    @Override
    public void update() {
        OpenComputersUtil.updateNode(this, node);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        OpenComputersUtil.removeNode(node);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        OpenComputersUtil.removeNode(node);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        OpenComputersUtil.readFromNbt(compound, node);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        OpenComputersUtil.writeToNbt(compound, node);
        return compound;
    }
}
