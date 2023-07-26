package test;

import java.util.ArrayList;
import java.util.Map;

import tool.Game;
import tool.IgnoreCaseString;
import tool.Initialization;
import tool.Learnset;
import tool.Move;
import tool.Species;

public class TestMain {
	
	public static void main(String[] args) {
		//testLearnsetAndSpeciesCrossReferences();
		testPlatinumTrainers();
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

	public static void testPlatinumTrainers() {
		try {
			Initialization.init(Game.PLATINUM);
			
			ArrayList<PokemonMatcher> list = new ArrayList<>();
			// Gardenia 1
			list.add(new PokemonMatcher(
					"LEADER_Gardenia_1", 2,
					new PokemonTest("Roserade", "F", 22, "Naive", "Natural Cure", 
							59, 37, 30, 61, 46, 49, 
							6,
							"Sitrus Berry", null, "Grass Knot", "Magical Leaf", "Poison Sting", "Stun Spore")
					));
			
			// Volkner 1
			list.add(new PokemonMatcher(
					"LEADER_Volkner_1", 3,
					new PokemonTest("Electivire", "M", 50, "Impish", "Motor Drive", 
							137, 129, 94, 98, 97, 109, 
							4, 3, 29, 18, 15, 19, 
							"Sitrus Berry", null, "Thunder Punch", "Fire Punch", "Quick Attack", "Giga Impact")
					));
			// E4 Barry
			list.add(new PokemonMatcher(
					"RIVAL_Barry_EMPOLEON_3", 5, 
					new PokemonTest("Empoleon", "M", 51, "Quiet", "Torrent", 
							158, 104, 107, 143, 120, 70, 
							24, 
							null, null, "Brine", "Aerial Ace", "Metal Claw", "Shadow Claw")
					));
			// E4 Lucian
			list.add(new PokemonMatcher(
					"ELITE_FOUR_Lucian_1", 1,
					new PokemonTest("Espeon", "M", 55, "Hardy", "Synchronize", 
							153, 93, 87, 164, 126, 142, 
							30, 
							null, null, "Psychic", "Shadow Ball", "Quick Attack", "Signal Beam")
					));

			for(PokemonMatcher pm : list) {
				Result r = pm.tryMatch();
				System.out.println(r);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
