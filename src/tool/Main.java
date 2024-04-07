package tool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import org.ini4j.Wini;

import tool.StatsContainer.ContainerType;
import tool.exception.config.ConfigMissingKeyException;
import tool.exception.config.ConfigWrongValueException;

public class Main {
	public static Pokemon mainPoke = null; // TODO : Really bad, but it works for scenario handling for now
    private static StringBuilder output = new StringBuilder();
    public static List<Exception> parsingExceptions = new ArrayList<>();
    
    public static void append(String s) {
        output.append(s);
    }
    public static void appendln(String s) {
        output.append(s + Constants.endl);
    }
    
    public static void main(String[] args) {
    	int exitCode = 0;
        String masterFileName = (args.length > 0) ? args[0] : "master.ini";
        String backupDebugFileName = (args.length > 1) ? args[1] : "debug.txt";
        
        Wini masterIni = null; // Only read
        PrintStream debugStream = null; // Only write
        Wini configIni = null; // Only read
        File routeFile = null; // Only read
        BufferedWriter outputWriter = null; // Only write
        
        try {
            /* ************** */
            /* INITIALIZATION */
            /* ************** */
        	
        	// Master file
        	try {
        		masterIni = new Wini(new File(masterFileName));
        	} catch(Exception e) {
        		throw new Exception(String.format("Inaccessible master file '%s'.", masterFileName));
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
    		} catch (Exception e) {
    			throw new Exception(String.format("Inaccessible debug file '%s' provided in '%s'. It is probably a typo.", customDebugFileStr, masterFileName));
    		}
        	
        	// Config file (mandatory)
        	String configFileStr = null;
    		if(!masterIni.get("master").containsKey("configFile"))
    			throw new Exception(String.format("Missing mandatory config file in '%s', in [%s] section. Is the line commented out ?", masterFileName, "master")); // TODO: hardcoded

    		configFileStr = masterIni.get("master","configFile");
    		try {
    			configIni = new Wini(new File(configFileStr));
    		} catch(Exception e) {
    			throw new Exception(String.format("Inaccessible config file '%s' provided in '%s'. It is probably a typo.", configFileStr, masterFileName));
    		}
    		
    		// Route file (mandatory)
    		String routeFileStr = null;
			if(!configIni.get("files").containsKey("routeFile"))
    			throw new Exception(String.format("Missing mandatory route file in '%s', in [%s]. Is the line commented out ?", configFileStr, "files")); // TODO: hardcoded
			
			routeFileStr = configIni.get("files","routeFile");
			routeFile = new File(routeFileStr);
			
			if(!routeFile.exists())
    			throw new Exception(String.format("Route file '%s' doesn't exist.", routeFileStr));
			if(!routeFile.canRead())
    			throw new Exception(String.format("Inaccessible route file '%s'.", routeFileStr));
            
    		// Output file (optional - default name if missing)
			String outputFilename = "outputs/out_"+routeFile.getName();
            if(configIni.get("files").containsKey("outputFile"))
            	outputFilename = configIni.get("files", "outputFile");
            File outputFile = new File(outputFilename);
            //outputWriter = new BufferedWriter(new FileWriter(outputFilename));
            outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8));
            
            /* At this point :
             * - The master ini is properly loaded
             * - A stream is opened to the debug file
             * - The config ini is properly loaded
             * - The route file exists and can be read from
             * - A writer is opened to the output file
             */
            
            // Set game and initialize
            if(!configIni.get("game").containsKey("game"))
    			throw new Exception(String.format("Missing mandatory game in '%s', in [%s] section.", configFileStr, "game")); // TODO: hardcoded
            	
            String gameName = configIni.get("game", "game");
            Game game = Game.getGameFromStr(gameName);
            if(game == null)
    			throw new Exception(String.format("Invalid game name '%s' in '%s'.%sThe list of supported games is : %s.", gameName, configFileStr, 
    					Constants.endl, Game.supportedGameNames()));
            
        	Initialization.init(game); // TODO: handle each Exception separately ?

            // TODO : handle custom files ?
        	
