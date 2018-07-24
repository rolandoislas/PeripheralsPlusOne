package com.austinv11.peripheralsplusplus.proxy;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.peripheralsplusplus.PeripheralsPlusPlus;
import com.austinv11.peripheralsplusplus.capabilities.nano.NanoBotHolder;
import com.austinv11.peripheralsplusplus.capabilities.nano.NanoBotHolderDefault;
import com.austinv11.peripheralsplusplus.capabilities.nano.NanoBotHolderStorage;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagHolder;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagHolderDefault;
import com.austinv11.peripheralsplusplus.capabilities.rfid.RfidTagStorage;
import com.austinv11.peripheralsplusplus.client.gui.GuiHandler;
import com.austinv11.peripheralsplusplus.entities.EntityNanoBotSwarm;
import com.austinv11.peripheralsplusplus.entities.EntityRidableTurtle;
import com.austinv11.peripheralsplusplus.event.handler.CapabilitiesHandler;
import com.austinv11.peripheralsplusplus.event.handler.PeripheralContainerHandler;
import com.austinv11.peripheralsplusplus.mount.DynamicMount;
import com.austinv11.peripheralsplusplus.network.ChatPacket;
import com.austinv11.peripheralsplusplus.network.CommandPacket;
import com.austinv11.peripheralsplusplus.network.GuiPacket;
import com.austinv11.peripheralsplusplus.network.InputEventPacket;
import com.austinv11.peripheralsplusplus.network.ParticlePacket;
import com.austinv11.peripheralsplusplus.network.PermCardChangePacket;
import com.austinv11.peripheralsplusplus.network.RidableTurtlePacket;
import com.austinv11.peripheralsplusplus.network.RobotEventPacket;
import com.austinv11.peripheralsplusplus.network.ScaleRequestPacket;
import com.austinv11.peripheralsplusplus.network.ScaleRequestResponsePacket;
import com.austinv11.peripheralsplusplus.network.SynthPacket;
import com.austinv11.peripheralsplusplus.network.SynthResponsePacket;
import com.austinv11.peripheralsplusplus.network.TextFieldInputEventPacket;
import com.austinv11.peripheralsplusplus.pocket.PocketMotionDetector;
import com.austinv11.peripheralsplusplus.recipe.RecipeRfidChip;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAIChatBox;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAnalyzerBee;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAnalyzerButterfly;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAnalyzerTree;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAntenna;
import com.austinv11.peripheralsplusplus.tiles.TileEntityChatBox;
import com.austinv11.peripheralsplusplus.tiles.TileEntityEnvironmentScanner;
import com.austinv11.peripheralsplusplus.tiles.TileEntityInteractiveSorter;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMEBridge;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMagReaderWriter;
import com.austinv11.peripheralsplusplus.tiles.TileEntityManaManipulator;
import com.austinv11.peripheralsplusplus.tiles.TileEntityModNotLoaded;
import com.austinv11.peripheralsplusplus.tiles.TileEntityOreDictionary;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPeripheralContainer;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPlayerInterface;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPlayerSensor;
import com.austinv11.peripheralsplusplus.tiles.TileEntityPrivacyGuard;
import com.austinv11.peripheralsplusplus.tiles.TileEntityRFCharger;
import com.austinv11.peripheralsplusplus.tiles.TileEntityResupplyStation;
import com.austinv11.peripheralsplusplus.tiles.TileEntityRfidReaderWriter;
import com.austinv11.peripheralsplusplus.tiles.TileEntitySpeaker;
import com.austinv11.peripheralsplusplus.tiles.TileEntityTeleporter;
import com.austinv11.peripheralsplusplus.tiles.TileEntityTimeSensor;
import com.austinv11.peripheralsplusplus.tiles.TileEntityTurtle;
import com.austinv11.peripheralsplusplus.villagers.VillagerProfessionPPP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;

import static com.austinv11.peripheralsplusplus.PeripheralsPlusPlus.NETWORK;

public class CommonProxy {

	public void setupVillagers() {
		VillagerRegistry.VillagerProfession profession = new VillagerProfessionPPP(
				Reference.MOD_ID + ":ccvillager",
				Reference.MOD_ID + ":textures/models/ccvillager.png",
				"minecraft:textures/entity/zombie_villager/zombie_villager.png");
		ForgeRegistries.VILLAGER_PROFESSIONS.register(profession);
	}

