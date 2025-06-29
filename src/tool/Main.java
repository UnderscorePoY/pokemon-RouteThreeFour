package tool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import org.ini4j.Wini;

import tool.StatsContainer.ContainerType;
import tool.exception.config.ConfigInaccessibleException;
import tool.exception.config.ConfigMissingKeyException;
import tool.exception.config.ConfigWrongValueException;
import tool.exception.master.MasterInaccessibleException;
import tool.exception.master.MasterMissingKeyException;

public class Main {
	protected static Pokemon mainPoke = null; // TODO : Really bad, but it works for scenario handling for now
    private static StringBuilder output = new StringBuilder();
    protected static List<Exception> parsingExceptions = new ArrayList<>();
    
    private static String defaultMasterFilename = "master.ini";
    private static String defaultDebugFilename = "debug.txt";
    
    public static void append(String s) {
        output.append(s);
    }
    public static void appendln(String s) {
        output.append(s + Constants.endl);
    }
    
    public static void main(String[] args) {
    	int exitCode = 0;
        String masterFileName = (args.length > 0) ? args[0] : defaultMasterFilename;
        String backupDebugFileName = (args.length > 1) ? args[1] : defaultDebugFilename;
        
        Wini masterIni = null; // Only read
        PrintStream debugStream = null; // Only write
        Wini configIni = null; // Only read
        File routeFile = null; // Only read
        BufferedWriter outputWriter = null; // Only write
        
        try {
        	
            /* *********** */
            /* MASTER FILE */
            /* *********** */
        	
        	// Master file
        	try {
        		masterIni = new Wini(new File(masterFileName));
        	} catch(Exception e) { // TODO : clean
        		// throw new MasterException(String.format("inaccessible master file '%s'.", masterFileName));
        		throw new MasterInaccessibleException("master", masterFileName, null, null);
        	}
        	
        	// Custom debug file (optional - default name if missing)
        	String customDebugFileStr = backupDebugFileName;
        	File debugFile = null;
    		if(masterIni.get("master").containsKey("debugFile")) {
    			customDebugFileStr = masterIni.get("master","debugFile");
        		debugFile = new File(customDebugFileStr);
    		}
    		
    		try {
    			debugStream = new PrintStream(debugFile);
    		} catch (Exception e) { // TODO : clean
    			// throw new MasterException(String.format("inaccessible debug file '%s' provided in '%s'. It is probably a typo.", customDebugFileStr, masterFileName));
    			throw new MasterInaccessibleException("debug", customDebugFileStr, masterFileName, "It is probably a typo.");
    		}
        	
        	// Config file (mandatory)
        	String configFileStr = null;
    		if(!masterIni.get("master").containsKey("configFile")) { // TODO : clean
    			// throw new MasterException(String.format("missing mandatory config file in '%s', in [%s] section. Is the line commented out ?", masterFileName, "master")); // TODO: hardcoded
    			throw new MasterMissingKeyException(masterFileName, "master", "configFile", "Is the line commented out ?"); 
    		}
    		
            /* *********** */
            /* CONFIG FILE */
            /* *********** */
    		
    		configFileStr = masterIni.get("master","configFile");
    		try {
    			configIni = new Wini(new File(configFileStr));
    		} catch(Exception e) { // TODO : clean
    			// throw new MasterException(String.format("inaccessible config file '%s' provided in '%s'. It is probably a typo.", configFileStr, masterFileName));
    			throw new MasterInaccessibleException("config", configFileStr, masterFileName, "It is probably a typo.");
    		}
    		
    		// Route file (mandatory)
    		String routeFileStr = null;
			if(!configIni.get("files").containsKey("routeFile")) { // TODO : clean
    			// throw new ConfigException(String.format("missing mandatory route file in '%s', in [%s]. Is the line commented out ?", configFileStr, "files")); // TODO: hardcoded
    			throw new ConfigMissingKeyException(configFileStr, "files", "routeFile", null);
			}
			
			routeFileStr = configIni.get("files","routeFile");
			routeFile = new File(routeFileStr);
			
			if(!routeFile.exists()) { // TODO : clean
    			// throw new ConfigException(String.format("route file '%s' doesn't exist.", routeFileStr));
				throw new ConfigInaccessibleException("route", routeFileStr, configFileStr, "Doesn't exist.");
			}
			if(!routeFile.canRead()) { // TODO : clean
    			// throw new ConfigException(String.format("inaccessible route file '%s'.", routeFileStr));
				throw new ConfigInaccessibleException("route", routeFileStr, configFileStr, "Can't read.");
			}
            
    		// Output file (optional - default name if missing)
			String outputFilename = "outputs/out_"+routeFile.getName();
            if(configIni.get("files").containsKey("outputFile"))
            	outputFilename = configIni.get("files", "outputFile");
            File outputFile = new File(outputFilename);
            if(!outputFile.canWrite()) {
    			throw new ConfigInaccessibleException("output", outputFilename, configFileStr, "Can't write.");
			}
            outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8));
            
            
            /* At this point :
             * - The master ini is properly loaded
             * - A stream is opened to the debug file
             * - The config ini is properly loaded
             * - The route file exists and can be read from
             * - A writer is opened to the output file
             */
            
