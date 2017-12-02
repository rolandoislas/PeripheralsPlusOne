package com.austinv11.peripheralsplusplus.utils.rfid;

import java.util.Arrays;

public class RfidAuthentication {
    private static final byte[] EMPTY_BLOCK = new byte[RfidTag.BLOCK_LENGTH];
    private final RfidTag.KeyType keyType;
    private final int block;
    private final byte[] key;

    public RfidAuthentication(RfidTag.KeyType keyType, int block, byte[] key) {
        this.keyType = keyType;
        this.block = block;
        this.key = key;
    }

    /**
     * Check if a block can be read with the current key
     * @param rfidTag tag that contains the block
     * @param blockIndex block index
     * @return the byte array with unreadable values set to zero
     */
    public byte[] readBlock(RfidTag rfidTag, int blockIndex) {
        if (!isSameSector(blockIndex))
            return EMPTY_BLOCK;
        if (blockIndex == RfidTag.MANUFACTURER_BLOCK)
            return rfidTag.getBlock(blockIndex);
        // Get access bits
        boolean[] perms;
        try {
            perms = verifyAndGetAccessBits(rfidTag, blockIndex);
        }
        catch (DataFormatException e) {
            return EMPTY_BLOCK;
        }
        // Verify key
        if (!verifyKey(rfidTag, blockIndex))
            return EMPTY_BLOCK;
        // Get the block
        byte[] block = rfidTag.getBlock(blockIndex);
        // Check access permissions for a sector trailer block
        if (isSectorTrailer(blockIndex)) {
            byte[] nullKey = new byte[RfidTag.DEFAULT_KEY.length];
            // Key A can never be read. Clear it.
            System.arraycopy(nullKey, 0, block, RfidTag.KEY_A_POSITION, nullKey.length);
            switch (keyType) {
                case A:
                    // Key A cannot read key B if none of these access conditions are met
                    if (
                            !hasPerm(perms, 0, 0, 0) && // 0 0 0
                            !hasPerm(perms, 0, 1, 0) && // 0 1 0
                            !hasPerm(perms, 0, 0, 1) // 0 0 1
                        )
                        System.arraycopy(nullKey, 0, block, RfidTag.KEY_B_POSITION, nullKey.length);
                    break;
                case B:
                    // Key B cannot read access bits if these conditions are met
                    if (
                            hasPerm(perms, 0, 0, 0) || // 0 0 0
                            hasPerm(perms, 0, 1, 0) || // 0 1 0
                            hasPerm(perms, 0, 0, 1) // 0 0 1
                        ) {
                        byte[] nullBits = new byte[RfidTag.DEFAULT_ACCESS_BYTES.length];
                        System.arraycopy(nullBits, 0, block, RfidTag.ACCESS_BITS_POSITION, nullBits.length);
                    }
                    // Key B can never read key B
                    System.arraycopy(nullKey, 0, block, RfidTag.KEY_B_POSITION, nullKey.length);
                    break;
            }
            return block;
        }
        // Check access permissions for a data block
        else {
            // If this condition is met, neither key can access the data block
            if (hasPerm(perms, 1, 1, 1)) // 1 1 1
                return EMPTY_BLOCK;
            switch (keyType) {
                case A:
                    // Key A cannot read the data block under these conditions
                    if (
                            hasPerm(perms, 0, 1, 1) || // 0 1 1
                            hasPerm(perms, 1, 0, 1) // 1 0 1
                        )
                        return EMPTY_BLOCK;
                    break;
                case B:
                    // If key B can be read, it cannot be used for operations
                    if (isKeyBReadable(rfidTag, blockIndex))
                        return EMPTY_BLOCK;
                    break;
            }
            return block;
        }
    }

    /**
     * Check if the sector of a block index is the same as the one currently used for authentication
     * @param blockIndex block index to check against stored block index
     * @return are the sectors the same
     */
    private boolean isSameSector(int blockIndex) {
        return getSectorTrailerIndex(blockIndex) == getSectorTrailerIndex(block);
    }

    /**
     * Checks if key B can be read.
     * No operations should accept key B in this case.
     * @param rfidTag tag that contains the block
     * @param blockIndex block index to check key B readability for
     * @return is key B readable
     */
    private boolean isKeyBReadable(RfidTag rfidTag, int blockIndex) {
        boolean[] perms;
        try {
            perms = verifyAndGetAccessBits(rfidTag, getSectorTrailerIndex(blockIndex));
        }
        catch (DataFormatException e) {
            return false;
        }
        return  hasPerm(perms, 0, 0, 0) || // 0 0 0
                hasPerm(perms, 0, 1, 0) || // 0 1 0
                hasPerm(perms, 0, 0, 1); // 0 0 1
    }

    /**
     * Check if the block index is a sector trailer
     * @param blockIndex block index to check
     * @return is the block index a sector trailer index
     */
    private boolean isSectorTrailer(int blockIndex) {
        return getSectorTrailerIndex(blockIndex) == blockIndex;
    }

