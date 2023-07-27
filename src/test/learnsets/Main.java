package test.learnsets;

import java.util.Map;

import tool.Game;
import tool.IgnoreCaseString;
import tool.Learnset;
import tool.Move;
import tool.Species;

public class Main {
	
	public static void main(String[] args) {
		testLearnsetAndSpeciesCrossReferences();
	}
	
	public static void testLearnsetAndSpeciesCrossReferences() {
		try {
			for(Game game : Game.values()) {
				// Skip games which don't have both files
				if(game.getSpeciesFilename() == null || game.getLearnsetsFilename() == null)
					continue;
				
				// Initialize what's needed
				Species.initSpecies(game);
				Move.initMoves(game);
				Learnset.initLearnsets(game);
				
				// Empty ?
				if(Species.entrySet().isEmpty())
					System.out.println(String.format(">>> Species are empty in game '%s'.", game));
				if(Learnset.entrySet().isEmpty())
					System.out.println(String.format(">>> Learnsets are empty in game '%s'.", game));
				
				// Check if all species have a learnset					
				for(Map.Entry<IgnoreCaseString, Species> entry : Species.entrySet()) {
					String speciesStr = ((IgnoreCaseString)entry.getKey()).getComparisonString();
					if(null == Learnset.getLearnsetByName(speciesStr))
						System.out.println(String.format(">>> Species %s doesn't exist in %s learnsets.", speciesStr, game));
				}
				
				// Check if all learnsets have an initialized species
				for(Map.Entry<IgnoreCaseString, Learnset> entry : Learnset.entrySet()) {
					String speciesStr = ((IgnoreCaseString)entry.getKey()).getComparisonString();
					if(null == Species.getSpeciesByName(speciesStr))
						System.out.println(String.format(">>> Learnset of %s doesn't have an existing species associated with it in %s.", speciesStr, game));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
