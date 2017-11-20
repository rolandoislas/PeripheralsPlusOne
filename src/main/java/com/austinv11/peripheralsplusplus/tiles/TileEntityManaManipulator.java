package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.collectiveframework.minecraft.tiles.TileEntityInventory;
import com.austinv11.collectiveframework.utils.math.ThreeDimensionalVector;
import com.austinv11.peripheralsplusplus.init.ModBlocks;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.wand.IWandBindable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

@Optional.InterfaceList(value = {
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.mana.IManaPool"),
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.mana.IManaSpreader"),
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.mana.IManaCollector"),
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.mana.spark.ISparkAttachable"),
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.mana.IThrottledPacket"),
        @Optional.Interface(
                modid="botania",
                iface="vazkii.botania.api.wand.IWandBindable")
})
public class TileEntityManaManipulator extends TileEntityInventory implements IPlusPlusPeripheral, ITickable, IManaPool,
        IManaSpreader, IManaCollector, ISparkAttachable, IThrottledPacket, IWandBindable {
    private static final int MAX_MANA = 100000;
    private static final int EMPTY_LENS = -1;
    private boolean firstUpdate = true;
    private long lastAutoBurstTime;
    private IManaReceiver reciever;

    private EnumDyeColor manaPoolColor;
    private int manaHeld;
    private boolean canShootManaBurst;
    private int manaBurstParticleTick;
    private int manaBurstTicksExisted;
    private float rotationX;
    private float rotationY;
    private boolean autoBurst;
    private boolean voidExcessMana;
    private UUID attachedSpark;
    private UUID identity;
    private Color burstColor;
    private int lensSlot;

    public TileEntityManaManipulator() {
        firstUpdate = true;
        identity = UUID.randomUUID();
        lastAutoBurstTime = System.currentTimeMillis();
        manaPoolColor = EnumDyeColor.BLACK;
        burstColor = new Color(
                (int) Math.floor(255 * Math.random()),
                (int) Math.floor(255 *Math.random()),
                (int) Math.floor(255 *Math.random())
        );
        invName = Reference.MOD_ID + ":tile_entity_mana_manipulator";
        canShootManaBurst = true;
        lensSlot = EMPTY_LENS;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(ModBlocks.MANA_MANIPULATOR.getUnlocalizedName() + ".name");
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack.getItem() instanceof ILens;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ManaNetworkEvent.removePool(this);
        ManaNetworkEvent.removeCollector(this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        ManaNetworkEvent.removePool(this);
        ManaNetworkEvent.removeCollector(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        manaPoolColor = EnumDyeColor.values()[compound.getInteger("manaPoolColor")];
        manaHeld = compound.getInteger("manaHeld");
        canShootManaBurst = compound.getBoolean("canShootManaBurst");
        manaBurstParticleTick = compound.getInteger("manaBurstParticleTick");
        manaBurstTicksExisted = compound.getInteger("manaBurstTicksExisted");
        rotationX = compound.getFloat("rotationX");
        rotationY = compound.getFloat("rotationY");
        autoBurst = compound.getBoolean("autoBurst");
        voidExcessMana = compound.getBoolean("voidExcessMana");
        attachedSpark = compound.getUniqueId("attachedSpark");
        identity = compound.getUniqueId("identity");
        burstColor = new Color(compound.getInteger("burstColor"));
        lensSlot = compound.getInteger("lens");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("manaPoolColor", manaPoolColor.ordinal());
        compound.setInteger("manaHeld", manaHeld);
        compound.setBoolean("canShootManaBurst", canShootManaBurst);
        compound.setInteger("manaBurstParticleTick", manaBurstParticleTick);
        compound.setInteger("manaBurstTicksExisted", manaBurstTicksExisted);
        compound.setFloat("rotationX", rotationX);
        compound.setFloat("rotationY", rotationY);
        compound.setBoolean("autoBurst", autoBurst);
        compound.setBoolean("voidExcessMana", voidExcessMana);
        if (attachedSpark != null)
            compound.setUniqueId("attachedSpark", attachedSpark);
        compound.setUniqueId("identity", identity);
        compound.setInteger("burstColor", burstColor.getRGB());
        compound.setInteger("lens", lensSlot);
        return compound;
    }

    private boolean tryShootBurst() {
        if (world.isRemote)
            return true;
        if (System.currentTimeMillis() - lastAutoBurstTime >= 100 && reciever != null &&
                reciever.canRecieveManaFromBursts() && !reciever.isFull()) {
            IManaBurst burst = getBurst(false);
            if (burst != null) {
                recieveMana(-burst.getStartingMana());
                burst.setShooterUUID(getIdentifier());
                getWorld().spawnEntity((Entity) burst);
                burst.ping();
                world.playSound(
                        null,
                        pos,
                        new SoundEvent(new ResourceLocation("botania", "spreaderfire")),
                        SoundCategory.BLOCKS,
                        0.05f,
                        (float) (0.7 + 0.3 * Math.random()));
                return true;
            }
            lastAutoBurstTime = System.currentTimeMillis();
        }
        return false;
    }

    private IManaBurst getBurst(boolean fake) {
        IManaBurst burst = getBotaniaBurstEntity(fake);
        BurstProperties properties = new BurstProperties(
                200,
                100,
                4,
                0,
                1,
                burstColor.getRGB()
        );
        if (burst != null) {
            ItemStack lens = getLens();
            if (!lens.isEmpty() && lens.getItem() instanceof ILensEffect)
                ((ILensEffect)lens.getItem()).apply(lens, properties);
            burst.setSourceLens(lens);
        }
        if (burst != null && (getCurrentMana() >= 200 || fake)) {
            burst.setColor(properties.color);
            burst.setMana(properties.maxMana);
            burst.setStartingMana(properties.maxMana);
            burst.setMinManaLoss(properties.ticksBeforeManaLoss);
            burst.setManaLossPerTick(properties.manaLossPerTick);
            burst.setGravity(properties.gravity);
            return burst;
        }
        return null;
    }

    @Nullable
    private IManaBurst getBotaniaBurstEntity(boolean fake) {
        Constructor botaniaBurst;
        try {
            botaniaBurst = Class.forName("vazkii.botania.common.entity.EntityManaBurst")
                    .getDeclaredConstructor(IManaSpreader.class, boolean.class);
            return (IManaBurst) botaniaBurst.newInstance(this, fake);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException |
                InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateReceiver();
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
    }

    private void updateReceiver() {
        ItemStack lens = getLens();
        ILensControl control = getLensControl(lens);
        if (control != null && !control.allowBurstShooting(lens, this, false))
            return;
        IManaBurst simulation = getBurst(true);
        TileEntity receiver = getCollidedTile(simulation);
        if (receiver instanceof IManaReceiver && receiver.hasWorld() &&
                receiver.getWorld().isBlockLoaded(receiver.getPos(), !receiver.getWorld().isRemote))
            this.reciever = (IManaReceiver) receiver;
        else
            this.reciever = null;
    }

    @Nullable
    private TileEntity getCollidedTile(IManaBurst burst) {
        try {
            Method setScanBeam = burst.getClass().getDeclaredMethod("setScanBeam");
            setScanBeam.invoke(burst);
            Method getCollidedTile = burst.getClass().getDeclaredMethod("getCollidedTile", boolean.class);
            return (TileEntity) getCollidedTile.invoke(burst, true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nonnull
    private ItemStack getLens() {
        if (lensSlot <= EMPTY_LENS || lensSlot >= items.size())
            return ItemStack.EMPTY;
        ItemStack itemStack = getStackInSlot(lensSlot);
        if (itemStack.isEmpty())
            lensSlot = EMPTY_LENS;
        return itemStack;
    }

    @Nullable
    private ILensControl getLensControl(ItemStack lens) {
        if (!lens.isEmpty() && lens.getItem() instanceof ILensControl) {
            ILensControl control = (ILensControl) lens.getItem();
            if (control.isControlLens(lens))
                return control;
        }
        return null;
    }

    private int getFirstMatchingLensIndex(ItemStack lens) {
        for (ItemStack itemStack : items)
            if (itemStack.isItemEqual(lens))
                return items.indexOf(itemStack);
        return EMPTY_LENS;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new SPacketUpdateTileEntity(pos, -999, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
    }

    private double getAngle(ThreeDimensionalVector first, ThreeDimensionalVector other) {
        return Math.acos(normalize(first).dotProduct(normalize(other)));
    }

    private ThreeDimensionalVector normalize(ThreeDimensionalVector vector) {
        double mag = Math.sqrt(
                vector.getX() * vector.getX() +
                        vector.getY() * vector.getY() +
                        vector.getZ() * vector.getZ()
        );
        if (mag != 0) {
            double multiplier = 1 / mag;
            return new ThreeDimensionalVector(
                    vector.getX() * multiplier,
                    vector.getY() * multiplier,
                    vector.getZ() * multiplier
            );
        }
        return vector;
    }

    /*---------- ITickable ----------*/

    @Override
    public void update() {
        // Handle registration
        if (firstUpdate) {
            ManaNetworkEvent.addPool(this);
            ManaNetworkEvent.addCollector(this);
            updateReceiver();
            firstUpdate = false;
        }
        updateReceiver();
        ItemStack lens = getLens();
        ILensControl control = getLensControl(lens);
        boolean canShoot = true;
        if (control != null && autoBurst) {
            control.onControlledSpreaderTick(lens, this, false);
            canShoot = control.allowBurstShooting(lens, this, false);
        }
        if (autoBurst && canShoot)
            tryShootBurst();
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
    }

    /*---------- IPlusPlusPeripheral ----------*/

    @Nonnull
    @Override
    public String getType() {
        return "mana_tank";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[] {
                "getMana", // ([boolean get_actual_value]) int amount
                "sendBurst", // (void) boolean success
                "setBurstDirection", // (int x_degrees, int y_degrees) void
                "getBurstDirection",  // (void) string direction
                "setAutoBurst", // (boolean do_burst) void
                "getAutoBurst",  // (void) boolean
                "setVoidExcessMana", // (boolean vent) void
                "getVoidExcessMana", // (void) boolean vent
                "setColor", // (int r, int g, int b) void
                "getColor", // (void) {int r, int g, int b}
                "setLens", // (string resource_location, int meta) void
                           // (int slot_index) void
                "getLens", // (void) {string resource_location, int meta}
                "getContainedLenses" // (void) list of contained lenses
        };
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method,
                               @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0:
                return getMana(arguments);
            case 1:
                return sendBurst();
            case 2:
                return setBurstDirection(arguments);
            case 3:
                return getBurstDirection();
            case 4:
                return setAutoBurst(arguments);
            case 5:
                return getAutoBurst();
            case 6:
                return setVoidExcessMana(arguments);
            case 7:
                return getVoidExcessMana();
            case 8:
                return setColorLua(arguments);
            case 9:
                return getColorLua();
            case 10:
                return setLensLua(arguments);
            case 11:
                return getLensLua();
            case 12:
                return getContainedLensesLua();
        }
        throw new LuaException("Unexpected error.");
    }

    private Object[] getContainedLensesLua() {
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        int lensIndex = 1;
        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty())
                continue;
            Map<String, Object> nestedMap = new HashMap<>();
            ResourceLocation regName = itemStack.getItem().getRegistryName();
            nestedMap.put("id", regName == null ? "null" : regName.toString());
            nestedMap.put("meta", itemStack.getMetadata());
            nestedMap.put("name", itemStack.getDisplayName());
            nestedMap.put("slot", items.indexOf(itemStack));
            nestedMap.put("nbt", itemStack.getTagCompound() == null ? null : itemStack.getTagCompound().toString());
            map.put(lensIndex++, nestedMap);
        }
        return new Object[]{map};
    }

    private Object[] getLensLua() {
        ItemStack lens = getLens();
        if (lens.isEmpty())
            return new Object[0];
        Map<String, Object> map = new HashMap<>();
        ResourceLocation regName = lens.getItem().getRegistryName();
        map.put("id", regName == null ? "null" : regName.toString());
        map.put("meta", lens.getMetadata());
        map.put("name", lens.getDisplayName());
        map.put("slot", lensSlot);
        map.put("nbt", lens.getTagCompound() == null ? null : lens.getTagCompound().toString());
        return new Object[]{map};
    }

    private Object[] setLensLua(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        // Assume the parameter is a slot index
        if (arguments.length < 2) {
            if (!(arguments[0] instanceof Double))
                throw new LuaException("Argument 1 should be an integer");
            int slotIndex = ((Double) arguments[0]).intValue();
            ItemStack itemStack = getStackInSlot(slotIndex);
            if (itemStack.isEmpty())
                throw new LuaException("Slot is empty");
            lensSlot = slotIndex;
        }
        // Assume the parameters are a resource location string and metadata integer
        else {
            if (!(arguments[0] instanceof String))
                throw new LuaException("Argument 1 should be a string");
            int meta;
            if (!(arguments[1] instanceof Double))
                throw new LuaException("Argument 2 should be an integer");
            else
                meta = ((Double) arguments[1]).intValue();
            ItemStack itemStack = GameRegistry.makeItemStack((String) arguments[0], meta, 1, "");
            int slotIndex = getFirstMatchingLensIndex(itemStack);
            if (slotIndex <= EMPTY_LENS)
                throw new LuaException("Lens not found in inventory");
            lensSlot = slotIndex;
        }
        return new Object[0];
    }

    private Object[] getColorLua() {
        Map<String, Integer> map = new HashMap<>();
        map.put("r", burstColor.getRed());
        map.put("g", burstColor.getGreen());
        map.put("b", burstColor.getBlue());
        return new Object[]{map};
    }

    private Object[] setColorLua(Object[] arguments) throws LuaException {
        if (arguments.length < 3)
            throw new LuaException("Not enough arguments");
        for (Object arg : arguments)
            if (!(arg instanceof Double))
                throw new LuaException(String.format("Argument %d is expected to be a number",
                        Arrays.asList(arguments).indexOf(arg)));
        burstColor = new Color(
                ((Double)arguments[0]).intValue(),
                ((Double)arguments[1]).intValue(),
                ((Double)arguments[2]).intValue()
        );
        return new Object[0];
    }

    private Object[] getVoidExcessMana() {
        return new Object[]{voidExcessMana};
    }

    private Object[] setVoidExcessMana(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Boolean))
            throw new LuaException("Argument 1 expected to be boolean");
        voidExcessMana = (boolean) arguments[0];
        return new Object[0];
    }

    private Object[] getAutoBurst() {
        return new Object[]{autoBurst};
    }

    private Object[] getBurstDirection() {
        Map<String, Float> map = new HashMap<>();
        map.put("x", rotationX);
        map.put("y", rotationY);
        return new Object[]{map};
    }

    private Object[] setAutoBurst(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Boolean))
            throw new LuaException("Argument 1 expected to be boolean");
        this.autoBurst = (boolean) arguments[0];
        return new Object[0];
    }

    private Object[] setBurstDirection(Object[] arguments) throws LuaException {
        if (arguments.length < 2)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof Double))
            throw new LuaException("Argument 1 expected to be a number");
        if (!(arguments[1] instanceof Double))
            throw new LuaException("Argument 2 expected to be an number");
        rotationX = ((Double) arguments[0]).floatValue();
        rotationY = ((Double) arguments[1]).floatValue();
        commitRedirection();
        return new Object[0];
    }

    private Object[] sendBurst() {
        ItemStack lens = getLens();
        ILensControl control = getLensControl(lens);
        boolean canShoot = true;
        if (control != null) {
            control.onControlledSpreaderTick(lens, this, false);
            canShoot = control.allowBurstShooting(lens, this, false);
        }
        boolean result = false;
        if (canShoot)
            result = tryShootBurst();
        return new Object[]{result};
    }

    private Object[] getMana(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            arguments = new Object[]{false};
        if (!(arguments[0] instanceof Boolean))
            throw new LuaException("Argument 1 is expected to be boolean");
        return new Object[]{((boolean) arguments[0]) ? manaHeld : world.rand.nextInt(manaHeld + 1)};
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    /*---------- IManaPool  ----------*/

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public EnumDyeColor getColor() {
        return manaPoolColor;
    }

    @Override
    public void setColor(EnumDyeColor color) {
        manaPoolColor = color;
    }

    @Override
    public boolean isFull() {
        return manaHeld >= MAX_MANA;
    }

    @Override
    public void recieveMana(int mana) {
        manaHeld += mana;
        manaHeld = Math.min(manaHeld, MAX_MANA);
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return voidExcessMana || !isFull();
    }

    @Override
    public int getCurrentMana() {
        return manaHeld;
    }

    /*---------- IManaSpreader  ----------*/

    @Override
    public void setCanShoot(boolean canShoot) {
        this.canShootManaBurst = canShoot;
    }

    @Override
    public int getBurstParticleTick() {
        return manaBurstParticleTick;
    }

    @Override
    public void setBurstParticleTick(int tick) {
        this.manaBurstParticleTick = tick;
    }

    @Override
    public int getLastBurstDeathTick() {
        return manaBurstTicksExisted;
    }

    @Override
    public void setLastBurstDeathTick(int ticksExisted) {
        this.manaBurstTicksExisted = ticksExisted;
    }

    @Override
    public IManaBurst runBurstSimulation() {
        IManaBurst burst = getBurst(true);
        if (burst != null)
            getCollidedTile(burst);
        return burst;
    }

    @Override
    public float getRotationX() {
        return rotationX;
    }

    @Override
    public float getRotationY() {
        return rotationY;
    }

    @Override
    public void setRotationX(float rot) {
        this.rotationX = rot;
    }

    @Override
    public void setRotationY(float rot) {
        this.rotationY = rot;
    }

    @Override
    public void commitRedirection() {
        updateReceiver();
    }

    @Override
    public void pingback(IManaBurst burst, UUID expectedIdentity) {
        lastAutoBurstTime = System.currentTimeMillis();
    }

    @Override
    public UUID getIdentifier() {
        return identity;
    }

    /*---------- IManaCollector  ----------*/

    @Override
    public void onClientDisplayTick() {

    }

    @Override
    public float getManaYieldMultiplier(IManaBurst burst) {
        return 0.9f;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    /*---------- ISparkAttachable ----------*/

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return getAttachedSpark() == null;
    }

    @Override
    public void attachSpark(ISparkEntity entity) {
        if (entity instanceof Entity)
            this.attachedSpark = ((Entity) entity).getPersistentID();
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(Math.abs(MAX_MANA - manaHeld), 0);
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        List<Entity> entities = getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos().up()));
        for (Entity entity : entities)
            if (entity instanceof ISparkEntity && entity.getPersistentID().equals(attachedSpark))
                return (ISparkEntity) entity;
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    /*---------- IThrottledPacket ----------*/

    @Override
    public void markDispatchable() {

    }

    /*---------- IWandBindable ----------*/

    @Override
    public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
        Vec3d from = new Vec3d(
                getPos().getX(),
                getPos().getY(),
                getPos().getZ()
        ).addVector(0.5, 0.5, 0.5);
        Vec3d to = new Vec3d(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        ).addVector(0.5, 0.5, 0.5);
        AxisAlignedBB axis = player.world.getBlockState(pos).getCollisionBoundingBox(player.world, pos);
        if(axis != null)
            axis = axis.offset(pos);
        else
            axis = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        if (!axis.contains(to))
            to = new Vec3d(
                    axis.minX + (axis.maxX - axis.minX) / 2,
                    axis.minY + (axis.maxY - axis.minY) / 2,
                    axis.minZ + (axis.maxZ - axis.minZ) / 2
            );
        ThreeDimensionalVector diff = new ThreeDimensionalVector(to.x, to.y, to.z).subtract(
                new ThreeDimensionalVector(from.x, from.y, from.z));
        ThreeDimensionalVector diff2d = new ThreeDimensionalVector(diff.getX(), diff.getZ(), 0);
        ThreeDimensionalVector rotate = new ThreeDimensionalVector(0, 1, 0);
        double angle = getAngle(rotate, diff2d) / Math.PI * 180;
        if (to.x < from.x)
            angle *= -1;
        System.out.println(angle + 90);
        setRotationX((float) (360 - (angle + 90)));
        rotate = new ThreeDimensionalVector(diff.getX(), 0, diff.getZ());
        angle = getAngle(diff, rotate) * 180 / Math.PI;
        if (to.y < from.y)
            angle *= -1;
        setRotationY((float) angle);
        commitRedirection();
        return true;
    }

    @Override
    public BlockPos getBinding() {
        if (reciever != null)
            return ((TileEntity) reciever).getPos();
        return null;
    }
}