    /**
     * Verify the current key against the key for a block
     * @param rfidTag tag that contains the block
     * @param blockIndex block to verify the key for
     * @return is the key valid
     */
    private boolean verifyKey(RfidTag rfidTag, int blockIndex) {
        // Get the sector trailer block
        int sectorTrailerIndex = getSectorTrailerIndex(blockIndex);
        byte[] sectorTrailer = rfidTag.getBlock(sectorTrailerIndex);
        byte[] keyA = new byte[RfidTag.DEFAULT_KEY.length];
        System.arraycopy(sectorTrailer, RfidTag.KEY_A_POSITION, keyA, 0, keyA.length);
        byte[] keyB = new byte[RfidTag.DEFAULT_KEY.length];
        System.arraycopy(sectorTrailer, RfidTag.KEY_B_POSITION, keyB, 0, keyB.length);
        // Check the key
        return (!keyType.equals(RfidTag.KeyType.A) || Arrays.equals(key, keyA)) &&
                (!keyType.equals(RfidTag.KeyType.B) || Arrays.equals(key, keyB));
    }

    /**
     * Verifies the access bits that control a block and returns them
     * @param rfidTag tag containing the block
     * @param blockIndex block to get access bits for
     * @return access bits
     * @throws DataFormatException access bits are formatted incorrectly
     */
    private boolean[] verifyAndGetAccessBits(RfidTag rfidTag, int blockIndex) throws DataFormatException {
        int sectorTrailerIndex = getSectorTrailerIndex(blockIndex);
        byte[] sectorTrailer = rfidTag.getBlock(sectorTrailerIndex);
        byte[] accessBytes = new byte[RfidTag.DEFAULT_ACCESS_BYTES.length];
        System.arraycopy(sectorTrailer, RfidTag.ACCESS_BITS_POSITION, accessBytes, 0,
                RfidTag.DEFAULT_ACCESS_BYTES.length);
        // Check the access bits are formatted correctly
        if (!verifyAccessBitsFormat(accessBytes))
            throw new DataFormatException();
        return getAccessBits(accessBytes, blockIndex % RfidTag.SECTOR_SIZE);
    }

    /**
     * @see RfidAuthentication#hasPerm(boolean[], boolean, boolean, boolean)
     * @param perms array of permission bits
     * @param one bit one
     * @param two bit two
     * @param three bit three
     * @return are they a match
     */
    private boolean hasPerm(boolean[] perms, int one, int two, int three) {
        return hasPerm(perms, one == 1, two == 1, three == 1);
    }

    /**
     * Check if a permissions bits array matched the passed permission bits
     * @param perms array to check against
     * @param one bit one
     * @param two bit two
     * @param three bit three
     * @return are they a match
     */
    private boolean hasPerm(boolean[] perms, boolean one, boolean two, boolean three) {
        return Arrays.equals(perms, new boolean[]{one, two, three});
    }

    /**
     * Get a boolean array representing the access bits
     * This does not verify the bit format.
     * An unchecked call can lead to invalid access.
     * @see RfidAuthentication#getAccessBitsRaw(byte[], int)
     * @param accessBytes bytes that contain the access bits
     * @param accessBitsIndex access bits index
     * @return boolean bits
     */
    private boolean[] getAccessBits(byte[] accessBytes, int accessBitsIndex) {
        boolean[] bitsRaw = getAccessBitsRaw(accessBytes, accessBitsIndex);
        boolean[] bits = new boolean[bitsRaw.length / 2];
        bits[0] = bitsRaw[4];
        bits[1] = bitsRaw[2];
        bits[2] = bitsRaw[5];
        return bits;
    }

    /**
     * Verify that the access bits are formatted correctly
     * Refer to https://www.nxp.com/docs/en/data-sheet/MF1S50YYX_V1.pdf section 8.7.1 for the format
     * @param accessBytes bits to check
     * @return formatted correctly
     */
    private boolean verifyAccessBitsFormat(byte[] accessBytes) {
        if (accessBytes.length != RfidTag.DEFAULT_ACCESS_BYTES.length)
            return false;
        for (int accessBitsIndex = 0; accessBitsIndex < RfidTag.SECTOR_SIZE; accessBitsIndex++) {
            boolean[] accessBits = getAccessBitsRaw(accessBytes, accessBitsIndex);
            if (
                    accessBits[4] == accessBits[0] ||
                    accessBits[2] == accessBits[3] ||
                    accessBits[5] == accessBits[1]
                )
                return false;
        }
        return true;
    }

