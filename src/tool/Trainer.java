package tool;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;

/**
 * A trainer. Also contains a static reference to all existing trainers.
 */
public class Trainer implements Battleable, Iterable<Pokemon> {
	private static HashMap<IgnoreCaseString, Trainer> trainersByName;
	private static final int MALE = 0, FEMALE = 1, DOUBLE = 2;
	
	/**
	 * Returns the trainer associated with the string, or null if there's no correspondence.
	 */
	public static Trainer getTrainerByName(String name) {
        return trainersByName.get(new IgnoreCaseString(name));
    }
	
	public static void initTrainers(Game game) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		trainersByName = new LinkedHashMap<IgnoreCaseString, Trainer>();
		
        BufferedReader in;
    	System.out.println("/resources/"+game.getTrainersFilename()); // TODO
        in = new BufferedReader(new InputStreamReader(Trainer.class
                .getResource("/resources/"+game.getTrainersFilename()).openStream())); // TODO : handle custom files ?
        
        if(game.isGen3())
        	initTrainersGen3(game, in);	
        
        else if(game == Game.DIAMOND || game == Game.PEARL)
        	initTrainersDP(game, in);
        
        else if(game == Game.PLATINUM)
        	initTrainersPt(game, in);
        
        else if(game == Game.HEARTGOLD || game == Game.SOULSILVER)
        	initTrainersHGSS(game, in);
        
