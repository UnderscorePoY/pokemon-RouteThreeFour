package tool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import org.ini4j.Wini;

import tool.StatsContainer.ContainerType;

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
            outputWriter = new BufferedWriter(new FileWriter(outputFilename));
            
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

            	// IVs (mandatory)
            if(!configIni.get("poke").containsKey("hpIV"))
    			throw new Exception(String.format("Missing mandatory hpIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            if(!configIni.get("poke").containsKey("atkIV"))
    			throw new Exception(String.format("Missing mandatory atkIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            if(!configIni.get("poke").containsKey("defIV"))
    			throw new Exception(String.format("Missing mandatory defIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            if(!configIni.get("poke").containsKey("spaIV"))
    			throw new Exception(String.format("Missing mandatory spaIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            if(!configIni.get("poke").containsKey("spdIV"))
    			throw new Exception(String.format("Missing mandatory spdIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            if(!configIni.get("poke").containsKey("speIV"))
    			throw new Exception(String.format("Missing mandatory speIV in '%s', in [%s] section.", configFileStr, "poke")); // TODO: hardcoded
            
            int hpIV = configIni.get("poke", "hpIV", int.class);
            int atkIV = configIni.get("poke", "atkIV", int.class);
            int defIV = configIni.get("poke", "defIV", int.class);
            int spaIV = configIni.get("poke", "spaIV", int.class);
            int spdIV = configIni.get("poke", "spdIV", int.class);
            int speIV = configIni.get("poke", "speIV", int.class);
            
            StatsContainer ivs = new StatsContainer(ContainerType.IV);
            ivs.put(Stat.HP, hpIV);
            ivs.put(Stat.ATK, atkIV);
            ivs.put(Stat.DEF, defIV);
            ivs.put(Stat.SPA, spaIV);
            ivs.put(Stat.SPD, spdIV);
            ivs.put(Stat.SPE, speIV);
            
            	// Boosted EXP (optional)
            boolean hasBoostedExp = false; // TODO : hardcoded
            if(configIni.get("poke").containsKey("boostedExp"))
            	hasBoostedExp = configIni.get("poke", "boostedExp", boolean.class);
            
        		// Pokerus (optional)
            boolean hasPokerus = false; // TODO : hardcoded
            if(configIni.get("poke").containsKey("pokerus"))
            	hasPokerus = configIni.get("poke", "pokerus", boolean.class);

            // Main Pokémon instanciation
        	mainPoke = new Pokemon(species, gender, level, nature, ability, ivs, hasBoostedExp, hasPokerus);

            // Other non-mandatory options
            if(configIni.get("util").containsKey("overallChanceKO"))
            	Settings.overallChanceKO = configIni.get("util", "overallChanceKO", boolean.class);
            if(configIni.get("util").containsKey("showGuarantees"))
            	Settings.showGuarantees = configIni.get("util", "showGuarantees", boolean.class);
            

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
