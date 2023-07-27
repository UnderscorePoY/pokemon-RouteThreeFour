package test.pokemon_matcher;

import java.util.ArrayList;

import tool.Pokemon;
import tool.Trainer;

public class PokemonMatch {
	private String trainerName;
	private int indexInParty;
	private PokemonTarget pTest;
	
	public PokemonMatch(String trainerName, int indexInParty, PokemonTarget pTest) {
		this.trainerName = trainerName;
		this.indexInParty = indexInParty;
		this.pTest = pTest;
	}
	
	public MatchResult tryMatch() {
		ArrayList<Pokemon> trainerParty = Trainer.getTrainerByName(trainerName).getParty();
		MatchResult r = pTest.tryMatch(trainerParty.get(indexInParty));
		r.setTrainerName(trainerName);
		return r;
	}
	
}