        else
        	throw new UnsupportedOperationException(String.format("Game '%s' is not supported yet.", game));
    }
	
	/**
	 * Initializes trainers for Ruby, Sapphire, Emerald, FireRed and LeafGreen. <br/>
	 * RS : TODO <br/>
	 * E :  <a href="https://github.com/pret/pokeemerald/blob/56ec3b6461c23b93b23d28a1b6d4d148eb94511b/src/battle_main.c#L1961-L2016">CreateNPCTrainerParty</a>. <br/>
	 * FRLG : TODO
	 */
	private static void initTrainersGen3(Game game, BufferedReader in) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		JSONParser jsonParser = new JSONParser();
		
		Trainer.initCharValues(game); // Required in Gen 3 only, to generate parties properly
		
        JSONObject trainersDic = (JSONObject) jsonParser.parse(in);
        for(Object trainerEntryObj : trainersDic.entrySet()) {
            Trainer trainer = null;
            
        	@SuppressWarnings("unchecked")
			Map.Entry<Object, Object> trainerEntry = (Map.Entry<Object, Object>) trainerEntryObj;
        	String trainerAlias = (String) trainerEntry.getKey();
        	JSONObject trainerDic = (JSONObject) trainerEntry.getValue();
        	
        	int partyFlags = ((Long) trainerDic.get("partyFlags")).intValue();
        	String trainerClass = (String) trainerDic.get("trainerClass");
        	int baseMoney = ((Long) trainerDic.get("MONEY")).intValue();
        	int encounterMusicGender = ((Long) trainerDic.get("encounterMusic_gender")).intValue();
        	int trainerPic = ((Long) trainerDic.get("trainerPic")).intValue();
        	String trainerName = (String) trainerDic.get("trainerName");
        	
        	ArrayList<Item> items = new ArrayList<>();
        	JSONArray itemsArray = (JSONArray) trainerDic.get("items");
        	for(Object itemObj : itemsArray) {
        		Item item = Item.getItemByName((String) itemObj);
        		items.add(item);
        	}
        	
        	int doubleBattle = ((Long) trainerDic.get("doubleBattle")).intValue();
        	int aiFlags = ((Long) trainerDic.get("aiFlags")).intValue();
        	
        	List<Stat> badgeBoosts = null;
        	if (trainerDic.containsKey("badgeBoosts")) {
        		badgeBoosts = new ArrayList<Stat>();
        		JSONArray badgeBoostsArray = (JSONArray) trainerDic.get("badgeBoosts");
        		for (Object badgeBoostObj : badgeBoostsArray) {
        			Stat stat = Stat.valueOf((String) badgeBoostObj);
        			badgeBoosts.add(stat);
        		}
        	}
        	
        	//int partySize = ((Long) trainerDic.get("partySize")).intValue();
        	
        	JSONObject partyDic = (JSONObject) trainerDic.get("party");
        	String partyType = (String) partyDic.get("PARTY_TYPE");
        	
        	JSONArray partyArray = (JSONArray) partyDic.get("TRAINER_PARTY");
        	
        	// personality value logic
            int nameHash = 0;
            int personalityValue;
        	//
            
        	ArrayList<Pokemon> party = new ArrayList<Pokemon>();
        	for(Object pokemonObj : partyArray){
        		JSONObject pokemonDic = (JSONObject) pokemonObj;
        		int difficulty = ((Long) pokemonDic.get("iv")).intValue();
        		int level = ((Long) pokemonDic.get("level")).intValue();
        		String speciesString = (String) pokemonDic.get("species");
        		Species species = Species.getSpeciesByName(speciesString);
        		
        		String hashName = species.getHashName();
        		
        		Item heldItem = null;
        		if (pokemonDic.containsKey("heldItem"))
        			heldItem = Item.getItemByName((String) pokemonDic.get("heldItem"));
        		
        		Moveset moveset = null;
        		
        		if (pokemonDic.containsKey("moves")) {
        			moveset = new Moveset();
        			JSONArray movesArray = (JSONArray) pokemonDic.get("moves");
        			for(Object moveNameObj : movesArray) {
        				String moveName = (String) moveNameObj;
        				Move move = Move.getMoveByName(moveName);
        				moveset.addMove(move);
        			}
        		}
        		else
        			moveset = Moveset.defaultMoveset(species, level);
        		
        		// personality value logic
        		// doc : https://github.com/pret/pokeemerald/blob/d79e62690b0d3f898beb18eff1a33fa36ab24408/src/battle_main.c#L1961
                if (doubleBattle != 0) personalityValue = 0x80;
                else if ((encounterMusicGender & 0x80) != 0) personalityValue = 0x78;
                else personalityValue = 0x88;
                
                for(int i = 0; i < trainerName.length(); i++)
                	nameHash = (nameHash + getCharValue(trainerName.charAt(i))) & 0xFFFFFFFF;
                
        		for(int i = 0; i < hashName.length(); i++)
                	nameHash = (nameHash + getCharValue(hashName.charAt(i))) & 0xFFFFFFFF;
        		personalityValue = (personalityValue + ((nameHash << 8) & 0xFFFFFFFF)) & 0xFFFFFFFF;
        		
        		int fixedIV = difficulty * 31 / 255;
        		//

        		Gender gender = Gender.getGenderFromSpeciesAndPersonalityValue(species, personalityValue);
        		Nature nature = Nature.getNatureFromPersonalityValue(personalityValue);
        		Ability ability = Ability.getAbilityFromPersonalityValue(species, personalityValue);
        		
        		Pokemon pokemon = new Pokemon(species, gender, level, nature, ability, fixedIV, moveset, heldItem);
        		party.add(pokemon);
        	}
        	trainer = new Trainer(trainerAlias, partyFlags, trainerClass, baseMoney, encounterMusicGender, trainerPic, 
        			trainerName, items, doubleBattle != 0, aiFlags, badgeBoosts, partyType, party); // TODO : hardcoded
        	
        	trainersByName.put(new IgnoreCaseString(trainerAlias), trainer);
        }
	}
	
	/**
	 * Initializes trainers for Diamond and Pearl. <br/>
	 * DP : <a href="https://github.com/pret/pokediamond/blob/7ea0191605db83cdef56cd54a10b2cfbe7989f20/arm9/src/trainer_data.c#L276-L327">CreateNPCTrainerParty</a>.
	 * @throws ToolInternalException 
	 * @throws SecurityException 
	 */
	private static void initTrainersDP(Game game, BufferedReader in) throws FileNotFoundException, IOException, ParseException, SecurityException, ToolInternalException {
		JSONParser jsonParser = new JSONParser();
		
		TrainerClass.initTrainerClasses(game); // Decided to split trainer data & trainer classes in Gen 4
    	
    	JSONObject trainersDic = (JSONObject) jsonParser.parse(in);
        for(Object trainerEntryObj : trainersDic.entrySet()) {
            Trainer trainer = null;
            
        	@SuppressWarnings("unchecked")
			Map.Entry<Object, Object> trainerEntry = (Map.Entry<Object, Object>) trainerEntryObj;
        	String trainerAlias = (String) trainerEntry.getKey();
        	JSONObject trainerDic = (JSONObject) trainerEntry.getValue();
        	
        	int index = ((Long) trainerDic.get("index")).intValue();
        	String partyType = (String) trainerDic.get("type");
        	String trainerClassStr = (String) trainerDic.get("class");
        	TrainerClass trainerClass = TrainerClass.getTrainerClassByName(trainerClassStr);
        	int baseMoney = trainerClass.getMoney();
        	String trainerName = (String) trainerDic.get("name");
        	
        	ArrayList<Item> items = new ArrayList<>();
        	JSONArray itemsArray = (JSONArray) trainerDic.get("items");
        	for(Object itemObj : itemsArray) {
        		Item item = Item.getItemByName((String) itemObj);
        		items.add(item);
        	}
        	
        	int genderOrCount = ((Long) trainerDic.get("doubleBattle")).intValue(); // in Gen 4, "2" indicates double
        	int aiFlags = 0;
        	
        	List<Stat> badgeBoosts = null;
        	
        	JSONArray partyArray = (JSONArray) trainerDic.get("party");
        	
        	// personality value logic
            int pid_gender = (trainerClass.getGenderOrCount() == FEMALE ? 0x78 : 0x88) & 0xFFFFFFFF;
            int personality;
        	//
            
        	ArrayList<Pokemon> party = new ArrayList<Pokemon>();
        	for(Object pokemonObj : partyArray){
        		JSONObject pokemonDic = (JSONObject) pokemonObj;
        		int difficulty = ((Long) pokemonDic.get("difficulty")).intValue();
        		int level = ((Long) pokemonDic.get("level")).intValue();
        		String speciesString = (String) pokemonDic.get("species");
        		Species species = Species.getSpeciesByName(speciesString);
        		if(species == null)
        			throw new ToolInternalException(null, speciesString, "in initTrainersDP.");
        		
        		Item heldItem = null;
        		if (pokemonDic.containsKey("item"))
        			heldItem = Item.getItemByName((String) pokemonDic.get("item"));
        		
        		Moveset moveset = null;
        		if (pokemonDic.containsKey("moves")) {
        			moveset = new Moveset();
        			JSONArray movesArray = (JSONArray) pokemonDic.get("moves");
        			for(Object moveNameObj : movesArray) {
        				String moveName = (String) moveNameObj;
        				Move move = Move.getMoveByName(moveName);
        				moveset.addMove(move);
        			}
        		}
        		else
        			moveset = Moveset.defaultMoveset(species, level);
        		
        		// personality value logic
        		int seed = difficulty + level + species.getDexNum() + index;
        		Random.SetLCRNGSeed(seed);
        		for(int i = 0; i < trainerClass.getId(); i++)
        			seed = Random.LCRandom();
        		
        		personality = (seed << 8) & 0xFFFFFFFF;
        		personality = (personality + pid_gender) & 0xFFFFFFFF;
        		
        		int fixedIV = (difficulty * 31 / 255) & 0x000000FF;
        		
        		Nature nature = Nature.getNatureFromPersonalityValue(personality);
        		Ability ability = Ability.getAbilityFromPersonalityValue(species, personality);
        		Gender gender = Gender.getGenderFromSpeciesAndPersonalityValue(species, personality);
        		
        		Pokemon pokemon = new Pokemon(species, gender, level, nature, ability, fixedIV, moveset, heldItem);
        		party.add(pokemon);
        		//
        	}
        	trainer = new Trainer(trainerAlias, 0, trainerClassStr, baseMoney, 0, 0,
        			trainerName, items, genderOrCount == 2, aiFlags, badgeBoosts, partyType, party); // TODO : hardcoded
        	
        	trainersByName.put(new IgnoreCaseString(trainerAlias), trainer);
        }
	}
	
	/**
	 *  Initializes trainers for Platinum. <br/>
	 *  Pt : Not decompiled, but based off HGSS comments in <a href="https://github.com/pret/pokeheartgold/blob/decd3cd653c535358a7f01a68cbc27a5823601a6/src/trainer_data.c#L281-L347">CreateNPCTrainerParty</a>.
	 */
	private static void initTrainersPt(Game game, BufferedReader in) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		// TODO: not properly implemented yet
		JSONParser jsonParser = new JSONParser();
		
		TrainerClass.initTrainerClasses(game); // Decided to split trainer data & trainer classes in Gen 4
    	
    	JSONObject trainersDic = (JSONObject) jsonParser.parse(in);
        for(Object trainerEntryObj : trainersDic.entrySet()) {
            Trainer trainer = null;
            
        	@SuppressWarnings("unchecked")
			Map.Entry<Object, Object> trainerEntry = (Map.Entry<Object, Object>) trainerEntryObj;
        	String trainerAlias = (String) trainerEntry.getKey();
        	JSONObject trainerDic = (JSONObject) trainerEntry.getValue();
        	
        	int index = ((Long) trainerDic.get("index")).intValue();
        	String partyType = (String) trainerDic.get("type");
        	String trainerClassStr = (String) trainerDic.get("class");
        	TrainerClass trainerClass = TrainerClass.getTrainerClassByName(trainerClassStr);
        	int baseMoney = trainerClass.getMoney();
        	String trainerName = (String) trainerDic.get("name");
        	
        	ArrayList<Item> items = new ArrayList<>();
        	JSONArray itemsArray = (JSONArray) trainerDic.get("items");
        	for(Object itemObj : itemsArray) {
        		Item item = Item.getItemByName((String) itemObj);
        		items.add(item);
        	}
        	
        	int isDoubleInt = ((Long) trainerDic.get("double")).intValue(); // in Gen 4, "2" indicates double
        	
        	int aiFlags = 0;
        	
        	List<Stat> badgeBoosts = null;
        	
        	JSONArray partyArray = (JSONArray) trainerDic.get("party");
        	
        	// personality value logic
            int pid_gender = (trainerClass.getGenderOrCount() == FEMALE ? 0x78 : 0x88) & 0xFFFFFFFF;
            int personality;
        	//
            
        	ArrayList<Pokemon> party = new ArrayList<Pokemon>();
        	for(Object pokemonObj : partyArray){
        		JSONObject pokemonDic = (JSONObject) pokemonObj;
        		int difficulty = ((Long) pokemonDic.get("difficulty")).intValue();
        		int level = ((Long) pokemonDic.get("level")).intValue();
        		String speciesString = (String) pokemonDic.get("species");
        		Species species = Species.getSpeciesByName(speciesString);

        		Item heldItem = null;
        		if (pokemonDic.containsKey("item"))
        			heldItem = Item.getItemByName((String) pokemonDic.get("item"));
        		
        		Moveset moveset = null;
        		if (pokemonDic.containsKey("moves")) {
        			moveset = new Moveset();
        			JSONArray movesArray = (JSONArray) pokemonDic.get("moves");
        			for(Object moveNameObj : movesArray) {
        				String moveName = (String) moveNameObj;
        				Move move = Move.getMoveByName(moveName);
        				moveset.addMove(move);
        			}
        		}
        		else
        			moveset = Moveset.defaultMoveset(species, level);
        		
        		// personality value logic
        		int speciesNum = species.getDexNum();
        		//int form = 0; // form is used for retrieving proper gender ratio, not needed for our implementation
        		
        		int seed = difficulty + level + speciesNum + index;
        		Random.SetLCRNGSeed(seed);
        		for(int i = 0; i < trainerClass.getId(); i++)
        			seed = Random.LCRandom();
        		
        		personality = (seed << 8) & 0xFFFFFFFF;
        		personality = (personality + pid_gender) & 0xFFFFFFFF;
        		
        		int fixedIV = (difficulty * 31 / 255) & 0x000000FF;
        		
        		StatsContainer ivs = null; 
        		if(fixedIV <= ContainerType.IV.getMaxPerStat())
        			ivs = new StatsContainer(ContainerType.IV, fixedIV);
        		else {
        			// Fixed IV of 32 and more are randomly rerolled.
        			// Useful for Volkner 1's Electivire, which would have a fixed IV of 303 otherwise.
        			
        			// Generate OT : https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L197-L199
        			int fixedOtId;
        			do {
        	            fixedOtId = (Random.LCRandom() | (Random.LCRandom() << 16));
        	        } while (shinyCheck(fixedOtId, personality));
        			
            		// Generate IVs : https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L223-L238
        			int iv3 = Random.LCRandom();
        			int hpIV  = iv3 & 0x1F;
        			int atkIV = (iv3 & 0x3E0) >> 5;
        			int defIV = (iv3 & 0x7C00) >> 10;
        			iv3 = Random.LCRandom();
        			int speIV  = iv3 & 0x1F;
        			int spaIV = (iv3 & 0x3E0) >> 5;
        			int spdIV = (iv3 & 0x7C00) >> 10;
        			ivs = new StatsContainer(ContainerType.IV);
        			ivs.put(Stat.HP,  hpIV);
        			ivs.put(Stat.ATK, atkIV);
        			ivs.put(Stat.DEF, defIV);
        			ivs.put(Stat.SPE, speIV);
        			ivs.put(Stat.SPA, spaIV);
        			ivs.put(Stat.SPD, spdIV);
        		}
        		

        		Nature nature = Nature.getNatureFromPersonalityValue(personality);
        		Ability ability = Ability.getAbilityFromPersonalityValue(species, personality);
        		Gender gender = Gender.getGenderFromSpeciesAndPersonalityValue(species, personality);
        		
        		Pokemon pokemon = new Pokemon(species, gender, level, nature, ability, ivs, moveset, heldItem);
        		
        		frustrationCheckAndSetFriendship(pokemon);
        		
        		party.add(pokemon);
        		//
        	}
        	trainer = new Trainer(trainerAlias, 0, trainerClassStr, baseMoney, 0, 0,
        			trainerName, items, isDoubleInt == 2, aiFlags, badgeBoosts, partyType, party); // TODO : hardcoded
        	
        	trainersByName.put(new IgnoreCaseString(trainerAlias), trainer);
        }
	}
	
	/**
	 * Returns true if the OTID and PID generate a shiny. Only useful in Platinum. <br/>
	 * HGSS (probably same in Pt) : <a href="https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L63-L68">SHINY_CHECK</a>
	 */
	private static boolean shinyCheck(int otid, int pid) {
		int val = ((otid & 0xFFFF0000) >>> 16) ^ (otid & 0xFFFF) ^ ((pid & 0xFFFF0000) >>> 16) ^ (pid & 0xFFFF);
		return 0 <= val && val < 8; // Emulates unsigned comparison to 8.
	}

	/**
	 *  Initializes trainers for HeartGold and SoulSilver. <br/>
	 *  HGSS : <a href="https://github.com/pret/pokeheartgold/blob/decd3cd653c535358a7f01a68cbc27a5823601a6/src/trainer_data.c#L281-L347">CreateNPCTrainerParty</a>.
	 */
	private static void initTrainersHGSS(Game game, BufferedReader in) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		//int index = 0;
		JSONParser jsonParser = new JSONParser();
		
		TrainerClass.initTrainerClasses(game); // Decided to split trainer data & trainer classes in Gen 4
    	
		//JSONArray trainersList = (JSONArray) jsonParser.parse(in);
    	//for(Object trainerEntryObj : trainersList) {
    	JSONObject trainersDic = (JSONObject) jsonParser.parse(in);
        for(Object trainerEntryObj : trainersDic.entrySet()) {
        	//index++;
            Trainer trainer = null;
            //String trainerAlias = null;
            //JSONObject trainerDic = null;
            
            //JSONObject trainersFakeDic = (JSONObject) trainerEntryObj;
            //for(Object entry : trainersFakeDic.entrySet()) {
            //	@SuppressWarnings("unchecked")
			//	Map.Entry<Object, Object> entry_ = (Map.Entry<Object, Object>) entry;
            //	trainerAlias = (String) entry_.getKey();
            //	trainerDic = (JSONObject) entry_.getValue();
            //}
            
        	@SuppressWarnings("unchecked")
			Map.Entry<Object, Object> trainerEntry = (Map.Entry<Object, Object>) trainerEntryObj;
        	String trainerAlias = (String) trainerEntry.getKey();
        	JSONObject trainerDic = (JSONObject) trainerEntry.getValue();
        	
        	int index = ((Long) trainerDic.get("index")).intValue();
        	String partyType = (String) trainerDic.get("type");
        	String trainerClassStr = (String) trainerDic.get("class");
        	TrainerClass trainerClass = TrainerClass.getTrainerClassByName(trainerClassStr);
        	int baseMoney = trainerClass.getMoney();
        	String trainerName = (String) trainerDic.get("name");
        	
        	ArrayList<Item> items = new ArrayList<>();
        	JSONArray itemsArray = (JSONArray) trainerDic.get("items");
        	for(Object itemObj : itemsArray) {
        		Item item = Item.getItemByName((String) itemObj);
        		items.add(item);
        	}
        	
        	int isDoubleInt = ((Long) trainerDic.get("double")).intValue(); // in Gen 4, "2" indicates double
        	
        	int aiFlags = 0;
        	
        	List<Stat> badgeBoosts = null;
        	
        	JSONArray partyArray = (JSONArray) trainerDic.get("party");
        	
        	// personality value logic
            int pid_gender = (trainerClass.getGenderOrCount() == FEMALE ? 0x78 : 0x88) & 0xFFFFFFFF;
            int personality;
        	//
            
        	ArrayList<Pokemon> party = new ArrayList<Pokemon>();
        	for(Object pokemonObj : partyArray){
        		JSONObject pokemonDic = (JSONObject) pokemonObj;
        		int difficulty = ((Long) pokemonDic.get("difficulty")).intValue();
        		int level = ((Long) pokemonDic.get("level")).intValue();
        		String speciesString = (String) pokemonDic.get("species");
        		Species species = Species.getSpeciesByName(speciesString);
        		String genderOverrideStr = (String) pokemonDic.get("genderOverride");
        		String abilityOverrideStr = (String) pokemonDic.get("abilityOverride");

        		Item heldItem = null;
        		if (pokemonDic.containsKey("item"))
        			heldItem = Item.getItemByName((String) pokemonDic.get("item"));
        		
        		Moveset moveset = null;
        		if (pokemonDic.containsKey("moves")) {
        			moveset = new Moveset();
        			JSONArray movesArray = (JSONArray) pokemonDic.get("moves");
        			for(Object moveNameObj : movesArray) {
        				String moveName = (String) moveNameObj;
        				Move move = Move.getMoveByName(moveName);
        				moveset.addMove(move);
        			}
        		}
        		else
        			moveset = Moveset.defaultMoveset(species, level);
        		
        		// personality value logic
        		int speciesNum = species.getDexNum();
        		//int form = 0; // form is used for retrieving proper gender ratio, not needed for our implementation
        		
        		// PIDGender override
        		int genderAbilityOverride = getGenderAbilityOverride(genderOverrideStr, abilityOverrideStr);
        		pid_gender = overridePIDGender(species, genderAbilityOverride, pid_gender);
        		
        		
        		int seed = difficulty + level + speciesNum + index;
        		Random.SetLCRNGSeed(seed);
        		for(int i = 0; i < trainerClass.getId(); i++)
        			seed = Random.LCRandom();
        		
        		personality = (seed << 8) & 0xFFFFFFFF;
        		personality = (personality + pid_gender) & 0xFFFFFFFF;
        		
        		int fixedIV = (difficulty * 31 / 255) & 0x000000FF;

        		Nature nature = Nature.getNatureFromPersonalityValue(personality);
        		Ability ability = Ability.getAbilityFromPersonalityValue(species, personality);
        		Gender gender = Gender.getGenderFromSpeciesAndPersonalityValue(species, personality);
        		
        		Pokemon pokemon = new Pokemon(species, gender, level, nature, ability, fixedIV, moveset, heldItem);
        		
        		frustrationCheckAndSetFriendship(pokemon);
        		
        		party.add(pokemon);
        		//
        	}
        	trainer = new Trainer(trainerAlias, 0, trainerClassStr, baseMoney, 0, 0,
        			trainerName, items, isDoubleInt == 2, aiFlags, badgeBoosts, partyType, party); // TODO : hardcoded
        	
        	trainersByName.put(new IgnoreCaseString(trainerAlias), trainer);
        }
	}
	
	/**
	 * Returns the gender and ability forced value from the given JSON-extracted strings. Emulates a read from `monSpeciesItem[i].genderAbilityOverride`. <br/>
	 * TODO : Platinum ? <br/>
	 * HGSS : <a href="https://github.com/pret/pokeheartgold/blob/master/include/trainer_data.h#L37-L43">TrainerMonSpecies.genderAbilityOverride</a>.
	 */
	private static int getGenderAbilityOverride(String genderOverrideStr, String abilityOverrideStr) throws ToolInternalException {
		int genderOverride, abilityOverride, genderAbilityOverride;
		
		// Ambiguous doc :
		// Bits 0-3: 0: No override
	    //           1: Force male
	    //           2: Force female
	    // Bits 4-7: 0: No override
	    //           1: Force ability 1
	    //           2: Force ability 2
		if(genderOverrideStr.equals("GENDER_OVERRIDE_OFF"))
			genderOverride = 0;
		else if(genderOverrideStr.equals("GENDER_OVERRIDE_FEMALE"))
			genderOverride = 2;
		else if(genderOverrideStr.equals("GENDER_OVERRIDE_MALE")) // Never matches overworld trainers
			genderOverride = 1;
		else
			throw new ToolInternalException(Trainer.class.getEnclosingMethod(), genderOverrideStr, "");
		
		if(abilityOverrideStr.equals("ABILITY_OVERRIDE_OFF"))
			abilityOverride = 0;
		else if(abilityOverrideStr.equals("ABILITY_OVERRIDE_SECOND"))
			abilityOverride = 2;
		else if(abilityOverrideStr.equals("ABILITY_OVERRIDE_FIRST")) // Never matches overworld trainers
			abilityOverride = 1;
		else
			throw new ToolInternalException(Trainer.class.getEnclosingMethod(), abilityOverrideStr, "");
		
		genderAbilityOverride = abilityOverride << 4 | genderOverride;
		return genderAbilityOverride;
	}
	
	/**
	 * Returns the new personality value, based on gender and ability forced values. <br/>
	 * HGSS only : <a href="https://github.com/pret/pokeheartgold/blob/decd3cd653c535358a7f01a68cbc27a5823601a6/src/trainer_data.c#L435">TrMon_OverridePidGender</a>.
	 */
	private static int overridePIDGender(Species species, int genderAbilityOverride, int pid) {
		int genderOverride = genderAbilityOverride & 0xF;
		int abilityOverride = (genderAbilityOverride & 0xF0) >>> 4;
		
		if(genderAbilityOverride != 0) {
			if(genderOverride != 0) {
				pid = getGenderRatio_handleAlternateForm(species);
				if(genderOverride == 1)
					pid += 2;
				else
					pid -= 2;
			}
			if(abilityOverride == 1)
				pid &= 0xFFFFFFFE; // ~1 in C language, but i'm afraid of Java signed integers.
			else if(abilityOverride == 2)
				pid |= 1;
		}
		
		return pid;
	}
	
	/**
	 * Returns the gender ratio 8-bit value (since we're considering alternate forms as completely different species, it's a simple call). <br/>
	 * HGSS only : <a href="https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L1844">GetMonBaseStat_HandleAlternateForm</a> 
	 * and <a href="https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L3992">ResolveMonForm</a>.
	 */
	private static int getGenderRatio_handleAlternateForm(Species species) {
		return species.getGenderRatio().getValue();
	}
	
	/**
	 * Sets friendship to maximum (255), unless the Pokémon knows Frustration, in which case it is set to minimum (0). <br />
	 * HGSS only : <a href="https://github.com/pret/pokeheartgold/blob/decd3cd653c535358a7f01a68cbc27a5823601a6/src/trainer_data.c#L455">TrMon_FrustrationCheckAndSetFriendship</a>.
	 */
	private static void frustrationCheckAndSetFriendship(Pokemon pokemon) throws ToolInternalException {
		int friendship = Happiness.MAX;
		for(Move m : pokemon.getMoveset()) {
			if(m.matchesAny("FRUSTRATION")) {
				friendship = Happiness.MIN;
				break; // disasm doesn't break, but that's the same
			}
		}
		pokemon.setHappiness(friendship);
	}
		
	
	private static HashMap<String, Integer> charValues = null;
	
	public static void initCharValues(Game game) throws FileNotFoundException, IOException, ParseException {
		charValues = new LinkedHashMap<String, Integer>();
		
		JSONParser jsonParser = new JSONParser();
        BufferedReader in;
    	System.out.println("/resources/"+game.getCharCodesFilename()); // TODO
        in = new BufferedReader(new InputStreamReader(Trainer.class
                .getResource("/resources/"+game.getCharCodesFilename()).openStream())); // TODO : handle custom files ?
        JSONObject charCodesDic = (JSONObject) jsonParser.parse(in);
        for(Object charCodesObj : charCodesDic.entrySet()) {
            @SuppressWarnings("unchecked")
			Map.Entry<Object, Object> charCodesEntry = (Map.Entry<Object, Object>) charCodesObj;
            String charr = (String) charCodesEntry.getKey();
            int value = ((Long) charCodesEntry.getValue()).intValue();
            
            charValues.put(charr, value);
        }
	}
	
	public static int getCharValue(char c) throws ToolInternalException {
		Object intValue = charValues.get(String.valueOf(c));
		if (intValue == null)
			throw new ToolInternalException(Trainer.class.getEnclosingMethod(), c, "Not in the char values table.");

		return (int)intValue;
	}
	
	public static void printTrainers() {
		for(Map.Entry<IgnoreCaseString, Trainer> entry : trainersByName.entrySet()) {
			System.out.println(entry.getKey()+"="+entry.getValue().allPokes());
		}
	}
	
	public static void main(String[] args) {
		try {
			Game game = Game.EMERALD;
			Trainer.initCharValues(game);
			Item.initItems(game);
			Move.initMoves(game);
			Species.initSpecies(game);
			Learnset.initLearnsets(game);
			Trainer.initTrainers(game);
			System.out.println();
			//System.out.println(Trainer.getTrainerByName("BRENDAN_1").party);
			// printTrainers();
			
			game = Game.DIAMOND;
			Item.initItems(game);
			Move.initMoves(game);
			Species.initSpecies(game);
			Learnset.initLearnsets(game);
			Trainer.initTrainers(game);
			//System.out.println(Trainer.getTrainerByName("YOUNGSTER_Tristan_1").party);
			//System.out.println(Trainer.getTrainerByName("LASS_Natalie").party);
			printTrainers();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private String trainerAlias;
	private int partyFlags;
	private String trainerClass;
	private int baseMoney;
	private int encounterMusicGender;
	private int trainerPic;
	private String trainerName;
	private List<Item> items;
	private boolean isDoubleBattle;
	private int aiFlags;
	private List<Stat> badgeBoosts;
	private String partyType;
	private ArrayList<Pokemon> party;
    private int lastLevelMonBeforeReordering;

    public Trainer(String trainerAlias, int partyFlags, String trainerClass, int baseMoney, int encounterMusicGender, int trainerPic, 
    		String trainerName, List<Item> items, boolean doubleBattle, int aiFlags, List<Stat> badgeBoosts, String partyType, ArrayList<Pokemon> party) {
    	this.trainerAlias = trainerAlias;
    	this.partyFlags = partyFlags;
    	this.trainerClass = trainerClass;
    	this.baseMoney = baseMoney;
    	this.encounterMusicGender = encounterMusicGender;
    	this.trainerPic = trainerPic;
    	this.trainerName = trainerName;
    	this.items = items;
    	this.isDoubleBattle = doubleBattle;
    	this.aiFlags = aiFlags;
    	this.badgeBoosts = badgeBoosts;
    	this.partyType = partyType;
    	this.party = party;
    	this.lastLevelMonBeforeReordering = party.get(party.size() - 1).getLevel();
    }
    
    public String getTrainerAlias() {
		return trainerAlias;
	}

	public int getPartyFlags() {
		return partyFlags;
	}

	public String getTrainerClass() {
		return trainerClass;
	}

	public int getBaseMoney() {
		return baseMoney;
	}

	public int getEncounterMusicGender() {
		return encounterMusicGender;
	}

	public int getTrainerPic() {
		return trainerPic;
	}

	public String getTrainerName() {
		return trainerName;
	}

	public List<Item> getItems() {
		return items;
	}

	public boolean isDoubleBattle() {
		return isDoubleBattle;
	}

	public int getAiFlags() {
		return aiFlags;
	}

	public String getPartyType() {
		return partyType;
	}

	public ArrayList<Pokemon> getParty() {
		return party;
	}

	public int getReward(Pokemon p) {
		int moneyItemMult = 1;
		if(p.getHeldItem() != null && p.getHeldItem().isMoneyBoosting()) {
				moneyItemMult = 2; // TODO: hardcoded
		}
		int moneyBattleTypeMult = isDoubleBattle() ? 2 : 1; // TODO: hardcoded
		
		return 4 * lastLevelMonBeforeReordering * baseMoney * moneyItemMult * moneyBattleTypeMult;
	}

	public List<Stat> getBadgeBoosts() {
		return badgeBoosts;
	}

	public void setParty(Collection<Pokemon> party) {
        this.party = new ArrayList<>(party);
    }

    @Override
    public void battle(Pokemon p, BattleOptions options) throws ToolInternalException {
        for (Pokemon tp : party) {
            tp.battle(p, options);
        }
    }

    @Override
    public Iterator<Pokemon> iterator() {
        return party.iterator();
    }
    
    public String toString(Pokemon p, BattleOptions options) {
    	StringBuffer sb = new StringBuffer();
    	boolean isMultiplyingRewardByTwo = options.isMultiplyingRewardByTwo();
    	sb.append(String.format("%s %s%s%s | REWARD: %s", 
    			trainerClass,
    			trainerName, 
    			options.getPartner(Side.ENEMY) == null ? "" : String.format(" + %s %s", options.getPartner(Side.ENEMY).getTrainerClass(), options.getPartner(Side.ENEMY).getTrainerName()),
    			isMultiplyingRewardByTwo || options.getPartner(Side.ENEMY) != null ? " <DOUBLE>" : "",
    			options.isBacktrackingAfterBattle() ? "0 (backtrack)" : getReward(p) + (options.getPartner(Side.ENEMY) == null ? 0 : options.getPartner(Side.ENEMY).getReward(p))));
    	sb.append(Constants.endl);
    	if (items.size() > 0) {
    		sb.append("[");
    		StringBuffer sbItems = new StringBuffer();
	    	for (Item item : items) {
	    		sbItems.append(item.getDisplayName());
	    		sbItems.append(", ");
	    	}
	    	sb.append(sbItems.delete(sbItems.length()-2, sbItems.length()).toString()); // TODO: hardcoded based on the length of string ", "
	    	sb.append("] ");
    	}
    	sb.append(String.format("{%s}", allPokes()));
    	
    	return sb.toString();
    }

    /*
    public String toDebugString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(String.format("%s %d %s %d %d %d %s ", 
    			trainerAlias, partyFlags, trainerClass, baseMoney, encounterMusicGender, trainerPic, trainerName));
    	for (Item item : items) {
    		sb.append(item.getDisplayName());
    		sb.append(" ");
    	}
    	sb.append(String.format("%d %d %s %d", doubleBattle, aiFlags, partyType, reward));
    	
    	return sb.toString();
    }
    */

    public String allPokes() {
        StringBuilder sb = new StringBuilder();
        for (Pokemon p : party) {
            sb.append(p.levelNameNatureAbilityItemStr() + ", ");
        }
        return sb.delete(sb.length()-2, sb.length()).toString(); // TODO: hardcoded based on the length of string ", "
    }

    /*
	public boolean isBoostingHappiness() {
		return getTrainerClass().isBoostingHappiness();
	}
	*/
    
}
