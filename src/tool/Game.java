package tool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Game {
	// GEN 3
	RUBY(
	/*       name */	"ruby", 
	/*  charCodes */	"char_values_rsefrlg.json",  
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_rs.json",
	/*      moves */	"move_data_rsefrlg.json",
	/*  learnsets */	"learnset_data_rs.json",
	/*   trainers */	"trainer_data_rs.json",
	/* tr classes */	null
	),
	SAPPHIRE(
	/*        name*/	"sapphire", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_rs.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_rs.json", 
	/*   trainers */	"trainer_data_rs.json", 
	/* tr classes */	null
	),
	
	EMERALD(
	/*        name*/	"emerald", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_e.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_e.json", 
	/*   trainers */	"trainer_data_e.json", 
	/* tr classes */	null
	),
	
	FIRERED(
	/*        name*/	"firered", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_fr.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_fr.json", 
	/*   trainers */	"trainer_data_frlg.json", 
	/* tr classes */	null
	),	
	LEAFGREEN(
	/*        name*/	"leafgreen", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_lg.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_lg.json", 
	/*   trainers */	"trainer_data_frlg.json", 
	/* tr classes */	null
	),
	
	// GEN 4
	DIAMOND(
	/*        name*/	"diamond",
	/*  charCodes */	null, 
	/*      items */	"item_data_dp.json", 
	/*    species */	"species_data_dp.json", 
	/*      moves */	"move_data_dp.json",
	/*  learnsets */	"learnset_data_dp.json", 
	/*   trainers */	"trainer_data_dp.json", 
	/* tr classes */	"trainer_classes_dp.json"
	),
	PEARL(
	/*        name*/	"pearl",
	/*  charCodes */	null, 
	/*      items */	"item_data_dp.json", 
	/*    species */	"species_data_dp.json", 
	/*      moves */	"move_data_dp.json",
	/*  learnsets */	"learnset_data_dp.json", 
	/*   trainers */	"trainer_data_dp.json", 
	/* tr classes */	"trainer_classes_dp.json"
	),
	
	PLATINUM(
	/*        name*/	"platinum", // TODO
	/*  charCodes */	null, 
	/*      items */	"item_data_pt.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_pt.json", 
	/*   trainers */	"trainer_data_pt.json", 
	/* tr classes */	"trainer_classes_pt.json"
	),
	
	HEARTGOLD(
	/*        name*/	"heartgold", // TODO
	/*  charCodes */	null, 
	/*      items */	"item_data_hgss.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_hgss.json", 
	/*   trainers */	"trainer_data_hgss.json", 
	/* tr classes */	"trainer_classes_hgss.json"
	),
	SOULSILVER(
	/*        name*/	"soulsilver", // TODO
	/*  charCodes */	null, 
	/*      items */	"item_data_hgss.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_hgss.json", 
	/*   trainers */	"trainer_data_hgss.json", 
	/* tr classes */	"trainer_classes_hgss.json"
	),
	;
	
	//CUSTOM("custom", null, null, null, null, null, null);
	
	private final String name;
	private final String charCodesFilename;
	private final String itemsFilename;
	private final String speciesFilename;
	private final String movesFilename;
	private final String learnsetsFilename;
	private final String trainersFilename;
	private final String trainerClassesFilename;
	
	private Game(String name, String charCodesFilename, String itemsFilename, String speciesFilename, String movesFilename, String learnsetsFilename, String trainersFilename, String trainerClassesFilename) {
		this.name = name;
		this.charCodesFilename = charCodesFilename;
		this.itemsFilename = itemsFilename;
		this.speciesFilename = speciesFilename;
		this.movesFilename = movesFilename;
		this.learnsetsFilename = learnsetsFilename;
		this.trainersFilename = trainersFilename;
		this.trainerClassesFilename = trainerClassesFilename;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCharCodesFilename() {
		return charCodesFilename;
	}

	public String getItemsFilename() {
		return itemsFilename;
	}
	
	public String getSpeciesFilename() {
		return speciesFilename;
	}

	public String getMovesFilename() {
		return movesFilename;
	}

	public String getLearnsetsFilename() {
		return learnsetsFilename;
	}

	public String getTrainersFilename() {
		return trainersFilename;
	}
	
	public boolean isGen3() {
		return isRSE() || isFRLG();
	}
	
	public boolean isGen4() {
		return isDPPt() || isHGSS(); //!isGen3();
	}
	
	public boolean isRSE() {
		return this == RUBY || this == SAPPHIRE || this == EMERALD;
	}
	
	public boolean isFRLG() {
		return this == FIRERED || this == LEAFGREEN;
	}
	
	boolean isDP() {
		return this == DIAMOND || this == PEARL;
	}
	
	public boolean isDPPt() {
		return this == DIAMOND || this == PEARL || this == PLATINUM;
	}
	
	public boolean isHGSS() {
		return this == HEARTGOLD || this == SOULSILVER;
	}

	/**
	 * Returns the game associated with the string, or null if there's no correspondence.
	 */
	public static Game getGameFromStr(String value) {
		for(Game v : values())
            if(v.getName().equalsIgnoreCase(value)) return v;
		return null;
	}

	public String getTrainerClassesFilename() {
		return trainerClassesFilename;
	}
	
	private boolean isImplementedGame() {
		return itemsFilename != null && speciesFilename != null && movesFilename != null && learnsetsFilename != null && trainersFilename != null;
	}
	
	private static final String sep = ", "; 
	public static String supportedGameNames() {
		StringBuilder sb = new StringBuilder();
		for(Game g : values()) {
			if(g.isImplementedGame()) {
				sb.append(g.getName());
				sb.append(sep);
			}
		}
		
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - sep.length());
	}

	public static final Set<Game> gen34GameSet = new HashSet<>();
	public static final Set<Game> emeraldUpToHGSSGameSet = new HashSet<>();
	public static final Set<Game> gen4GameSet = new HashSet<>();
	public static final Set<Game> FRLGGameSet = new HashSet<>();
	public static final Set<Game> DPPtGameSet = new HashSet<>();
	public static final Set<Game> HGSSGameSet = new HashSet<>();
	static {
		Collections.addAll(gen34GameSet, Game.RUBY, Game.SAPPHIRE, Game.EMERALD, Game.FIRERED, Game.LEAFGREEN, Game.DIAMOND, Game.PEARL, Game.PLATINUM, Game.HEARTGOLD, Game.SOULSILVER);
		Collections.addAll(emeraldUpToHGSSGameSet, Game.EMERALD, Game.FIRERED, Game.LEAFGREEN, Game.DIAMOND, Game.PEARL, Game.PLATINUM, Game.HEARTGOLD, Game.SOULSILVER);
		Collections.addAll(gen4GameSet, Game.DIAMOND, Game.PEARL, Game.PLATINUM, Game.HEARTGOLD, Game.SOULSILVER);
		Collections.addAll(FRLGGameSet, Game.FIRERED, Game.LEAFGREEN);
		Collections.addAll(DPPtGameSet, Game.DIAMOND, Game.PEARL, Game.PLATINUM);
		Collections.addAll(HGSSGameSet, Game.HEARTGOLD, Game.SOULSILVER);
	}
}
