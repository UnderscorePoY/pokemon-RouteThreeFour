package tool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.ini4j.jdk14.edu.emory.mathcs.backport.java.util.Arrays;

import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;
import tool.exception.route.BattleFlagNoParamException;
import tool.exception.route.BattleFlagParamException;
import tool.exception.route.RouteParserException;
import tool.exception.route.RouteParserInternalException;

public class RouteParser {
    public static int lineNum = 0;
    public static final String COMMENT_STARTER = "//";
    public static final String COMMENT_STARTER_REGEX = COMMENT_STARTER;
    public static final String COMMENT_STARTER2 = "##";
    public static final String COMMENT_STARTER2_REGEX = COMMENT_STARTER2;
    public static final String FLAG_STARTER = "-";
    public static final String FLAG_STARTER_REGEX = FLAG_STARTER;
    public static final String TOKEN_SEPARATOR = " ";
    public static final String TOKEN_SEPARATOR_REGEX = TOKEN_SEPARATOR;

    public static List<GameAction> parseFile(File file) throws RouteParserException, ToolInternalException, FileNotFoundException, IOException {
        lineNum = 0;
        ArrayList<GameAction> actions = new ArrayList<GameAction>();
        
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (in.ready()) {
                lineNum++;
                String wholeLine = in.readLine();
                String[] lines = wholeLine.split(COMMENT_STARTER_REGEX); // remove comments
                if(lines.length == 0) continue;
                lines = lines[0].split(COMMENT_STARTER2_REGEX); // remove more comments
                String line = lines[0];
                
                GameAction action = null;
                try {
                	action = parseLine(line);
                } catch (Exception e) {
                	Main.parsingExceptions.add(e);
                }
                
                if(action != null) // Line can be full of spaces or tabulations, and those would return null from the parsing ... maybe ?
                	actions.add(action);
            }
        } catch (Exception e) {
            throw e;
        } finally {
        	in.close();
        }