    /**
     * Gets the 6 bits that define the access bits in three bytes.
     *
     * @param accessBytes three byte array that contains the access blocks
     * @param accessBitsIndex 0 data block, 1 data block, 2 data block, 3 sector trailer
     *                        Refer to https://www.nxp.com/docs/en/data-sheet/MF1S50YYX_V1.pdf section 8.7.1
     * @return array with the bits mapped as boolean
     */
    private boolean[] getAccessBitsRaw(byte[] accessBytes, int accessBitsIndex) {
        int bitsSize = RfidTag.DEFAULT_ACCESS_BYTES.length - 1; // Actual access bytes size
        boolean[] bits = new boolean[bitsSize * 2];
        int mask = 1;
        for (int bitIndex = 0; bitIndex < bitsSize; bitIndex++) {
            bits[bitIndex] = ((accessBytes[bitIndex] & (mask << accessBitsIndex)) >> accessBitsIndex) == 1;
            bits[bitIndex + bitsSize] = ((accessBytes[bitIndex] & (mask << (accessBitsIndex + bitsSize + 1))) >>
                    (accessBitsIndex + bitsSize + 1)) == 1;
        }
        return bits;
    }

    /**
     * Calculate a sector trailer for a block
     * @param block block within a sector
     * @return sector trailer for block
     */
    private int getSectorTrailerIndex(int block) {
        if ((block + 1) % RfidTag.SECTOR_SIZE == 0)
            return block;
        else
            return block + RfidTag.SECTOR_SIZE -  ((block + 1) % RfidTag.SECTOR_SIZE);
    }

    /**
     * Try to write to a block. Unwrtable sections are ignored.
     * @param rfidTag rfid tag to operate on
     * @param blockIndex block index to work with
     * @param data
     */
    public void writeBlock(RfidTag rfidTag, int blockIndex, byte[] data) {
        if (block == RfidTag.MANUFACTURER_BLOCK || !isSameSector(blockIndex))
            return;
        // Get permissions
        boolean[] perms;
        try {
            perms = verifyAndGetAccessBits(rfidTag, blockIndex);
        }
        catch (DataFormatException e) {
            return;
        }
        // Check key
        if (!verifyKey(rfidTag, blockIndex))
            return;
        byte[] block = rfidTag.getBlock(blockIndex);
        // Write to sector trailer block
        if (isSectorTrailer(blockIndex)) {
            switch (keyType) {
                case A:
                    // Check if Key A can write to the keys
                    if (
                            hasPerm(perms, 0, 0, 0) || // 0 0 0
                            hasPerm(perms, 0, 0, 1) // 0 0 1
                        ) {
                        System.arraycopy(data, RfidTag.KEY_A_POSITION, block, RfidTag.KEY_A_POSITION,
                                RfidTag.DEFAULT_KEY.length);
                        System.arraycopy(data, RfidTag.KEY_B_POSITION, block, RfidTag.KEY_B_POSITION,
                                RfidTag.DEFAULT_KEY.length);
                    }
                    // Check if key A can write to the access bits
                    if (
                            hasPerm(perms, 0, 0, 1) // 0 0 1
                        )
                        System.arraycopy(data, RfidTag.ACCESS_BITS_POSITION, block, RfidTag.ACCESS_BITS_POSITION,
                                RfidTag.DEFAULT_ACCESS_BYTES.length);
                    break;
                case B:
                    // Check if Key B can write to the keys
                    if (
                            hasPerm(perms, 1, 0, 0) || // 1 0 0
                            hasPerm(perms, 0, 1, 1) // 0 1 1
                        ) {
                        System.arraycopy(data, RfidTag.KEY_A_POSITION, block, RfidTag.KEY_A_POSITION,
                                RfidTag.DEFAULT_KEY.length);
                        System.arraycopy(data, RfidTag.KEY_B_POSITION, block, RfidTag.KEY_B_POSITION,
                                RfidTag.DEFAULT_KEY.length);
                    }
                    // Check if key B can write to the access bits
                    if (
                            hasPerm(perms, 0, 1, 1) || // 0 1 1
                            hasPerm(perms, 1, 0, 1) // 1 0 1
                        )
                        System.arraycopy(data, RfidTag.ACCESS_BITS_POSITION, block, RfidTag.ACCESS_BITS_POSITION,
                                RfidTag.DEFAULT_ACCESS_BYTES.length);
                    break;
            }
        }
        // Write to a data block
        else {
            switch (keyType) {
                case A:
                    // Key A can write only on this condition
                    if (hasPerm(perms, 0, 0, 0)) // 0 0 0
                        block = data;
                    break;
                case B:
                    // If key B can be read, it cannot be used for operations
                    if (isKeyBReadable(rfidTag, blockIndex))
                        return;
                    // Key B can write on these conditions
                    if (
                            hasPerm(perms, 0, 0, 0) || // 0 0 0
                            hasPerm(perms, 1, 0, 0) || // 1 0 0
                            hasPerm(perms, 1, 1, 0) || // 1 1 0
                            hasPerm(perms, 0, 1, 1) // 0 1 1
                        )
                        block = data;
                    break;
            }
        }
        // Perform the actual write. Like the standard MIFARE Classic format, writing the incorrect data can
        // lock the sector permanently
        rfidTag.setBlock(block, blockIndex);
    }
}
