package tool;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tool.Happiness.HappinessEvent;
import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;

//represents a battle, with planned statmods
public class Battle extends GameAction {
    private Battleable opponent;
    private BattleOptions options;

    public Battle(Battleable b) throws ToolInternalException {
        opponent = b;
        options = new BattleOptions();
    }

    public Battle(Battleable b, BattleOptions options) {
        opponent = b;
        this.options = options;
    }

    public BattleOptions getOptions() {
        return options;
    }

    public StatModifier getStatModifier(Side side) {
    	return options.getStatModifier(side);
    }
    /*
    public StatModifier getMod1() {
        return options.getMod1();
    }

    public StatModifier getMod2() {
        return options.getMod2();
    }
    */

    public VerboseLevel getVerbose() {
        return options.getVerbose();
    }
    /*
    public static Battle makeBattle(Trainer trainer) {
        return new Battle(trainer);
    }
*/
    public static Battle makeBattle(Trainer trainer, BattleOptions options) {
        return new Battle(trainer, options);
    }
/*
    public static Battle makeBattle(Pokemon p) {
        return new Battle(p);
    }
*/
    public static Battle makeBattle(Pokemon p, BattleOptions options) {
        return new Battle(p, options);
    }

    @Override
    public void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
        doBattle(p);

        if (!(opponent instanceof Trainer))
        	return; 
        
        Trainer t = (Trainer) opponent;
        
        /* TODO
        // Happiness boost from leaders, E4 and Champ
        if(t.isBoostingHappiness()) {
        	p.setHappiness(HappinessEvent.GYM_LEADER_E4_CHAMPION.getFinalHappiness(p.getHappiness(), p.getHeldItem() != null && p.getHeldItem().isBoostingHappiness(), p.isInLuxuryBall(), false));
        }
        */
        
        // check for special gym leader badges
        List<Stat> badgeBoosts = t.getBadgeBoosts();
        if(badgeBoosts == null)
        	return;
        
