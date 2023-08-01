package tool;
import java.util.ArrayList;
import java.util.List;

import tool.exception.ToolInternalException;
import tool.exception.route.RouteParserException;

//represents a battle, with planned statmods
public class Battle extends GameAction {
    private Battleable opponent;
    private BattleOptions options;

    /*
    public Battle(Battleable b) throws ToolInternalException {
        opponent = b;
        options = new BattleOptions();
    }
    */

    public Battle(Battleable b, BattleOptions options) throws RouteParserException, ToolInternalException {
        this.opponent = b;
        this.options = options;
        options.compileAndValidate(b);
    }
    
    public BattleOptions getOptions() {
        return options;
    }

    public StatModifier getStatModifier(Side side) {
    	return options.getStatModifier(side);
    }


    public VerboseLevel getVerbose() {
        return options.getVerbose();
    }


    @Override
    public void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
        doBattle(p);

        if (!(opponent instanceof Trainer))
        	return; 
        
        Trainer t = (Trainer) opponent;
        
        /* TODO before or after ?
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
        	options.prepareStatModifiers(p, (Pokemon)opponent, false);
        	
        	if(getVerbose() != VerboseLevel.NONE)
        		printBattle(p, (Pokemon) opponent);

            opponent.battle(p, options);
            if (p.getLevel() > lastLvl) {
                lastLvl = p.getLevel();
                if (options.isPrintStatRangesOnLvl()) {
                    //Main.appendln(p.statRanges(false));
                	Main.appendln(p.statRanges());
                }
                if (options.isPrintStatsOnLvl()) {
                    //Main.appendln(p.statRanges(true));
                	Main.appendln(p.getDetailledStatsStr());
                }
            }
            if(getVerbose() != VerboseLevel.NONE)
                Main.appendln(p.levelAndExperienceNeededToLevelUpStr());
           
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
            
            //if(options.getPartner(Side.ENEMY) != null)
            //	System.out.println("Battle.doBattle");
            
            // Reordering enemy trainer pokes
            ArrayList<ArrayList<Pokemon>> trainerPokesByBatch = t.reorderPokesWithPartner(yPartner, order);
            
            // TODO : handle player partner somehow ?
                        
            if(getVerbose() != VerboseLevel.NONE) {
                Main.appendln(Constants.endl + t.toString(p, options));
            }

            for(ArrayList<Pokemon> batchPokes : trainerPokesByBatch) {            	
	            for (Pokemon opps : batchPokes) {
	            	// Postpone EXP if we're not at the last Pokémon of the batch
	            	//boolean isPostponedExperience = opps != batchPokes.get(batchPokes.size() - 1);
	            	
	            	options.prepareStatModifiers(p, opps, false);

                    if (getVerbose() != VerboseLevel.NONE)                 	
                    	printBattle(p, opps);

	            	options.resetSingleTimeAbility(Side.ENEMY); // Each enemy can trigger a one-time ability increase/drop, not the player | TODO: pretty bad, but works
	            } // end batch 
	            
	            options.backtrackCurrentOpponentIndex(batchPokes.size());
	            
	            // Update EVs and EXP only at the end of a batch
	            for (Pokemon opps : batchPokes) {
	            	options.prepareStatModifiers(p, opps, true);
	            	options.disablePostponedExperience(); // always do it after preparing StatMods, due to inner index update

                    opps.battle(p, options);
                                        
                    // test if you leveled up on this pokemon
                    if (p.getLevel() > lastLvl) {
                        lastLvl = p.getLevel();
                        if (options.isPrintStatRangesOnLvl()) {
                            //Main.appendln(p.statRanges(false));
                        	Main.appendln(p.statRanges());
                        }
                        if (options.isPrintStatsOnLvl()) {
                            //Main.appendln(p.getDetailledStatsStr(false));
                        	Main.appendln(p.getDetailledStatsStr());
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
    

    public void printBattle(Pokemon us, Pokemon them) throws UnsupportedOperationException, ToolInternalException {
        Main.appendln(DamageCalculator.battleSummary(us, them, options));
    }
    
    public Battleable getBattleable() {
    	return opponent;
    }
}
