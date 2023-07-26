package test;

import java.util.ArrayList;

import tool.Pokemon;
import tool.Trainer;

public class PokemonMatcher {
	private String trainerName;
	private int indexInParty;
	private PokemonTest pTest;
	
	public PokemonMatcher(String trainerName, int indexInParty, PokemonTest pTest) {
		this.trainerName = trainerName;
		this.indexInParty = indexInParty;
		this.pTest = pTest;
	}
	
	public Result tryMatch() {
		ArrayList<Pokemon> trainerParty = Trainer.getTrainerByName(trainerName).getParty();
		Result r = pTest.tryMatch(trainerParty.get(indexInParty));
		r.setTrainerName(trainerName);
		return r;
	}
	
}
