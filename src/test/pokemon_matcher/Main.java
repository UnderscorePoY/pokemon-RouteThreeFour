package test.pokemon_matcher;

import java.util.ArrayList;
import java.util.Map;

import tool.Game;
import tool.IgnoreCaseString;
import tool.Initialization;
import tool.Learnset;
import tool.Move;
import tool.Species;

public class Main {
	
	public static void main(String[] args) {
		//testLearnsetAndSpeciesCrossReferences();
		testPlatinumTrainers();
	}

	public static void testPlatinumTrainers() {
		try {
			Initialization.init(Game.PLATINUM);
			
			ArrayList<PokemonMatch> list = new ArrayList<>();
			// Gardenia 1
			list.add(new PokemonMatch(
					"LEADER_Gardenia_1", 2,
					new PokemonTarget("Roserade", "F", 22, "Naive", "Natural Cure", 
							59, 37, 30, 61, 46, 49, 
							6,
							"Sitrus Berry", null, "Grass Knot", "Magical Leaf", "Poison Sting", "Stun Spore")
					));
			
			// Volkner 1
			list.add(new PokemonMatch(
					"LEADER_Volkner_1", 3,
					new PokemonTarget("Electivire", "M", 50, "Impish", "Motor Drive", 
							137, 129, 94, 98, 97, 109, 
							4, 3, 29, 18, 15, 19, 
							"Sitrus Berry", null, "Thunder Punch", "Fire Punch", "Quick Attack", "Giga Impact")
					));
			// E4 Barry
			list.add(new PokemonMatch(
					"RIVAL_Barry_EMPOLEON_3", 5, 
					new PokemonTarget("Empoleon", "M", 51, "Quiet", "Torrent", 
							158, 104, 107, 143, 120, 70, 
							24, 
							null, null, "Brine", "Aerial Ace", "Metal Claw", "Shadow Claw")
					));
			// E4 Lucian
			list.add(new PokemonMatch(
					"ELITE_FOUR_Lucian_1", 1,
					new PokemonTarget("Espeon", "M", 55, "Hardy", "Synchronize", 
							153, 93, 87, 164, 126, 142, 
							30, 
							null, null, "Psychic", "Shadow Ball", "Quick Attack", "Signal Beam")
					));

			for(PokemonMatch pm : list) {
				MatchResult r = pm.tryMatch();
				System.out.println(r);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
