package com.austinv11.peripheralsplusplus.utils;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import li.cil.oc.api.API;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;

public class OpenComputersUtil {
    public static final String EVENT = "computer.signal";

    /**
     * Generate a new component node with network visibility
     * @param host node host
     * @param name component name
     * @return node
     */
    @Nullable
    public static Node createNode(TileEntity host, String name) {
        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS_CORE) && API.network != null && host instanceof Environment)
            return API.network.newNode((Environment)host, Visibility.Network)
                    .withComponent(name)
                    .create();
        return null;
    }

    /**
     * Perform a generic node update that will attempt to join or create a network
     * @param tileEntity tile entity of node
     * @param node node to create/join network
     */
    public static void updateNode(TileEntity tileEntity, @Nullable Node node) {
        if (node != null && node.network() == null)
            API.network.joinOrCreateNetwork(tileEntity);
    }

    /**
     * Safe call to @see Node#remove()
     * @param node node to remove
     */
    public static void removeNode(@Nullable Node node) {
        if (node != null)
            node.remove();
    }

    /**
     * Read node data from nbt
     * @param compound nbt compound containing node data
     * @param node node to load into
     */
    public static void readFromNbt(NBTTagCompound compound, @Nullable Node node) {
        if (node != null && compound.hasKey("oc_node"))
            node.load(compound.getCompoundTag("oc_node"));
    }

    /**
     * Write node to compound
     * @param compound compound to write into
     * @param node node to write
     */
    public static void writeToNbt(NBTTagCompound compound, @Nullable Node node) {
        NBTTagCompound nodeTag = new NBTTagCompound();
        if (node != null)
            node.save(nodeTag);
        compound.setTag("oc_node", nodeTag);
    }

    /**
     * Attempt to send and event to all reachable nodes
     * @param node node to send event from
     * @param eventName name of event
     * @param eventData event data
     */
    public static void sendToReachable(@Nullable Node node, String eventName, Object... eventData) {
        if (node == null)
            return;
        Object[] ocEvent = new Object[eventData.length + 1];
        ocEvent[0] = eventName;
        System.arraycopy(eventData, 0, ocEvent, 1, eventData.length);
        node.sendToReachable(OpenComputersUtil.EVENT, ocEvent);
    }
}
