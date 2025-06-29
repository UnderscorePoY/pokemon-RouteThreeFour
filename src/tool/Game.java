package tool;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Game {
	// GEN 3
	RUBY(
	/*display name*/	"Ruby", 
	/*       name */	"ruby", 
	/*  charCodes */	"char_values_rsefrlg.json",  
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_rs.json",
	/*      moves */	"move_data_rsefrlg.json",
	/*  learnsets */	"learnset_data_rs.json",
	/*   trainers */	"trainer_data_rs.json",
	/* tr classes */	null,
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	SAPPHIRE(
	/*display name*/	"Sapphire", 
	/*        name*/	"sapphire", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_rs.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_rs.json", 
	/*   trainers */	"trainer_data_rs.json", 
	/* tr classes */	null,
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	
	EMERALD(
	/*display name*/	"Emerald", 
	/*        name*/	"emerald", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_e.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_e.json", 
	/*   trainers */	"trainer_data_e.json", 
	/* tr classes */	null,
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	
	FIRERED(
	/*display name*/	"Fire Red", 
	/*        name*/	"firered", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_fr.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_fr.json", 
	/*   trainers */	"trainer_data_frlg.json", 
	/* tr classes */	null,
	/*      langs */	new Language[] {Language.ENGLISH, Language.FRENCH}
	),	
	LEAFGREEN(
	/*display name*/	"Leaf Green", 
	/*        name*/	"leafgreen", 
	/*  charCodes */	"char_values_rsefrlg.json", 
	/*      items */	"item_data_rsefrlg.json", 
	/*    species */	"species_data_lg.json", 
	/*      moves */	"move_data_rsefrlg.json", 
	/*  learnsets */	"learnset_data_lg.json", 
	/*   trainers */	"trainer_data_frlg.json", 
	/* tr classes */	null,
	/*      langs */	new Language[] {Language.ENGLISH, Language.FRENCH}
	),
	
	// GEN 4
	DIAMOND(
	/*display name*/	"Diamond", 
	/*        name*/	"diamond",
	/*  charCodes */	null, 
	/*      items */	"item_data_dp.json", 
	/*    species */	"species_data_dp.json", 
	/*      moves */	"move_data_dp.json",
	/*  learnsets */	"learnset_data_dp.json", 
	/*   trainers */	"trainer_data_dp.json", 
	/* tr classes */	"trainer_classes_dp.json",
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	PEARL(
	/*display name*/	"Pearl", 
	/*        name*/	"pearl",
	/*  charCodes */	null, 
	/*      items */	"item_data_dp.json", 
	/*    species */	"species_data_dp.json", 
	/*      moves */	"move_data_dp.json",
	/*  learnsets */	"learnset_data_dp.json", 
	/*   trainers */	"trainer_data_dp.json", 
	/* tr classes */	"trainer_classes_dp.json",
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	
	PLATINUM(
	/*display name*/	"Platinum", 
	/*        name*/	"platinum",
	/*  charCodes */	null, 
	/*      items */	"item_data_pt.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_pt.json", 
	/*   trainers */	"trainer_data_pt.json", 
	/* tr classes */	"trainer_classes_pt.json",
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	
	HEARTGOLD(
	/*display name*/	"Heart Gold", 
	/*        name*/	"heartgold",
	/*  charCodes */	null, 
	/*      items */	"item_data_hgss.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_hgss.json", 
	/*   trainers */	"trainer_data_hgss.json", 
	/* tr classes */	"trainer_classes_hgss.json",
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	SOULSILVER(
	/*display name*/	"Soul Silver", 
	/*        name*/	"soulsilver",
	/*  charCodes */	null, 
	/*      items */	"item_data_hgss.json", 
	/*    species */	"species_data_pthgss.json", 
	/*      moves */	"move_data_pthgss.json",
	/*  learnsets */	"learnset_data_hgss.json", 
	/*   trainers */	"trainer_data_hgss.json", 
	/* tr classes */	"trainer_classes_hgss.json",
	/*      langs */	new Language[] {Language.ENGLISH}
	),
	;
	
	//CUSTOM("custom", null, null, null, null, null, null);

	private final String displayName;
	private final String name;
	private final String charCodesFilename;
	private final String itemsFilename;
	private final String speciesFilename;
	private final String movesFilename;
	private final String learnsetsFilename;
	private final String trainersFilename;
	private final String trainerClassesFilename;
	private final Language[] supportedLanguages;
	
	private Game(
			String displayName, String name, String charCodesFilename, String itemsFilename, 
			String speciesFilename, String movesFilename, String learnsetsFilename, 
			String trainersFilename, String trainerClassesFilename, Language[] supportedLanguages
	) {
		this.displayName = displayName;
		this.name = name;
		this.charCodesFilename = charCodesFilename;
		this.itemsFilename = itemsFilename;
		this.speciesFilename = speciesFilename;
		this.movesFilename = movesFilename;
		this.learnsetsFilename = learnsetsFilename;
		this.trainersFilename = trainersFilename;
		this.trainerClassesFilename = trainerClassesFilename;
		this.supportedLanguages = supportedLanguages;
	}
	
	public String getDisplayName() {
		return displayName;
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
	
	public Language[] getSupportedLanguages() {
		return this.supportedLanguages;
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
	
	private boolean isImplementedGame(Language lang) {
		return (
			itemsFilename != null 
			&& speciesFilename != null 
			&& movesFilename != null 
			&& learnsetsFilename != null 
			&& trainersFilename != null
			&& Arrays.asList(supportedLanguages).contains(lang)
		);
	}
	
	private static final String gameSep = ", "; 
	private static final String langSep = "/"; 
	public static String supportedGameNamesWithLanguages() {
		StringBuilder sb = new StringBuilder();
		for(Game g : values()) {
			if(g.isImplementedGame(Language.default_))
				sb.append(g.getName());
				sb.append(" ");
			
			StringBuilder langSb = new StringBuilder();
			List<Language> langList = Arrays.asList(g.supportedLanguages);
			for(Language lang : langList) {
				if(g.isImplementedGame(lang)) {
					langSb.append(lang.getName());
					langSb.append(langSep);
				}
			}
			String langStr = "(" + langSb.substring(0, langSb.length() - langSep.length()) + ")";
			sb.append(langStr);
			sb.append(gameSep);
		}
		
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - gameSep.length());
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