            // Set game & language, and initialize
            if(!configIni.get("game").containsKey("game")) { // TODO : clean
    			// throw new ConfigException(String.format("missing mandatory game in '%s', in [%s] section.", configFileStr, "game")); // TODO: hardcoded
    			throw new ConfigMissingKeyException(configFileStr, "game", "game", null);
            }
            String gameName = configIni.get("game", "game");
            Game game = Game.getGameFromStr(gameName);
            if(game == null) { // TODO : clean
    			// throw new ConfigException(String.format("invalid game name '%s' in '%s'.%sThe list of supported games is : %s.", gameName, configFileStr, 
    			//		Constants.endl, Game.supportedGameNamesWithLanguages()));
    			throw new ConfigWrongValueException(configFileStr, "game", "game", gameName, String.format("The list of supported games/languages is : %s.", Game.supportedGameNamesWithLanguages()));
            }
            
            String langName = configIni.get("game", "language");
            Language lang;
            if(langName == null)
            	lang = Language.default_;
            else
            	lang = Language.getLanguageFromStr(langName);
            
            if(lang == null) {
            	throw new ConfigWrongValueException(configFileStr, "game", "language", langName, String.format("The list of supported games/languages is : %s.", Game.supportedGameNamesWithLanguages()));
        	}
            
        	Initialization.init(game, lang); // TODO: handle each Exception separately ?

            // TODO : handle custom files ?
        	
            // Set pokemon | TODO : handle trainer party ?
        		// Species (mandatory)
        	if(!configIni.get("poke").containsKey("species")) { // TODO : clean
    			// throw new ConfigException(String.format("missing mandatory species in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
    			throw new ConfigMissingKeyException(configFileStr, "poke", "species", null);
        	}
        	
            String speciesStr = configIni.get("poke", "species");
            Species species = Species.getSpeciesByName(speciesStr);
            if(species == null) { // TODO : clean
    			// throw new ConfigException(String.format("invalid species '%s' in '%s'.", speciesStr, configFileStr));
            	throw new ConfigWrongValueException(configFileStr, "poke", "species", speciesStr, null);
            }
            
            	// Level (mandatory)
            if(!configIni.get("poke").containsKey("level")) { // TODO : clean
    			// throw new ConfigException(String.format("missing mandatory level in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            	throw new ConfigMissingKeyException(configFileStr, "poke", "level", null);
            }
            int level;
            try {
            	level = configIni.get("poke", "level", int.class);
            	if(level < Pokemon.MIN_LEVEL || level > Pokemon.MAX_LEVEL) {
            		throw new Exception();
            	}
            } catch(Exception e) {
            	throw new ConfigWrongValueException(configFileStr, "poke", "level", configIni.get("poke", "level"), String.format("must be an integer between '%d' and '%d' inclusive.", Pokemon.MIN_LEVEL, Pokemon.MAX_LEVEL));
            }
            
            	// Gender (mandatory)
            if(!configIni.get("poke").containsKey("gender")) { // TODO : clean
    			//throw new ConfigException(String.format("missing mandatory gender in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
    			throw new ConfigMissingKeyException(configFileStr, "poke", "gender", null);
            }
            
            String genderStr = configIni.get("poke", "gender");
            Gender gender = Gender.getGenderFromStr(genderStr);
            if(gender == null) { // TODO : clean
    			// throw new ConfigException(String.format("invalid gender '%s' in '%s'.", genderStr, configFileStr));
            	throw new ConfigWrongValueException(configFileStr, "poke", "gender", genderStr, null);
            }
            
