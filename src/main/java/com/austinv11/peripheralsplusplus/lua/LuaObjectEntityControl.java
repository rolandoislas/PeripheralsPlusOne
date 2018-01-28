package com.austinv11.peripheralsplusplus.lua;

import com.austinv11.collectiveframework.minecraft.utils.Location;
import com.austinv11.collectiveframework.minecraft.utils.WorldUtils;
import com.austinv11.collectiveframework.utils.math.MathUtils;
import com.austinv11.peripheralsplusplus.PeripheralsPlusPlus;
import com.austinv11.peripheralsplusplus.capabilities.nano.CapabilityNanoBot;
import com.austinv11.peripheralsplusplus.items.ItemNanoSwarm;
import com.austinv11.peripheralsplusplus.network.RobotEventPacket;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.utils.Util;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

public class LuaObjectEntityControl implements ILuaObject {

	// Antenna id
	private UUID id;
	
	private boolean isPlayer;
	private EntityPlayer player;
	private EntityLiving entity;
	
	public LuaObjectEntityControl(UUID id, Entity entity) {
		this.id = id;
		if (entity instanceof EntityPlayer) {
			isPlayer = true;
			player = (EntityPlayer) entity;
		} else {
			isPlayer = false;
			this.entity = (EntityLiving) entity;
		}
	}
	
