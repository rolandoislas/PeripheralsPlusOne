package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.collectiveframework.minecraft.tiles.TileEntityInventory;
import com.austinv11.peripheralsplusplus.capabilities.rfid.CapabilityRfid;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagHolder;
import com.austinv11.peripheralsplusplus.init.ModBlocks;
import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersUtil;
import com.austinv11.peripheralsplusplus.utils.ReflectionHelper;
import com.austinv11.peripheralsplusplus.utils.Util;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidAuthentication;
import com.austinv11.peripheralsplusplus.utils.rfid.RfidTag;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TileEntityRfidReaderWriter extends TileEntityInventory implements IPlusPlusPeripheral,
        OpenComputersPeripheral {

    private byte[] selectedId;
    private RfidAuthentication authentication;
    private Node node;

    public TileEntityRfidReaderWriter() {
        super();
        invName = Reference.MOD_ID + ":tile_entity_rfid_reader_writer";
        node = OpenComputersUtil.createNode(this, getType());
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(ModBlocks.RFID_READER_WRITER.getUnlocalizedName() + ".name");
    }

    @Nonnull
    @Override
    public String getType() {
        return "rfid_reader_writer";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{
                "search", // (void) byte[] uid
                "select", // (byte[] uid) void
                "auth", // (int key_type, int block, byte[] key) void
                "deauth", // (void) void
                "read", // (int block) byte[] block_data
                "write"/*, // (int block, byte[] block_data) boolean success
                "increment",
                "decrement",
                "restore",
                "transfer"*/
        };
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method,
                               @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        if (!Config.enableRfidItems)
            throw new LuaException("RFID items are not enabled in the config");
        switch (method) {
            case 0:
                return searchLua();
            case 1:
                return selectLua(arguments);
            case 2:
                return authLua(arguments);
            case 3:
                return deauthLua();
            case 4:
                return readLua(arguments);
            case 5:
                return writeLua(arguments);
            case 6:
                return incrementLua(arguments);
            case 7:
                return decrementLua(arguments);
            case 8:
                return restoreLua(arguments);
            case 9:
                return transferLua(arguments);
        }
        throw new LuaException("Unexpected error");
    }

    private Object[] transferLua(Object[] arguments) {
        return new Object[0];
    }

    private Object[] restoreLua(Object[] arguments) {
        return new Object[0];
    }

    private Object[] decrementLua(Object[] arguments) {
        return new Object[0];
    }

    private Object[] incrementLua(Object[] arguments) {
        return new Object[0];
    }

    /**
     * Attempt to write a block to the currently selected RFID card
     * @param arguments block id, byte array of data to write
     * @return boolean success
     */
    private Object[] writeLua(Object[] arguments) throws LuaException {
        if (arguments.length < 2)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Double))
            throw new LuaException("Argument 1 expected to be an int");
        if (selectedId == null || authentication == null)
            return new Object[]{false};
        int block = ((Double)arguments[0]).intValue();
        byte[] data = parseLuaIdArg(arguments[1], RfidTag.BLOCK_LENGTH);
        ItemStack chip = getRfidItem(selectedId);
        if (chip.isEmpty())
            return new Object[]{false};
        RfidTag rfidTag = new RfidTag(chip);
        authentication.writeBlock(rfidTag, block, data);
        RfidTag.removeTag(chip);
        RfidTag.addTag(chip, rfidTag);
        return new Object[]{true};
    }

    /**
     * Attempt to read a block from the currently selected RFID card.
     * There must be an active authentication stored.
     * @param arguments block id
     * @return array of bytes or nil if the card could not be accessed
     * @throws LuaException invalid arguments or no card selected
     */
    private Object[] readLua(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Double))
            throw new LuaException("Argument 1 expected to be an integer");
        if (selectedId == null || authentication == null)
            return new Object[0];
        int block = ((Double)arguments[0]).intValue();
        ItemStack chip = getRfidItem(selectedId);
        if (chip.isEmpty())
            return new Object[0];
        RfidTag rfidTag = new RfidTag(chip);
        byte[] blockBytes = authentication.readBlock(rfidTag, block);
        return new Object[]{Util.arrayToMap(Util.byteArraytoUnsignedIntArray(blockBytes))};
    }

    /**
     * Remove stored auth key
     * @return nil
     */
    private Object[] deauthLua() {
        authentication = null;
        return new Object[0];
    }

    /**
     * Set an auth key
     * @param arguments object array with expected auth key type and key
     * @return nil - zero sized object array
     * @throws LuaException invalid arguments
     */
    private Object[] authLua(Object[] arguments) throws LuaException {
        if (arguments.length < 3)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Double))
            throw new LuaException("Argument 1 expected to be an integer");
        if (!(arguments[1] instanceof Double))
            throw new LuaException("Argument 2 expected to be an integer");
        int type = ((Double)arguments[0]).intValue();
        if (type != RfidTag.KeyType.A.ordinal() && type != RfidTag.KeyType.B.ordinal())
            throw new LuaException("Invalid key type");
        int block = ((Double)arguments[1]).intValue();
        if (block < RfidTag.MANUFACTURER_BLOCK || block >= RfidTag.SECTORS * RfidTag.SECTOR_SIZE)
            throw new LuaException("Block index out of range");
        authentication = new RfidAuthentication(RfidTag.KeyType.values()[type], block, parseLuaIdArg(arguments[2],
                RfidTag.DEFAULT_KEY.length));
        return new Object[0];
    }

    /**
     * Parse an object expected to be an map of string to a byte array representing an id
     * @param argument Map\<String, String\>
     * @param size size the map is expected to be
     * @return id array
     * @throws LuaException argument was not a map, incorrect size, or had invalid parameters
     */
    private byte[] parseLuaIdArg(Object argument, int size) throws LuaException {
        String argOneError = "Expected an array of unsigned integers";
        if (!(argument instanceof Map))
            throw new LuaException(argOneError);
        List<Double> passedId;
        try {
            passedId = doubleMapToList((Map<Double, Double>)argument);
        }
        catch (NumberFormatException e) {
            throw new LuaException(argOneError);
        }
        if (passedId.size() != size)
            throw new LuaException(String.format(
                    "Table size is incorrect. Found: %d Expected: %d",
                    passedId.size(), size));
        return doubleListToByteArray(passedId);
    }

    /**
     * Select an RFID chip to be used for other commands
     * @param arguments object array with the first parameter an array of Doubles
     * @return Object array with boolean success
     * @throws LuaException on invalid arguments
     */
    private Object[] selectLua(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        byte[] id = parseLuaIdArg(arguments[0], RfidTag.ID_SIZE);
        ItemStack rfidItem = getRfidItem(id);
        if (!rfidItem.isEmpty())
            selectedId = id;
        return new Object[0];
    }

    /**
     * Convert a map to a list
     * Map is expected to have keys of Double values to strings without any dangling decimal values
     * @param map map to convert
     * @return list of items from the map casted to the passed type
     */
    private List<Double> doubleMapToList(Map<Double, Double> map) throws NumberFormatException {
        List<Double> list = new ArrayList<>();
        for (Double index = 1d; index <= map.size(); index++) {
            if (!map.containsKey(index))
                throw new NumberFormatException();
            list.add(map.get(index));
        }
        return list;
    }

    /**
     * Search inventories for an RFID item.
     * Returns empty itemstack if an item was not found
     * @param id ID the item should have - If the id is null the first item found will be returned
     */
    @Nonnull
    private ItemStack getRfidItem(@Nullable byte[] id) {
        // Check internal inventory
        ItemStack foundRfidItem = getRfidItemFromInventory(this, id);
        if (!foundRfidItem.isEmpty())
            return foundRfidItem;
        // Check the turtle inventory if this is a turtle peripheral
        TileEntity te = getWorld().getTileEntity(getPos());
        if (te != null)
            try {
                ITurtleAccess turtle = ReflectionHelper.getTurtle(te);
                foundRfidItem = getRfidItemFromInventory(turtle.getInventory(), id);
                if (!foundRfidItem.isEmpty())
                    return foundRfidItem;
            } catch (Exception ignore) {}
        // Perform a search
        for (int currentRadius = 1; currentRadius <= 3; currentRadius++) {
            BlockPos start = getPos().west(currentRadius).north(currentRadius).down(currentRadius);
            int diameter = currentRadius * 2 + 1;
            for (int y = 0; y < diameter; y++) {
                for (int x = 0; x < diameter; x++) {
                    for (int z = 0; z < diameter; z++) {
                        BlockPos pos = start.east(x).south(z).up(y);
                        // Search for entities at this block and then search their inventories
                        List<Entity> entities =
                                getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
                        for (Entity entity : entities) {
                            // Check the player's inventory
                            if (entity instanceof EntityPlayer) {
                                foundRfidItem = getRfidItemFromInventory(((EntityPlayer) entity).inventory, id);
                                if (!foundRfidItem.isEmpty())
                                    return foundRfidItem;
                            }
                            // Check an entity item in-world
                            else if (entity instanceof EntityItem) {
                                foundRfidItem = ((EntityItem) entity).getItem();
                                if ((RfidTag.hasTag(foundRfidItem) ||
                                        foundRfidItem.isItemEqual(new ItemStack(ModItems.RFID_CHIP))) &&
                                        foundRfidItem.getCount() == 1 && (id == null ||
                                        RfidTag.itemIdEquals(foundRfidItem, id)))
                                    return foundRfidItem;
                            }
                            // General held item check
                            Iterable<ItemStack> items = entity.getEquipmentAndArmor();
                            for (ItemStack item : items)
                                if ((RfidTag.hasTag(item) ||
                                        item.isItemEqual(new ItemStack(ModItems.RFID_CHIP)))
                                        && item.getCount() == 1) {
                                    foundRfidItem = item;
                                    if (!foundRfidItem.isEmpty() &&
                                            (id == null || RfidTag.itemIdEquals(foundRfidItem, id)))
                                        return foundRfidItem;
                                    foundRfidItem = ItemStack.EMPTY;
                                }
                            // Check the entity's capability for a tag
                            if (entity.hasCapability(CapabilityRfid.INSTANCE, null)) {
                                RfidTagHolder tagHolder = entity.getCapability(CapabilityRfid.INSTANCE, null);
                                if (tagHolder != null && !tagHolder.getTag().isEmpty() &&
                                        (id == null || RfidTag.itemIdEquals(tagHolder.getTag(), id)))
                                    return tagHolder.getTag();
                            }
                        }
                        // Search for tile entities if the item was not found in an entity's inventory
                        TileEntity tileEntity = getWorld().getTileEntity(pos);
                        if (foundRfidItem.isEmpty() && tileEntity instanceof IInventory) {
                            foundRfidItem = getRfidItemFromInventory((IInventory) tileEntity, id);
                            if (!foundRfidItem.isEmpty())
                                return foundRfidItem;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Convert a Double List to a byte array, converting the doubles to ints first.
     * @param doubleList array to convert
     * @return byte array
     */
    private byte[] doubleListToByteArray(List<Double> doubleList) {
        byte[] byteArray = new byte[doubleList.size()];
        for (int number = 0; number < doubleList.size(); number++) {
            byteArray[number] =  (byte) (int) Math.floor(doubleList.get(number));
        }
        return byteArray;
    }

    /**
     * Search for any RFID item
     * @return object array with the id converted to a uint map
     */
    private Object[] searchLua() {
        ItemStack foundRfidItem = getRfidItem(null);
        if (!foundRfidItem.isEmpty()) {
            RfidTag tag = new RfidTag(foundRfidItem);
            if (!RfidTag.hasTag(foundRfidItem) || tag.getIdLong() <= 0)
                RfidTag.addTag(foundRfidItem, tag);
            return new Object[]{Util.arrayToMap(Util.byteArraytoUnsignedIntArray(tag.getId()))};
        }
        return new Object[0];
    }

    /**
     * Search an inventory for an rfid item.
     * @param inventory inventory to search
     * @param id id the RFID item should contain, or null to match any
     * @return item found to empty itemstack
     */
    @Nonnull
    private ItemStack getRfidItemFromInventory(IInventory inventory, @Nullable byte[] id) {
        for (int itemIndex = 0; itemIndex < inventory.getSizeInventory(); itemIndex++) {
            ItemStack item = inventory.getStackInSlot(itemIndex);
            if ((RfidTag.hasTag(item) || item.isItemEqual(new ItemStack(ModItems.RFID_CHIP))) && item.getCount() == 1
                    && (id == null || RfidTag.itemIdEquals(item, id)))
                return item;
        }
        return ItemStack.EMPTY;
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
    public Object[] invoke(String method, Context context, Arguments arguments) throws Exception {
        if (!Config.enableRfidItems)
            throw new LuaException("RFID items are not enabled in the config");
        switch (method) {
            case "search":
                return searchLua();
            case "select":
                return selectLua(arguments.toArray());
            case "auth":
                return authLua(arguments.toArray());
            case "deauth":
                return deauthLua();
            case "read":
                return readLua(arguments.toArray());
            case "write":
                return writeLua(arguments.toArray());
            case "increment":
                return incrementLua(arguments.toArray());
            case "decrement":
                return decrementLua(arguments.toArray());
            case "restore":
                return restoreLua(arguments.toArray());
            case "transfer":
                return transferLua(arguments.toArray());
        }
        throw new LuaException("Unexpected error");
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public Node node() {
        return node;
    }

    @Override
    public void update() {
        super.update();
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
