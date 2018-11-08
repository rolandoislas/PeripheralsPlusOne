package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.peripheralsplusplus.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.genetics.IAlleleArea;
import forestry.api.genetics.IAlleleBoolean;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IAlleleTolerance;
import forestry.api.genetics.IGenome;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;

public class TileEntityAnalyzerBee extends TileEntityAnalyzer {

	public TileEntityAnalyzerBee() {
		super();
	}

	@Override
	public String getName(){
		return "tileEntityBeeAnalyzer";
	}

	@Override
	public String getType() {
		return "beeAnalyzer";
	}

	@Override
	protected String getRootType() {
		return "rootBees";
	}

	@Override
	protected void addGenome(ItemStack stack, IGenome origGenome, HashMap<String, Object> ret, boolean activeAllele) {
		IBeeRoot root = (IBeeRoot) getRoot();
		IBeeGenome genome = (IBeeGenome) origGenome;
		ret.put("type", root.getType(stack) == null ? null : root.getType(stack).name());
		if (activeAllele) {
			ret.put("speciesPrimary", genome.getPrimary().getAlleleName()); // Deprecated
			ret.put("species", genome.getPrimary().getAlleleName());
			ret.put("speciesSecondary", genome.getSecondary().getAlleleName()); // Deprecated
			ret.put("speed", genome.getSpeed());
			ret.put("lifespan", genome.getLifespan());
			ret.put("fertility", genome.getFertility());
			ret.put("neverSleeps", genome.getNeverSleeps());
			ret.put("toleratesRain", genome.getToleratesRain());
			ret.put("caveDwelling", genome.getCaveDwelling());
			ret.put("flower", genome.getFlowerProvider().getDescription());
			ret.put("flowering", genome.getFlowering());
			ret.put("territory", Util.arrayToMap(new int[]{genome.getTerritory().getX(), genome.getTerritory().getY(),
					genome.getTerritory().getZ()}));
			ret.put("effect", genome.getEffect().getUID());
			ret.put("temperature", genome.getPrimary().getTemperature().toString());
			ret.put("toleranceTemperature", genome.getToleranceTemp().toString());
			ret.put("humidity", genome.getPrimary().getHumidity().toString());
			ret.put("toleranceHumidity", genome.getToleranceHumid().toString());
		}
		else {
			ret.put("species", genome.getInactiveAllele(EnumBeeChromosome.SPECIES).getAlleleName());
			ret.put("speed", ((IAlleleFloat)genome.getInactiveAllele(EnumBeeChromosome.SPEED)).getValue());
			ret.put("lifespan", ((IAlleleInteger)genome.getInactiveAllele(EnumBeeChromosome.LIFESPAN)).getValue());
			ret.put("fertility", ((IAlleleInteger)genome.getInactiveAllele(EnumBeeChromosome.FERTILITY)).getValue());
			ret.put("neverSleeps", ((IAlleleBoolean)genome.getInactiveAllele(EnumBeeChromosome.NEVER_SLEEPS))
					.getValue());
			ret.put("toleratesRain", ((IAlleleBoolean)genome.getInactiveAllele(EnumBeeChromosome.TOLERATES_RAIN))
					.getValue());
			ret.put("caveDwelling", ((IAlleleBoolean)genome.getInactiveAllele(EnumBeeChromosome.CAVE_DWELLING))
					.getValue());
			ret.put("flower", ((IAlleleFlowers)genome.getInactiveAllele(EnumBeeChromosome.FLOWER_PROVIDER))
					.getProvider().getDescription());
			ret.put("flowering", ((IAlleleInteger)genome.getInactiveAllele(EnumBeeChromosome.FLOWERING)).getValue());
			Vec3i territory = ((IAlleleArea) genome.getInactiveAllele(EnumBeeChromosome.TERRITORY)).getValue();
			ret.put("territory", Util.arrayToMap(new int[]{territory.getX(), territory.getY(), territory.getZ()}));
			ret.put("effect", genome.getInactiveAllele(EnumBeeChromosome.EFFECT).getUID());
			ret.put("temperature", genome.getSecondary().getTemperature().toString());
			ret.put("toleranceTemperature", ((IAlleleTolerance)genome
					.getInactiveAllele(EnumBeeChromosome.TEMPERATURE_TOLERANCE)).getValue().toString());
			ret.put("humidity", genome.getSecondary().getHumidity().toString());
			ret.put("toleranceHumidity", ((IAlleleTolerance)genome
					.getInactiveAllele(EnumBeeChromosome.HUMIDITY_TOLERANCE)).getValue().toString());
		}
	}

	@Override
	protected IPeripheral getInstance() {
		return this;
	}
}