	public void registerTileEntities() {
		registerTileEntity(TileEntityChatBox.class);
		registerTileEntity(TileEntityAIChatBox.class);
		registerTileEntity(TileEntityPlayerSensor.class);
		registerTileEntity(TileEntityRFCharger.class);
		registerTileEntity(TileEntityOreDictionary.class);
		if (Loader.isModLoaded(ModIds.FORESTRY)) {
			registerTileEntity(TileEntityAnalyzerBee.class);
			registerTileEntity(TileEntityAnalyzerButterfly.class);
			registerTileEntity(TileEntityAnalyzerTree.class);
		}
		registerTileEntity(TileEntityTeleporter.class);
		registerTileEntity(TileEntityEnvironmentScanner.class);
		registerTileEntity(TileEntitySpeaker.class);
		registerTileEntity(TileEntityAntenna.class);
		registerTileEntity(TileEntityPeripheralContainer.class);
		if (Loader.isModLoaded(ModIds.APPLIED_ENGERGISTICS))
			registerTileEntity(TileEntityMEBridge.class);
		registerTileEntity(TileEntityTurtle.class);
		registerTileEntity(TileEntityTimeSensor.class);
		registerTileEntity(TileEntityInteractiveSorter.class);
        registerTileEntity(TileEntityPlayerInterface.class);
		registerTileEntity(TileEntityResupplyStation.class);
		registerTileEntity(TileEntityModNotLoaded.class);
		if (Loader.isModLoaded("botania"))
			registerTileEntity(TileEntityManaManipulator.class);
		registerTileEntity(TileEntityRfidReaderWriter.class);
		registerTileEntity(TileEntityMagReaderWriter.class);
		registerTileEntity(TileEntityPrivacyGuard.class);
    }

	private void registerTileEntity(Class<? extends TileEntity> tileEntity) {
		GameRegistry.registerTileEntity(tileEntity, new ResourceLocation(Reference.MOD_ID, tileEntity.getSimpleName()));
	}

	public void textureAndModelInit() {}

	public void registerRenderers() {}

	public void prepareGuis() {
		NetworkRegistry.INSTANCE.registerGuiHandler(PeripheralsPlusPlus.instance, new GuiHandler());
	}

	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new TileEntityChatBox.ChatListener());
		MinecraftForge.EVENT_BUS.register(new PeripheralContainerHandler());
		MinecraftForge.EVENT_BUS.register(new TileEntityAntenna());
		MinecraftForge.EVENT_BUS.register(new CapabilitiesHandler());
		MinecraftForge.EVENT_BUS.register(new PocketMotionDetector());
		MinecraftForge.EVENT_BUS.register(new DynamicMount());
		MinecraftForge.EVENT_BUS.register(new RecipeRfidChip());
	}

	public void registerEntities() {
		EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "ridable_turtle"),
				EntityRidableTurtle.class, "ridable_turtle", 1, PeripheralsPlusPlus.instance,
				64, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "nano_bot_swarm"),
				EntityNanoBotSwarm.class, "nano_bot_swarm", 2, PeripheralsPlusPlus.instance,
				64, 20, true);
	}

	public void registerNetwork() {
		int discriminator = 0;
		NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
		NETWORK.registerMessage(ChatPacket.ChatPacketHandler.class, ChatPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(ScaleRequestPacket.ScaleRequestPacketHandler.class, ScaleRequestPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(ScaleRequestResponsePacket.ScaleRequestResponsePacketHandler.class, ScaleRequestResponsePacket.class, discriminator++, Side.SERVER);
		NETWORK.registerMessage(CommandPacket.CommandPacketHandler.class, CommandPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(ParticlePacket.ParticlePacketHandler.class, ParticlePacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(InputEventPacket.InputEventPacketHandler.class, InputEventPacket.class, discriminator++, Side.SERVER);
		NETWORK.registerMessage(GuiPacket.GuiPacketHandler.class, GuiPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(TextFieldInputEventPacket.TextFieldInputEventPacketHandler.class, TextFieldInputEventPacket.class, discriminator++, Side.SERVER);
		NETWORK.registerMessage(RidableTurtlePacket.RidableTurtlePacketHandler.class, RidableTurtlePacket.class, discriminator++, Side.SERVER);
		NETWORK.registerMessage(RobotEventPacket.RobotEventPacketHandler.class, RobotEventPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(PermCardChangePacket.PermCardChangePacketHandler.class, PermCardChangePacket.class, discriminator++, Side.SERVER);
		NETWORK.registerMessage(SynthPacket.SynthPacketHandler.class, SynthPacket.class, discriminator++, Side.CLIENT);
		NETWORK.registerMessage(SynthResponsePacket.SynthResponsePacketHandler.class, SynthResponsePacket.class, discriminator++, Side.SERVER);
	}

	public void registerCapabilities() {
		CapabilityManager.INSTANCE.register(NanoBotHolder.class, new NanoBotHolderStorage(), NanoBotHolderDefault::new);
		CapabilityManager.INSTANCE.register(RfidTagHolder.class, new RfidTagStorage(), RfidTagHolderDefault::new);
	}
}