        for (Stat stat : t.getBadgeBoosts())
        	p.setBadge(stat);
    }

    private void doBattle(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
    	// Used in case of restoring scenario
        String scenarioName = options.getScenarioName();
        boolean isBacktrackingAfterBattle = options.isBacktrackingAfterBattle();
        Pokemon pCopy = new Pokemon(p);
        
        if(scenarioName != null || isBacktrackingAfterBattle) {
        	Main.appendln("");
        	Main.appendln(String.format("Performing scenario%s.%s", 
        			scenarioName == null ? "" : String.format(" '%s'", scenarioName),
        			isBacktrackingAfterBattle ?  " Backtracking state after next battle." : ""));
        }
        	
        int lastLvl = p.getLevel();

        if (opponent instanceof Pokemon) {
        	/*
        	// set currHP | TODO : duplicate code within Trainer
            int xCurrHP = options.getCurrentHP(Side.PLAYER);
            int yCurrHP = options.getCurrentHP(Side.ENEMY);
        	{
            StatModifier mod1 = options.getMod1();
            mod1.setCurrHP(xCurrHP == 0 ? p.getHP() : xCurrHP);
        	}
        	{
            StatModifier mod2 = options.getMod2();
            mod2.setCurrHP(yCurrHP == 0 ? ((Pokemon)opponent).getHP() : yCurrHP);
        	}
        	*/
        	boolean isPostponedExperience = false;
        	options.updateStatModifiersAndOptions(p, (Pokemon)opponent, isPostponedExperience);
        	
        	if(getVerbose() != VerboseLevel.NONE)
        		printBattle(p, (Pokemon) opponent);

            opponent.battle(p, options);
            if (p.getLevel() > lastLvl) {
                lastLvl = p.getLevel();
                if (options.isPrintStatRangesOnLvl()) {
                    Main.appendln(p.statRanges(false));
                }
                if (options.isPrintStatsOnLvl()) {
                    Main.appendln(p.statRanges(true));
                }
            }
            if(getVerbose() != VerboseLevel.NONE)
                Main.appendln(p.levelAndExperienceNeededToLevelUpStr());
            
            /* TODO: Fix this (non working) hack, because battleTower will be handled differently
            if(!((Pokemon) opponent).isTowerPoke()) {
                opponent.battle(p, options);
                if (p.getLevel() > lastLvl) {
                    lastLvl = p.getLevel();
                    if (options.isPrintSRsOnLvl()) {
                        Main.appendln(p.statRanges(false));
                    }
                    if (options.isPrintSRsBoostOnLvl()) {
                        Main.appendln(p.statRanges(true));
                    }
                }
                if(getVerbose() == BattleOptions.EVERYTHING) {
                    Main.appendln(String.format("LVL: %d EXP NEEDED: %d/%d", p.getLevel(),
                        p.expToNextLevel(), p.expForLevel()));
                }
            }
            */
        } else { // is a Trainer
            Trainer t = (Trainer) opponent;
            
        	// Adding money only if not backtracking
            if(!isBacktrackingAfterBattle){
                int totalMoneyYield = t.getReward(p);
                totalMoneyYield += (options.getPartner(Side.ENEMY) == null) ? 0 : options.getPartner(Side.ENEMY).getReward(p);
                Settings.money += totalMoneyYield;
            }
            
            
			// Trainer xPartner = options.getPartner(Side.PLAYER);
            Trainer yPartner = options.getPartner(Side.ENEMY);
            ArrayList<ArrayList<Integer>> order = options.getOrder();
            
            // TODO: force battle tower
            /*
            if (options.isTrueBattleTower() == null) // no override
            	options.setBattleTower(t.getBattleTower() != 0);
            // override is already in options otherwise
            */
                        
            
            // Reordering enemy trainer pokes
            ArrayList<Pokemon> finalTrainerPokes = null; // to set in trainer
            ArrayList<ArrayList<Pokemon>> finalTrainerPokesByBatch = new ArrayList<>();
            
            ArrayList<Pokemon> pokes = new ArrayList<>();
            for(Pokemon poke : t)
            	pokes.add(poke);
            
            if(yPartner != null) {
                for(Pokemon poke : yPartner)
                	pokes.add(poke);
            }
            
            if(order.isEmpty()) {
            	finalTrainerPokes = pokes;
            	order = new ArrayList<>();
            	for(Pokemon poke : pokes) {
            		ArrayList<Pokemon> singleton = new ArrayList<>();
            		singleton.add(poke);
            		finalTrainerPokesByBatch.add(singleton);
            	}
            } else {
            	finalTrainerPokes = new ArrayList<>();
                Set<Integer> expectedOrderIndices = new HashSet<Integer>();
                for(int i = 1; i <= pokes.size(); i++)
                	expectedOrderIndices.add(i);
                
                for(ArrayList<Integer> batch : order) {
                	ArrayList<Pokemon> batchPokes = new ArrayList<>();
                	for(int index : batch) {
                		Pokemon poke = null;
                		try {
                			poke = pokes.get(index-1);
                		} catch (Exception e) {
                			throw new IndexOutOfBoundsException(String.format("In trainer '%s', index '%d' is too high in order option.",
                					t.getTrainerAlias(), index));
            			}
                			
                		finalTrainerPokes.add(poke);
                		batchPokes.add(poke);
                		
                		if(!expectedOrderIndices.remove(index))
                			throw new IndexOutOfBoundsException(String.format("In trainer '%s', duplicate index '%d' in order option.",
                					t.getTrainerAlias(), index));
                	}
                	finalTrainerPokesByBatch.add(batchPokes);
                }
                
                if(!expectedOrderIndices.isEmpty()) {
        			throw new IllegalArgumentException(String.format("In trainer '%s', missing indices %s in order option.",
        					t.getTrainerAlias(), expectedOrderIndices.toString()));
                }
            }
            t.setParty(finalTrainerPokes);
            
            // TODO : handle player partner somehow 
            
            
            if(getVerbose() != VerboseLevel.NONE) {
                Main.appendln(Constants.endl + t.toString(p, options));
            }

            int minIV = ContainerType.IV.getMinPerStat();
    		int maxIV = ContainerType.IV.getMaxPerStat();
            for(ArrayList<Pokemon> batchPokes : finalTrainerPokesByBatch) {            	
	            for (Pokemon opps : batchPokes) {
	            	// Postpone EXP if we're not at the last Pokémon of the batch
	            	boolean isPostponedExperience = opps != batchPokes.get(batchPokes.size() - 1);
	            	
	            	options.updateStatModifiersAndOptions(p, opps, isPostponedExperience);

                    if (getVerbose() != VerboseLevel.NONE) {                    	
                    	printBattle(p, opps);
                    	
                    	// Give speed information based on IVs and nature
                    	Pokemon pSpeedCopy = new Pokemon(p);
                    	int currentSpeed = options.getStatModifier(Side.PLAYER).getFinalSpeed(p);
                    	int oppSpeed = options.getStatModifier(Side.ENEMY).getFinalSpeed(opps);
                    	
                    	Main.appendln(String.format("SPEED INFO (vs. %s %s SPE)", opps.getDisplayName(), oppSpeed));
                    	
                    		// Check if player is always slower or always faster, whatever nature it has
                    	pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMinPerStat()); // min iv
                		pSpeedCopy.setNature(Nature.BRAVE); // minus spe
                    	int meMinSpeed = options.getStatModifier(Side.PLAYER).getFinalSpeed(pSpeedCopy);
                    	
                    	pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMaxPerStat()); //max iv
                		pSpeedCopy.setNature(Nature.TIMID); // bonus spe
                    	int meMaxSpeed = options.getStatModifier(Side.PLAYER).getFinalSpeed(pSpeedCopy);
                    	
                    	if(meMinSpeed > oppSpeed) {
                    		Main.appendln(">> Player is always faster than enemy.");
                            Main.appendln("");
                    	} else if(meMaxSpeed < oppSpeed) {
                    		Main.appendln(">> Player is always slower than enemy.");
                            Main.appendln("");
                    	} else {  // There exist speed thresholds
                    		// Current speed comparison
                    		if(currentSpeed > oppSpeed) {
                        		Main.appendln("~~ Player is currently faster than enemy.");
                    		} else if (currentSpeed < oppSpeed) {
                        		Main.appendln("~~ Player is currently slower than enemy.");
                    		} else {
                        		Main.appendln("~~ Player is currently speedtied with enemy.");
                    		}
                    		
                    		// Speed IV and nature variation
                    		String[] names = new String[] {"-", "", "+"}; // TODO : hardcoded
                        	Nature[] natures = new Nature[] {Nature.BRAVE, Nature.HARDY, Nature.TIMID}; // Spe : minus, neutral, bonus
                        	for(int k = 0; k < natures.length; k++) { 
                        		Nature nature = natures[k];
                        		String statStr = String.format(">> %s%1s : ", Stat.SPE, names[k]);
                        		
                        		pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMinPerStat());
                        		pSpeedCopy.setNature(nature);
    	                    	meMinSpeed = options.getStatModifier(Side.PLAYER).getFinalSpeed(pSpeedCopy);
    	                    	
                        		pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMaxPerStat());
                        		pSpeedCopy.setNature(nature);
    	                    	meMaxSpeed = options.getStatModifier(Side.PLAYER).getFinalSpeed(pSpeedCopy);
    	                    	
                        		Main.append(statStr);
                        		
    	                    		// Checking always slower/faster for this nature
    	                    	if(meMaxSpeed < oppSpeed) {
    	                    		Main.appendln("Player always slower than enemy.");
    	                            //Main.appendln("");
    	                            continue;
    	                    	} else if (meMinSpeed > oppSpeed) {
    	                    		Main.appendln("Player always faster than enemy.");
    	                            Main.appendln("");
    	                    		break;
    	                    	}
    	                    	
    	                    		// If there exist speed thresholds for this nature
    	                    	else if (meMinSpeed <= oppSpeed && meMaxSpeed >= oppSpeed) {    	                    		
    	                            int minIvToSpeedtie = Integer.MAX_VALUE, minIvToOutspeed = Integer.MAX_VALUE;
    	                            for (int sIV = minIV; sIV <= maxIV; sIV++) {
    	                            	pSpeedCopy.getIVs().put(Stat.SPE, sIV);
    	                        		pSpeedCopy.setNature(nature);
    	                                int mySpd = options.getStatModifier(Side.PLAYER).getFinalSpeed(pSpeedCopy);
    	                                if (mySpd == oppSpeed && sIV < minIvToSpeedtie) {
    	                                    minIvToSpeedtie = sIV;
    	                                }
    	                                if (mySpd > oppSpeed && sIV < minIvToOutspeed) {
    	                                    minIvToOutspeed = sIV;
    	                                    break;
    	                                }
    	                            }
    	                            
    	                            int maxIvToSpeedtie = minIvToOutspeed - 1;
    	                            //Main.append("(Speed IV required");
    	                            if (minIvToSpeedtie != Integer.MAX_VALUE && minIvToOutspeed != Integer.MAX_VALUE && (minIvToSpeedtie != minIvToOutspeed)) {
    	                            	String speedtieStr = minIvToSpeedtie == maxIvToSpeedtie || maxIvToSpeedtie == 0 ?
    	                            			String.format("%d", minIvToSpeedtie) : String.format("%d-%d", minIvToSpeedtie, maxIvToSpeedtie);
    	                            	String outspeedStr = minIvToOutspeed == 31 ? String.format("%d", minIvToOutspeed) : String.format("%d-%d", minIvToOutspeed, 31);
    	                            	
    	                            	Main.append(String.format("%s to outspeed, %s to speedtie.", outspeedStr, speedtieStr));
    	                                //Main.append(" to outspeed: " + outspeedIV + ", to speedtie: " + tieIV);
    	                            } else if (minIvToOutspeed != Integer.MAX_VALUE) {
    	                            	minIvToOutspeed = (int)Math.max(0, minIvToOutspeed);
    	                            	String outspeedStr = minIvToOutspeed == 31 ? String.format("%d", minIvToOutspeed) : String.format("%d-%d", (int)minIvToOutspeed, 31);
    	                            	
    	                            	Main.append(String.format("%s to outspeed.", outspeedStr));
    	                            	//Main.append(" to outspeed: " + outspeedIV);
    	                            } else {
    	                            	maxIvToSpeedtie = (int)Math.min(31, maxIvToSpeedtie);
    	                            	String speedtieStr = minIvToSpeedtie == maxIvToSpeedtie || maxIvToSpeedtie == 0 ?
    	                            			String.format("%d", minIvToSpeedtie) : String.format("%d-%d", minIvToSpeedtie, maxIvToSpeedtie);
    	                            	
    	                            	Main.append(String.format("%s to speedtie.", speedtieStr));
    	                                //Main.append(" to speedtie: " + tieIV);
    	                            }
    	                            //Main.appendln(" with the same nature)");
    	                            Main.appendln("");
    	
    	                    	} // end speed thresholds
                            } // end for 
                            Main.appendln("");
                    	}
                    	
                    	
                    } // end if we print
	            } // end batch 
	            
	            // currentOpponentIndex -= batch.size(); // backtrack the current index number
	            options.backtrackCurrentOpponentIndex(batchPokes.size());
	            
	            // Update EVs and EXP only at the end of a batch
	            for (Pokemon opps : batchPokes) {
	            	boolean isPostponedExperience = false;
	            	options.updateStatModifiersAndOptions(p, opps, isPostponedExperience);

                    opps.battle(p, options);
                                        
                    // test if you leveled up on this pokemon
                    if (p.getLevel() > lastLvl) {
                        lastLvl = p.getLevel();
                        if (options.isPrintStatRangesOnLvl()) {
                            Main.appendln(p.statRanges(false));
                        }
                        if (options.isPrintStatsOnLvl()) {
                            Main.appendln(p.getDetailledStatsStr(false));
                        }
                    }
	            } // end batch
	            
                if(getVerbose() != VerboseLevel.NONE)
                    Main.appendln(p.levelAndExperienceNeededToLevelUpStr());
                
            } // end trainer pokes
        } // end trainer
        
        // If Pokémon is Deoxys, then always update stats, even if it didn't level-up.
        if(p.getSpecies().isUpdatingStatsAfterEveryBattle())
        	p.updateEVsAndCalculateStats();
        
        // Scenario name and backtracking
        if(getVerbose() != VerboseLevel.NONE) {
        	Main.appendln("");
	        if(scenarioName != null) {
	        	Main.append(String.format("End of scenario%s. ", 
	        			scenarioName == null ? "" : String.format(" '%s'", scenarioName)));
	        }
	        if(isBacktrackingAfterBattle) {
	        	Main.mainPoke = pCopy;
	        	Main.append("Backtracking before last battle.");
	        }
        	Main.appendln("");
        }
    }

    // does not actually do the battle, just prints summary
    public void printBattle(Pokemon us, Pokemon them) throws UnsupportedOperationException, ToolInternalException {
        Main.appendln(DamageCalculator.battleSummary(us, them, options));
    }
    
    public Battleable getBattleable() {
    	return opponent;
    }

    // does not actually do the battle, just prints short summary
    /*
    public void printShortBattle(Pokemon us, Pokemon them) {
        Main.appendln(DamageCalculator.shortBattleSummary(us, them, options));
    }
    */
}