	@Override
	public String[] getMethodNames() {
		if (isPlayer)
			return new String[]{"isPlayer", "hurt", "heal", "getHealth", "getMaxHealth", "isDead", "getRemainingBots", "getDisplayName",
					"getPlayerName", "getUUID", "getHunger", "click", "clickRelease", "keyPress", "keyRelease", "mouseMove", "whisper"};
		else
			return new String[]{"isPlayer", "hurt", "heal", "getHealth", "getMaxHealth", "isDead", "getRemainingBots", "getDisplayName",
					"getEntityName", "setTarget", "setAttackTarget", "setMovementTarget", "setTurnAngle", "toggleJumping"};
	}
	
	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		// Update player object
		if (isPlayer) {
			EntityPlayer updatedPlayer = Util.getPlayer(player.getPersistentID());
			if (updatedPlayer != null)
				player = updatedPlayer;
		}
		// Handle methods
		if (method < 8) {
			switch (method) {
				// isPlayer()
				case 0:
					if (!ItemNanoSwarm.doInstruction(id, isPlayer ? player : entity, true, 1))
						throw new LuaException("Entity with id "+id+" cannot be interacted with");
					return new Object[]{isPlayer};
				// hurt()
				case 1:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, false, 1))
							return new Object[]{false};
						player.attackEntityFrom(new DamageSource(Reference.MOD_ID.toLowerCase()+".nanobot").setDamageBypassesArmor(), 1.0F);
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							return new Object[]{false};
						entity.attackEntityFrom(new DamageSource(Reference.MOD_ID.toLowerCase()+".nanobot").setDamageBypassesArmor(), 1.0F);
					}
					break;
				// heal()
				case 2:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, false, 1))
							return new Object[]{false};
						player.heal(1.0F);
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							return new Object[]{false};
						entity.heal(1.0F);
					}
					break;
				// getHealth()
				case 3:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getHealth()};
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{entity.getHealth()};
					}
				// getMaxHealth()
				case 4:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getMaxHealth()};
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{entity.getMaxHealth()};
					}
				// isDead()
				case 5:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.isDead};
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{entity.isDead};
					}
				// getRemainingBots()
				case 6:
                    if (!ItemNanoSwarm.doInstruction(id, isPlayer ? player : entity, true, 0))
				        throw new LuaException("Cannot get bots from entity");
					if (isPlayer) {
						//noinspection ConstantConditions
						return new Object[]{player.getCapability(CapabilityNanoBot.INSTANCE, null).getBots()};
					} else {
						//noinspection ConstantConditions
						return new Object[]{entity.getCapability(CapabilityNanoBot.INSTANCE, null).getBots()};
					}
				// getDisplayName()
				case 7:
					if (isPlayer) {
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getDisplayName().getFormattedText()};
					} else {
						if (!ItemNanoSwarm.doInstruction(id, entity, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{entity.getCustomNameTag()};
					}
			}
		} else {
			if (isPlayer) {
				switch (method) {
					// getPlayerName()
					case 8:
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getGameProfile().getName()};
					// getUUID()
					case 9:
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getGameProfile().getId().toString()};
					// getHunger()
					case 10:
						if (!ItemNanoSwarm.doInstruction(id, player, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{player.getFoodStats().getFoodLevel()};
					// click()
					case 11:
					// clickRelease()
					case 12:
					// keyPress()
					case 13:
					// keyRelease()
					case 14:
					// mouseMove()
					case 15:
						if (arguments.length < 1 || (method == 15 && arguments.length < 2))
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof Double && method < 13) &&
								!(arguments[0] instanceof String && method < 15) && !(arguments[0] instanceof Double && method == 15))
							throw new LuaException("Bad argument #1 (expected string or number)");
						if (arguments.length > 1 && !(arguments[1] instanceof Double))
							throw new LuaException("Bad argument #2 (expected number)");
						if (!ItemNanoSwarm.doInstruction(id, player, false, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						RobotEventPacket.PressType type = MathUtils.isEvenNumber(method) ? RobotEventPacket.PressType.RELEASE : RobotEventPacket.PressType.PRESS;
						RobotEventPacket.ActionType action = method < 13 ? RobotEventPacket.ActionType.MOUSE_CLICK : method < 15 ? RobotEventPacket.ActionType.KEYBOARD : RobotEventPacket.ActionType.MOUSE_MOVE;
						Object args = arguments.length == 1 && arguments[0] instanceof Double ? (Integer)(int)(double)(Double)arguments[0] : 
								arguments[0] instanceof String ? (String)arguments[0] : new int[]{(int)(double)(Double)arguments[0], (int)(double)(Double)arguments[1]};
						PeripheralsPlusPlus.NETWORK.sendTo(new RobotEventPacket(action, type, args), (EntityPlayerMP) player);
						break;
					// Whisper
					case 16:
						if (arguments.length < 1)
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof String))
							throw new LuaException("Bad argument #1 (expected string)");
						if (arguments.length > 1 && !(arguments[1] instanceof String))
							throw new LuaException("Bad argument #2 (expected string)");
						if (!ItemNanoSwarm.doInstruction(id, player, false, 1))
							return new Object[]{false};
						String sender = arguments.length > 1 ? "<"+arguments[1]+"> " : "";
						player.sendMessage(new TextComponentString(sender+arguments[0]));
						break;
				}
				
			} else {
				switch (method) {
					// getEntityName()
					case 8:
						if (!ItemNanoSwarm.doInstruction(id, entity, true, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						return new Object[]{entity.getClass().getSimpleName()};
					// setTarget()
					case 9:
						if (arguments.length < 1)
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof String || arguments[0] instanceof Double))
							throw new LuaException("Bad argument #1 (expected string or number)");
						if (arguments[0] instanceof Double && !(arguments.length > 1 && arguments[1] instanceof Double))
							throw new LuaException("Bad argument #2 (expected number)");
						if (arguments[0] instanceof Double && !(arguments.length > 2 && arguments[2] instanceof Double))
							throw new LuaException("Bad argument #3 (expected number)");
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						Entity target;
						if (arguments[0] instanceof String)
							target = WorldUtils.getPlayerForWorld((String)arguments[0], entity.world);
						else
							target = WorldUtils.getNearestEntityToLocation(new Location((Double)arguments[0],
									(Double)arguments[1], (Double)arguments[2], entity.world));
						if (!(target instanceof EntityLivingBase))
						    throw new LuaException("Target must be living");
						entity.setAttackTarget((EntityLivingBase) target);
						return new Object[]{true};
					// setAttackTarget()
					case 10:
						if (arguments.length < 1)
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof String || arguments[0] instanceof Double))
							throw new LuaException("Bad argument #1 (expected string or number)");
						if (arguments[0] instanceof Double && !(arguments.length > 1 && arguments[1] instanceof Double))
							throw new LuaException("Bad argument #2 (expected number)");
						if (arguments[0] instanceof Double && !(arguments.length > 2 && arguments[2] instanceof Double))
							throw new LuaException("Bad argument #3 (expected number)");
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							throw new LuaException("Entity with id "+id+" cannot be interacted with");
						Entity attackTarget;
						if (arguments[0] instanceof String)
							attackTarget = WorldUtils.getPlayerForWorld((String)arguments[0], entity.world);
						else
							attackTarget = WorldUtils.getNearestEntityToLocation(new Location((Double)arguments[0],
									(Double)arguments[1], (Double)arguments[2], entity.world));
						entity.setAttackTarget((EntityLivingBase) attackTarget);
						return new Object[]{attackTarget != null};
					// setMovementTarget()
					case 11:
						if (arguments.length < 3)
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof Double))
							throw new LuaException("Bad argument #1 (expected number)");
						if (!(arguments[1] instanceof Double))
							throw new LuaException("Bad argument #2 (expected number)");
						if (!(arguments[2] instanceof Double))
							throw new LuaException("Bad argument #3 (expected number)");
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							return new Object[]{false};
						entity.getNavigator().setPath(entity.getNavigator().getPathToXYZ((Double)arguments[0], (Double)arguments[1],
								(Double)arguments[2]), entity.getAIMoveSpeed());
						break;
					// setTurnAngle()
					case 12:
						if (arguments.length < 1)
							throw new LuaException("Too few arguments");
						if (!(arguments[0] instanceof Double))
							throw new LuaException("Bad argument #1 (expected number)");
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							return new Object[]{false};
						entity.rotationYaw = (float)(double)(Double)arguments[0];
						entity.rotationYawHead = (float)(double)(Double)arguments[0];
						break;
					// toggleJumping()
					case 13:
						if (!ItemNanoSwarm.doInstruction(id, entity, false, 1))
							return new Object[]{false};
						entity.setJumping(!entity.isJumping);
						break;
				}
			}
		}
		return new Object[]{true};
	}
}
