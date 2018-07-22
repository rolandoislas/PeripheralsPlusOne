package com.austinv11.peripheralsplusplus.tiles;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.peripheralsplusplus.init.ModBlocks;
import com.austinv11.peripheralsplusplus.reference.Config;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

@Optional.InterfaceList(value = {
		@Optional.Interface(
				modid="appliedenergistics2",
				iface="appeng.api.networking.security.IActionHost",
				striprefs=true),
		@Optional.Interface(
				modid="appliedenergistics2",
				iface="appeng.api.networking.IGridBlock",
				striprefs=true),
		@Optional.Interface(
				modid="appliedenergistics2",
				iface="appeng.api.networking.IGridHost",
				striprefs=true),
		@Optional.Interface(
				modid="appliedenergistics2",
				iface="appeng.api.networking.security.IActionSource",
				striprefs=true)
})
public class TileEntityMEBridge extends TileEntity implements IActionHost, IGridBlock, ITickable, IActionSource,
		IGridHost, IPlusPlusPeripheral, OpenComputersPeripheral {
	private HashMap<IComputerAccess, Boolean> computers = new HashMap<>();
	private IGridNode node;
	private boolean initialized = false;
	private EntityPlayer placed;
	private Node nodeOc;

	public TileEntityMEBridge() {
		super();
		nodeOc = OpenComputersUtil.createNode(this, getType());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (world == null || world.isRemote)
			return;
		if (node != null)
			node.destroy();
		node = AEApi.instance().grid().createGridNode(this);
		node.loadFromNBT("node", nbttagcompound);
		initialized = false;
		OpenComputersUtil.readFromNbt(nbttagcompound, nodeOc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (world == null || world.isRemote)
			return nbttagcompound;
		if (node != null)
			node.saveToNBT("node", nbttagcompound);
		OpenComputersUtil.writeToNbt(nbttagcompound, nodeOc);
		return nbttagcompound;
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			if (!initialized) {
				node = AEApi.instance().grid().createGridNode(this);
				if (placed != null)
					node.setPlayerID(AEApi.instance().registries().players().getID(placed));
				node.updateState();
				initialized = true;
			}
			OpenComputersUtil.updateNode(this, nodeOc);
		}
	}

	@Override
	public String getType() {
		return "meBridge";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"listAll", "listItems", "listCraft", "retrieve", "craft", "export", "import"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		if (!Config.enableMEBridge)
			throw new LuaException("ME Bridges have been disabled");
		if (!Loader.isModLoaded(ModIds.APPLIED_ENGERGISTICS))
			throw new LuaException("Applied Energistics 2 is not installed");
		IMEMonitor<IAEItemStack> grid = ((IStorageGrid)node.getGrid().getCache(IStorageGrid.class))
				.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
		switch (method) {
			case 0:
				return new Object[]{iteratorToMap(grid.getStorageList().iterator(), 0)};
			case 1:
				return new Object[]{iteratorToMap(grid.getStorageList().iterator(), 1)};
			case 2:
				return new Object[]{iteratorToMap(grid.getStorageList().iterator(), 2)};
			case 3:
			case 5:
				return importOrExportItem(arguments, grid, false);
			case 4:
				return craft(arguments, grid);
			case 6:
				return importOrExportItem(arguments, grid, true);
		}
		return new Object[0];
	}

	private Object[] craft(Object[] arguments, IMEMonitor<IAEItemStack> monitor) throws LuaException {
		if (arguments.length < 3)
			throw new LuaException("Too few arguments");
		if (!(arguments[0] instanceof String))
			throw new LuaException("Bad argument #1: name should be a string");
		if (!(arguments[1] instanceof Double))
			throw new LuaException("Bad argument #2: Meta should be an number");
		if (!(arguments[2] instanceof Double))
			throw new LuaException("Bad argument #3: amount should be a number");
		if (arguments.length > 3 && !(arguments[3] instanceof String))
			throw new LuaException("Bad argument #4: amount should be a string");
		String itemName = (String) arguments[0];
		int meta = (int) (double) arguments[1];
		long amount = (long) (double) arguments[2];
		String nbtString = arguments.length > 3 ? (String) arguments[3] : "";
		ItemStack toCraft = GameRegistry.makeItemStack(itemName, meta, 1, nbtString);
		if (toCraft.isEmpty())
			throw new LuaException("Failed to find item");
		IAEItemStack aeToCraft = findAEStackFromItemStack(monitor, toCraft);
		if (aeToCraft == null)
			throw new LuaException("Failed to find item in AE system");
		if (!aeToCraft.isCraftable())
			throw new LuaException("AE system cannot craft item");
		aeToCraft = aeToCraft.copy();
		aeToCraft.setStackSize(amount);
		synchronized (this) {
			ICraftingGrid craftingGrid = node.getGrid().getCache(ICraftingGrid.class);
			craftingGrid.beginCraftingJob(world, node.getGrid(), this, aeToCraft, job -> {
				ICraftingLink result = craftingGrid.submitJob(job, null, null, false,
						TileEntityMEBridge.this);
				ResourceLocation itemName1 = ForgeRegistries.ITEMS.getKey(job.getOutput().getItem());
				Object[] event = new Object[]{
						itemName1 == null ? "null" : itemName1.toString(),
						job.getOutput().getStackSize(),
						job.getByteTotal(),
						result != null
				};
                for (IComputerAccess comp : computers.keySet())
                    comp.queueEvent("craftingComplete", event);
				OpenComputersUtil.sendToReachable(nodeOc, "craftingComplete", event);
            });
		}
		return new Object[]{};
	}

	/**
	 * Import an item from an inventory to the ae2 system
	 * @param arguments lua args array
	 * @param monitor ae2 monitor
	 * @param doImport should the item be imported, otherwise it is exported
	 * @return amount of items imported
	 */
	private Object[] importOrExportItem(Object[] arguments, IMEMonitor<IAEItemStack> monitor, boolean doImport)
			throws LuaException {
		if (arguments.length < 4)
			throw new LuaException("Too few arguments");
		if (!(arguments[0] instanceof String))
			throw new LuaException("Bad argument #1: name should be a string");
		if (!(arguments[1] instanceof Double))
			throw new LuaException("Bad argument #2: Meta should be an number");
		if (!(arguments[2] instanceof Double))
			throw new LuaException("Bad argument #3: amount should be a number");
		if (!(arguments[3] instanceof String) && !(arguments[3] instanceof Double))
			throw new LuaException("Bad argument #4: direction should be a string or number");
		if (arguments.length > 4 && !(arguments[4] instanceof String))
			throw new LuaException("Bad argument #5: nbt should be a string");
		String itemName = (String) arguments[0];
		int meta = (int) (double) arguments[1];
		long amount = (long) (double) arguments[2];
		EnumFacing direction;
		if (arguments[3] instanceof String)
			direction = EnumFacing.valueOf(String.valueOf(arguments[3]).toUpperCase(Locale.US));
		else
			direction = EnumFacing.getFront((int) (double) arguments[3]);
		String nbtString = arguments.length > 4 ? (String) arguments[4] : "";
		// Check inventory to output to
		IInventory inventory = TileEntityInteractiveSorter.getInventoryForSide(world, getPos(), direction);
		if (inventory == null)
			throw new LuaException("Block is not a valid inventory");
		// Check item is valid
		ItemStack item = GameRegistry.makeItemStack(itemName, meta, 1, nbtString);
		if (item.isEmpty())
			throw new LuaException("Item not found");

		if (doImport) {
			if (importItems(amount, inventory, item, monitor, true) > 0) {
				return new Object[]{
						importItems(amount, inventory, item, monitor, false)
				};
			}
			else
				return new Object[]{0};
		}

		// Export
		long extracted = 0;
		IAEItemStack stack = findAEStackFromItemStack(monitor, item);
		if (stack != null) {
			if (amount > stack.getStackSize())
				amount = stack.getStackSize();
			if (amount > getRemainingSlots(item.getItem(), inventory))
				amount = getRemainingSlots(item.getItem(), inventory);
			IAEItemStack stackToGet = stack.copy();
			stackToGet.setStackSize(amount);
			IAEItemStack resultant = monitor.extractItems(stackToGet, Actionable.MODULATE, this);
			if (resultant != null) {
				extracted = resultant.getStackSize();
				int[] slots = inventory instanceof ISidedInventory ?
						((ISidedInventory) inventory).getSlotsForFace(direction.getOpposite()) :
						getDefaultSlots(inventory);
				int currentSlot = 0;
				ItemStack itemStack = resultant.createItemStack();
				itemStack.setCount(1);
				while (!(resultant.getStackSize() < 1) && currentSlot < slots.length) {
					if (inventory.isItemValidForSlot(slots[currentSlot], itemStack.copy())) {
						if (inventory.getStackInSlot(slots[currentSlot]).isEmpty()) {
							ItemStack toAdd = itemStack.copy();
							inventory.setInventorySlotContents(slots[currentSlot], toAdd);
							resultant.setStackSize(resultant.getStackSize() - 1);
						} else {
							ItemStack current = inventory.getStackInSlot(slots[currentSlot]);
							ItemStack toAdd = itemStack.copy();
							if (ItemStack.areItemsEqual(current, toAdd) &&
									ItemStack.areItemStackTagsEqual(current, toAdd) &&
									toAdd.getCount() + current.getCount() <= Math.min(current.getMaxStackSize(),
											inventory.getInventoryStackLimit())) {
								current.setCount(current.getCount() + 1);
								inventory.setInventorySlotContents(slots[currentSlot], current);
								resultant.setStackSize(resultant.getStackSize() - 1);
							}
							else
								currentSlot++;
						}
						inventory.markDirty();
					}
					else
						currentSlot++;
				}
			}
		}
		return new Object[]{extracted};
	}

	/**
	 * Import items from an inventory to an AE2 system
	 * @param amount amount of items to import
	 * @param inventory the inventory to import from
	 * @param item the item to import
	 * @param monitor the ae2 system import to
	 * @param simulate should this be simulated
	 * @throws LuaException error
	 * @return amount of items imported
	 */
	private long importItems(long amount, IInventory inventory, ItemStack item, IMEMonitor<IAEItemStack> monitor,
								boolean simulate) throws LuaException {
		long importAmount = amount;
		for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
			if (importAmount <= 0)
				break;
			ItemStack potentialImport = inventory.getStackInSlot(slotIndex);
			if (ItemStack.areItemsEqual(potentialImport, item) &&
					ItemStack.areItemStackTagsEqual(potentialImport, item)) {
				if (!simulate) {
					ItemStack imported = inventory.decrStackSize(slotIndex, importAmount > Integer.MAX_VALUE ?
							Integer.MAX_VALUE : (int) importAmount);
					if (!imported.isEmpty())
						importAmount -= imported.getCount();
				}
				else {
					importAmount -= Math.min(importAmount, potentialImport.getCount());
				}
			}
		}
		long importedAmount = amount - importAmount;
		IAEItemStack aeStack = monitor.getChannel().createStack(item);
		if (aeStack == null)
			throw new LuaException("Failed to create itemstack in AE system");
		aeStack.setStackSize(importedAmount);
		IAEItemStack notInjected = monitor.injectItems(aeStack, simulate ? Actionable.SIMULATE : Actionable.MODULATE,
				this);
		if (notInjected != null)
			throw new LuaException("Not enough space in AE system");
		return importedAmount;
	}

	private int[] getDefaultSlots(IInventory inventory) {
		int[] array = new int[inventory.getSizeInventory()];
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			array[i] = i;
		return array;
	}

	private int getRemainingSlots(Item item, IInventory inventory) {
		int slots = 0;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			if (inventory.isItemValidForSlot(i, new ItemStack(item)) && inventory.getStackInSlot(i).isEmpty())
				slots += inventory.getInventoryStackLimit();
			else if (inventory.isItemValidForSlot(i, new ItemStack(item)) &&
					inventory.getStackInSlot(i).getItem() == item &&
					(inventory.getInventoryStackLimit() >= inventory.getStackInSlot(i).getCount()))
				slots += inventory.getInventoryStackLimit() - inventory.getStackInSlot(i).getCount();
		}
		return slots;
	}

	@Override
	public void attach(IComputerAccess computer) {
		computers.put(computer, true);
	}

	@Override
	public void detach(IComputerAccess computer) {
		computers.remove(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		return (this == other);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (node != null) {
			node.destroy();
			node = null;
			initialized = false;
		}
		OpenComputersUtil.removeNode(nodeOc);
	}

	private IAEItemStack findAEStackFromItemStack(IMEMonitor<IAEItemStack> monitor, ItemStack item) {
		IAEItemStack stack = null;
		for (IAEItemStack temp : monitor.getStorageList()) {
			if (temp.isSameType(item)) {
				stack = temp;
				break;
			}
		}
		return stack;
	}

	private HashMap<Integer, Object> iteratorToMap(Iterator<IAEItemStack> iterator, int flag) {
		HashMap<Integer,Object> map = new HashMap<Integer,Object>();
		int i = 1;
		while (iterator.hasNext()) {
			Object o = getObjectFromStack(iterator.next(), flag);
			if (o != null)
				map.put(i++, o);
		}
		return map;
	}

	private Object getObjectFromStack(IAEItemStack stack, int flag) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ResourceLocation itemResourceLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
		String itemName = itemResourceLocation == null ? "null" : itemResourceLocation.toString();
		int meta = stack.getItemDamage();
		long amount = stack.getStackSize();
		String displayName = new ItemStack(stack.getItem()).getDisplayName();
		NBTTagCompound nbt = stack.createItemStack().getTagCompound();
		map.put("name", itemName);
		map.put("meta", meta);
		map.put("amount", amount);
		map.put("displayName", displayName);
		map.put("nbt", nbt == null ? "" : nbt.toString());
		if (flag == 0) {
			return map;
		} else if (flag == 1) {
			if (stack.getStackSize() > 0)
				return map;
		} else if (flag == 2) {
			if (stack.isCraftable())
				return map;
		}
		return null;
	}

	@Override
	public double getIdlePowerUsage() {
		return 1;
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
	}
	
	
	@Override
	public boolean isWorldAccessible() {
		return true;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
	}

	@Override
	public AEColor getGridColor() {
		return AEColor.TRANSPARENT;
	}

	@Override
	public void onGridNotification(GridNotification notification) {
		for (IComputerAccess computer : computers.keySet())
			computer.queueEvent("gridNotification", new Object[]{notification.toString()});
		OpenComputersUtil.sendToReachable(nodeOc, "gridNotification", notification.toString());
	}

	@Override
	public void setNetworkStatus(IGrid grid, int channelsInUse) {
	}

	@Override
	public EnumSet<EnumFacing> getConnectableSides() {
		return EnumSet.allOf(EnumFacing.class);
	}

	@Override
	public IGridHost getMachine() {
		return this;
	}

	@Override
	public void gridChanged() {
		for (IComputerAccess computer : computers.keySet())
			computer.queueEvent("gridChanged", new Object[0]);
		OpenComputersUtil.sendToReachable(nodeOc, "gridChanged");
	}

	@Override
	public ItemStack getMachineRepresentation() {
		return new ItemStack(ModBlocks.ME_BRIDGE);
	}

	@Override
	public IGridNode getActionableNode() {
		return node;
	}

	@Nonnull
	@Override
	public java.util.Optional<EntityPlayer> player() {
		return java.util.Optional.empty();
	}

	@Nonnull
	@Override
	public java.util.Optional<IActionHost> machine() {
		return java.util.Optional.of(this);
	}

	@Nonnull
	@Override
	public <T> java.util.Optional<T> context(@Nonnull Class<T> key) {
		return java.util.Optional.empty();
	}

	public void setPlayer(EntityPlayer player) {
		placed = player;
	}

	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		return node;
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.COVERED;
	}

	@Override
	public void securityBreak() {
		for (IComputerAccess computer : computers.keySet())
			computer.queueEvent("securityBreak", new Object[0]);
		OpenComputersUtil.sendToReachable(nodeOc, "securityBreak");
		world.setBlockToAir(getPos());
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
			case "listAll":
				return callMethod(null, null, 0, args.toArray());
			case "listItems":
				return callMethod(null, null, 1, args.toArray());
			case "listCraft":
				return callMethod(null, null, 2, args.toArray());
			case "retrieve":
				return callMethod(null, null, 3, args.toArray());
			case "craft":
				return callMethod(null, null, 4, args.toArray());
		}
		throw new NoSuchMethodException(method);
	}

	@Override
	@Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
	public Node node() {
		return nodeOc;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		OpenComputersUtil.removeNode(nodeOc);
	}
}