            // Set pokemon | TODO : handle trainer party ?
        		// Species (mandatory)
        	if(!configIni.get("poke").containsKey("species"))
    			throw new Exception(String.format("Missing mandatory species in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
        	
            String speciesStr = configIni.get("poke", "species");
            Species species = Species.getSpeciesByName(speciesStr);
            if(species == null)
    			throw new Exception(String.format("Invalid species '%s' in '%s'.", speciesStr, configFileStr));
            
            	// Level (mandatory)
            if(!configIni.get("poke").containsKey("level"))
    			throw new Exception(String.format("Missing mandatory level in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            int level = configIni.get("poke", "level", int.class); // TODO : check level validity here ?
            
            	// Gender (mandatory)
            if(!configIni.get("poke").containsKey("gender"))
    			throw new Exception(String.format("Missing mandatory gender in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            
            String genderStr = configIni.get("poke", "gender");
            Gender gender = Gender.getGenderFromStr(genderStr);
            if(gender == null)
    			throw new Exception(String.format("Invalid gender '%s' in '%s'.", genderStr, configFileStr));
            
            	// Nature (mandatory)
            if(!configIni.get("poke").containsKey("nature"))
    			throw new Exception(String.format("Missing mandatory nature in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            
            String natureStr = configIni.get("poke", "nature");
            Nature nature = Nature.getNatureFromString(natureStr);
            if(nature == null)
    			throw new Exception(String.format("Invalid nature '%s' in '%s'.", natureStr, configFileStr));
            
            	// Ability (mandatory)
            if(!configIni.get("poke").containsKey("ability"))
    			throw new Exception(String.format("Missing mandatory ability in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            
            String abilityStr = configIni.get("poke", "ability");
            Ability ability = Ability.getAbilityFromString(abilityStr);
            if(ability == null)
    			throw new Exception(String.format("Invalid ability '%s' in '%s'.", abilityStr, configFileStr));
            if(ability != species.getAbility1() && ability != species.getAbility2())
    			throw new Exception(String.format("%s ability can only be %s%s.", species, species.getAbility1(),
    					species.getAbility2() == Ability.NONE ? "" : String.format(" or %s", species.getAbility2())));
            	

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
        			hasBoostedExp = true;
        			isInternationalTraded = true;
            	} else {
	            	try{
	            		hasBoostedExp = Boolean.parseBoolean(boostedExpStr);
	            	} catch (Exception e) {
	            		throw new ConfigWrongValueException(configFileStr, "poke", "boostedExp", boostedExpStr,
	            				String.format("must be either '%s' or '%s'. In Diamond/Pearl, '%s' is available for international trades", true, false, "international"));
            		}
            	}
            }
            
        		// Pokerus (optional)
            boolean hasPokerus = false; // TODO : hardcoded
            if(configIni.get("poke").containsKey("pokerus")) {
            	String pokerusStr = null;
            	try{
            		pokerusStr = (String) configIni.get("poke", "pokerus");
            		hasPokerus = Boolean.parseBoolean(pokerusStr);
            	} catch (Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "poke", "pokerus", pokerusStr,
            				String.format("must be either '%s' or '%s'.", true, false));
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
            	try{
            		verboseLevelStr = (String) configIni.get("util", "defaultOutputDetails");
            		int verboseLevelInt = Integer.parseInt(verboseLevelStr);
            		Settings.verboseLevel = VerboseLevel.values()[verboseLevelInt];
            	} catch (Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultOutputDetails", verboseLevelStr,
            				String.format("must be an integer between '%s' and '%s'.", VerboseLevel.NONE.ordinal(), VerboseLevel.EVERYTHING.ordinal()));
            	}
            }
            
            if(configIni.get("util").containsKey("defaultShowStatsOnLevelUp")) {
            	String defaultShowStatsOnLevelUpStr = null;
            	try {
            		defaultShowStatsOnLevelUpStr = (String) configIni.get("util", "defaultShowStatsOnLevelUp");
            		Settings.showStatsOnLevelUp = Boolean.parseBoolean(defaultShowStatsOnLevelUpStr);
            	} catch(Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultShowStatsOnLevelUp", defaultShowStatsOnLevelUpStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            }
            
            if(configIni.get("util").containsKey("defaultShowStatRangesOnLevelUp")) {
            	String defaultShowStatRangesOnLevelUpStr = null;
            	try {
            		defaultShowStatRangesOnLevelUpStr = (String) configIni.get("util", "defaultShowStatRangesOnLevelUp");
            		Settings.showStatsOnLevelUp = Boolean.parseBoolean(defaultShowStatRangesOnLevelUpStr);
            	} catch(Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultShowStatRangesOnLevelUp", defaultShowStatRangesOnLevelUpStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            }
            
            if(configIni.get("util").containsKey("defaultIvVariation")) {
            	String defaultIvVariationStr = null;
            	try {
            		defaultIvVariationStr = (String) configIni.get("util", "defaultIvVariation");
            		Settings.defaultIvVariation = Boolean.parseBoolean(defaultIvVariationStr);
            	} catch(Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "defaultIvVariation", defaultIvVariationStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            }
            
            if(configIni.get("util").containsKey("overallChanceKO")) {
            	String overallChanceKOStr = null;
            	try {
            		overallChanceKOStr = (String) configIni.get("util", "overallChanceKO");
            		Settings.overallChanceKO = Boolean.parseBoolean(overallChanceKOStr);
            	} catch(Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "overallChanceKO", overallChanceKOStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
            }
            if(configIni.get("util").containsKey("showGuarantees")){
            	String showGuaranteesStr = null;
            	try {
            		showGuaranteesStr = (String) configIni.get("util", "showGuarantees");
            		Settings.showGuarantees = Boolean.parseBoolean(showGuaranteesStr);
            	} catch(Exception e) {
            		throw new ConfigWrongValueException(configFileStr, "util", "showGuarantees", showGuaranteesStr, 
            				String.format("must be either '%s' or '%s'.", true, false));
            	}
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
