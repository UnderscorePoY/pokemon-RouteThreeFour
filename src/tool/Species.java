package tool;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tool.exception.ToolInternalException;

public class Species {
	private static HashMap<IgnoreCaseString, Species> speciesByName;
	
	/**
	 * Return the species associated with the string, or null if there's no correspondence.
	 */
	public static Species getSpeciesByName(String name) {
        return speciesByName.get(new IgnoreCaseString(name));
    }
	
	
	public static void initSpecies(Game game) throws FileNotFoundException, ToolInternalException, IOException, ParseException {
		speciesByName = new LinkedHashMap<IgnoreCaseString, Species>();

        BufferedReader in;
        String speciesResourcePathName = Settings.getResourceRelativePathName(game.getSpeciesFilename());
        in = new BufferedReader(new InputStreamReader(Species.class.getResource(
        		speciesResourcePathName).openStream())); // TODO : handle custom files ?
        
    	if(game.isGen3())
    		initSpeciesGen3(in);
    	else if (game.isGen4())
    		initSpeciesGen4(in);
    	else
    		throw new ToolInternalException(Species.class.getEnclosingMethod(), game, "");

        System.out.println(String.format("INFO: Species loaded from '%s'", speciesResourcePathName));
    }
	
	private static void initSpeciesGen3(BufferedReader in) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
        Species species = null;
        JSONArray array = (JSONArray) jsonParser.parse(in);
        for(Object speciesObj : array) {
        	JSONObject speciesDic = (JSONObject) speciesObj;
        	String name = (String) speciesDic.get("name");
        	String hashName;
        	if (speciesDic.containsKey("hashName")) {
        		hashName = (String) speciesDic.get("hashName");
        	} else {
        		hashName = name;
        	}
        	String displayName = (String) speciesDic.get("displayName");
        	// System.out.println(name + " " + displayName); // TODO : delete
        	int dexNum = ((Long) speciesDic.get("dexNum")).intValue();
        	
        	JSONArray baseStatsArray = (JSONArray) speciesDic.get("baseStats");
        	int baseHP = ((Long) baseStatsArray.get(Stat.HP.getId())).intValue();
        	int baseAtk = ((Long) baseStatsArray.get(Stat.ATK.getId())).intValue();
        	int baseDef = ((Long) baseStatsArray.get(Stat.DEF.getId())).intValue();
        	int baseSpa = ((Long) baseStatsArray.get(Stat.SPA.getId())).intValue();
        	int baseSpd = ((Long) baseStatsArray.get(Stat.SPD.getId())).intValue();
        	int baseSpe = ((Long) baseStatsArray.get(Stat.SPE.getId())).intValue();
        	
        	JSONArray typesArray = (JSONArray) speciesDic.get("types");
        	Type type1 = Type.valueOf((String) typesArray.get(0));
        	Type type2 = (typesArray.size() <= 1) ? Type.NONE : Type.valueOf((String) typesArray.get(1));

        	int baseExp = ((Long) speciesDic.get("baseExp")).intValue();
        	
        	JSONArray evYieldsArray = (JSONArray) speciesDic.get("evYields");
        	int HPEV = ((Long) evYieldsArray.get(Stat.HP.getId())).intValue();
        	int atkEV = ((Long) evYieldsArray.get(Stat.ATK.getId())).intValue();
        	int defEV = ((Long) evYieldsArray.get(Stat.DEF.getId())).intValue();
        	int spaEV = ((Long) evYieldsArray.get(Stat.SPA.getId())).intValue();
        	int spdEV = ((Long) evYieldsArray.get(Stat.SPD.getId())).intValue();
        	int speEV = ((Long) evYieldsArray.get(Stat.SPE.getId())).intValue();
        	
        	Gender genderRatio = Gender.valueOf((String) speciesDic.get("genderRatio"));
        	ExpCurve expCurve = ExpCurve.valueOf((String) speciesDic.get("expCurve"));
        	
        	JSONArray abilitiesArray = (JSONArray) speciesDic.get("abilities");
        	Ability ability1 = Ability.valueOf((String) abilitiesArray.get(0));
        	Ability ability2 = (abilitiesArray.size() <= 1) ? Ability.NONE : Ability.valueOf((String) abilitiesArray.get(1));
        	int weight = ((Long) speciesDic.get("weight")).intValue();
        	int friendship = ((Long) speciesDic.get("friendship")).intValue();
        	
        	species = new Species(hashName, displayName, dexNum,
            		baseHP, baseAtk, baseDef, baseSpa, baseSpd, baseSpe,
            		type1, type2, baseExp,
            		HPEV, atkEV, defEV, spaEV, spdEV, speEV,
            		genderRatio, expCurve, ability1, ability2, weight, friendship);
        			
        	speciesByName.put(new IgnoreCaseString(name), species);
      	}
	}
	
	private static void initSpeciesGen4(BufferedReader in) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		Species species = null;
	    JSONArray array = (JSONArray) jsonParser.parse(in);
	    for(Object speciesObj : array) {
        	JSONObject speciesDic = (JSONObject) speciesObj;
			String name = (String) speciesDic.get("species");
	    	String hashName = name;
	    	String displayName = name;
	    	int dexNum = ((Long) speciesDic.get("speciesId")).intValue();
	    	
	    	int baseHP = ((Long) speciesDic.get("hp")).intValue();
	    	int baseAtk = ((Long) speciesDic.get("atk")).intValue();
	    	int baseDef = ((Long) speciesDic.get("def")).intValue();
	    	int baseSpa = ((Long) speciesDic.get("spatk")).intValue();
	    	int baseSpd = ((Long) speciesDic.get("spdef")).intValue();
	    	int baseSpe = ((Long) speciesDic.get("speed")).intValue();
	    	
	    	JSONArray typesArray = (JSONArray) speciesDic.get("types");
	    	Type type1 = Type.valueOf((String) typesArray.get(0));
	    	Type type2 = (typesArray.size() <= 1) ? Type.NONE : Type.valueOf((String) typesArray.get(1));
	    	if(type2 == type1)
	    		type2 = Type.NONE;
	
	    	int baseExp = ((Long) speciesDic.get("expYield")).intValue();
	    	
	    	int HPEV = ((Long) speciesDic.get("hp_yield")).intValue();
	    	int atkEV = ((Long) speciesDic.get("atk_yield")).intValue();
	    	int defEV = ((Long) speciesDic.get("def_yield")).intValue();
	    	int spaEV = ((Long) speciesDic.get("spatk_yield")).intValue();
	    	int spdEV = ((Long) speciesDic.get("spdef_yield")).intValue();
	    	int speEV = ((Long) speciesDic.get("speed_yield")).intValue();
	    	
	    	Gender genderRatio = Gender.valueOf((String) speciesDic.get("genderRatio"));
	    	ExpCurve expCurve = ExpCurve.valueOf((String) speciesDic.get("growthRate"));
	    	
	    	JSONArray abilitiesArray = (JSONArray) speciesDic.get("abilities");
	    	Ability ability1 = Ability.valueOf((String) abilitiesArray.get(0));
	    	Ability ability2 = (abilitiesArray.size() <= 1) ? Ability.NONE : Ability.valueOf((String) abilitiesArray.get(1));
	    	if(ability2 == ability1)
	    		ability2 = Ability.NONE;
	    	int weight = ((Long) speciesDic.get("weight")).intValue();
	    	int friendship = ((Long) speciesDic.get("friendship")).intValue();
	    	
	    	species = new Species(hashName, displayName, dexNum,
	        		baseHP, baseAtk, baseDef, baseSpa, baseSpd, baseSpe,
	        		type1, type2, baseExp,
	        		HPEV, atkEV, defEV, spaEV, spdEV, speEV,
	        		genderRatio, expCurve, ability1, ability2, weight, friendship);
	    			
	    	speciesByName.put(new IgnoreCaseString(name), species);
		}
	}
	
	private static void printSpecies() {
		for(Map.Entry<IgnoreCaseString, Species> entry : speciesByName.entrySet()) {
			System.out.println(entry.getKey()+"="+entry.getValue().getDetailledStr());
		}
	}
	
	public static Set<Entry<IgnoreCaseString, Species>> entrySet(){
		return speciesByName.entrySet();
	}
	
    private String hashName;
    private String displayName;
    private int dexNum;
    private int baseHP;
    private int baseAtk;
    private int baseDef;
    private int baseSpa;
    private int baseSpd;
    private int baseSpe;
    private Type type1;
    private Type type2;
    private int baseExp;
    private int HPEV;
    private int atkEV;
    private int defEV;
    private int spaEV;
    private int spdEV;
    private int speEV;
    private Gender genderRatio;
    private ExpCurve expCurve;
    private Ability ability1;
    private Ability ability2;
    private int weight; // in hectograms
    private int friendship;

    private Species(String name, String displayName, int dexNum,
    		int baseHP, int baseAtk, int baseDef, int baseSpa, int baseSpd, int baseSpe,
    		Type type1, Type type2, int killExp,
    		int HPEV, int atkEV, int defEV, int spaEV, int spdEV, int speEV,
    		Gender gender, ExpCurve expCurve, Ability ability1, Ability ability2, int weight, int friendship) {
        this.hashName = name;
        this.displayName = displayName;
        this.dexNum = dexNum;
        this.baseHP = baseHP;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.baseSpa = baseSpa;
        this.baseSpd = baseSpd;
        this.baseSpe = baseSpe;
        this.type1 = type1;
        this.type2 = type2;
        this.baseExp = killExp;
        this.HPEV = HPEV;
        this.atkEV = atkEV;
        this.defEV = defEV;
        this.spaEV = spaEV;
        this.spdEV = spdEV;
        this.speEV = speEV;
        this.genderRatio = gender;
        this.expCurve = expCurve;
        this.ability1 = ability1;
        this.ability2 = ability2;
        this.weight = weight;
        this.friendship = friendship;
    }

    public String getHashName() {
        return hashName;
    }
    
    public String getDisplayName() {
    	return displayName;
    }

    public int getDexNum() {
        return dexNum;
    }
    
    public int getBaseStat(Stat stat) {
    	Integer ev = null;
    	switch(stat) {
    	case HP:  ev = baseHP;  break;
    	case ATK: ev = baseAtk; break;
    	case DEF: ev = baseDef; break;
    	case SPA: ev = baseSpa; break;
    	case SPD: ev = baseSpd; break;
    	case SPE: ev = baseSpe; break;
    	default:
    		throw new IllegalArgumentException(String.format("Method '%s' received stat '%s'.", 
    				Species.class.getEnclosingMethod(), stat));
    	}
    	
    	return ev;
    }
    
   
    public Type getType1() {
        return type1;
    }

    public Type getType2() {
        return type2;
    }
    
    public Type getType1ByPrecedence() {
        return Type.getType1ByPrecedence(type1, type2);
    }
    
    public Type getType2ByPrecedence() {
        return Type.getType2ByPrecedence(type1, type2);
    }


    public int getBaseExp() {
        return baseExp;
    }
    
    public int getEvYield(Stat stat) {
    	Integer ev = null;
    	switch(stat) {
    	case HP:  ev = HPEV;  break;
    	case ATK: ev = atkEV; break;
    	case DEF: ev = defEV; break;
    	case SPA: ev = spaEV; break;
    	case SPD: ev = spdEV; break;
    	case SPE: ev = speEV; break;
    	default:
    		throw new IllegalArgumentException(String.format("Method '%s' received stat '%s'.", 
    				Species.class.getEnclosingMethod(), stat));
    	}
    	
    	return ev;
    }
    
  
    public Gender getGenderRatio() {
    	return genderRatio;
    }

    public ExpCurve getExpCurve() {
        return expCurve;
    }
    
    public Ability getAbility1() {
    	return ability1;
    }
    
    public Ability getAbility2() {
    	return ability2;
    }
    
    public int getWeight() {
    	return weight;
    }
    
    public String getPossibleAbilitiesStr() {
    	return String.format("%s%s", 
    			getAbility1().noSpaces(), 
    			getAbility2() != null && getAbility2() != Ability.NONE && getAbility2() != getAbility1() ? ", "+getAbility2().noSpaces() : "");
    }
    
    @Override
    public String toString() {
    	return getDisplayName();
    }
    
    public String getDetailledStr() {
    	return String.format("{'%s' '%s' #%d [%d/%d/%d/%d/%d/%d] '%s'%s %d <%d/%d/%d/%d/%d/%d> '%s' '%s' ['%s'%s] w:%d f:%d}", 
    			hashName, displayName, dexNum, baseHP, baseAtk, baseDef, baseSpa, baseSpd, baseSpe, 
    			type1, (type2 != Type.NONE) ? "/'"+type2+"'" : "", 
    			baseExp, HPEV, atkEV, defEV, spaEV, spdEV, speEV, 
    			genderRatio, expCurve, 
    			ability1, (ability2 != Ability.NONE) ? "/'"+ability2+"'" : "", weight, friendship
    			);
    }
    
    public boolean isUpdatingStatsAfterEveryBattle() {
    	return this.matchesAny("DEOXYS", "DEOXYSATK", "DEOXYSDEF", "DEOXYSSPE");
    }
    
    // TODO : only testing purposes
    public static void main(String[] args) {
    	try {
	    	initSpecies(Game.RUBY);
	    	printSpecies();
	    	
	    	initSpecies(Game.DIAMOND);
	    	printSpecies();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @SuppressWarnings("unlikely-arg-type")
    public boolean matchesAny(String... names) {
    	if(names == null)
    		return false;
    	
    	/**
         * Returns true if the move matches any of the species in the list. Ignores non alphanumerical characters.
         */
    	IgnoreCaseString ics = new IgnoreCaseString(displayName);
    	for(String name : names) {
    		if(ics.equals(name))
    			return true;
    	}
    	
    	return false;
    }


	public int getFriendship() {
		return friendship;
	}
}