            	// Nature (mandatory)
            if(!configIni.get("poke").containsKey("nature")) { // TODO : clean
    			//throw new ConfigException(String.format("missing mandatory nature in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
    			throw new ConfigMissingKeyException(configFileStr, "poke", "nature", null);
            }
            String natureStr = configIni.get("poke", "nature");
            Nature nature = Nature.getNatureFromString(natureStr);
            if(nature == null) { // TODO : clean
    			// throw new ConfigException(String.format("invalid nature '%s' in '%s'.", natureStr, configFileStr));
            	throw new ConfigWrongValueException(configFileStr, "poke", "nature", natureStr, null);
            }
            
            	// Ability (mandatory)
            if(!configIni.get("poke").containsKey("ability")) { // TODO : clean
    			throw new ConfigMissingKeyException(configFileStr, "poke", "ability", null);
				//throw new ConfigException(String.format("missing mandatory ability in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            }
            
            String abilityStr = configIni.get("poke", "ability");
            Ability ability = Ability.getAbilityFromString(abilityStr);
            if(ability == null) { // TODO : clean
    			// throw new ConfigException(String.format("invalid ability '%s' in '%s'.", abilityStr, configFileStr));
            	throw new ConfigWrongValueException(configFileStr, "poke", "ability", abilityStr, null);
            }
            if(ability != species.getAbility1() && ability != species.getAbility2()) { // TODO : clean
    			// throw new ConfigException(String.format("%s ability can only be %s%s.", species, species.getAbility1(),
    			// 		species.getAbility2() == Ability.NONE ? "" : String.format(" or %s", species.getAbility2())));
            	throw new ConfigWrongValueException(configFileStr, "poke", "ability", abilityStr, 
            			String.format("%s ability can only be %s%s.", 
            					species, species.getAbility1(),
            			 		species.getAbility2() == Ability.NONE ? "" : String.format(" or %s", species.getAbility2())
		 				)
            	);
            }
            	

            	// IVs (mandatory)
            if(!configIni.get("poke").containsKey("hpIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "hpIV", null);
            if(!configIni.get("poke").containsKey("atkIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "atkIV", null);
            if(!configIni.get("poke").containsKey("defIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "defIV", null);
            if(!configIni.get("poke").containsKey("spaIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "spaIV", null);
            if(!configIni.get("poke").containsKey("spdIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "spdIV", null);
            if(!configIni.get("poke").containsKey("speIV"))
            	throw new ConfigMissingKeyException(configFileStr, "poke", "speIV", null);
            
            int hpIV;
            
        	String hpIVStr = null;
            try {
            	hpIVStr = (String) configIni.get("poke", "hpIV");
            	hpIV = Integer.parseInt(hpIVStr);
            	if(!ContainerType.IV.isInStatBound(hpIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "hpIV", hpIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }
            
            int atkIV;
        	String atkIVStr = null;
            try {
            	atkIVStr = (String) configIni.get("poke", "atkIV");
            	atkIV = Integer.parseInt(atkIVStr);
            	if(!ContainerType.IV.isInStatBound(atkIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "atkIV", atkIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }
            
            int defIV;
        	String defIVStr = null;
            try {
            	defIVStr = (String) configIni.get("poke", "defIV");
            	defIV = Integer.parseInt(defIVStr);
            	if(!ContainerType.IV.isInStatBound(defIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "defIV", defIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }
            
            int spaIV;
        	String spaIVStr = null;
            try {
            	spaIVStr = (String) configIni.get("poke", "spaIV");
            	spaIV = Integer.parseInt(spaIVStr);
            	if(!ContainerType.IV.isInStatBound(spaIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "spaIV", spaIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }
            
            int spdIV;
        	String spdIVStr = null;
            try {
            	spdIVStr = (String) configIni.get("poke", "spdIV");
            	spdIV = Integer.parseInt(spdIVStr);
            	if(!ContainerType.IV.isInStatBound(spdIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "spdIV", spdIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }
            
            int speIV;
        	String speIVStr = null;
            try {
            	speIVStr = (String) configIni.get("poke", "speIV");
            	speIV = Integer.parseInt(speIVStr);
            	if(!ContainerType.IV.isInStatBound(speIV))
            		throw new Exception();
            } catch(Exception e) {
        		throw new ConfigWrongValueException(configFileStr, "poke", "speIV", speIVStr,
        				String.format("must be an integer between '%s' or '%s'.", ContainerType.IV.getMinPerStat(), ContainerType.IV.getMaxPerStat()));
            }

            StatsContainer ivs = new StatsContainer(ContainerType.IV);
            ivs.put(Stat.HP, hpIV);
            ivs.put(Stat.ATK, atkIV);
            ivs.put(Stat.DEF, defIV);
            ivs.put(Stat.SPA, spaIV);
            ivs.put(Stat.SPD, spdIV);
            ivs.put(Stat.SPE, speIV);
            
        	// Evs (optional)
	        int hpEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("hpEV")) {
		    	String hpEVStr = null;
		        try {
		        	hpEVStr = (String) configIni.get("poke", "hpEV");
		        	hpEV = Integer.parseInt(hpEVStr);
		        	if(!ContainerType.EV.isInStatBound(hpEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "hpEV", hpEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
	        int atkEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("atkEV")) {
		    	String atkEVStr = null;
		        try {
		        	atkEVStr = (String) configIni.get("poke", "atkEV");
		        	atkEV = Integer.parseInt(atkEVStr);
		        	if(!ContainerType.EV.isInStatBound(atkEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "atkEV", atkEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
	        int defEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("defEV")) {
		    	String defEVStr = null;
		        try {
		        	defEVStr = (String) configIni.get("poke", "defEV");
		        	defEV = Integer.parseInt(defEVStr);
		        	if(!ContainerType.EV.isInStatBound(defEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "defEV", defEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
	        int spaEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("spaEV")) {
		    	String spaEVStr = null;
		        try {
		        	spaEVStr = (String) configIni.get("poke", "spaEV");
		        	spaEV = Integer.parseInt(spaEVStr);
		        	if(!ContainerType.EV.isInStatBound(spaEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "spaEV", spaEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
	        int spdEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("spdEV")) {
		    	String spdEVStr = null;
		        try {
		        	spdEVStr = (String) configIni.get("poke", "spdEV");
		        	spdEV = Integer.parseInt(spdEVStr);
		        	if(!ContainerType.EV.isInStatBound(spdEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "spdEV", spdEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
	        int speEV = ContainerType.EV.getDefaultValue();
	        if(configIni.get("poke").containsKey("speEV")) {
		    	String speEVStr = null;
		        try {
		        	speEVStr = (String) configIni.get("poke", "speEV");
		        	speEV = Integer.parseInt(speEVStr);
		        	if(!ContainerType.EV.isInStatBound(speEV))
		        		throw new Exception();
		        } catch(Exception e) {
		    		throw new ConfigWrongValueException(configFileStr, "poke", "speEV", speEVStr,
		    				String.format("must be an integer between '%s' or '%s'.", ContainerType.EV.getMinPerStat(), ContainerType.EV.getMaxPerStat()));
		        }
	        }
	        
            
            	// Boosted EXP (optional)
            boolean hasBoostedExp = false; // TODO : hardcoded
            boolean isInternationalTraded = false;
            if(configIni.get("poke").containsKey("boostedExp")) {
            	String boostedExpStr = (String) configIni.get("poke", "boostedExp");
            	
            	if(boostedExpStr.equalsIgnoreCase("international")) { // TODO: hardcoded
            		if(!game.isDP()) {
            			throw new ConfigWrongValueException(configFileStr, "poke", "boostedExp", boostedExpStr,
                				String.format("'%s' is not available in '%s'.", "international", game.getName())
        				);
            		}
        			hasBoostedExp = true;
        			isInternationalTraded = true;
            	} else {
            		// Boolean.parseBoolean is too inclusive when it comes to non-true values ...
            		if(null == Utils.parseBoolean(boostedExpStr.toLowerCase())) {
            			throw new ConfigWrongValueException(configFileStr, "poke", "boostedExp", boostedExpStr,
        				String.format("must be either '%s' or '%s'.%s", true, false, !game.isDP() ? "" : String.format("In Diamond/Pearl, '%s' is available for international trades", "international"))
        				);
            		}
            	}
            }
            
        		// Pokerus (optional)
            boolean hasPokerus = false; // TODO : hardcoded
            if(configIni.get("poke").containsKey("pokerus")) {
            	String pokerusStr = (String) configIni.get("poke", "pokerus");
            	if(null == Utils.parseBoolean(pokerusStr.toLowerCase())) {
        			throw new ConfigWrongValueException(configFileStr, "poke", "pokerus", pokerusStr,
    				String.format("must be either '%s' or '%s'.", true, false)
    				);
        		}
        	}

            // Main Pokémon instanciation
        	mainPoke = new Pokemon(species, gender, level, nature, ability, ivs, hasBoostedExp, hasPokerus);
        	mainPoke.setInternationalTraded(isInternationalTraded);
        	mainPoke.setEV(Stat.HP, hpEV);
        	mainPoke.setEV(Stat.ATK, atkEV);
        	mainPoke.setEV(Stat.DEF, defEV);
        	mainPoke.setEV(Stat.SPA, spaEV);
        	mainPoke.setEV(Stat.SPD, spdEV);
        	mainPoke.setEV(Stat.SPE, speEV);
        	mainPoke.updateEVsAndCalculateStats();

            // Other non-mandatory options
            if(configIni.get("util").containsKey("defaultOutputDetails")) {
            	String verboseLevelStr = null;
            	VerboseLevel verboseLevel;
            	try{
            		verboseLevelStr = (String) configIni.get("util", "defaultOutputDetails");
            		int verboseLevelInt = Integer.parseInt(verboseLevelStr);
            		verboseLevel = VerboseLevel.values()[verboseLevelInt];
            	} catch (Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultOutputDetails", verboseLevelStr,
            				String.format("must be an integer between '%s' and '%s'.", VerboseLevel.NONE.ordinal(), VerboseLevel.EVERYTHING.ordinal()));
            	}
        		Settings.verboseLevel = verboseLevel;
            }
            
            if(configIni.get("util").containsKey("defaultShowStatsOnLevelUp")) {
            	String defaultShowStatsOnLevelUpStr = (String) configIni.get("util", "defaultShowStatsOnLevelUp");
        		Boolean showStatsOnLevelUp = Utils.parseBoolean(defaultShowStatsOnLevelUpStr);
        		if(null == showStatsOnLevelUp) {
        			throw new ConfigWrongValueException(configFileStr, "util", "defaultShowStatsOnLevelUp", defaultShowStatsOnLevelUpStr, 
        				String.format("must be either '%s' or '%s'.", true, false));
        		}
        		Settings.showStatsOnLevelUp = showStatsOnLevelUp;
            }
            
            if(configIni.get("util").containsKey("defaultShowStatRangesOnLevelUp")) {
            	String defaultShowStatRangesOnLevelUpStr = (String) configIni.get("util", "defaultShowStatRangesOnLevelUp");
            	Boolean defaultShowStatRangesOnLevelUp = Utils.parseBoolean(defaultShowStatRangesOnLevelUpStr);
            	if(null == defaultShowStatRangesOnLevelUp) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultShowStatRangesOnLevelUp", defaultShowStatRangesOnLevelUpStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            	Settings.showStatRangesOnLevelUp = defaultShowStatRangesOnLevelUp;
            }
            
            if(configIni.get("util").containsKey("defaultIvVariation")) {
            	String defaultIvVariationStr = (String) configIni.get("util", "defaultIvVariation");
            	Boolean defaultIvVariation = Utils.parseBoolean(defaultIvVariationStr);
            	if(null == defaultIvVariation) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultIvVariation", defaultIvVariationStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            	Settings.defaultIvVariation = defaultIvVariation;
            }
            
            if(configIni.get("util").containsKey("overallChanceKO")) {
            	String overallChanceKOStr = (String) configIni.get("util", "overallChanceKO");
            	Boolean overallChanceKO = Utils.parseBoolean(overallChanceKOStr);
            	if(null == overallChanceKO) {
            		throw new ConfigWrongValueException(configFileStr, "util", "overallChanceKO", overallChanceKOStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            	Settings.overallChanceKO = overallChanceKO;
            }
            if(configIni.get("util").containsKey("showGuarantees")){
            	String showGuaranteesStr = (String) configIni.get("util", "showGuarantees");
            	Boolean showGuarantees = Utils.parseBoolean(showGuaranteesStr);
            	if(null == showGuarantees) {
            		throw new ConfigWrongValueException(configFileStr, "util", "showGuarantees", showGuaranteesStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
        		Settings.showGuarantees = showGuarantees;
            }
            

            /* ************* */
            /* ROUTE PARSING */
            /* ************* */
            
            List<GameAction> actions = RouteParser.parseFile(routeFile);
            
            if(!parsingExceptions.isEmpty())
            	throw new Exception();

            /* *************** */
            /* ROUTE EXECUTION */
            /* *************** */
            
            for(GameAction a : actions) {
        		a.performAction(mainPoke); // TODO: what if we want to switch main ?
            }
            
            // TODO : implement tracking of X Items + Consumables + etc. via additional options (see leftover code below)
            
            /* ***************** */
            /* OUTPUT GENERATION */
            /* ***************** */
            File parentFolder = outputFile.getParentFile();
            if (parentFolder != null && !parentFolder.exists() && !parentFolder.mkdirs()) {
                throw new IllegalStateException(String.format("Wasn't able to create output folder '%s'.", parentFolder));
            }
            outputWriter.write(output.toString());
            
        } catch (Exception exc) {
        	// Don't forget any non-route parsing exceptions
        	if(parsingExceptions.isEmpty())
        		parsingExceptions.add(exc);

        	for(Exception e : parsingExceptions) {
    			e.printStackTrace();
 	            e.printStackTrace(debugStream);
 	            try { outputWriter.write(e.getMessage()+Constants.endl); } catch(Exception e2) {}
    		}
        	
    		exitCode = -1;
        } finally {
    		try { debugStream.close(); } catch(Exception e) {}
    		try { outputWriter.close(); } catch(Exception e) {}
        }
        
        System.exit(exitCode);
    }
        
        /*
        int[] XItems = {0,0,0,0,0, 0}; //atk,def,spa,spd,spe,acc
        int numBattles = 0;
        int rareCandies = 0;
        int HPUp = 0;
        int iron = 0;
        int protein = 0;
        int carbos = 0;
        int zinc = 0;
        int calcium = 0;
        for(GameAction a : actions) {
        	try {
        		a.performAction(mainPoke);
        	} catch (Exception exc) {
        		exc.printStackTrace();
        		appendln("Unexpected error : are your array parameters of proper length ?");
                FileWriter fw = new FileWriter(outputFilename);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(output.toString());
                bw.close();
                System.exit(-1); //TODO : bad workaround
        	}
            if (a instanceof Battle) { //TODO : some boosts might not come from X items, so what do we do ?
                StatModifier sm = ((Battle) a).getMod1();
                XItems[0] += Math.max(0, sm.getAtkStage());
                XItems[1] += Math.max(0, sm.getDefStage());
                XItems[2] += Math.max(0, sm.getSpaStage());
                XItems[3] += Math.max(0, sm.getSpeStage());
                XItems[4] += Math.max(0, sm.getAccStage());
                numBattles++;
            } else if (a == GameAction.eatRareCandy) {
                rareCandies++;
            } else if (a == GameAction.eatHPUp){
                HPUp++;
            } else if (a == GameAction.eatProtein){
                protein++;
            } else if (a == GameAction.eatIron){
                iron++;
            } else if (a == GameAction.eatCalcium){
                calcium++;
            } else if (a == GameAction.eatZinc){
                zinc++;
            } else if (a == GameAction.eatCarbos){
                carbos++;
            } 
        }        
        
        if(configIni.get("util", "printxitems", boolean.class)) { // TODO: bad code because hardcoded price values & boosts can come from moves, not items
            if(XItems[0] != 0)
                appendln("X ATKS: " + XItems[0]);
            if(XItems[1] != 0)
                appendln("X DEFS: " + XItems[1]);
            if(XItems[2] != 0)
                appendln("X SPAS: " + XItems[2]);
            if(XItems[3] != 0)
                appendln("X SPES: " + XItems[3]);
            if(XItems[4] != 0)
                appendln("X ACCURACYS: " + XItems[4]);
            int cost = XItems[0] * 500 + XItems[1] * 550 + XItems[2] * 350 + XItems[3] * 350 + XItems[4] * 950;
            if(cost != 0)
                appendln("X item cost: " + cost);
        }
        
        if(configIni.get("util", "printrarecandies", boolean.class)) {
            if(rareCandies != 0)
                appendln("Total Rare Candies: " + rareCandies);
        }
        if(configIni.get("util", "printstatboosters", boolean.class)) {
            if(HPUp != 0) {
                appendln("HP UP: " + HPUp);
            }
            if(protein != 0) {
                appendln("PROTEIN: " + protein);
            }
            if(iron != 0) {
                appendln("IRON: " + iron);
            }
            if(calcium != 0) {
                appendln("CALCIUM: " + calcium);
            }
            if(zinc != 0) {
                appendln("ZINC: " + zinc);
            }
            if(carbos != 0) {
                appendln("CARBOS: " + carbos);
            }
        }
        */
}