        return actions;
    }

    
    public static enum Command {
    	UNKNOWN(),
    	
    	WILD(true, "[Ll][0-9]+"),
    	
    	EVOLVE("e", "evolve", "changeForm"),
    	
    	LEARN_MOVE("lm", "learnMove"),
    	UNLEARN_MOVE("um", "unlearnMove"),
    	
    	ADD_MONEY("addMoney", "earnMoney", "gainMoney"),
    	SPEND_MONEY("spendMoney", "loseMoney"),
    	
    	BUY_ITEM("buy", "buyItem"),
    	SELL_ITEM("sell", "sellItem"),
    	
    	RARE_CANDY("rc", "rareCandy"),
    	HP_UP("hpup"),
    	PROTEIN("protein"),
    	IRON("iron"),
    	CALCIUM("calcium"),
    	ZINC("zinc"),
    	CARBOS("carbos"),
    	
    	STONE_BADGE("stoneBadge", "RoxanneBadge"),
    	DYNAMO_BADGE("dynamoBadge", "WattsonBadge"),
    	BALANCE_BADGE("balanceBadge", "NormanBadge"),
    	MIND_BADGE("mindBadge", "TateAndLizaBadge", "T&LBadge"),
    	BOULDER_BADGE("boulderBadge", "BrockBadge"),
    	THUNDER_BADGE("thunderBadge", "LTSurgeBadge", "SurgeBadge"),
    	SOUL_BADGE("soulBadge, KogaBadge"),
    	VOLCANO_BADGE("volcanoBadge", "BlaineBadge"),
    	
    	EQUIP_ITEM("equip", "equipItem"),
    	UNEQUIP_ITEM("unequip", "unequipItem"),
    	
    	SET_POKERUS("setPokerus", "setPkrs"),
    	UNSET_POKERUS("unsetPokerus", "unsetPkrs"),
    	
    	//SET_BOOSTED_EXP("setBoostedExp"),
    	//UNSET_BOOSTED_EXP("unsetBoostedExp"),
    	
    	PRINT_MONEY("money"),
    	PRINT_STATS("stats"),
    	PRINT_STATS_RANGES("ranges", "statsRanges"),
    	
    	PC_WITHDRAWAL("pc", "pcUpdate", "pcWithdrawal"),
    	
    	
    	SET_HAPPINESS("setHappiness"),
    	GAIN_HAPPINESS("addHappiness"),
    	//LOSE_HAPPINESS("loseHappiness"),
    	
    	SET_BATTLE_TOWER(),
    	UNSET_BATTLE_TOWER(),
    	
    	// Trainer is always considered the default
    	;
    	
    	private ArrayList<String> labels;
    	private boolean areLabelsRegex;
    	
    	private Command(boolean areLabelsRegex, String... labels) {
    		this.areLabelsRegex = areLabelsRegex;
    		this.labels = new ArrayList<>();
    		for(String label : labels) {
    			this.labels.add(label);
    		}
    	}
    	
    	private Command(String... labels) {
    		this(false, labels);
    	}
    	
    	public boolean areLabelsRegex() {
    		return areLabelsRegex;
    	}
    	
    	public static Command getCommandFromString(String s) {
    		if(s == null)
    			return UNKNOWN;
    		
    		for(Command cmd : Command.values()) {
    			for(String label : cmd.labels) {
    				if(cmd.areLabelsRegex()) {
    					if(s.matches(label))
        					return cmd;
    				} else {
        				if(label.equalsIgnoreCase(s))
        					return cmd;
    				}
    			}
    		}
    		return UNKNOWN;
    	}
    }
    
    
    /**
     * Returns a game action compiled from the given string (comments are assumed to have been removed), 
     *   or null if the first encountered token of the line is empty (not sure whether this is required).
     * Throws exceptions if a parsing error is encountered.
     */
    private static GameAction parseLine(String line) throws RouteParserException, ToolInternalException {
        String[] tokens = line.split(TOKEN_SEPARATOR_REGEX);
        String[] tokensForWild = line.split(FLAG_STARTER_REGEX)[0].split(TOKEN_SEPARATOR_REGEX); // Retrieves non flag options for wilds
        int nbTokens = tokens.length;
        int nbNonOptionalParams = tokensForWild.length;
        
        // Return if line is empty
        if (nbTokens == 0 || tokens[0].length() == 0)
            return null;

        String cmdToken = tokens[0];
        Command cmd = Command.getCommandFromString(cmdToken);
        
        switch(cmd) {
        case WILD:
			{ // scope hack to avoid variable name interference between cases   	
                if(nbNonOptionalParams < 2) // Requires at least level & species
                	throw new RouteParserException("a wild encounter must at least have a level and a species.");
                
                int flagIdx = 0;
                
                // Level
                int lvl = -1;
                String lvlStr = cmdToken.substring(1);
                try {
                	lvl = Integer.parseInt(lvlStr);
                	flagIdx++;
                } catch(Exception exc) {
                	throw new RouteParserException(String.format("invalid wild encounter level '%s'.", lvlStr));
                }
                
                // Species
                String speciesStr = tokens[flagIdx++];
                Species species = Species.getSpeciesByName(speciesStr);
                if(species == null)
                	throw new RouteParserException(String.format("invalid wild encounter species '%s'.", speciesStr));
            
                // Nature
                Nature nature = null;
                if(nbNonOptionalParams < 3) {
                	nature = Nature.DEFAULT;
                } else {
        	        String natureStr = tokens[flagIdx++];
    	            nature = Nature.getNatureFromString(natureStr);
                    if(nature == null)
                    	throw new RouteParserException(String.format("invalid wild encounter nature '%s'.", natureStr));
                }
                
                // Ability
                Ability ability = null;
                if(nbNonOptionalParams < 4) {
                	ability = species.getAbility1();
                } else {
                	String abilityStr = tokens[flagIdx++];
            		ability = Ability.getAbilityFromString(abilityStr);
            		if(ability != species.getAbility1() && ability != species.getAbility2())
                    	throw new RouteParserException(String.format("invalid wild encounter ability '%s'.%s"
                    			+ "For %s, the ability must be within the following list : %s.", 
                    			abilityStr, Constants.endl,
                    			species, species.getPossibleAbilitiesStr()));
                }
                
                // Gender
                Gender gender = null;
                if(nbNonOptionalParams < 5) {
                	gender = Gender.predominantGender(species);
                } else {
    	        	String genderStr = tokens[flagIdx++];
    	        	gender = Gender.getGenderFromStr(genderStr);
    	            if(gender == null)
                    	throw new RouteParserException(String.format("invalid wild encounter gender '%s'.", genderStr));
                }
                
                // IVs
                StatsContainer ivs = null;
                try
                {
                	Integer hp  = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    Integer atk = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    Integer def = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    Integer spa = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    Integer spd = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    Integer spe = Integer.parseInt(tokens[flagIdx]); flagIdx++;
                    ivs = new StatsContainer(ContainerType.IV);
                    ivs.put(Stat.HP, hp);
                    ivs.put(Stat.ATK, atk);
                    ivs.put(Stat.DEF, def);
                    ivs.put(Stat.SPA, spa);
                    ivs.put(Stat.SPD, spd);
                    ivs.put(Stat.SPE, spe);
                } catch(Exception exc) {
                	Main.appendln(String.format("WARNING ON LINE " + lineNum + ": wasn't able to parse all 6 IVs => all defaulted to '%s'.",
                			ContainerType.IV.getDefaultValue())); // TODO: Better warning system.
                    ivs = new StatsContainer(ContainerType.IV);
                }
                Pokemon b = new Pokemon(species, gender, lvl, nature, ability, ivs);
                //b.setGender(gender);
                
                String[] flagTokens = (String[]) Arrays.copyOfRange(tokens, flagIdx, nbTokens);
                return addFlagsToBattleable(b, flagTokens);
			} // end scope hack
			
        case EVOLVE:
			{ // scope hack to avoid variable name interference between cases 
                if (nbNonOptionalParams < 2)
                	throw new RouteParserException("missing species name.");
                
                String speciesStr = tokens[1];
                Species s = Species.getSpeciesByName(speciesStr);
                if(s == null)
                	throw new RouteParserException(String.format("invalid species name '%s'.", speciesStr));

                return new Evolve(s);
            } // end scope hack
			
        case LEARN_MOVE:
        case UNLEARN_MOVE:
			{ // scope hack to avoid variable name interference between cases 
	            if (nbNonOptionalParams < 2)
	            	throw new RouteParserException("missing move name.");
	            
		        String moveStr = tokens[1];
		        Move move = Move.getMoveByName(moveStr);
		        if(move == null)
                	throw new RouteParserException(String.format("invalid move name '%s'.", moveStr));
		        	
		        switch(cmd) {
		        case LEARN_MOVE: return new LearnMove(move);
		        case UNLEARN_MOVE: return new UnlearnMove(move);
		        default: // TODO : clean
		        	//throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "(Un)Learnmove.");
		        	throw new ToolInternalException(cmd, "(Un)Learnmove.");
			    }
			} // end scope hack
			
        case ADD_MONEY:
        case SPEND_MONEY:
			{ // scope hack to avoid variable name interference between cases 
	            if (nbNonOptionalParams < 2)
	            	throw new RouteParserException("missing money value.");

	        	String moneyStr = tokens[1];
	        	Integer money = null;
	        	try {
	            	money = Integer.parseInt(moneyStr);
	            	if(money < 0) {
	            		throw new Exception();
	            	}
	            } catch (Exception e) {
                	throw new RouteParserException(String.format("invalid money value '%s'. Must be a positive integer.", moneyStr));
	            }
	        	
		        switch(cmd) {
		        case ADD_MONEY: return new AddMoney(money);
		        case SPEND_MONEY: return new AddMoney(-money);
		        default: // TODO : Clean
		        	// throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "Add/Spend-money.");
		        	throw new ToolInternalException(cmd, "Add/Spend-money.");
		        }
			} // end scope hack
			
        case BUY_ITEM: // TODO: sometimes, there might be discounts. Which one ? How to factor that in properly ?
        case SELL_ITEM:
			{ // scope hack to avoid variable name interference between cases 
				if (nbNonOptionalParams < 2)
	            	throw new RouteParserException("missing item name.");

	        	String itemStr = null;
        		Item item = null;
        		int quantity = 1;
        		if (nbNonOptionalParams == 2) { // no quantity
            		itemStr = tokens[1];
        		}
        		if (nbNonOptionalParams >= 3) { // quantity, then item
        			String quantityStr = tokens[1];
    	        	try {
    	        		quantity = Integer.parseInt(quantityStr);
    	        		itemStr = tokens[2];
    	        		if (quantity < 1)
    	        			throw new Exception();
    	        	} catch (Exception e) {
    	        		throw new RouteParserException(String.format("invalid quantity '%s'. Must be an integer greater or equal to 1.", quantityStr));
    	        	}
        		}
        		
        		item = Item.getItemByName(itemStr);
        		if(item == null)
                	throw new RouteParserException(String.format("invalid item name '%s'.", itemStr));
        		
        		int totalValue = item.getBuyPrice() * quantity;
        		
        		switch(cmd) {
        		case BUY_ITEM: return new AddMoney(-totalValue);
        		case SELL_ITEM: return new AddMoney(totalValue/2);
        		default: // TODO : Clean
		        	// throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "Buy/Sell-item.");
        			throw new ToolInternalException(cmd, "Buy/Sell-item.");
        		}
			} // end scope hack
			
        case RARE_CANDY:
        case HP_UP:
        case PROTEIN:
        case IRON:
        case CALCIUM:
        case ZINC:
        case CARBOS:
			{ // scope hack to avoid variable name interference between cases 
	        	int quantity = 1;
				if (nbNonOptionalParams >= 2) { // quantity (discarding all other tokens)
					String quantityStr = tokens[1];
					try {
						quantity = Integer.parseInt(quantityStr);
						if(quantity < 1)
							throw new Exception();
					} catch (Exception e) {
		        		throw new RouteParserException(String.format("invalid quantity '%s'. Must be an integer greater or equal to 1.", quantityStr));
					}
				}
				
				switch(cmd) {
		        case RARE_CANDY: return new EatConsumable(Consumable.RARE_CANDY, quantity);
		        case HP_UP:      return new EatConsumable(Consumable.HP_UP, quantity);
		        case PROTEIN:    return new EatConsumable(Consumable.PROTEIN, quantity);
		        case IRON:       return new EatConsumable(Consumable.IRON, quantity);
		        case CALCIUM:    return new EatConsumable(Consumable.CALCIUM, quantity);
		        case ZINC:       return new EatConsumable(Consumable.ZINC, quantity);
		        case CARBOS:     return new EatConsumable(Consumable.CARBOS, quantity);
	        	default: // TODO : Clean
		        	// throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "Consumables.");
		        	throw new ToolInternalException(cmd, "Consumables.");
				}
			} // end scope hack		
			
        case STONE_BADGE:
        case DYNAMO_BADGE:
        case BALANCE_BADGE:
        case MIND_BADGE:
			{ // scope hack to avoid variable name interference between cases 
				if(!Settings.game.isRSE())
	        		throw new RouteParserException(String.format("badge doesn't exist in this game.")); // TODO : more explicit ?
				
				switch(cmd) {
		        case STONE_BADGE:   return new EarnBadge(Stat.ATK);
		        case DYNAMO_BADGE:  return new EarnBadge(Stat.SPE);
		        case BALANCE_BADGE: return new EarnBadge(Stat.DEF);
		        case MIND_BADGE:    return new EarnBadge(Stat.SPA, Stat.SPD);
	        	default: // TODO : Clean
		        	//throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "RSE badges.");
		        	throw new ToolInternalException(cmd, "RSE badges.");
				}
			} // end scope hack
			
        case BOULDER_BADGE:
        case THUNDER_BADGE:
        case SOUL_BADGE:
        case VOLCANO_BADGE:
			{ // scope hack to avoid variable name interference between cases 
				if(!Settings.game.isFRLG())
	        		throw new RouteParserException(String.format("badge doesn't exist in this game.")); // TODO : more explicit ?
				
				switch(cmd) {
		        case BOULDER_BADGE: return new EarnBadge(Stat.ATK);
		        case THUNDER_BADGE: return new EarnBadge(Stat.SPE);
		        case SOUL_BADGE:    return new EarnBadge(Stat.DEF);
		        case VOLCANO_BADGE: return new EarnBadge(Stat.SPA, Stat.SPD);
	        	default: // TODO : Clean
		        	// throw new ToolInternalException(RouteParser.class.getEnclosingMethod(), cmd, "FRLG badges.");
		        	throw new ToolInternalException(cmd, "FRLG badges.");
				}
			} // end scope hack
			
        case SET_BATTLE_TOWER: // TODO : handle directly from trainers if possible ?
        	return GameAction.setBattleTowerFlag;
        	
        case UNSET_BATTLE_TOWER:
        	return GameAction.unsetBattleTowerFlag;
        	
        case EQUIP_ITEM:
			{ // scope hack to avoid variable name interference between cases 
            	if (nbNonOptionalParams < 2)
	        		throw new RouteParserException(String.format("item name is missing."));
            	
            	String itemName = null;
        		itemName = tokens[1];
        		Item item = Item.getItemByName(itemName);
        		if(item == null)
        			throw new RouteParserException(String.format("invalid item name '%s'.", itemName));
        		
        		return new Equip(item);
			} // end scope hack
        	
        case UNEQUIP_ITEM:
        	return GameAction.unequip;
		
        case SET_POKERUS:
        	return GameAction.setPokerus;
        	
        case UNSET_POKERUS:
        	return GameAction.unsetPokerus;
        
        /*
        case SET_BOOSTED_EXP: // TODO: get rid ? Since there's an option in the config files now ...
        	return GameAction.setBoostedExp;
        	
        case UNSET_BOOSTED_EXP: // TODO: get rid ? Since there's an option in the config files now ...
        	return GameAction.unsetBoostedExp;
    	*/
        	
        case PRINT_MONEY:
        	return GameAction.printMoney;
			
        case PRINT_STATS:
        	return GameAction.printAllStatsNoBoost;
        	/*
        	if(nbNonOptionalParams > 1 && tokens[1].equalsIgnoreCase("-b")) { // TODO: hacky
        		if(!Settings.game.isGen3())
        			throw new RouteParserException("badge boosts are not available in this game.");
        		
                return GameAction.printAllStats;
        	} else
        		return GameAction.printAllStatsNoBoost;
    		*/
        	
        case PRINT_STATS_RANGES:
    		return GameAction.printStatRangesNoBoost;
        	/*
        	if(nbNonOptionalParams > 1 && tokens[1].equalsIgnoreCase("-b")) { // TODO: hacky
        		if(!Settings.game.isGen3())
        			throw new RouteParserException("badge boosts are not available in this game.");
        		
                return GameAction.printStatRanges;
        	} else
        		return GameAction.printStatRangesNoBoost;
    		*/
        	
        case PC_WITHDRAWAL:
        	return GameAction.updateEVs;
        	
        case SET_HAPPINESS:
        	{
	        	if (nbNonOptionalParams < 2)
	        		throw new RouteParserException(String.format("happiness value is missing."));
	        	
	        	Integer happiness = null;
	        	try {
	        		happiness = Integer.parseInt(tokens[1]);
	        		if(!Happiness.isInBound(happiness))
	        			throw new Exception();
	        	} catch (Exception e) {
	        		throw new RouteParserException(String.format("invalid happiness '%s'. Must be an integer between '%d' and '%d'.", tokens[1], Happiness.MIN, Happiness.MAX));
	        	}
	        	
	        	return new SetHappiness(happiness);
        	}
        	
        case GAIN_HAPPINESS:
        	{
	        	if (nbNonOptionalParams < 2)
	        		throw new RouteParserException(String.format("happiness increment is missing."));
	        	
	        	Integer happiness = null;
	        	try {
	        		happiness = Integer.parseInt(tokens[1]);
	        	} catch (Exception e) {
	        		throw new RouteParserException(String.format("invalid happiness '%s'. Must be an integer.", tokens[1]));
	        	}
	        	
	        	return new AddHappiness(happiness);
        	}
        	
        default : // trainer
        	if(cmdToken.trim().isEmpty())
        		return null; // TODO : when can this happen with the current parsing strategy ? Maybe these are some weird space characters, or tabulations ?
        	Battleable b = Trainer.getTrainerByName(cmdToken);
            if (b == null)
    			throw new RouteParserException(String.format("invalid trainer name '%s'.", cmdToken));
        
            String[] flagTokens = (String[]) Arrays.copyOfRange(tokens, 1, nbTokens);
            return addFlagsToBattleable(b, flagTokens);
        } // end command parsing
    }

      
    private static enum BattleFlag {
    	/* ********** */
    	/* ERROR FLAG */  
    	/* ********** */
    	
    	UNKNOWN(),
    	
    	/* ****************** */
    	/* NO-PARAMETER FLAGS */  
    	/* ****************** */
    	
    	// Pure-printing flags
        LEVEL_UP_RANGES("lvranges", "levelUpRanges"),
    	LEVEL_UP_STATS("lvstats", "levelUpStats"),
    	
    	// Non-pure-printing flags
        //SET_WILD("w", "wild"),
        SET_TRAINER("t", "trainer"),
        
        MULTI_TARGET_DAMAGE("multiDmg", "multiTargetDamage"),
        SINGLE_TARGET_DAMAGE("singleDmg", "singleTargetDamage"),
        
        FORCE_ENTIRE_DOUBLE_BATTLE("doubleBattle"),
        FORCE_ENTIRE_SINGLE_BATTLE("singleBattle"),
        
        BACKTRACK("backtrack"),
        
        
    	/* ******************** */
    	/* WITH-PARAMETER FLAGS */  
    	/* ******************** */
        
    	// Pure-printing flags
        SCENARIO_NAME("name", "scenarioName", "scenario"),
        VERBOSE("v", "verbose"),

    	// Non-pure-printing flags
        	// Increment since Poke 1 for whole fight
		INCREMENT_X_ATK("xAtkUse"),
		INCREMENT_X_DEF("xDefUse"),
		INCREMENT_X_SPA("xSpaUse"),
		INCREMENT_X_SPD("xSpdUse"),
		INCREMENT_X_SPE("xSpeUse"),
		INCREMENT_X_ACC("xAccUse"),

		INCREMENT_Y_ATK("yAtkUse"),
		INCREMENT_Y_DEF("yDefUse"),
		INCREMENT_Y_SPA("ySpaUse"),
		INCREMENT_Y_SPD("ySpdUse"),
		INCREMENT_Y_SPE("ySpeUse"),
		INCREMENT_Y_ACC("yAccUse"),

    		// Force set since Poke 1 for whole fight
		FORCE_X_ATK("xAtkSet"),
		FORCE_X_DEF("xDefSet"),
		FORCE_X_SPA("xSpaSet"),
		FORCE_X_SPD("xSpdSet"),
		FORCE_X_SPE("xSpeSet"),
		FORCE_X_ACC("xAccSet"),

		FORCE_Y_ATK("yAtkSet"),
		FORCE_Y_DEF("yDefSet"),
		FORCE_Y_SPA("ySpaSet"),
		FORCE_Y_SPD("ySpdSet"),
		FORCE_Y_SPE("ySpeSet"),
		FORCE_Y_ACC("yAccSet"),
        
    		// Increment each poke individually
		INCREMENT_X_ATKS("xAtksUse"),
		INCREMENT_X_DEFS("xDefsUse"),
		INCREMENT_X_SPAS("xSpasUse"),
		INCREMENT_X_SPDS("xSpdsUse"),
		INCREMENT_X_SPES("xSpesUse"),
		INCREMENT_X_ACCS("xAccsUse"),

		INCREMENT_Y_ATKS("yAtksUse"),
		INCREMENT_Y_DEFS("yDefsUse"),
		INCREMENT_Y_SPAS("ySpasUse"),
		INCREMENT_Y_SPDS("ySpdsUse"),
		INCREMENT_Y_SPES("ySpesUse"),
		INCREMENT_Y_ACCS("yAccsUse"),
		
			// Force set each poke individually
		FORCE_X_ATKS("xAtksSet"),
		FORCE_X_DEFS("xDefsSet"),
		FORCE_X_SPAS("xSpasSet"),
		FORCE_X_SPDS("xSpdsSet"),
		FORCE_X_SPES("xSpesSet"),
		FORCE_X_ACCS("xAccsSet"),
	
		FORCE_Y_ATKS("yAtksSet"),
		FORCE_Y_DEFS("yDefsSet"),
		FORCE_Y_SPAS("ySpasSet"),
		FORCE_Y_SPDS("ySpdsSet"),
		FORCE_Y_SPES("ySpesSet"),
		FORCE_Y_ACCS("yAccsSet"),
        
        SHARE_EXP("sxp", "shareExp"),
        SHARE_EXPS("sxps", "shareExps"), 
        
        ORDER("order", "yOrder"),
        
        X_STATUS1("xStatus", "xStatus1"),
        Y_STATUS1("yStatus", "yStatus1"),
        
        X_STATUS2("xStatus2"),
        Y_STATUS2("yStatus2"),
        
        X_STATUSES1("xStatuses", "xStatuses1"),
        Y_STATUSES1("yStatuses", "yStatuses1"),
        
        X_STATUSES2("xStatuses2"),
        Y_STATUSES2("yStatuses2"),
        
        X_CURR_HP("xCurrHP"),
        Y_CURR_HP("yCurrHP"),
        
        WEATHER("w", "weather"),
        WEATHERS("ws", "weathers"),
        
        X_PARTNER("xPartner"),
        Y_PARTNER("yPartner"),
        
        IV_VARIATION("ivVariation"),
        
        RETURN_AVERAGE("returnAverage")
        ;
    	
    	public static final String parameterSeparator = "/";
    	public static final String parameterSeparatorRegex = parameterSeparator;
    	public static final String parameterUnifier = "+";
    	public static final String parameterUnifierRegex = "\\+"; // escaping the desired character.

    	private ArrayList<String> labels;
    	
    	private BattleFlag(String... labels) {
    		this.labels = new ArrayList<>();
    		for(String label : labels) {
    			this.labels.add(String.format("%s%s", FLAG_STARTER, label));
    		}
    	}
    	
    	public static BattleFlag getNextFlagFromString(String s) {
    		for(BattleFlag nf : BattleFlag.values()) {
    			for(String label : nf.labels) {
    				if(label.equalsIgnoreCase(s))
    					return nf;
    			}
    		}
    		return UNKNOWN;
    	}
    }
    
    private static enum IncompatibleFlagCombination {
    	A(2, BattleFlag.FORCE_ENTIRE_DOUBLE_BATTLE, BattleFlag.FORCE_ENTIRE_SINGLE_BATTLE),
    	
    	B(2, BattleFlag.FORCE_ENTIRE_DOUBLE_BATTLE, BattleFlag.SINGLE_TARGET_DAMAGE),
    	C(2, BattleFlag.FORCE_ENTIRE_DOUBLE_BATTLE, BattleFlag.SHARE_EXP),
    	D(2, BattleFlag.FORCE_ENTIRE_DOUBLE_BATTLE, BattleFlag.SHARE_EXPS),

    	E(2, BattleFlag.FORCE_ENTIRE_SINGLE_BATTLE, BattleFlag.MULTI_TARGET_DAMAGE),
    	F(2, BattleFlag.FORCE_ENTIRE_SINGLE_BATTLE, BattleFlag.SHARE_EXP),
    	G(2, BattleFlag.FORCE_ENTIRE_SINGLE_BATTLE, BattleFlag.SHARE_EXPS),
    	
    	H(2, BattleFlag.INCREMENT_X_ATK, BattleFlag.FORCE_X_ATK, BattleFlag.INCREMENT_X_ATKS, BattleFlag.FORCE_X_ATKS),
    	I(2, BattleFlag.INCREMENT_X_DEF, BattleFlag.FORCE_X_DEF, BattleFlag.INCREMENT_X_DEFS, BattleFlag.FORCE_X_DEFS),
    	J(2, BattleFlag.INCREMENT_X_SPA, BattleFlag.FORCE_X_SPA, BattleFlag.INCREMENT_X_SPAS, BattleFlag.FORCE_X_SPAS),
    	K(2, BattleFlag.INCREMENT_X_SPD, BattleFlag.FORCE_X_SPD, BattleFlag.INCREMENT_X_SPDS, BattleFlag.FORCE_X_SPDS),
    	L(2, BattleFlag.INCREMENT_X_SPE, BattleFlag.FORCE_X_SPE, BattleFlag.INCREMENT_X_SPES, BattleFlag.FORCE_X_SPES),
    	M(2, BattleFlag.INCREMENT_X_ACC, BattleFlag.FORCE_X_ACC, BattleFlag.INCREMENT_X_ACCS, BattleFlag.FORCE_X_ACCS),
    	
    	H_(2, BattleFlag.INCREMENT_Y_ATK, BattleFlag.FORCE_Y_ATK, BattleFlag.INCREMENT_Y_ATKS, BattleFlag.FORCE_Y_ATKS),
    	I_(2, BattleFlag.INCREMENT_Y_DEF, BattleFlag.FORCE_Y_DEF, BattleFlag.INCREMENT_Y_DEFS, BattleFlag.FORCE_Y_DEFS),
    	J_(2, BattleFlag.INCREMENT_Y_SPA, BattleFlag.FORCE_Y_SPA, BattleFlag.INCREMENT_Y_SPAS, BattleFlag.FORCE_Y_SPAS),
    	K_(2, BattleFlag.INCREMENT_Y_SPD, BattleFlag.FORCE_Y_SPD, BattleFlag.INCREMENT_Y_SPDS, BattleFlag.FORCE_Y_SPDS),
    	L_(2, BattleFlag.INCREMENT_Y_SPE, BattleFlag.FORCE_Y_SPE, BattleFlag.INCREMENT_Y_SPES, BattleFlag.FORCE_Y_SPES),
    	M_(2, BattleFlag.INCREMENT_Y_ACC, BattleFlag.FORCE_Y_ACC, BattleFlag.INCREMENT_Y_ACCS, BattleFlag.FORCE_Y_ACCS),
    	
    	N(2, BattleFlag.SHARE_EXP, BattleFlag.SHARE_EXPS),
    	
    	O(2, BattleFlag.X_STATUS1, BattleFlag.X_STATUSES1),
    	O_(2, BattleFlag.Y_STATUS1, BattleFlag.Y_STATUSES1),
    	
    	P(2, BattleFlag.X_STATUS2, BattleFlag.X_STATUSES2),
    	P_(2, BattleFlag.Y_STATUS2, BattleFlag.Y_STATUSES2),
    	
    	Q(2, BattleFlag.WEATHER, BattleFlag.WEATHERS),
    	;
    	
    	private int minNumberToBeIncompatible;
		private List<BattleFlag> flags;
    	
    	private IncompatibleFlagCombination(int minNumberToBeIncompatible, BattleFlag... flags) {
    		this.minNumberToBeIncompatible = minNumberToBeIncompatible;
    		this.flags = new ArrayList<BattleFlag>();
    		for(BattleFlag flag : flags)
    			this.flags.add(flag);
    	}
    	
    	public int getMinNumberToBeIncompatible() {
			return minNumberToBeIncompatible;
		}
    	
    	public List<BattleFlag> getFlags() {
			return flags;
		}
    }
    
    /**
     * Stores a mapping where a key is a Flag and a value is the String token used to obtain the corresponding Flag.
     */
    private static class FlagMap extends HashMap<BattleFlag, String> {
		private static final long serialVersionUID = 1L;

		public FlagMap() {
    		super();
    	}
    	
    	/**
    	 * Returns the list of encountered flags if the combination is in the map. Returns null otherwise.
    	 */
    	public List<BattleFlag> containsCombination(IncompatibleFlagCombination combination) {
    		int minToBeIncompatible = combination.getMinNumberToBeIncompatible();
    		List<BattleFlag> encountered = new ArrayList<>();
    		for(BattleFlag flag : combination.getFlags()) {
    			if(this.keySet().contains(flag))
    				encountered.add(flag);
    		}
    		
    		if(encountered.size() >= minToBeIncompatible)
    			return encountered;
    		
    		return null;    			
    	}
    		
    	public String getStringOfTokensFromFlags(List<BattleFlag> flags) {
    		StringBuffer sb = new StringBuffer();
    		for(BattleFlag flag : flags) {
    			sb.append(String.format("'%s',", this.get(flag)));
    		}
    		return sb.substring(0, sb.length() == 0 ? 1 : sb.length() - 1);
    	}
    }

    private static GameAction addFlagsToBattleable(Battleable b, String[] flagTokens) throws RouteParserException, ToolInternalException {
    	Queue<String> flagTokensQ = new ArrayDeque<String>();
    	for(String flagToken : flagTokens)
    		flagTokensQ.add(flagToken);
    	
        BattleOptions options = new BattleOptions(b instanceof Trainer && ((Trainer) b).isDoubleBattle());
        FlagMap alreadyEncounteredFlags = new FlagMap();


		//if(b instanceof Trainer && ((Trainer)b).getTrainerName().equals("TATE&LIZA"))
		//	System.out.println("RouteParser.addFlagsToBattleable");
        
        
        // Iterate until there's no more flag to parse
        toNextFlag: 
        while(!flagTokensQ.isEmpty()) {
        	String flagToken = flagTokensQ.remove();
        	BattleFlag currentFlag = BattleFlag.getNextFlagFromString(flagToken);
        	
        	// Error if unknown flag
        	if(currentFlag == BattleFlag.UNKNOWN)
        		throw new BattleFlagNoParamException(flagToken, "This flag is unknown.");
        	
        	// Error if we encounter the same flag twice
        	if(null != alreadyEncounteredFlags.put(currentFlag, flagToken))
        		throw new BattleFlagNoParamException(flagToken, "This flag has already been used for this fight.");
        	
        	// Error if we have incompatible flag combinations
        	for(IncompatibleFlagCombination combination : IncompatibleFlagCombination.values()) {
        		List<BattleFlag> incompatibles = alreadyEncounteredFlags.containsCombination(combination);
	        	if(incompatibles != null) {
	        		throw new BattleFlagNoParamException(flagToken, String.format("This flag is incompatible with these other flags : %s.%s%s",
	        				alreadyEncounteredFlags.getStringOfTokensFromFlags(incompatibles), Constants.endl, 
	        				"These flags would either cancel each other out, have contradictory effects or introduce undefined behaviour from the reader's/tool's perspective."));
	        	}
        	}
        	
        	// No-parameter flags only
        	noParameterFlags:
        	switch(currentFlag) {
        	case SET_TRAINER: // Consider this Pokemon to be a trainer's
        		if (b instanceof Trainer)
        			throw new BattleFlagNoParamException(flagToken, "This flag is useless on trainers.");

        		((Pokemon) b).setWild(false);
        		continue toNextFlag;
        		
        	case MULTI_TARGET_DAMAGE:
            	options.setForcedMultiTargetDamage();
            	continue toNextFlag;
            	
        	case SINGLE_TARGET_DAMAGE:
            	options.setForcedSingleTargetDamage();
            	continue toNextFlag;
            	
        	case FORCE_ENTIRE_DOUBLE_BATTLE:
        		options.setForcedDoubleBattle();
        		continue toNextFlag;
        		
        	case FORCE_ENTIRE_SINGLE_BATTLE:
        		options.setForcedSingleBattle();
        		continue toNextFlag;
        		
        	case BACKTRACK:
        		options.setBacktrackingAfterBattle();
        		continue toNextFlag;
        	
        	case LEVEL_UP_RANGES:
                options.setPrintStatRangesOnLvl();
                continue toNextFlag;
                
        	case LEVEL_UP_STATS:
                options.setPrintStatsOnLvl();
                continue toNextFlag;
                
        	case IV_VARIATION:
        		options.setIVvariation(true);
        		continue toNextFlag;
                
        	default: // Go on with parameter flags
        		break noParameterFlags;
        	}
        	
        	// Extracting parameter
        	String parameterToken = flagTokensQ.poll(); // If null, the next exception will provide the desired parameter format as info, which is better.
        	        	
        	switch(currentFlag) {
        	case SCENARIO_NAME:
        		options.setScenarioName(parameterToken);
            	continue toNextFlag;
            	
        	case VERBOSE:
			{
        		VerboseLevel verboseLevel;
				try {
					if (parameterToken.matches("[0-9]+")) { // Parsing verbose level as number | TODO : hardcoded
						Integer verboseLevelInt = Integer.parseInt(parameterToken);
						verboseLevel = VerboseLevel.values()[verboseLevelInt];
					} else { // Parsing level as string
						verboseLevel = VerboseLevel.fromString(parameterToken);
					}
                } catch(Exception e) {
                	throw new BattleFlagParamException(flagToken, parameterToken,
                			String.format("Verbose level must belong to the following list :%s%s", Constants.endl, VerboseLevel.allStrings()));
                }
				options.setVerboseLevel(verboseLevel);
				
            	continue toNextFlag;
			}
        	
        	case INCREMENT_X_ATK:
        	case INCREMENT_X_DEF:
        	case INCREMENT_X_SPA:
        	case INCREMENT_X_SPD:
        	case INCREMENT_X_SPE:
        	case INCREMENT_X_ACC:
        		
        	case INCREMENT_Y_ATK:
        	case INCREMENT_Y_DEF:
        	case INCREMENT_Y_SPA:
        	case INCREMENT_Y_SPD:
        	case INCREMENT_Y_SPE:
        	case INCREMENT_Y_ACC:
    		{
        		Integer increment = null;
            	try{
            		increment = Integer.parseInt(parameterToken);
            		if(increment < ContainerType.STAT_INCREMENTS.getMinPerStat() || increment > ContainerType.STAT_INCREMENTS.getMaxPerStat())
            			throw new IllegalArgumentException();
            	} catch(Exception e) {
            		throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("Stat increment must be an integer between %d and %d.", 
            				ContainerType.STAT_INCREMENTS.getMinPerStat(), ContainerType.STAT_INCREMENTS.getMaxPerStat()));
            	}
            	
            	Side side = null;
            	Stat stat = null;
            	boolean isForced = false; // unchanged
            	switch(currentFlag) {
            	case INCREMENT_X_ATK: side = Side.PLAYER; stat = Stat.ATK; break;
            	case INCREMENT_X_DEF: side = Side.PLAYER; stat = Stat.DEF; break;
            	case INCREMENT_X_SPA: side = Side.PLAYER; stat = Stat.SPA; break;
            	case INCREMENT_X_SPD: side = Side.PLAYER; stat = Stat.SPD; break;
            	case INCREMENT_X_SPE: side = Side.PLAYER; stat = Stat.SPE; break;
            	case INCREMENT_X_ACC: side = Side.PLAYER; stat = Stat.ACC; break;
            	
            	case INCREMENT_Y_ATK: side = Side.ENEMY; stat = Stat.ATK; break;
            	case INCREMENT_Y_DEF: side = Side.ENEMY; stat = Stat.DEF; break;
            	case INCREMENT_Y_SPA: side = Side.ENEMY; stat = Stat.SPA; break;
            	case INCREMENT_Y_SPD: side = Side.ENEMY; stat = Stat.SPD; break;
            	case INCREMENT_Y_SPE: side = Side.ENEMY; stat = Stat.SPE; break;
            	case INCREMENT_Y_ACC: side = Side.ENEMY; stat = Stat.ACC; break;
            	
            	default :
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	options.setStage(side, stat, increment, isForced);
            	
            	continue toNextFlag;
    		}
        		
        	case FORCE_X_ATK:
        	case FORCE_X_DEF:
        	case FORCE_X_SPA:
        	case FORCE_X_SPD:
        	case FORCE_X_SPE:
        	case FORCE_X_ACC:

        	case FORCE_Y_ATK:
        	case FORCE_Y_DEF:
        	case FORCE_Y_SPA:
        	case FORCE_Y_SPD:
        	case FORCE_Y_SPE:
        	case FORCE_Y_ACC:
			{
        		Integer stage = null;
            	try {
            		stage = Integer.parseInt(parameterToken);
            		if(stage < ContainerType.STAT_STAGES.getMinPerStat() || stage > ContainerType.STAT_STAGES.getMaxPerStat())
            			throw new IllegalArgumentException();
            	} catch(Exception e) {
            		throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("Stat stage must be an integer between %d and %d.", 
            						ContainerType.STAT_STAGES.getMinPerStat(), ContainerType.STAT_STAGES.getMaxPerStat()));
            	}
            	
            	Side side = null;
            	Stat stat = null;
            	boolean isForced = true; // unchanged
            	switch(currentFlag) {
            	case FORCE_X_ATK: side = Side.PLAYER; stat = Stat.ATK; break;
            	case FORCE_X_DEF: side = Side.PLAYER; stat = Stat.DEF; break;
            	case FORCE_X_SPA: side = Side.PLAYER; stat = Stat.SPA; break;
            	case FORCE_X_SPD: side = Side.PLAYER; stat = Stat.SPD; break;
            	case FORCE_X_SPE: side = Side.PLAYER; stat = Stat.SPE; break;
            	case FORCE_X_ACC: side = Side.PLAYER; stat = Stat.ACC; break;
            	
            	case FORCE_Y_ATK: side = Side.ENEMY; stat = Stat.ATK; break;
            	case FORCE_Y_DEF: side = Side.ENEMY; stat = Stat.DEF; break;
            	case FORCE_Y_SPA: side = Side.ENEMY; stat = Stat.SPA; break;
            	case FORCE_Y_SPD: side = Side.ENEMY; stat = Stat.SPD; break;
            	case FORCE_Y_SPE: side = Side.ENEMY; stat = Stat.SPE; break;
            	case FORCE_Y_ACC: side = Side.ENEMY; stat = Stat.ACC; break;
            	
            	default:
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	options.setStage(side, stat, stage, isForced);
            	
            	continue toNextFlag;
			}
                
        	case INCREMENT_X_ATKS:
        	case INCREMENT_X_DEFS:
        	case INCREMENT_X_SPAS:
        	case INCREMENT_X_SPDS:
        	case INCREMENT_X_SPES:
        	case INCREMENT_X_ACCS:

        	case INCREMENT_Y_ATKS:
        	case INCREMENT_Y_DEFS:
        	case INCREMENT_Y_SPAS:
        	case INCREMENT_Y_SPDS:
        	case INCREMENT_Y_SPES:
        	case INCREMENT_Y_ACCS:
			{
            	String[] incrementsStrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
            	ArrayList<Integer> increments = new ArrayList<>();
            	String incrementStrBackup = null;
            	int currentTotal = ContainerType.STAT_INCREMENTS.getDefaultValue(); // 0

            	for(String stageStr : incrementsStrArr) {
            		incrementStrBackup = stageStr;
            		int increment = Integer.parseInt(stageStr);
            		if(increment < ContainerType.STAT_INCREMENTS.getMinPerStat() || increment > ContainerType.STAT_INCREMENTS.getMaxPerStat())
                		throw new BattleFlagParamException(flagToken, incrementStrBackup, 
                				String.format("List of stage increments must be a '%s'-separated list of integers between %d and %d.", 
                        				BattleFlag.parameterSeparator, ContainerType.STAT_INCREMENTS.getMinPerStat(), ContainerType.STAT_INCREMENTS.getMaxPerStat()));
                	
            		
            		currentTotal += increment;
            		if(currentTotal < ContainerType.STAT_INCREMENTS.getMinPerStat() || currentTotal > ContainerType.STAT_INCREMENTS.getMaxPerStat())
                		throw new BattleFlagParamException(flagToken, incrementStrBackup, 
                				String.format("The total increment '%d' exceeds the maximum of '%d' in absolute value.", currentTotal, ContainerType.STAT_INCREMENTS.getMaxPerStat()));
        	
            		increments.add(currentTotal);
                }
            	
            	Side side = null;
            	Stat stat = null;
            	boolean isForced = false; // unchanged
            	switch(currentFlag) {
            	case INCREMENT_X_ATKS: side = Side.PLAYER; stat = Stat.ATK; break;
            	case INCREMENT_X_DEFS: side = Side.PLAYER; stat = Stat.DEF; break;
            	case INCREMENT_X_SPAS: side = Side.PLAYER; stat = Stat.SPA; break;
            	case INCREMENT_X_SPDS: side = Side.PLAYER; stat = Stat.SPD; break;
            	case INCREMENT_X_SPES: side = Side.PLAYER; stat = Stat.SPE; break;
            	case INCREMENT_X_ACCS: side = Side.PLAYER; stat = Stat.ACC; break;
            	
            	case INCREMENT_Y_ATKS: side = Side.ENEMY; stat = Stat.ATK; break;
            	case INCREMENT_Y_DEFS: side = Side.ENEMY; stat = Stat.DEF; break;
            	case INCREMENT_Y_SPAS: side = Side.ENEMY; stat = Stat.SPA; break;
            	case INCREMENT_Y_SPDS: side = Side.ENEMY; stat = Stat.SPD; break;
            	case INCREMENT_Y_SPES: side = Side.ENEMY; stat = Stat.SPE; break;
            	case INCREMENT_Y_ACCS: side = Side.ENEMY; stat = Stat.ACC; break;
            	
            	default :
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	options.setStages(side, stat, increments, isForced);
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
            	
            	continue toNextFlag;
			}
        		
        	case FORCE_X_ATKS:
        	case FORCE_X_DEFS:
        	case FORCE_X_SPAS:
        	case FORCE_X_SPDS:
        	case FORCE_X_SPES:
        	case FORCE_X_ACCS:

        	case FORCE_Y_ATKS:
        	case FORCE_Y_DEFS:
        	case FORCE_Y_SPAS:
        	case FORCE_Y_SPDS:
        	case FORCE_Y_SPES:
        	case FORCE_Y_ACCS:
			{
				String[] stagesStrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
            	ArrayList<Integer> stages = new ArrayList<>();
            	String stageStrBackup = null;
            	try {
	            	for(String stageStr : stagesStrArr) {
	            		stageStrBackup = stageStr;
	            		int stage = Integer.parseInt(stageStr);
	            		if(stage < ContainerType.STAT_STAGES.getMinPerStat() || stage > ContainerType.STAT_STAGES.getMaxPerStat())
	            			throw new Exception();
	            		
	            		stages.add(stage);
	                }
            	} catch (Exception e) {
            		throw new BattleFlagParamException(flagToken, stageStrBackup, 
            				String.format("List of stage stages must be a '%s'-separated list of integers between %d and %d.", 
                    				BattleFlag.parameterSeparator, ContainerType.STAT_STAGES.getMinPerStat(), ContainerType.STAT_STAGES.getMaxPerStat()));
            	}
            	
            	Side side = null;
            	Stat stat = null;
            	boolean isForced = true; // unchanged
            	switch(currentFlag) {
            	case FORCE_X_ATKS: side = Side.PLAYER; stat = Stat.ATK; break;
            	case FORCE_X_DEFS: side = Side.PLAYER; stat = Stat.DEF; break;
            	case FORCE_X_SPAS: side = Side.PLAYER; stat = Stat.SPA; break;
            	case FORCE_X_SPDS: side = Side.PLAYER; stat = Stat.SPD; break;
            	case FORCE_X_SPES: side = Side.PLAYER; stat = Stat.SPE; break;
            	case FORCE_X_ACCS: side = Side.PLAYER; stat = Stat.ACC; break;
            	
            	case FORCE_Y_ATKS: side = Side.ENEMY; stat = Stat.ATK; break;
            	case FORCE_Y_DEFS: side = Side.ENEMY; stat = Stat.DEF; break;
            	case FORCE_Y_SPAS: side = Side.ENEMY; stat = Stat.SPA; break;
            	case FORCE_Y_SPDS: side = Side.ENEMY; stat = Stat.SPD; break;
            	case FORCE_Y_SPES: side = Side.ENEMY; stat = Stat.SPE; break;
            	case FORCE_Y_ACCS: side = Side.ENEMY; stat = Stat.ACC; break;
            	
            	default :
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	options.setStages(side, stat, stages, isForced);
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
            	
            	continue toNextFlag;
			}
    			
        	case SHARE_EXP:
			{
				Integer participantNb = null;
            	try {
            		participantNb = Integer.parseInt(parameterToken);
            		if(participantNb < BattleOptions.MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT || participantNb > BattleOptions.MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT)
            			throw new Exception();
            	} catch(Exception e) {
            		throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("Shared experience must be an integer between %d and %d, representing the number of Pokémon involved in the sharing.", 
            						BattleOptions.MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT, BattleOptions.MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT));
            	}

        		options.setSxp(participantNb);
            		
            	continue toNextFlag;
    			
			}
				
        	case SHARE_EXPS:
			{
				String[] participantNbStrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
                //ArrayList<Integer> list = new ArrayList<>();
                String participantNbStrBackup = null;
                try {
	                for(String  participantNbStr : participantNbStrArr) {
	                	participantNbStrBackup = participantNbStr;
	                	int participantNb = Integer.parseInt(participantNbStr);
	            		if(participantNb < BattleOptions.MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT || participantNb > BattleOptions.MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT)
	            			throw new Exception();
	            		
	                    options.addSxp(participantNb);
	                }
                } catch(Exception e) {
                	throw new BattleFlagParamException(flagToken, participantNbStrBackup, 
            				String.format("List of shared experiences must be a '%s'-separated list of integers between %d and %d.%s"
            						+ "Each integer represents the number of Pokémon involved in the sharing for this slot.", 
                    				BattleFlag.parameterSeparator, BattleOptions.MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT, BattleOptions.MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT,
                    				Constants.endl));
                }
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
                
            	continue toNextFlag;
            }
    			
        	case ORDER:
			{
				HashSet<Integer> alreadyEncounteredSlots = new HashSet<Integer>(); // to prevent duplicates by accidents
            	
                String[] batchStrs = parameterToken.split(BattleFlag.parameterSeparatorRegex);
            	//ArrayList<ArrayList<Integer>> listOfBatches = new ArrayList<>();
            	String batchStrBackup = null;
            	String indexStrBackup = null;
            	try {
	                for(String batchStr : batchStrs) {
	                	batchStrBackup = batchStr;
	                	String[] indexStrArr = batchStr.split(BattleFlag.parameterUnifierRegex);
	                	if(indexStrArr.length < BattleOptions.MIN_BATCH_SIZE || indexStrArr.length > BattleOptions.MAX_BATCH_SIZE) {
	                		throw new IndexOutOfBoundsException();
	                	}

	                	ArrayList<Integer> batch = new ArrayList<>();
	                	for(String numStr : indexStrArr) {
	                		indexStrBackup = numStr;
	                		int index = Integer.parseInt(numStr);
	                		if (index < BattleOptions.MIN_NUMBER_OF_BATTLERS_PER_FIGHT || index > BattleOptions.MAX_NUMBER_OF_BATTLERS_PER_FIGHT)
	                			throw new Exception();
	                		batch.add(index);
	                		
	                		if(!alreadyEncounteredSlots.add(index))
	                			throw new ArrayStoreException();
	                	}
	                	options.addOrderBatch(batch);
	                }
            	} catch(ArrayStoreException e) {
            		throw new BattleFlagParamException(flagToken, indexStrBackup, "Duplicate index.");
            	} catch (Exception e) {
            		throw new BattleFlagParamException(flagToken, batchStrBackup, 
            				String.format("Ordering must be an '%s'-separated list of battler indices (or batches of battler indices).%s"
            						+ "Each index must be an integer between %d and %d.%s"
            						+ "Each batch must be an single index, or a '%s'-separated list of at most '%d' indices.%s"
            						+ "Examples of valid orders : '%d%s%d%s%d%s%d', '%d%s%d%s%d%s%d', '%d%s%d%s%d%s%d%s%d'.",
            				BattleFlag.parameterSeparator, Constants.endl,
            				BattleOptions.MIN_NUMBER_OF_BATTLERS_PER_FIGHT, BattleOptions.MAX_NUMBER_OF_BATTLERS_PER_FIGHT, Constants.endl,
            				BattleFlag.parameterUnifier, BattleOptions.MAX_BATCH_SIZE, Constants.endl,
            				1, BattleFlag.parameterSeparator, 4, BattleFlag.parameterSeparator, 3, BattleFlag.parameterSeparator, 2,
            				1, BattleFlag.parameterUnifier, 4, BattleFlag.parameterSeparator, 2, BattleFlag.parameterSeparator, 3,
            				1, BattleFlag.parameterUnifier, 3, BattleFlag.parameterSeparator, 2, BattleFlag.parameterUnifier, 5, BattleFlag.parameterSeparator, 4)); // TODO : maybe factor this out.
            	}
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
                
                continue toNextFlag;
			}
            	
        	case X_STATUS1:
        	case Y_STATUS1:
    		{
            	Status status = null;
            	try {
	            	status = Status.getStatusFromString(parameterToken);
	            	if(!status.isStatus1())
	            		throw new Exception();
            	} catch (Exception e) {
            		throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("A single primary status must belong to the following list :%s%s",
            						Constants.endl, Status.getAllStatus1AsString()));
            	}
            	
            	Side side = null;
            	switch(currentFlag) {
            	case X_STATUS1: side = Side.PLAYER; break;
            	case Y_STATUS1: side = Side.ENEMY;  break;
            	default :
            		throw new RouteParserInternalException(parameterToken, flagTokensQ);
            	}
            	
            	options.setStatus1(side, status);
            	
            	continue toNextFlag;
            }
            	
        	case X_STATUS2:
        	case Y_STATUS2:
			{
            	String[] status23StrArr = parameterToken.split(BattleFlag.parameterUnifierRegex);
            	EnumSet<Status> statuses2_3 = Status.noStatus2_3();
            	
            	String status23StrBackup = null;
            	try {
	            	for(String status23Str : status23StrArr) {
	            		status23StrBackup = status23Str;
	            		Status status23 = Status.getStatusFromString(status23Str);

		            	if(!status23.isStatus2_3()) {
		            		throw new Exception();
		            	}
		            		
	            		statuses2_3.add(status23);
	            	}
            	} catch (Exception e) {
            		throw new BattleFlagParamException(flagToken, status23StrBackup, 
        				String.format("A secondary status group must be a '%s'-separated list of statuses belonging to the following list :%s%s", 
                				BattleFlag.parameterUnifier, Constants.endl, Status.getAllStatus23AsString()));
            	}
            	
            	Side side = null;
            	switch(currentFlag) {
            	case X_STATUS2: side = Side.PLAYER; break;
            	case Y_STATUS2: side = Side.ENEMY;  break;
            	default:
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
                options.setStatuses2_3(side, statuses2_3);
                    
                continue toNextFlag;
			}
                
        	case X_STATUSES1:
        	case Y_STATUSES1:
        	{
            	Side side = null;
            	switch(currentFlag) {
            	case X_STATUSES1: side = Side.PLAYER; break;
            	case Y_STATUSES1: side = Side.ENEMY;  break;
            	default :
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	String[] status1StrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
                String status1StrBackup = null;
	            try {
                    for(String status1Str : status1StrArr) {
                    	status1StrBackup = status1Str;
		            	Status status1 = Status.getStatusFromString(status1Str);
		            	if(!status1.isStatus1())
		            		throw new Exception();
		            	
		            	options.addStatus1(side, status1);
                    }
	            } catch (Exception e) {
	            	throw new BattleFlagParamException(flagToken, status1StrBackup, 
        				String.format("A list of primary statuses must be a '%s'-separated list of statuses.%s"
        						+ "Each belonging to the following list :%s%s",
	            				BattleFlag.parameterSeparator, Constants.endl, 
	            				Constants.endl, Status.getAllStatus1AsString()));
        		}
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
	            
	            continue toNextFlag;
            }
           
        	case X_STATUSES2:
        	case Y_STATUSES2:
			{
            	Side side = null;
            	switch(currentFlag) {
            	case X_STATUSES2: side = Side.PLAYER; break;
            	case Y_STATUSES2: side = Side.ENEMY;  break;
            	default:
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
				String[] statuses23StrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
				String status23StrBackup = null;
            	try {
					for(String statuses23Str : statuses23StrArr) {
						String[] status23StrArr = statuses23Str.split(BattleFlag.parameterUnifierRegex);
						EnumSet<Status> statuses2_3 = Status.noStatus2_3();
	            	
		            	for(String status23Str : status23StrArr) {
		            		status23StrBackup = status23Str;
		            		Status status23 = Status.getStatusFromString(status23Str);
	
			            	if(!status23.isStatus2_3()) {
			            		throw new Exception();
			            	}
			            		
		            		statuses2_3.add(status23);
		            	}
		            	options.addStatuses2_3(side, statuses2_3);
					}
            	} catch (Exception e) {
	            	throw new BattleFlagParamException(flagToken, status23StrBackup, 
            				String.format("A list of secondary statuses must be a '%s'-separated list of statuses groups.%s"
            						+ "Each statuses group must either be a single status, or a '%s'-separated list of statuses.%s"
            						+ "Each status must belong to the following list :%s%s", 
    	            				BattleFlag.parameterSeparator, Constants.endl,
    	            				BattleFlag.parameterUnifier, Constants.endl,
    	            				Constants.endl, Status.getAllStatus23AsString()));
            	}
            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
            	
            	continue toNextFlag;
			}
                
        	case X_CURR_HP:
        	case Y_CURR_HP:
			{
				Side side = null;
            	switch(currentFlag) {
            	case X_CURR_HP: side = Side.PLAYER; break;
            	case Y_CURR_HP: side = Side.ENEMY;  break;
	            default:
            		throw new RouteParserInternalException(flagToken, flagTokensQ);
            	}
            	
            	Integer currHP = options.getCurrentHP(side);
            	CurrentHPid currHPid = null;
            	try {
            		currHPid = CurrentHPid.getCurrentHPidFromString(parameterToken);
            		if(currHPid == CurrentHPid.CUSTOM_VALUE) {
            			currHP = Integer.parseInt(parameterToken);
            			if(currHP < 0)
            				throw new Exception();
            		}
            	} catch (Exception e) {
            		throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("Current HP must either be a positive integer or a predefinite expression among the following list :%s%s",
            						Constants.endl, CurrentHPid.getAllIDsString()));
            	}
            	
            	options.setCurrentHP(side, currHP);
            	options.setCurrentHPid(side, currHPid);
                continue toNextFlag;
			}
                
        	case WEATHER:
        	{
				Weather weather = null;
				try {
					weather = Weather.getWeatherFromString(parameterToken);
				} catch (Exception e) {
					throw new BattleFlagParamException(flagToken, parameterToken, 
							String.format("Single weather must belong to the following list :%s%s",
									Constants.endl, Weather.getAllWeathersString()));
            	}
				
            	options.setWeather(weather);
            	continue toNextFlag;
			}
			            
        	case WEATHERS:
        	{
            	String[] weatherStrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
            	String weatherStrBackup = null;
            	try {
	            	for (String weatherStr : weatherStrArr) {
	            		weatherStrBackup = weatherStr;
	            		Weather weather = Weather.getWeatherFromString(weatherStr);
	            		options.addWeather(weather);
	            	}

				} catch (Exception e) {
					throw new BattleFlagParamException(flagToken, weatherStrBackup, 
							String.format("A list of weathers must be a '%s'-separated list of weathers.%s"
									+ "Each weather belongs to the following list :%s%s", 
									BattleFlag.parameterSeparator, Constants.endl,
									Constants.endl, Weather.getAllWeathersString()));
            	}
            	            	
            	// Can't check now if the number of indices is correct, because additional partners could come later
            	            	
	        	continue toNextFlag;
			}

        	case X_PARTNER:
        	case Y_PARTNER:
        	{
        		Trainer partner = Trainer.getTrainerByName(parameterToken);
        		if(partner == null)
        			throw new BattleFlagParamException(flagToken, parameterToken, "Partner name is unknown.");
        		
        		Side side = null;
        		switch(currentFlag) {
        		case X_PARTNER: side = Side.PLAYER; break;
        		case Y_PARTNER: side = Side.ENEMY;  break;
        		default:
        			throw new RouteParserInternalException(flagToken, flagTokensQ);
        		}
        		
        		options.setPartner(side, partner);
        		
	        	continue toNextFlag;
			}
        	
        	case RETURN_AVERAGE:
        	{
        		String[] returnStrArr = parameterToken.split(BattleFlag.parameterSeparatorRegex);
				//String returnStrBackup = null;
				
            	try {
            		int returnOffset = Integer.parseInt(returnStrArr[0]);
            		int returnMaxAdded = Integer.parseInt(returnStrArr[1]);
            		
            		options.addReturnAverage(returnOffset, returnMaxAdded);
            	} catch (Exception e) {
	            	throw new BattleFlagParamException(flagToken, parameterToken, 
            				String.format("Expected format : <offset>%s<max_added>", BattleFlag.parameterSeparator)
            				);
            	}
            	
            	continue toNextFlag;
        	}

			default:
    			throw new RouteParserInternalException(flagToken, flagTokensQ);
			} // end switch Flag
        } // end flagTokens
        
        // Data goes through another round of validation in the Battle creation too
        return new Battle(b, options);
    }
}
