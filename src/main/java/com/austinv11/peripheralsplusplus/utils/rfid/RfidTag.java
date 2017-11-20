package com.austinv11.peripheralsplusplus.utils.rfid;

import com.austinv11.collectiveframework.minecraft.utils.Colors;
import com.austinv11.collectiveframework.minecraft.utils.NBTHelper;
import com.austinv11.peripheralsplusplus.data.world.WorldDataRfidUniqueId;
import com.austinv11.peripheralsplusplus.init.ModItems;
import com.google.common.primitives.Longs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RfidTag {
    private static final String RFID_TAG = ModItems.RFID_CHIP.getRegistryName().toString();
    public static final int SECTOR_SIZE = 4;
    public static final int SECTORS = 16;
    public static final int BLOCK_LENGTH = 16;
    public static final int ID_SIZE = 7;
    private static final long MAX_ID = 0xFFFFFFFFFFFFFFL;
    public static final byte[] DEFAULT_KEY = new byte[]{
            (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff
    };
    public static final byte[] DEFAULT_ACCESS_BYTES = new byte[]{
            (byte) 0xff, 0x07, (byte) 0x80, 0x00
    };
    public static final int MANUFACTURER_BLOCK = 0;
    public static final int ACCESS_BITS_POSITION = DEFAULT_KEY.length;
    public static final int KEY_A_POSITION = 0;
    public static final int KEY_B_POSITION = 10;
    private static final String LORE_STRING = Colors.RESET + "Contains RFID Chip";
    private byte[][] blocks;

    private RfidTag() {
        blocks = new byte[SECTOR_SIZE * SECTORS][BLOCK_LENGTH];
        // Write the initial access bits and keys
        for (int blockIndex = 0; blockIndex < SECTORS * SECTOR_SIZE; blockIndex++) {
            if ((blockIndex + 1) % SECTOR_SIZE != 0 || blockIndex == MANUFACTURER_BLOCK)
                continue;
            System.arraycopy(DEFAULT_KEY, 0, blocks[blockIndex], KEY_A_POSITION, DEFAULT_KEY.length);
            System.arraycopy(DEFAULT_KEY, 0, blocks[blockIndex], KEY_B_POSITION, DEFAULT_KEY.length);
            System.arraycopy(DEFAULT_ACCESS_BYTES, 0, blocks[blockIndex], ACCESS_BITS_POSITION,
                    DEFAULT_ACCESS_BYTES.length);
        }
    }

    /**
     * Creates a RFID tag from an itemstack or initialize an empty one if the itemstack is not tagged
     * @param itemStack itemstack that may contain a tag
     */
    public RfidTag(ItemStack itemStack) {
        this();
        NBTTagCompound rfidTag = getTag(itemStack);
        if (rfidTag != null)
            readFromNbt(rfidTag);
    }

    /**
     * Get the NBT tag that represents the RFID tag.
     * @param itemStack item with the tag
     * @return tag
     */
    @Nullable
    private NBTTagCompound getTag(ItemStack itemStack) {
        if (!hasTag(itemStack))
            return null;
        return (NBTTagCompound) itemStack.getTagCompound().getTag(RFID_TAG);
    }

    /**
     * Check if an item has an RFID tag
     * @param itemStack item to check for a tag
     * @return has tag
     */
    public static boolean hasTag(ItemStack itemStack) {
        return itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey(RFID_TAG);
    }

    /**
     * Adds a default tag to an item.
     * @see RfidTag#addTag(ItemStack, RfidTag)
     * @param item item to add the tag to
     */
    public static void addTag(ItemStack item) {
        addTag(item, new RfidTag());
    }

    /**
     * Add a tag to an item without a tag. If the item already has a tag, no additional tag will be added.
     * @param item item without a tag
     * @param rfidTag tag to add
     */
    public static void addTag(ItemStack item, RfidTag rfidTag) {
        if (item.isEmpty() || hasTag(item))
            return;
        NBTTagCompound itemTag = item.getTagCompound();
        if (itemTag == null)
            itemTag = new NBTTagCompound();
        if (rfidTag.getIdLong() <= 0)
            rfidTag.setId();
        NBTTagCompound rfidTagNbt = new NBTTagCompound();
        itemTag.setTag(RFID_TAG, rfidTag.writeToNbt(rfidTagNbt));
        item.setTagCompound(itemTag);
        if (!item.isItemEqual(new ItemStack(ModItems.RFID_CHIP))) {
            List<String> text = new ArrayList<>();
            text.add(LORE_STRING);
            NBTHelper.addInfo(item, text);
        }
    }

    /**
     * Return the 7 byte uuid as an integer
     * @return uuid int
     */
    public long getIdLong() {
        byte[] idPadded = new byte[Long.BYTES];
        byte[] id = getId();
        System.arraycopy(id, 0, idPadded, Long.BYTES - ID_SIZE, ID_SIZE);
        return Longs.fromByteArray(idPadded);
    }

    /**
     * Return a 7 byte array that represents the uuid of a tag
     * @return tag id
     */
    public byte[] getId() {
        byte[] id = new byte[ID_SIZE];
        System.arraycopy(blocks[0], 0, id, 0, ID_SIZE);
        return id;
    }

    /**
     * Sets a new unique (per world) id on the tag.
     */
    public void setId() {
        WorldDataRfidUniqueId worldDataRfidUniqueId = WorldDataRfidUniqueId.get();
        if (worldDataRfidUniqueId == null)
            return;
        long newId = (worldDataRfidUniqueId.getLastId() + 1) % MAX_ID;
        worldDataRfidUniqueId.setLastId(newId);
        worldDataRfidUniqueId.markDirty();
        byte[] newIdBytes = Longs.toByteArray(newId);
        System.arraycopy(newIdBytes, Long.BYTES - ID_SIZE, blocks[0], 0, ID_SIZE);
    }

    /**
     * Writes the block list directly to the root of the passed compound
     * @param compound tag to write to
     */
    private NBTTagCompound writeToNbt(NBTTagCompound compound) {
        NBTTagList blockList = new NBTTagList();
        for (byte[] block : blocks)
            blockList.appendTag(new NBTTagByteArray(block));
        compound.setTag("block_list", blockList);
        return compound;
    }

    /**
     * Read from a tag compound
     * @param compound compound to read from
     */
    private void readFromNbt(NBTTagCompound compound) {
        NBTTagList blockList = compound.getTagList("block_list", Constants.NBT.TAG_BYTE_ARRAY);
        int blockIndex = 0;
        for (NBTBase blockBase : blockList) {
            NBTTagByteArray block = (NBTTagByteArray) blockBase;
            if (blockIndex >= blocks.length)
                break;
            blocks[blockIndex] = block.getByteArray();
            blockIndex++;
        }
    }

    /**
     * Create an rfid chip with a tag
     * @param tag tag to use
     * @return rfid chip
     */
    @Nonnull
    public static ItemStack createChip(RfidTag tag) {
        ItemStack chip = new ItemStack(ModItems.RFID_CHIP);
        addTag(chip, tag);
        return chip;
    }

    /**
     * Checks if the item matches the passed id
     * @param item item to check id against
     * @param id id to check for
     * @return ids are equal
     */
    public static boolean itemIdEquals(ItemStack item, byte[] id) {
        if (!RfidTag.hasTag(item))
            return false;
        RfidTag rfidTag = new RfidTag(item);
        return Arrays.equals(rfidTag.getId(), id);
    }

    /**
     * Get a block's byte array
     * @param block block to get
     * @return array of block bytes
     */
    public byte[] getBlock(int block) {
        return blocks[block].clone();
    }

    /**
     * Set a block's byte array
     * @param blockData block array
     * @param block block index
     */
    public void setBlock(byte[] blockData, int block) {
        blocks[block] = blockData.clone();
    }

    /**
     * Remove the tag from an itemstack
     * @param itemStack stack to remove the tag from
     */
    public static void removeTag(ItemStack itemStack) {
        if (!hasTag(itemStack))
            return;
        NBTTagCompound compound = itemStack.getTagCompound();
        assert compound != null;
        compound.removeTag(RFID_TAG);
        if (compound.hasKey("display")) {
            NBTTagCompound display = compound.getCompoundTag("display");
            if (display.hasKey("Lore")) {
                NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
                int loreIndex = 0;
                for (NBTBase loreString : lore) {
                    if (((NBTTagString)loreString).getString().equals(LORE_STRING)) {
                        lore.removeTag(loreIndex);
                        break;
                    }
                    loreIndex++;
                }
                if (lore.tagCount() > 0)
                    display.setTag("Lore", lore);
                else
                    display.removeTag("Lore");
            }
            if (display.hasKey("Lore"))
                compound.setTag("display", display);
            else
                compound.removeTag("display");
        }
        if (compound.getSize() > 0)
            itemStack.setTagCompound(compound);
        else
            itemStack.setTagCompound(null);
    }

    /**
     * Type of sector keys
     */
    public enum KeyType {
        A, B
    }
}
