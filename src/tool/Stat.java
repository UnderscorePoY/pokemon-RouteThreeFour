package tool;
import java.util.Collections;
import java.util.LinkedHashSet;

public enum Stat {	
	HP, ATK, DEF, SPA, SPD, SPE, ACC, EVA;
	// TODO : list of accuracy multipliers : https://bulbapedia.bulbagarden.net/wiki/Accuracy#Generations_III_and_IV
	
	/**
	 * Follows EVs ordering : HP, Attack, Defense, Speed, SpAttack, SpDefense.
	 */
	public static LinkedHashSet<Stat> evCompliantStats;
	
	/**
	 * Follows pokémon menu ordering : HP, Attack, Defense, SpAttack, SpDefense, Speed.
	 */
	public static LinkedHashSet<Stat> pokemonMenuStats;
	
	/**
	 * Primary battle stats : Attack, Defense, SpAttack, SpDefense, Speed.
	 */
	public static LinkedHashSet<Stat> primaryStagesStats;
	
	/**
	 * Secondary battle stats : Accuracy, Evasion.
	 */
	public static LinkedHashSet<Stat> secondaryStagesStats;
	
	/**
	 * Primary then secondary battle stats : Attack, Defense, SpAttack, SpDefense, Speed, Accuracy, Evasion.
	 */
	public static LinkedHashSet<Stat> stagesStats;
	
	static { // TODO: Disgusting, but at least that avoids desync between stats
		evCompliantStats = new LinkedHashSet<Stat>();
		Collections.addAll(evCompliantStats, HP, ATK, DEF, SPE, SPA, SPD);
		
		pokemonMenuStats = new LinkedHashSet<Stat>();
		Collections.addAll(pokemonMenuStats, HP, ATK, DEF, SPA, SPD, SPE);
		
		primaryStagesStats = new LinkedHashSet<Stat>();
		Collections.addAll(primaryStagesStats, ATK, DEF, SPA, SPD, SPE);

		secondaryStagesStats = new LinkedHashSet<Stat>();
		Collections.addAll(secondaryStagesStats, ACC, EVA);
		
		stagesStats = new LinkedHashSet<Stat>(primaryStagesStats);
		stagesStats.addAll(secondaryStagesStats);
	}	
	
	public static Stat[] allStats = values();
	
	public int getId() {
		return ordinal();
	}
	
}
