package com.austinv11.peripheralsplusplus.tiles;

import dan200.computercraft.api.peripheral.IPeripheral;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.IAlleleFruit;
import forestry.api.arboriculture.IAlleleTreeSpecies;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IChromosome;
import forestry.api.genetics.IGenome;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class TileEntityAnalyzerTree extends TileEntityAnalyzer {

	public TileEntityAnalyzerTree() {
		super();
	}

	@Override
	public String getName(){
		return "tileEntityTreeAnalyzer";
	}

	@Override
	public String getType() {
		return "treeAnalyzer";
	}

	@Override
	protected String getRootType() {
		return "rootTrees";
	}

	@Override
	protected void addGenome(ItemStack stack, IGenome origGenome, HashMap<String, Object> ret, boolean activeAllele) {
		ITreeGenome genome = (ITreeGenome) origGenome;
		if (activeAllele) {
			ret.put("speciesPrimary", genome.getPrimary().getAlleleName()); // Deprecated
			ret.put("species", genome.getPrimary().getAlleleName());
			ret.put("speciesSecondary", genome.getSecondary().getAlleleName()); // Deprecated
			ret.put("height", genome.getHeight());
			ret.put("fertility", genome.getFertility());
			ret.put("yield", genome.getYield());
			ret.put("sappiness", genome.getSappiness());
			ret.put("matures", genome.getMaturationTime());
			ret.put("fruit", genome.getFruitProvider().getDescription());
			ret.put("girth", genome.getGirth());
			ret.put("effect", genome.getEffect().getUID());
			ret.put("decorativeLeaves", genome.getDecorativeLeaves().getDisplayName());
			ret.put("matchesTemplateGenome", genome.matchesTemplateGenome());
		}
		else {
			ret.put("speciesy", genome.getSecondary().getAlleleName());
			ret.put("height", ((IAlleleFloat)genome.getInactiveAllele(EnumTreeChromosome.HEIGHT)).getValue());
			ret.put("fertility", ((IAlleleFloat)genome.getInactiveAllele(EnumTreeChromosome.FERTILITY)).getValue());
			ret.put("yield", ((IAlleleFloat)genome.getInactiveAllele(EnumTreeChromosome.YIELD)).getValue());
			ret.put("sappiness", ((IAlleleFloat)genome.getInactiveAllele(EnumTreeChromosome.SAPPINESS)).getValue());
			ret.put("matures", ((IAlleleInteger)genome.getInactiveAllele(EnumTreeChromosome.MATURATION)).getValue());
			ret.put("fruit", ((IAlleleFruit)genome.getInactiveAllele(EnumTreeChromosome.FRUITS)).getProvider()
					.getDescription());
			ret.put("girth", ((IAlleleInteger)genome.getInactiveAllele(EnumTreeChromosome.GIRTH)).getValue());
			ret.put("effect", genome.getInactiveAllele(EnumTreeChromosome.EFFECT).getUID());
			ret.put("decorativeLeaves", genome.getSecondary().getLeafProvider().getDecorativeLeaves().getDisplayName());
			ret.put("matchesTemplateGenome", matchesTemplateGenome(genome.getSecondary(), genome.getChromosomes()));
		}
	}

	private boolean matchesTemplateGenome(IAlleleTreeSpecies alleleTreeSpecies, IChromosome[] chromosomes) {
		IAllele[] template = getRoot().getTemplate(alleleTreeSpecies);
		for (int chromosomeIndex = 0; chromosomeIndex < chromosomes.length; chromosomeIndex++) {
			IChromosome chromosome = chromosomes[chromosomeIndex];
			String templateUid = template[chromosomeIndex].getUID();
			IAllele primaryAllele = chromosome.getPrimaryAllele();
			if (!primaryAllele.getUID().equals(templateUid))
				return false;
			IAllele secondaryAllele = chromosome.getSecondaryAllele();
			if (!secondaryAllele.getUID().equals(templateUid))
				return false;
		}
		return true;
	}

	@Override
	protected IPeripheral getInstance() {
		return this;
	}
}
