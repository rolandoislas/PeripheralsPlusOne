package com.austinv11.peripheralsplusplus.tiles;

import dan200.computercraft.api.peripheral.IPeripheral;
import forestry.api.genetics.IAlleleBoolean;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IAlleleTolerance;
import forestry.api.genetics.IGenome;
import forestry.api.lepidopterology.EnumButterflyChromosome;
import forestry.api.lepidopterology.IButterflyGenome;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class TileEntityAnalyzerButterfly extends TileEntityAnalyzer {

	public TileEntityAnalyzerButterfly() {
		super();
	}

	@Override
	public String getName(){
		return "tileEntityButterflyAnalyzer";
	}

	@Override
	public String getType() {
		return "butterflyAnalyzer";
	}

	@Override
	protected String getRootType() {
		return "rootButterflies";
	}

	@Override
	protected void addGenome(ItemStack stack, IGenome origGenome, HashMap<String, Object> ret, boolean activeAllele) {
		IButterflyGenome genome = (IButterflyGenome) origGenome;
		if (activeAllele) {
			ret.put("speciesPrimary", genome.getPrimary().getAlleleName()); // Deprecated
			ret.put("species", genome.getPrimary().getAlleleName());
			ret.put("speciesSecondary", genome.getSecondary().getAlleleName()); // Deprecated
			ret.put("speed", genome.getSpeed());
			ret.put("lifespan", genome.getLifespan());
			ret.put("metabolism", genome.getMetabolism());
			ret.put("fertility", genome.getFertility());
			ret.put("nocturnal", genome.getNocturnal());
			ret.put("tolerantFlyer", genome.getTolerantFlyer());
			ret.put("fireResistant", genome.getFireResist());
			ret.put("flower", genome.getFlowerProvider().getDescription());
			ret.put("effect", genome.getEffect().getUID());
			ret.put("temperature", genome.getPrimary().getTemperature().toString());
			ret.put("toleranceTemperature", genome.getToleranceTemp().toString());
			ret.put("humidity", genome.getPrimary().getHumidity().toString());
			ret.put("toleranceHumidity", genome.getToleranceHumid().toString());
			ret.put("cocoon", genome.getCocoon().toString());
		}
		else {
			ret.put("species", genome.getSecondary().getAlleleName());
			ret.put("speed", ((IAlleleFloat)genome.getInactiveAllele(EnumButterflyChromosome.SPEED)).getValue());
			ret.put("lifespan", ((IAlleleInteger)genome.getInactiveAllele(EnumButterflyChromosome.LIFESPAN)).getValue());
			ret.put("metabolism", ((IAlleleInteger)genome.getInactiveAllele(EnumButterflyChromosome.METABOLISM))
					.getValue());
			ret.put("fertility", ((IAlleleInteger)genome.getInactiveAllele(EnumButterflyChromosome.FERTILITY))
					.getValue());
			ret.put("nocturnal", ((IAlleleBoolean)genome.getInactiveAllele(EnumButterflyChromosome.NOCTURNAL))
					.getValue());
			ret.put("tolerantFlyer", ((IAlleleBoolean)genome.getInactiveAllele(EnumButterflyChromosome.TOLERANT_FLYER))
					.getValue());
			ret.put("fireResistant", ((IAlleleBoolean)genome.getInactiveAllele(EnumButterflyChromosome.FIRE_RESIST))
					.getValue());
			ret.put("flower", ((IAlleleFlowers)genome.getInactiveAllele(EnumButterflyChromosome.FLOWER_PROVIDER))
					.getProvider().getDescription());
			ret.put("effect", genome.getInactiveAllele(EnumButterflyChromosome.EFFECT).getUID());
			ret.put("temperature", genome.getSecondary().getTemperature().toString());
			ret.put("toleranceTemperature", ((IAlleleTolerance)genome
					.getInactiveAllele(EnumButterflyChromosome.TEMPERATURE_TOLERANCE)).getValue().toString());
			ret.put("humidity", genome.getSecondary().getHumidity().toString());
			ret.put("toleranceHumidity", ((IAlleleTolerance)genome
					.getInactiveAllele(EnumButterflyChromosome.HUMIDITY_TOLERANCE)).getValue().toString());
			ret.put("cocoon", genome.getInactiveAllele(EnumButterflyChromosome.COCOON).toString());
		}
	}

	@Override
	protected IPeripheral getInstance() {
		return this;
	}
}
