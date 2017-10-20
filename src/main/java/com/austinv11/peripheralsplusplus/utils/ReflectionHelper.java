package com.austinv11.peripheralsplusplus.utils;

import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.utils.proxy.LuaApi;
import com.austinv11.peripheralsplusplus.utils.proxy.PeripheralChangeListener;
import com.austinv11.peripheralsplusplus.utils.proxy.Task;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ReflectionHelper {

	private static ArrayList<Integer> usedIds = new ArrayList<Integer>();

	/**
	 * Get the turtle access from a tile entity
	 * @param te tile entity that contains the turtle access
	 * @return turtle access or null
	 * @throws Exception Turlte could not be obtained via reflection
	 */
	public static ITurtleAccess getTurtle(TileEntity te) throws Exception {
		if (te instanceof ITurtleAccess)
			return (ITurtleAccess) te;
		Class teClass = te.getClass();
		if (teClass.getName().equals("dan200.computercraft.shared.turtle.blocks.TileTurtleExpanded") ||
				teClass.getName().equals("dan200.computercraft.shared.turtle.blocks.TileTurtleAdvanced") ||
				teClass.getName().equals("dan200.computercraft.shared.turtle.blocks.TileTurtle")) {
			Method getAccess = teClass.getMethod("getAccess", new Class[0]);
			ITurtleAccess turtle = (ITurtleAccess)getAccess.invoke(te, new Object[0]);
			if (turtle != null)
				return turtle;
		}
		return null;
	}

	/**
	 * Gets a computer object from the computer id
	 * @param id computer id
	 * @return IComputer or null if not found
	 */
	@Nullable
	public static Object getICompFromId(int id) throws ClassNotFoundException, NoSuchMethodException,
			NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		Class computerRegistry = Class.forName("dan200.computercraft.shared.computer.core.ComputerRegistry");
		Method getCompFromId = computerRegistry.getMethod("lookup", int.class);
		Object registry = Class.forName("dan200.computercraft.ComputerCraft").getField("serverComputerRegistry")
				.get(null);
		return getCompFromId.invoke(registry, id);
	}

	/**
	 * Converts a Java Object to a Lua value
	 * @param o object to convert
	 * @return LuaValue
	 */
	public static Object objectToLuaValue(Object o) throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, NoSuchMethodException, InvocationTargetException {
		Class luaMachine = Class.forName("dan200.computercraft.core.lua.LuaJLuaMachine");
		Object machine = luaMachine.newInstance();
		Method toVal = luaMachine.getDeclaredMethod("toValue", Object.class);
		toVal.setAccessible(true);
		return toVal.invoke(machine, o);
	}

	/**
	 * Gets a Lua machine from computer id
	 * @param id computer id
	 * @return ILuaMachine
	 */
	@Nullable
	public static Object getLuaMachineFromId(int id) throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		Object iComp = getICompFromId(id);
		if (iComp == null)
			return null;
		Class iCompClass = iComp.getClass().asSubclass(
				Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
		Field compField = findField(iCompClass, "m_computer");
		compField.setAccessible(true);
		Object comp = compField.get(iComp);
		Class compClass = comp.getClass();
		Field machine = compClass.getDeclaredField("m_machine");
		machine.setAccessible(true);
		return machine.get(comp);
	}

	/**
	 * Attempt to run a Lua program in the specified computer's context
	 * @param path path of program to run
	 * @param id computer id
	 */
	public static void runProgram(String path, int id) throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, ClassNotFoundException, NoSuchFieldException, InstantiationException {
		Object machineObject = getLuaMachineFromId(id);
		if (machineObject == null)
			return;
		Class machineClass = machineObject.getClass();
		Field globals = machineClass.getDeclaredField("m_globals");
		globals.setAccessible(true);
		Object globalsObj = globals.get(machineObject);
		Method load = globalsObj.getClass().getDeclaredMethod("get", Class.forName("org.luaj.vm2.LuaValue"));
		load.setAccessible(true);
		Object chunk = load.invoke(globalsObj, objectToLuaValue("os"));
		Method get_ = chunk.getClass().getDeclaredMethod("get", Class.forName("org.luaj.vm2.LuaValue"));
		get_.setAccessible(true);
		Object run = get_.invoke(chunk, objectToLuaValue("run"));
		Method call = run.getClass().getDeclaredMethod("call", Class.forName("org.luaj.vm2.LuaValue"),
				Class.forName("org.luaj.vm2.LuaValue"));
		call.invoke(run, Class.forName("org.luaj.vm2.LuaTable").newInstance(), objectToLuaValue(path));
	}

	/**
	 * Register a Lua API on a computer
	 * @param id computer id
	 * @param api lua api implementation
	 */
	public static void registerAPI(final int id, final LuaApi api) throws ClassNotFoundException, NoSuchMethodException,
			NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		if (!Config.enableAPIs || usedIds.contains(id))
			return;
		Object o;
		if ((o = getLuaMachineFromId(id)) == null) {
			new Thread("Computer API") {

				private boolean work = true;

				@Override
				public void run() {
					if (work) {
						Object serverComputerObject;
						try {
							synchronized (this) {
								this.wait(20);
							}
							serverComputerObject = getICompFromId(id);
							if (serverComputerObject == null)
								return;
							Class serverCompClass = serverComputerObject.getClass().asSubclass(
									Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
							Field comp = findField(serverCompClass, "m_computer");
							comp.setAccessible(true);
							Object o = comp.get(serverComputerObject);
							Method addAPI = o.getClass().getDeclaredMethod("addAPI",
									Class.forName("dan200.computercraft.core.apis.ILuaAPI"));
							addAPI.invoke(o, Proxy.newProxyInstance(
									Class.forName("dan200.computercraft.core.apis.ILuaAPI").getClassLoader(),
									new Class[]{Class.forName("dan200.computercraft.core.apis.ILuaAPI")}, api));
							Method reboot = o.getClass().getDeclaredMethod("reboot");
							reboot.invoke(o);
							work = false;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
			usedIds.add(id);
			return;
		}
		Method addAPI = o.getClass().getDeclaredMethod("addAPI",
				Class.forName("dan200.computercraft.core.apis.ILuaAPI"));
		addAPI.invoke(o, Proxy.newProxyInstance(
				Class.forName("dan200.computercraft.core.apis.ILuaAPI").getClassLoader(),
				new Class[]{Class.forName("dan200.computercraft.core.apis.ILuaAPI")}, api));
	}

	/**
	 * Gets the API environment for a computer
	 * @param computerServer ServerComputer int id
	 * @return IAPIEnvironment
	 */
	@Nullable
	public static Object getApiEnvironment(Object computerServer) throws NoSuchFieldException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		if (computerServer instanceof Integer) {
			Object computer = getICompFromId((Integer) computerServer);
			if (computer == null)
				return null;
			Class serverCompClass = computer.getClass().asSubclass(
					Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
			Method getAPIEnvironment = findMethod(serverCompClass, "getAPIEnvironment");
			return getAPIEnvironment.invoke(computer);
		}
		Class serverCompClass = computerServer.getClass().asSubclass(
				Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
		Field computerField = findField(serverCompClass, "m_computer");
		computerField.setAccessible(true);
		Object computer = computerField.get(computerServer);
		Field id = computer.getClass().getDeclaredField("m_id");
		id.setAccessible(true);
		return getApiEnvironment(id.get(computer));
	}

	/**
	 * Tries to find a method by name stating with the passed class and searching all super classes
	 * @param clazz class to begin search at
	 * @param fieldName name of field
	 * @return method
	 */
	private static Field findField(Class clazz, String fieldName) throws NoSuchFieldException {
		Field found = null;
		Class searchClass = clazz;
		while (found == null && searchClass != null) {
			try {
				found = searchClass.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException ignore) {
				searchClass = searchClass.getSuperclass();
			}
		}
		if (found == null)
			throw new NoSuchFieldException(clazz.getName() + fieldName);
		found.setAccessible(true);
		return found;
	}

	/**
	 * Tries to find a method by name stating with the passed class and searching all super classes
	 * @param clazz class to begin search at
	 * @param methodName name of method
	 * @param params method description
	 * @return method
	 */
	private static Method findMethod(Class clazz, String methodName, Class... params) throws NoSuchMethodException {
		Method found = null;
		Class searchClass = clazz;
		while (found == null && searchClass != null) {
			try {
				found = searchClass.getDeclaredMethod(methodName, params);
			}
			catch (NoSuchMethodException ignore) {
				searchClass = searchClass.getSuperclass();
			}
		}
		if (found == null)
			throw new NoSuchMethodException(clazz.getName() + methodName + Arrays.toString(params));
		found.setAccessible(true);
		return found;
	}

	/**
	 * Get all server computers
	 * @return Collection of ServerComputers
	 */
	public static Collection<Object> getServerComputers() throws ClassNotFoundException, NoSuchMethodException,
			NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		Class computerRegistry = Class.forName("dan200.computercraft.shared.computer.core.ComputerRegistry");
		Method getComputers = computerRegistry.getMethod("getComputers");
		Object registry = Class.forName("dan200.computercraft.ComputerCraft").getField("serverComputerRegistry")
				.get(null);
		return (Collection) getComputers.invoke(registry);
	}

	/**
	 * Get a peripheral from the computer on the specified side
	 * @param computer IComputer instance
	 * @param side 0-5
	 * @return the peripheral
	 */
	public static IPeripheral getPeripheral(Object computer, int side) throws ClassNotFoundException,
			NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class serverCompClass = computer.getClass().asSubclass(
				Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
		Method getPeripheral = findMethod(serverCompClass, "getPeripheral", int.class);
		return (IPeripheral) getPeripheral.invoke(computer, side);
	}

	/**
	 * Get the peripheral change listener from a computer
	 * @param computer ServerComputer instance
	 * @return IAPIEnvironment.IPeripheralChangeListener
	 */
	@Nullable
	public static Object getPeripheralChangeListener(Object computer) throws NoSuchFieldException,
			IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		Object apiEnvironment = getApiEnvironment(computer);
		if (apiEnvironment == null)
			return null;
		Class apiEnvironmentClass = Class.forName("dan200.computercraft.core.computer.Computer$APIEnvironment");
		Field peripheralListener = findField(apiEnvironmentClass, "m_peripheralListener");
		return peripheralListener.get(apiEnvironment);
	}

	/**
	 * Set the peripheral change listener for a computer
	 * @param computer ServerComputer instance
	 * @param peripheralChangeListener new listener
	 */
	public static void setPeripheralChangeListener(Object computer, PeripheralChangeListener peripheralChangeListener)
			throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException {
		Object apiEnvironment = getApiEnvironment(computer);
		if (apiEnvironment == null)
			return;
		Class apiEnvironmentClass = Class.forName("dan200.computercraft.core.computer.Computer$APIEnvironment");
		Class peripheralChangedListenerClass =
				Class.forName("dan200.computercraft.core.apis.IAPIEnvironment$IPeripheralChangeListener");
		Method setPeripheralChangeListener = findMethod(apiEnvironmentClass, "setPeripheralChangeListener",
				peripheralChangedListenerClass);
		setPeripheralChangeListener.invoke(apiEnvironment, Proxy.newProxyInstance(
				peripheralChangedListenerClass.getClassLoader(),
				new Class[]{peripheralChangedListenerClass}, peripheralChangeListener));
	}

	/**
	 * Call onPeripheralChanged(int, IPeripheral) on the passed PeripheralChangeListener
	 * @param side side of peripheral
	 * @param newPeripheral new peripheral or null
	 * @param peripheralChangeListener peripheralchangelistener to call
	 */
	public static void onPeripheralChanged(int side, IPeripheral newPeripheral, Object peripheralChangeListener)
			throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
		Class peripheralChangeListenerClass = peripheralChangeListener.getClass().asSubclass(
				Class.forName("dan200.computercraft.core.apis.IAPIEnvironment$IPeripheralChangeListener"));
		Method onPeripheralChanged = findMethod(peripheralChangeListenerClass,
				"onPeripheralChanged", int.class, IPeripheral.class);
		onPeripheralChanged.invoke(peripheralChangeListener, side, newPeripheral);
	}

	/**
	 * Wrap a peripheral
	 * @param peripheral peripheral to wrap
	 * @param side side name
	 * @return PeripheralAPI$PeripheralWrapper
	 */
	public static Object wrapPeripheral(IPeripheral peripheral, String side) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?> peripheralWrapper =
				Class.forName("dan200.computercraft.core.apis.PeripheralAPI$PeripheralWrapper")
						.getConstructor(IPeripheral.class, String.class);
		return peripheralWrapper.newInstance(peripheral, side);
	}

	/**
	 * Convert an int side to a string name
	 * @param side side int
	 * @return side name
	 */
	public static String sideIntToString(int side) throws ClassNotFoundException, NoSuchFieldException,
			IllegalAccessException {
		Class computer = Class.forName("dan200.computercraft.core.computer");
		String[] names = (String[]) findField(computer, "s_sideNames").get(null);
		return names[side];
	}

	/**
	 * Gets the peripheral wrapper from the peripheral change listener
	 * @param peripheralChangeListener listener containing the peripherals
	 * @param side side of peripheral
	 * @return PeripheralAPI$PeripheralWrapper
	 */
	@Nullable
	public static Object getPeripheralWrapperFromChangeListener(Object peripheralChangeListener, int side)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		if (peripheralChangeListener == null)
			return null;
		Class peripheralApi = Class.forName("dan200.computercraft.core.apis.PeripheralAPI");
		Field peripheralsField = findField(peripheralApi, "m_peripherals");
		peripheralsField.setAccessible(true);
		Object peripherals = peripheralsField.get(peripheralChangeListener);
		return Array.get(peripherals, side);
	}

	/**
	 * Queue an event on the ComputerThread
	 * @param task task to queue
	 * @param queueComputer Computer instance to queue for
	 */
	public static void queueTask(Task task, Object queueComputer) throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException {
		Class computerThread = Class.forName("dan200.computercraft.core.computer.ComputerThread");
		Class iTask = Class.forName("dan200.computercraft.core.computer.ITask");
		Class computer = Class.forName("dan200.computercraft.core.computer.Computer");
		Method queueTask = findMethod(computerThread, "queueTask", iTask, computer);
		queueTask.invoke(computerThread,
				Proxy.newProxyInstance(iTask.getClassLoader(), new Class[]{iTask}, task),
				queueComputer);
	}

	/**
	 * Get the position of a computer
	 * @param computer ServerComputer
	 * @return BlockPos will be null on pocket computers
	 */
	@Nullable
	public static BlockPos getPos(Object computer) throws ClassNotFoundException, NoSuchFieldException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Class serverCompClass = computer.getClass().asSubclass(
				Class.forName("dan200.computercraft.shared.computer.core.ServerComputer"));
		Field position = findField(serverCompClass, "m_position");
		return (BlockPos) position.get(computer);
	}

	/**
	 * Checks if a wrapped peripheral is attached
	 * @param wrappedPeripheral PeripheralAPI$PeripheralWrapper
	 * @return attached status
	 */
	public static boolean isWrappedPeripheralAttached(Object wrappedPeripheral) throws ClassNotFoundException,
			NoSuchFieldException, IllegalAccessException {
		Class peripheralWrapper = Class.forName("dan200.computercraft.core.apis.PeripheralAPI$PeripheralWrapper");
		Field attached = findField(peripheralWrapper, "m_attached");
		return (boolean) attached.get(wrappedPeripheral);
	}
}
