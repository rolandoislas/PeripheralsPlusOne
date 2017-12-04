package com.austinv11.peripheralsplusplus.capabilities.rfid;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Capability interface for entities that contain an embedded RFID chip
 */
public interface RfidTagHolder {

    /**
     * Sets the prodded state of the entity.
     * @param prodded has the entity been prodded
     */
    void setProdded(boolean prodded);

    /**
     * Gets the prodded state of the entity.
     * @return prodded state
     */
    boolean hasBeenProdded();

    /**
     * Get the tag attached to the entity
     * @return rfid rag
     */
    @Nonnull
    ItemStack getTag();

    /**
     * Set the tag for an entity
     * @param tag tag to set
     */
    void setTag(@Nonnull ItemStack tag);
}
