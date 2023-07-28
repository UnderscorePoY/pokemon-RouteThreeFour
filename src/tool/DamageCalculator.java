package tool;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;

/* This code tries to merge Gen 3 and Gen 4 logic. Documentation might be missing or entangled. */

/* Updates for new move/damage logics :
 * **************************************
 * 1. to add a special damage roll generation :
 *    ==>> DamageCalculator.damage()
 *    
 * 2. to generate special rolls (PSYWAVE, ...) :
 *    ==>> Damages.calculate()
 *    
 * 3. to handle moves with multiple multipliers (RAGE, ROLLOUT, ...) or base powers (MAGNITUDE, ...) :
 *    ==>> DamageCalculator.damagesSummaryCore()
 *    
 * 4. to handle text formatting :
 *    ==>> DamageCalculator.damage()
 */
public class DamageCalculator {
    private static int MIN_ROLL = 85;
    private static int MAX_ROLL = 100;
    // private static int NUM_ROLLS = MAX_ROLL - MIN_ROLL + 1;
    
    private static int MIN_PSYWAVE_ROLL = 50;
    private static int MAX_PSYWAVE_ROLL = 150;
    //private static int PSYWAVE_DIV = 100;
    private static int PSYWAVE_ROLL_STEP = 10;

    /**
     * A wrapper class for handling damages.
     */
	public static class Damages {
		private TreeMap<Integer, Long> normalDamageRolls;
		private TreeMap<Integer, Long> critDamageRolls;
		private boolean hasCrit = true;
		private int numRolls = 0;
		
		private Move attackMove;
		private Pokemon attacker;
		private Pokemon defender;
		private StatModifier atkMod;
		private StatModifier defMod;
		private int extra_multiplier;
		private boolean isBattleTower;
		private boolean isDoubleBattle;
		
		private long normalHitsGreaterOrEqualThanEnemyHP = 0;
		private long critHitsGreaterOrEqualThanEnemyHP = 0;
		
		
		public Damages(Move attackMove, Pokemon attacker, Pokemon defender,
                StatModifier atkMod, StatModifier defMod, int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) throws UnsupportedOperationException, ToolInternalException {
			normalDamageRolls = new TreeMap<>();
			critDamageRolls = new TreeMap<>();
			
			this.attackMove = attackMove;
			this.attacker = attacker;
			this.defender = defender;
			this.atkMod = atkMod;
			this.defMod = defMod;
			this.extra_multiplier = extra_multiplier;
			this.isBattleTower = isBattleTower;
			this.isDoubleBattle = isDoubleBattle;
			
			
			//if(attackMove.matchesAny("TACKLE") && defender.getSpecies() == Species.getSpeciesByName("GEODUDE"))
			//	System.out.println("Damages : in");
			
			
			this.calculate();
			for(long mult : normalDamageRolls.tailMap(defender.getStatValue(Stat.HP)).values())
				normalHitsGreaterOrEqualThanEnemyHP += mult;
			for(long mult : critDamageRolls.tailMap(defender.getStatValue(Stat.HP)).values())
				critHitsGreaterOrEqualThanEnemyHP += mult;
		}
		
		private void calculate() throws UnsupportedOperationException, ToolInternalException {
			Move modifiedMove = null; // used to store a copy of the move, which is modified by the damage functions as side-effects
			
			if(Settings.game.isGen3()) {
				switch(attackMove.getEffect()) {
				case PSYWAVE:
				case RANDOM_DAMAGE:
					this.setCrit(false);
					for (int roll = MIN_PSYWAVE_ROLL; roll <= MAX_PSYWAVE_ROLL; roll += PSYWAVE_ROLL_STEP) {
						modifiedMove = new Move(attackMove);
						int dmg = damageGen3(modifiedMove, attacker, defender, atkMod, defMod, roll, false, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addNormalDamage(dmg);
					}
					break;
					
				case FUTURE_SIGHT:
				case HIT_LATER:
					this.setCrit(false);
					modifiedMove = new Move(attackMove);
					int dmg = calculateBaseDamageGen3(modifiedMove, attacker, defender, atkMod, defMod, false, extra_multiplier, isBattleTower, isDoubleBattle);
					this.addNormalDamage(dmg);
					break;
					
				case LEVEL_DAMAGE:
				case DRAGON_RAGE:
				case SONICBOOM:
					this.setCrit(false);
					modifiedMove = new Move(attackMove);
					dmg = calculateBaseDamageGen3(modifiedMove, attacker, defender, atkMod, defMod, false, extra_multiplier, isBattleTower, isDoubleBattle);
					this.addNormalDamage(dmg);
					break;
					
				default:
					for (int roll = MIN_ROLL; roll <= MAX_ROLL; roll++) {
						modifiedMove = new Move(attackMove);
						dmg = damageGen3(modifiedMove, attacker, defender, atkMod, defMod, roll, false, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addNormalDamage(dmg);

						modifiedMove = new Move(attackMove);
						int critDmg = damageGen3(modifiedMove, attacker, defender, atkMod, defMod, roll, true, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addCritDamage(critDmg);
					}
					break;
				}
			} else { //Gen 4
				switch(attackMove.getEffect()) {
				case RANDOM_DAMAGE: // Psywave
					this.setCrit(false);
					for (int roll = MIN_PSYWAVE_ROLL; roll <= MAX_PSYWAVE_ROLL; roll += PSYWAVE_ROLL_STEP) {
						modifiedMove = new Move(attackMove);
						int dmg = damageGen4(modifiedMove, attacker, defender, atkMod, defMod, roll, false, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addNormalDamage(dmg);
					}
					attackMove.setName(modifiedMove.getName()); // TODO: atrocious
					break;

				case HIT_LATER: // Future Sight, Doom Desire
					this.setCrit(false);
					modifiedMove = new Move(attackMove);
					int dmg = damageGen4(modifiedMove, attacker, defender, atkMod, defMod, MAX_ROLL, false, extra_multiplier, isBattleTower, isDoubleBattle);
					this.addNormalDamage(dmg);
					break;
					
				case LEVEL_DAMAGE:
				case FIXED_20: // Sonicboom
				case FIXED_40: // Dragon Rage
					this.setCrit(false);
					modifiedMove = new Move(attackMove);
					dmg = damageGen4(modifiedMove, attacker, defender, atkMod, defMod, MAX_ROLL, false, extra_multiplier, isBattleTower, isDoubleBattle);
					this.addNormalDamage(dmg);
					break;
					
				default:
					for (int roll = MIN_ROLL; roll <= MAX_ROLL; roll++) {
						modifiedMove = new Move(attackMove);
						dmg = damageGen4(modifiedMove, attacker, defender, atkMod, defMod, roll, false, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addNormalDamage(dmg);

						modifiedMove = new Move(attackMove);
						int critDmg = damageGen4(modifiedMove, attacker, defender, atkMod, defMod, roll, true, extra_multiplier, isBattleTower, isDoubleBattle);
						this.addCritDamage(critDmg);
					}
					break;
				} // end witch effect
			} // end gen split
			attackMove.setName(modifiedMove.getName()); // Retrieves the modified move name only once | TODO: atrocious, innit ?
		}
		
		
		/* ************************ */
		/* INSTANCE-RELATED METHODS */
		/* ************************ */
		
		private int lowestNormalDamage() {
			return normalDamageRolls.firstKey();
		}
		
		private int highestNormalDamage() {
			return normalDamageRolls.lastKey();
		}
		
		private int lowestCritDamage() {
			return critDamageRolls.firstKey();
		}
		
		private int highestCritDamage() {
			return critDamageRolls.lastKey();
		}
		
		private int lowestDamage() {
			return lowestNormalDamage();
		}
		
		private int highestDamage() {
			if(hasCrit)
				return critDamageRolls.lastKey();
			return normalDamageRolls.lastKey();
		}
		
		private void increment(TreeMap<Integer, Long> map, int dmg) {
			if (!map.containsKey(dmg))
				map.put(dmg, (long) 1);
			else
				map.put(dmg, 1 + map.get(dmg));
			
			if(map == normalDamageRolls)
				numRolls++;
		}
		
		public void addNormalDamage(int dmg) {
			increment(normalDamageRolls, dmg);
		}
		
		public void addCritDamage(int dmg) {
			increment(critDamageRolls, dmg);
		}
		
		public boolean hasCrit() {
			return hasCrit;
		}
		
		public void setCrit(boolean hasCrit) {
			this.hasCrit = hasCrit;
		}
		
		public int getNumRolls() {
			return numRolls;
		}
		
		public boolean hasDamage() {
			return highestDamage() != 0;
		}
		
	    private long oneShotNumerator(boolean crit) {
			if(crit)
				return critHitsGreaterOrEqualThanEnemyHP;
			else
				return normalHitsGreaterOrEqualThanEnemyHP;
		}
	    
	    public void capDamagesWithHP(int hp) {
	    	normalDamageRolls = capMapWithHP(normalDamageRolls, hp);
	    	if(hasCrit)
	    		critDamageRolls = capMapWithHP(critDamageRolls, hp);
	    }
	    
	    
	    
	    
		
		/* ************************ */
		/* PRINTING-RELATED METHODS */
		/* ************************ */
		
		private void appendDamages(StringBuilder sb, TreeMap<Integer, Long> map, boolean withFinalNewLine) {
			String endl = Constants.endl;
			
			int enemyHP = defender.getStatValue(Stat.HP);
			int lastHPtoPrint = Math.min(map.lastKey(), enemyHP);
			
			TreeMap<Integer, Long> cappedMap = capMapWithHP(map, enemyHP);
			for (Map.Entry<Integer, Long> entry : cappedMap.entrySet()) {
				int dmg = entry.getKey();
				long mult = entry.getValue();
				sb.append(String.format("%dx%d", dmg, mult));
				if(dmg < lastHPtoPrint)
					sb.append(", ");
			}
			
			/*
			// All but last damage strictly lower than enemyHP displayed one by one
			SortedMap<Integer, Long> headMap = map.headMap(lastHPtoPrint, false); // Map corresponding to [minDmg, lastHPtoPrint[
			for (Map.Entry<Integer, Long> headEntry : headMap.entrySet()) {
				int dmg = headEntry.getKey();
				long mult = headEntry.getValue();
				sb.append(String.format("%dx%d, ", dmg, mult));
			}
			
			// Last or all damage higher or equal than enemyHP displayed only once
			SortedMap<Integer, Long> tailMap = map.tailMap(lastHPtoPrint); // Map corresponding to [lastHPtoPrint, maxDmg]
			long totalMult = 0;
			for (Map.Entry<Integer, Long> tailEntry : tailMap.entrySet()) {
				long mult = tailEntry.getValue();
				totalMult += mult;
			}
			sb.append(String.format("%dx%d", lastHPtoPrint, totalMult));
			*/
			
			if(withFinalNewLine)
				sb.append(endl);
		}
		
		private void appendDamages(StringBuilder sb, TreeMap<Integer, Long> map) {
			appendDamages(sb, map, true); 
		}
		
		public void appendNormalDamages(StringBuilder sb) {
			appendDamages(sb, normalDamageRolls);

		}
		
		public void appendCritDamages(StringBuilder sb) {
			appendDamages(sb, critDamageRolls);
		}
		
		public void appendRecoilDamagesIfApplicable(StringBuilder sb, TreeMap<Integer, Long> damageRolls) {
			boolean isRecoilMove = attackMove.getEffect().isRecoil();
			boolean recoilApplies = !attacker.getAbility().avoidsRecoil();
			
			if(isRecoilMove && recoilApplies) {
				int recoilDivider = attackMove.getEffect().getRecoilDivider();
				sb.append("\t(recoil: ");
				if(attackMove.matchesAny("STRUGGLE") && !Settings.game.isGen3())
					sb.append(String.format("%d", attacker.getStatValue(Stat.HP)/recoilDivider));
				else {
					TreeMap<Integer, Long> recoilMap = capMapWithHP(damageRolls, defender.getStatValue(Stat.HP));
					recoilMap = divideAllDamageRollsBy(recoilMap, recoilDivider);
					appendDamages(sb, recoilMap, false);
					
				}
				sb.append(")");
				sb.append(Constants.endl);
			}
		}		
		
		public void appendPostKODamageIfApplicable(StringBuilder sb) {
			if(defender.getAbility() == Ability.AFTERMATH && attackMove.makesContact() && attacker.getAbility() != Ability.DAMP) {
				sb.append(String.format("\t(aftermath: %d)", attacker.getStatValue(Stat.HP) / 4)); // TODO: hardcoded constant
				sb.append(Constants.endl);
			} 
			
			else if(attackMove.makesContact() && defender.getAbility() == Ability.ROUGH_SKIN) {
				int div = Settings.game.isGen3() ? 16 : 8; // TODO : hardcoded constants
				sb.append(String.format("\t(%s: %s)%s", Ability.ROUGH_SKIN, attacker.getStatValue(Stat.HP)/div, Constants.endl));
			}
		}
		
		/**
		 * Returns a new map where all damage (keys) that are higher or equal to max hp are gathered (rolls added).
		 */
		private TreeMap<Integer, Long> capMapWithHP(TreeMap<Integer, Long> damageRolls, int maxHP) {
			TreeMap<Integer, Long> newMap = new TreeMap<>();
			for(Map.Entry<Integer, Long> entry : damageRolls.entrySet()) {
				int damage = entry.getKey();
				damage = (damage > maxHP) ? maxHP: damage;
				Long roll = entry.getValue();
				if (!newMap.containsKey(damage))
					newMap.put(damage, roll);
				else
					newMap.put(damage, roll + newMap.get(damage));
			}
			return newMap;
		}
		
		/**
		 * Returns a new map where all damage (keys) are divided by the divider, and gathered (rolls added) according to these new damage values.
		 */
		private TreeMap<Integer, Long> divideAllDamageRollsBy(TreeMap<Integer, Long> damageRolls,
				Integer recoilDivider) {
			TreeMap<Integer, Long> newMap = new TreeMap<>();
			for(Map.Entry<Integer, Long> entry : damageRolls.entrySet()) {
				int damage = entry.getKey() / recoilDivider;
				Long roll = entry.getValue();
				if (!newMap.containsKey(damage))
					newMap.put(damage, roll);
				else
					newMap.put(damage, roll + newMap.get(damage));
			}
			return newMap;
		}
		
		public void appendShortDamages(StringBuilder sb) {
			String endl = Constants.endl;
			
			int minDmg = lowestNormalDamage();
			int maxDmg = highestNormalDamage();
			
	        double minPct = toDmgPercent(minDmg);
	        double maxPct = toDmgPercent(maxDmg);
	        
	        if(minDmg == maxDmg)
	        	sb.append(String.format("%d %.02f%%", minDmg, minPct));
	        else
	        	sb.append(String.format("%d-%d %.02f-%.02f%%", minDmg, maxDmg, minPct, maxPct));
	        
	        if(hasCrit() && minPct < 100.) { // Only display crit if normal rolls are not guaranteed
		        sb.append("\t(crit: ");
		        
		        int critMinDmg = lowestCritDamage();
				int critMaxDmg = highestCritDamage();
	
		        double critMinPct = toDmgPercent(critMinDmg);
		        double critMaxPct = toDmgPercent(critMaxDmg);
		        
		        if(critMinDmg == critMaxDmg)
		        	sb.append(String.format("%d %.02f%%)", critMinDmg, critMinPct));
		        else
		        	sb.append(String.format("%d-%d %.02f-%.02f%%)", critMinDmg, critMaxDmg, critMinPct, critMaxPct));
	        }
	        
	        sb.append(endl);
		}
		
		public void appendOverallChanceKO(StringBuilder sb) {
			String endl = Constants.endl;
			
			int oppHP = defender.getStatValue(Stat.HP);
			int realminDmg = lowestDamage();
	        int realmaxDmg = highestDamage();

	        int critStage = 0;
	        if(attackMove.isIncreasedCritRatio()) // TODO: Defender Ability blocking crits ? 
	        	critStage++;
	        if(attacker.getHeldItem() != null)
	        	critStage += attacker.getHeldItem().getHoldEffect().getCritStage(attacker.getSpecies());  // TODO: Defender Ability blocking item ? 
	        // TODO : handle Focus Energy/Dire Hit (+2 stage each)
	        // TODO : multi-hits
	        // TODO : Rage, Rollout etc.
	        
            double critChance = 1. / (critStage == 0 ? 16 : critStage == 1 ? 8 : critStage == 2 ? 4 : critStage == 3 ? 3 : 2); //TODO : hardcoded

            for (int hits = 1; hits <= 8; hits++) {
                if (realminDmg * hits < oppHP && realmaxDmg * hits >= oppHP) {
                    double totalKOPct = 0;
                    for (int crits = 0; crits <= hits; crits++) {
                        double nShotPct = nShotPercentage(hits - crits, crits, normalDamageRolls, critDamageRolls);
                        totalKOPct += nShotPct * choose(hits, crits) * Math.pow(critChance, crits)
                                * Math.pow(1 - critChance, hits - crits);
                    }
                    if (totalKOPct >= 0.1 && totalKOPct <= 99.999) {
                        sb.append(String.format("\t(Overall %d-hit KO%%: %.04f%%)", hits, totalKOPct));
                        sb.append(endl);
                    }
                }
            }
		}
		
		
		public void appendOneShotProbIfNotGuaranteed(StringBuilder sb) {
			String endl = Constants.endl;
			int oppHP = defender.getStatValue(Stat.HP);

			int minDmg = lowestNormalDamage();
			int maxDmg = highestNormalDamage();
			
            if (maxDmg >= oppHP && minDmg < oppHP && maxDmg != minDmg) {
                long oneShotNum = oneShotNumerator(false); // TODO : Rage, Rollout etc.
                sb.append(String.format("\t(One shot prob.: %d/%d | %.02f%%)", oneShotNum, numRolls, toPercent(oneShotNum, numRolls)));
                sb.append(endl);
            }
		}
		
		public void appendNShots(StringBuilder sb) {
			String endl = Constants.endl;
			
			int oppHP = defender.getStatValue(Stat.HP);

			int minDmg = lowestNormalDamage();
			int maxDmg = highestNormalDamage();

            // test if crits can KO in 1shot
            if(hasCrit()) {
    			int critMinDmg = lowestCritDamage();
    			int critMaxDmg = highestCritDamage();
	            if (critMaxDmg >= oppHP && critMinDmg < oppHP) {
	                long oneShotNum = oneShotNumerator(true); // TODO : Rage, Rollout etc.
	                sb.append(String.format("\t(Crit one shot prob.: %d/%d | %.02f%%)", oneShotNum, numRolls, toPercent(oneShotNum,numRolls)));
	                sb.append(endl);
	            }
            }

            // n-shot
            int minDmgWork = minDmg;
            int maxDmgWork = maxDmg;
            int hits = 1;
            while (minDmgWork < oppHP && hits < 5) {
                hits++;
                minDmgWork += minDmg;
                maxDmgWork += maxDmg;
                if (maxDmgWork >= oppHP && minDmgWork < oppHP) {
                    //System.out.println("working out a " + hits + "-shot");
                    double nShotPct = nShotPercentage(hits, 0, normalDamageRolls, critDamageRolls); /// TODO : Rage, Rollout etc.
                    sb.append(String.format("\t(%d shot prob.: %.04f%%)", hits, nShotPct));
                    sb.append(endl);
                }
            }

            // n-crit-shot
            if(hasCrit()) {
    			int critMinDmg = lowestCritDamage();
    			int critMaxDmg = highestCritDamage();
	            minDmgWork = critMinDmg;
	            maxDmgWork = critMaxDmg;
	            hits = 1;
	            while (minDmgWork < oppHP && hits < 5) {
	                hits++;
	                minDmgWork += critMinDmg;
	                maxDmgWork += critMaxDmg;
	                if (maxDmgWork >= oppHP && minDmgWork < oppHP) {
	                    //System.out.println("working out a " + hits + "-crit-shot");
	                    double nShotPct = nShotPercentage(0, hits, normalDamageRolls, critDamageRolls); /// TODO : Rage, Rollout etc.
	                    sb.append(String.format("\t(%d crits death prob.: %.04f%%)", hits, nShotPct));
	                    sb.append(endl);
	                }
	            }
            

				int realminDmg = lowestNormalDamage();
		        //int realmaxDmg = highestCritDamage();
	            // mixed a-noncrit and b-crit shot
	            for (int non = 1; non <= 5 && realminDmg * (non + 1) < oppHP; non++) {
	                for (int crit = 1; non + crit <= 5 && realminDmg * (non + crit) < oppHP; crit++) {
	                    int sumMin = critMinDmg * crit + minDmg * non;
	                    int sumMax = critMaxDmg * crit + maxDmg * non;
	                    if (sumMin < oppHP && sumMax >= oppHP) {
	                        //System.out.printf("working out %d non-crits + %d crits\n", non, crit);
	                        double nShotPct = nShotPercentage(non, crit, normalDamageRolls, critDamageRolls); /// TODO : Rage, Rollout etc.
	                        sb.append(String.format("\t(%d non-crit%s + %d crit%s death prob.: %.04f%%)", non,
	                                non > 1 ? "s" : "", crit, crit > 1 ? "s" : "", nShotPct));
	                        sb.append(endl);
	                    }
	                }
	            }
            }
		}
		
		public void appendGuaranteed(StringBuilder sb) {
			String endl = Constants.endl;
			int oppHP = defender.getStatValue(Stat.HP);
			int realminDmg = lowestDamage();
            int guarantee = (int) Math.ceil(((double) oppHP) / realminDmg);
            
            sb.append(String.format("\t(guaranteed %d-shot)", guarantee));
            sb.append(endl);
		}
		
		 private void appendDetailledPercentMap(StringBuilder sb, TreeMap<Integer, Double> map) {
		    	final int ROLLS_PER_LINE = 8;
		    	
		        String endl = Constants.endl;
		    	
		    	Pokemon p2 = this.defender;
		    	int minDmg = map.firstKey();
		    	
		    	int cnt = -1;
	            for(int dmg : map.keySet()) {
	            	cnt++;
	                if(cnt % ROLLS_PER_LINE == 0) {
	                    sb.append(endl);
	                    if(dmg == p2.getStatValue(Stat.HP) && minDmg != p2.getStatValue(Stat.HP)) {
	                        sb.append(endl);
	                    }
	                    sb.append("            ");
	                }
	                else if(dmg == p2.getStatValue(Stat.HP) && minDmg != p2.getStatValue(Stat.HP)) {
	                    sb.append(endl);
	                    sb.append(endl);
	                    sb.append("            ");
	                }
	                sb.append(String.format("%3d: %6.02f%%     ", dmg, map.get(dmg)));
	            }
	            sb.append(endl);
	            sb.append(endl);
		    }
		    
		    public void appendDetailledPercentDamages(StringBuilder sb) {
		    	Move move = this.attackMove;
		    	//int _extra_modifier = this.extra_multiplier;
		    	//Pokemon p1 = attacker;
		    	Pokemon p2 = defender;
		    	//StatModifier mod1 = atkMod;
		    	//StatModifier mod2 = defMod;
		    	//Object param = null;

		        String endl = Constants.endl;
		        
		        if(hasDamage()) {
		            TreeMap<Integer,Double> dmgMap = percentMapWithMaxHP(this.normalDamageRolls, p2.getStatValue(Stat.HP));
		            
		            //appendFormattedMoveName(sb, move, p1, p2, mod1, mod2, _extra_modifier, isBattleTower, isDoubleBattle, param);
		            appendFormattedMoveName(sb, move);
		            sb.append(endl);
		            
		            sb.append("          NON-CRITS");
		            appendDetailledPercentMap(sb, dmgMap);
		            	            
		            if(hasCrit()) {
			            TreeMap<Integer,Double> critMap = percentMapWithMaxHP(this.critDamageRolls, p2.getStatValue(Stat.HP));
			            sb.append("          CRITS");
			            appendDetailledPercentMap(sb, critMap);
		            }
		        }
		    }
		    
		    public void appendAllMoveInfo(StringBuilder sb) throws UnsupportedOperationException, ToolInternalException {
				String endl = Constants.endl;
				Move m = this.attackMove;
				//int _extra_multiplier = this.extra_multiplier;
				Pokemon p1 = attacker;
				Pokemon p2 = defender;
				//StatModifier mod1 = atkMod;
				//StatModifier mod2 = defMod;
				//Object param = null;

				//appendFormattedMoveName(sb, m, p1, p2, mod1, mod2, _extra_multiplier, isBattleTower, isDoubleBattle, param);
				appendFormattedMoveName(sb, m);
				
				if (hasDamage()) {
					sb.append("\t");
					appendShortDamages(sb);
					
					// normal rolls
					sb.append("\tNormal rolls: ");
					appendNormalDamages(sb);
					
					appendOneShotProbIfNotGuaranteed(sb);
					appendRecoilDamagesIfApplicable(sb, normalDamageRolls);
					//appendPostKODamageIfApplicable(sb);
					printDamageWithIVvariationIfApplicable(sb, m, p1, p2, atkMod, defMod, 1, isBattleTower, isDoubleBattle, false);
					
					// crit rolls
					if(hasCrit() && lowestDamage() < p2.getStatValue(Stat.HP)) {
						sb.append("\tCrit rolls: ");
						appendCritDamages(sb);
						appendNShots(sb);
						appendRecoilDamagesIfApplicable(sb, critDamageRolls);
						//appendPostKODamageIfApplicable(sb);
						printDamageWithIVvariationIfApplicable(sb, m, p1, p2, atkMod, defMod, 1, isBattleTower, isDoubleBattle, true);
					}
					appendPostKODamageIfApplicable(sb);
					
					
					// guaranteed n-shot
					if (Settings.showGuarantees)
					appendGuaranteed(sb);
					
					// overall chance of KO
					if (Settings.overallChanceKO)
					appendOverallChanceKO(sb);
				}
				
				sb.append(endl);
			}
		    
		    public void appendBasicMoveInfo(StringBuilder sb) throws UnsupportedOperationException, ToolInternalException {
				String endl = Constants.endl;
				Move m = this.attackMove;
				//int _extra_multiplier = this.extra_multiplier;
				Pokemon p1 = attacker;
				Pokemon p2 = defender;
				//StatModifier mod1 = atkMod;
				//StatModifier mod2 = defMod;
				//Object param = null;

				//appendFormattedMoveName(sb, m, p1, p2, mod1, mod2, _extra_multiplier, isBattleTower, isDoubleBattle, param);
				appendFormattedMoveName(sb, m);
				
				if (hasDamage()) {
					sb.append("\t");
					appendShortDamages(sb);
					
					// normal rolls
					sb.append("\tNormal rolls: ");
					appendNormalDamages(sb);
					
					appendOneShotProbIfNotGuaranteed(sb);
					appendRecoilDamagesIfApplicable(sb, normalDamageRolls);
					//appendPostKODamageIfApplicable(sb);
					printDamageWithIVvariationIfApplicable(sb, m, p1, p2, atkMod, defMod, 1, isBattleTower, isDoubleBattle, false);
					
					// crit rolls
					if(hasCrit() && lowestDamage() < p2.getStatValue(Stat.HP)) { // Only display when normal rolls aren't guaranteed
						sb.append("\tCrit rolls: ");
						appendCritDamages(sb);
					}
					appendPostKODamageIfApplicable(sb);
					
					/*
					appendNShots(sb);
					
					// guaranteed n-shot
					if (Settings.showGuarantees)
					appendGuaranteed(sb);
					
					// overall chance of KO
					if (Settings.overallChanceKO)
					appendOverallChanceKO(sb);
					*/
				}
				
				sb.append(endl);
			}
		    
		    /**
		     * Only compares equality of extremal crit and non-crit damages.
		     */
		    @Override
		    public boolean equals(Object o) {
		    	if(this == o)
		    		return true;
		    	if(o == null)
		    		return false;
		    	if(!(o instanceof Damages))
		    		return false;
		    	
		    	Damages d = (Damages)o;
		    	return this.highestCritDamage() == d.highestCritDamage()     && this.critDamageRolls.get(this.highestCritDamage()) == d.critDamageRolls.get(d.highestCritDamage())
		    		&& this.lowestCritDamage() == d.lowestCritDamage()       && this.critDamageRolls.get(this.lowestCritDamage()) == d.critDamageRolls.get(d.lowestCritDamage())
		    		&& this.highestNormalDamage() == d.highestNormalDamage() && this.normalDamageRolls.get(this.highestNormalDamage()) == d.normalDamageRolls.get(d.highestNormalDamage())
		    		&& this.lowestNormalDamage() == d.lowestNormalDamage()   && this.normalDamageRolls.get(this.lowestNormalDamage()) == d.normalDamageRolls.get(d.lowestNormalDamage());
		    }

		
		    
		/* ********************* */
		/* MATHS UTILITY METHODS */
		/* ********************* */
		
	    /**
	     * Converts a damage value in percentage of the defender max HP.
	     */
		private double toDmgPercent(int dmg) {
			return toPercent(dmg, defender.getStatValue(Stat.HP));
		}
		
		/**
		 * Returns the percentage value of num divided by div.
		 */
		private static double toPercent(long num, long div) {
			return 100. * num / div;
		}
		
	    private static long choose(long total, long choose) {
	        if (total < choose)
	            return 0;
	        if (choose == 0 || choose == total)
	            return 1;
	        if (choose == 1 || choose == total - 1)
	            return total;
	        return choose(total - 1, choose - 1) + choose(total - 1, choose);
	    }

	    
		/* ******************* */
		/* MAP UTILITY METHODS */
		/* ******************* */
	    
	    /**
	     * Creates a new damage map where keys are the sum of one damage from each map, and values are the associated roll numbers.
	     * Example : {2: 15, 3: 1} x {3: 15, 4: 1} returns {5: 225, 6: 30, 7: 1}, where the damage '6' comes from both sums '2+4' and '3+3', each contributing to '15' rolls.
	     */
	    private TreeMap<Integer, Long> multiplyMaps(TreeMap<Integer, Long> map1, TreeMap<Integer, Long> map2){
	    	TreeMap<Integer, Long> results = new TreeMap<>();
	    	for (Map.Entry<Integer, Long> map1Entry : map1.entrySet()) {
	    		int dmg1 = map1Entry.getKey();
	    		long mult1 = map1Entry.getValue();
	    		for (Map.Entry<Integer, Long> map2Entry : map2.entrySet()) {
	    			int dmg2 = map2Entry.getKey();
		    		long mult2 = map2Entry.getValue();
		    		
		    		int dmg = dmg1 + dmg2;
		    		long mult = mult1 * mult2;
		    		long oldMult = results.containsKey(dmg) ? results.get(dmg) : 0;
		    		
	    			results.put(dmg, mult + oldMult);	
	    		}
	    	}
	    	
	    	return results;
	    }
	    
	    /**
	     * Calculates the repeated {@link #multiplyMaps(TreeMap, TreeMap) multiplication} of a damage map with itself.
	     * Zeroth power returns a single map {0: 1}, first power returns the map itself, and the nth power returns map x ... x map where map appears n times. 
	     */
	    private TreeMap<Integer, Long> powerMap(TreeMap<Integer, Long> map1, int nb){
	    	TreeMap<Integer, Long> results;
	    	
	    	if (nb <= 0) {
	    		results = new TreeMap<>();
	    		results.put(0, (long) 1);
	    		return results;
	    	}
	    	
	    	results = new TreeMap<>(map1);
	    	if (nb == 1) {
	    		results = new TreeMap<>(map1);
	    		return map1;
	    	}
	    	for (int i = 1; i < nb; i++) {
	    		results = multiplyMaps(results, map1);
	    	}
	    	
	    	return results;
	    }
	    
	    private static long totalHits(SortedMap<Integer, Long> map) {
	    	long total = 0;
	    	
	    	for(Map.Entry<Integer, Long> entry : map.entrySet())
	    		total += entry.getValue();
	    	
	    	return total;
	    }
	    
	    private double nShotPercentage(int normalHits, int critHits, TreeMap<Integer, Long> noCritMap, TreeMap<Integer, Long> critMap) {
	    	TreeMap<Integer, Long> multiHitsDmgMap = multiHitsDmgMap(normalHits, critHits, noCritMap, critMap);
	    	
	    	SortedMap<Integer, Long> koMap = multiHitsDmgMap.tailMap(defender.getStatValue(Stat.HP));
	    	long numKORolls = totalHits(koMap);
	    	long totalRolls = totalHits(multiHitsDmgMap);
	    	
	    	return toPercent(numKORolls, totalRolls);
	    }
	    
	    private TreeMap<Integer, Long> multiHitsDmgMap(int normalHits, int hitsLeftCrit, TreeMap<Integer, Long> noCritMap, TreeMap<Integer, Long> critMap) {
	    	
	    	TreeMap<Integer, Long> noCritPower = powerMap(noCritMap, normalHits);
	    	TreeMap<Integer, Long> critPower = powerMap(critMap, hitsLeftCrit);
	    	TreeMap<Integer, Long> results = multiplyMaps(noCritPower, critPower);

	    	return results;
	    }
	    
	    public static TreeMap<Integer,Double> percentMapWithMaxHP(TreeMap<Integer, Long> damages, int maxHP) {
			TreeMap<Integer,Double> percentMap = new TreeMap<Integer,Double>();
	    	long totalHits = totalHits(damages);
	    	int lastHPtoPrint = Math.min(damages.lastKey(), maxHP);
	    		    	
	    	// All but last damage strictly lower than enemyHP displayed one by one
			SortedMap<Integer, Long> headMap = damages.headMap(lastHPtoPrint, false); // Map corresponding to [minDmg, lastHPtoPrint[
			for (Map.Entry<Integer, Long> headEntry : headMap.entrySet()) {
				int dmg = headEntry.getKey();
				long rollNb = headEntry.getValue();
				double percent = ((double)rollNb/totalHits) * 100;
	    		percentMap.put(dmg, percent);
			}
			
			// Last or all damage higher or equal than enemyHP displayed only once
			SortedMap<Integer, Long> tailMap = damages.tailMap(lastHPtoPrint); // Map corresponding to [lastHPtoPrint, maxDmg]
			long totalMult = 0;
			for (Map.Entry<Integer, Long> tailEntry : tailMap.entrySet()) {
				long mult = tailEntry.getValue();
				totalMult += mult;
			}
			double percent = ((double)totalMult/totalHits) * 100;
    		percentMap.put(lastHPtoPrint, percent);

			return percentMap;
		}	    
	} // end Damages class
	
	
    // ****************** //
    //        GEN 3       //
	// MAIN DAMAGE METHOD //
    // ****************** //
    
    // Layout : 
    // 1. https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L230-L273
    // Main routines : 
    // 1. Inner damage : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/calculate_base_damage.c#L93
    // 2. Outer damage : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L1475
    // Other routines (maybe useful) :
    // - https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L1410
    // - https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L6430
    // - https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8559
	
	/* TODO : Implement damage effects
	 * @ = implemented
	 * ~ = partially implemented
	 * - = to-do
	 * 
	 * @ PSYWAVE :
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1266
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L7690
	 * 	 > (remark) No damage variation, only base power variation based on level
	 *   
	 * @ FLAIL :
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1409
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8014
	 *   
	 * @ RAGE : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1180
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L4298
	 *          : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8211
	 *   > (remark) For Gen III onwards, Rage increases the ATK stat stage
	 *   > (implementation) Not implemented in a special way
	 *          
	 * @ ROLLOUT : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1673
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8211
	 *   > (implementation) not handling Defense Curl, but pushing the multiplier up to stage 6 instead
	 *   
	 * @ FURY_CUTTER : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1711
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8250
	 *   
	 * @ MAGNITUDE : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1764
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8324
	 *   
	 * ~ PRESENT : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1745
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8281
	 *   > TODO: healing
	 *   
	 * @ PURSUIT : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L3121
	 *            : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L3161

	 * - RETURN
	 * - FRUSTRATION
	 * - TWISTER : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1908
	 * - EARTHQUAKE : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1917
	 * - GUST : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1981
	 * - SPIT_UP : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2196
	 * - FACADE : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2357
	 * - SMELLINGSALT : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2373
	 * - NATURE_POWER : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2394
	 * - REVENGE : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2520
	 * 
	 * @ LOW_KICK : 
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2665
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L9033
	 * 
	 * - STOMP : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1987
	 * 
	 * TODO : Implement multi-hit effects
	 * - TRIPLE_KICK : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1450
	 * 
	 * TODO : Residual damage
	 * - Statuses
	 * - Weathers
	 * - SPIKES
	 * 
	 * @ FUTURE_SIGHT :
	 *   > script : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L1974
	 *            : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L3559
	 *   > code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L8559
	 *   
	 * TODO : Special types
	 * - FORESIGHT
	 * - WEATHER_BALL : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/data/battle_scripts_1.s#L2754
	 * 
	 * TODO : Implement priority markers 
	 * - QUICK_ATTACK
	 * 
	 * TODO : Implement heal moves
	 * - MORNING_SUN
	 * - SYNTHESIS
	 * - MOONLIGHT
	 * - SOFTBOILED
	 * - SWALLOW
	 * - INGRAIN
	 */
	
	/**
	 * Calculate one damage value based on a provided roll, in Gen 3.
	 * @param attackMove the used move.
	 * @param attacker the attacker Pokemon.
	 * @param defender the defender Pokemon.
	 * @param atkMod the attacker's stat modifiers.
	 * @param defMod the defender's stat modifiers.
	 * @param roll the provided roll.
	 * @param isCrit if the move is a critical hit.
	 * @param extra_multiplier the desired extra multiplier
	 * @param isBattleTower if the battle occurs in Battle Tower.
	 * @param isDoubleBattle if the battle is a double battle.
	 * @param param an optional parameter used in specific cases.
	 * @return the calculated damage value.
	 * @throws ToolInternalException 
	 * @throws UnsupportedOperationException 
	 */
    private static int damageGen3(Move modifiedAttackMove, Pokemon attacker, Pokemon defender,
                              StatModifier atkMod, StatModifier defMod, int roll,
                              boolean isCrit, int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) throws UnsupportedOperationException, ToolInternalException {
        //Move modifiedAttackMove = new Move(attackMove); // Copy
        
        boolean ghostRevealed = defMod.hasStatus2_3(Status.FORESIGHT);
        
        /* Psywave :
         * - Code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L7690
         * - Expected rolls : {50, 60, ..., 150} | Only possible damages are level * {50, 60, ..., 150} / 100
         */
        
        /* Handled by damagesSummaryCore
        if (modifiedAttackMove.getEffect() == MoveEffect.PSYWAVE) {
        	Type atkType = modifiedAttackMove.getType();
        	Type defType1 = defender.getSpecies().getType1();
        	Type defType2 = defender.getSpecies().getType2();
        	if(Type.isImmune(atkType, defType1, defType2, ghostRevealed, false))
        		return 0; //TODO : hardcoded
        	
        	return attacker.getLevel() * roll / PSYWAVE_DIV;
        }
        */
        
        
        /* Low Kick : 
         * - Base power table : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L995
         * - Comparison code : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/battle_script_commands.c#L9033
         */
        /* Handled by damagesSummaryCore
        else if (modifiedAttackMove.getEffect() == MoveEffect.LOW_KICK) {
        	int defenderWeight = defender.getSpecies().getWeight();
        	int basePower; //TODO : hardcoded
        	if (defenderWeight < 100)
        		basePower = 20; //TODO : hardcoded
        	else if (defenderWeight < 250)
        		basePower = 40; //TODO : hardcoded
        	else if (defenderWeight < 500)
        		basePower = 60; //TODO : hardcoded
        	else if (defenderWeight < 1000)
        		basePower = 80; //TODO : hardcoded
        	else if (defenderWeight < 2000)
        		basePower = 100; //TODO : hardcoded
        	else 
        		basePower = 120; //TODO : hardcoded
        	
        	modifiedAttackMove.setPower(basePower);
        }
        */
        
        /* Reversal :
         * TODO: Gen 3 doc | Temporary doc : smogon
         */
        /* Handled by damagesSummaryCore
        else if(modifiedAttackMove.getEffect() == MoveEffect.FLAIL) { // Flail & Reversal in Gen 3
			int p = (int)Math.floor((48 * atkMod.getCurrHP()) / attacker.getStatValue(Stat.HP));
		    int basePower = p <= 1 ? 200 : p <= 4 ? 150 : p <= 9 ? 100 : p <= 16 ? 80 : p <= 32 ? 40 : 20;
		    modifiedAttackMove.setPower(basePower);
        }
        */
                        
        /* Plan :
         * 
         * [EffectHit]
         *   Surf on Dive multiplier
         * 
         * [atk05_damagecalc]
         * >> (CalculateBaseDamage)
         *     Forced override  | TODO : what is this ? Probably the "power == 1" cases and some more
         *     Enigma Berry
         *     Huge Power, Pure Power
         *     Badge boosts
         *     Type boosting items
         *     Choice Band
         *     Soul Dew
         *     Deep Sea Tooth
         *     Deep Sea Scale
         *     Light Ball
         *     Metal Power
         *     Thick Club
         *     Thick Fat
         *     Hustle
         *     Plus & Minus
         *     Guts
         *     Marvel Scale
         *     Mud Sport
         *     Water Spout
         *     Overgrow, Blaze, Torrent, Swarm
         *     Explosion (effect)
         * 
         *     Base damage
         *     Reflect, Lightscreen
         *     Double battle damage halving (split damage)
         *     Weather checks
         *     Flash Fire boost
         * 
         *     Add 2
         * >> (End CalculateBaseDamage)
         * 
         *   Crit multiplier 
         *   Damage multiplier (TODO : probably Rage, Fury Cutter, Rollout, etc.)
         *   Charged up
         *   Helping hand
         * [End atk05_damagecalc]
		 *
         * [atk06_typecalc]
         *   STAB
         *   Levitate
         *   Foresight
         *   Type effectiveness
         *   Wonder Guard
         * [End atk06_typecalc]
         * 
         * [atk07_adjustnormaldamage]
         *   Damage rolls
         */
        
        
        // *********** //
        // [EffectHit] //
        // *********** //
        
        // Overwrite multiplier if Surfing an underwater pokemon
        // TODO : maybe put this somewhere else, idk
        if(modifiedAttackMove.matchesAny("SURF") && defMod.hasStatus2_3(Status.UNDERWATER)) {
        	modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), Status.UNDERWATER));
        	extra_multiplier = 2;
        }
        
        // *************** //
        // [End EffectHit] //
        // ############### //
        
        
        // ****************** //
        // [atk05_damagecalc] //
        // ****************** //
        
        int damage = calculateBaseDamageGen3(modifiedAttackMove, attacker, defender,
                              atkMod, defMod,
                              isCrit, extra_multiplier, isBattleTower, isDoubleBattle);
        
        /*
        if(modifiedAttackMove.getEffect() == MoveEffect.FUTURE_SIGHT) // Future Sight stops here
        	return damage;
        */
        
        
        // Critical hit gBattleMoveDamage * gCritMultiplier * gBattleStruct->dmgMultiplier;
        if (isCrit)
        	damage *= 2;
        
        // Damage multiplier (TODO: What is this exactly ? Seems to be sDMG_MULTIPLIER)
        if(modifiedAttackMove.matchesAny("Pursuit") && defMod.hasStatus2_3(Status.SWITCHING_OUT)) {
        	extra_multiplier = 2; // TODO : hardcoded
        	modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), Status.SWITCHING_OUT));
        }
        
        damage *= extra_multiplier;
        
        // Charged up
        if (modifiedAttackMove.getType() == Type.ELECTRIC && atkMod.hasStatus2_3(Status.CHARGED_UP)) {
        	modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.CHARGED_UP));
        	damage *= 2;
        }
        
        // Helping hand
        // TODO
        
        
    	// ********************** //
        // [end atk05_damagecalc] //
        // ###################### //
        
        
        // **************** //
        // [atk06_typecalc] //
        // **************** //
        
        if(!modifiedAttackMove.matchesAny("Struggle")) {
        	// STAB
        	if(attacker.getSpecies().getType1() == modifiedAttackMove.getType()
        			|| attacker.getSpecies().getType2() == modifiedAttackMove.getType())
        		damage = damage * 15 / 10;
        	
        	// Levitate
        	if(defender.getAbility() == Ability.LEVITATE && modifiedAttackMove.getType() == Type.GROUND) {
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Ability.LEVITATE));
            	return 0;
        	}
        	
        	// Type effectiveness
        	damage = Type.applyTypeEffectiveness(damage, modifiedAttackMove.getType(), defender.getSpecies().getType1ByPrecedence(), ghostRevealed, false);
        	damage = Type.applyTypeEffectiveness(damage, modifiedAttackMove.getType(), defender.getSpecies().getType2ByPrecedence(), ghostRevealed, false);
        	
        	// Wonder Guard
        	if(defender.getAbility() == Ability.WONDER_GUARD 
        	&& !Type.isSuperEffective(modifiedAttackMove.getType(), defender.getSpecies().getType1(), defender.getSpecies().getType2(), ghostRevealed, false)) {
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Ability.WONDER_GUARD));
            	return 0;
        	}
        }
        
        // ******************** //
        // [End atk06_typecalc] //
        // #################### //
        
        // ************************** //
        // [atk07_adjustnormaldamage] //
        // ************************** //
        
        // Damage roll
        // https://github.com/pret/pokeemerald/blob/master/src/battle_script_commands.c#L1639
        if(damage != 0) {
	        damage = damage * roll / MAX_ROLL;
	        if(damage == 0)
	        	damage = 1;
        }
        
        // Bunch of stuff skipped because not useful or linked to damage
        
        // ****************************** //
        // [End atk07_adjustnormaldamage] //
        // ****************************** //
        
        return damage;
    }
    
    public static int calculateBaseDamageGen3(
    		Move modifiedAttackMove, Pokemon attacker, Pokemon defender,
            StatModifier atkMod, StatModifier defMod,
            boolean isCrit, int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) throws UnsupportedOperationException, ToolInternalException {
    	// ************************ //
        // >> (CalculateBaseDamage) //
        // ************************ //
        
        //TODO: Line randomly added there, might move it somewhere
        if (modifiedAttackMove.getPower() == 0) {
        	return 0; // TODO: hardcoded
        }
        boolean ghostRevealed = defMod.hasStatus2_3(Status.FORESIGHT);
        
        // Forced override
        // TODO : better power & type overrides
        if (modifiedAttackMove.getEffect() == MoveEffect.LEVEL_DAMAGE) {
        	Type atkType = modifiedAttackMove.getType();
        	Type defType1 = defender.getSpecies().getType1();
        	Type defType2 = defender.getSpecies().getType2();
        	if(Type.isImmune(atkType, defType1, defType2, ghostRevealed, false))
        		return 0; //TODO : hardcoded
        	return attacker.getLevel();
        } 
        /* Already handled in damagesSummaryCore
        else if (modifiedAttackMove.getEffect() == MoveEffect.ROLLOUT || modifiedAttackMove.getEffect() == MoveEffect.FURY_CUTTER) {
    		modifiedAttackMove.setPower(modifiedAttackMove.getPower() * extra_multiplier);
    		extra_multiplier = 1;
        }
        */
        
        if (modifiedAttackMove.getPower() == 1) { // Special cases seem to have this in common
            // TODO: more special cases
        	switch(modifiedAttackMove.getEffect()) {        		
        	case HIDDEN_POWER:
                Type type = attacker.getIVs().getHiddenPowerType();
                int power = attacker.getIVs().getHiddenPowerPower();
                modifiedAttackMove.setType(type);
                modifiedAttackMove.setPower(power);
                modifiedAttackMove.setName(String.format("%s %s %d",modifiedAttackMove.getName(), type, power));
            	break;
                
        	case SONICBOOM:
        		Type atkType = modifiedAttackMove.getType();
            	Type defType1 = defender.getSpecies().getType1();
            	Type defType2 = defender.getSpecies().getType2();
            	if(Type.isImmune(atkType, defType1, defType2, ghostRevealed, false))
            		return 0; //TODO : hardcoded
        		return 20; //TODO : hardcoded
        		
        	case DRAGON_RAGE:
        		atkType = modifiedAttackMove.getType();
            	defType1 = defender.getSpecies().getType1();
            	defType2 = defender.getSpecies().getType2();
            	if(Type.isImmune(atkType, defType1, defType2, ghostRevealed, false))
            		return 0; //TODO : hardcoded
        		return 40; //TODO : hardcoded

        	default: return 0;
        	}
        }
        
        // Enigma Berry
        // TODO
        
        // Adding stat stages to move name now
        if(modifiedAttackMove.isPhysical() && atkMod.getStage(Stat.ATK) != 0)
    		modifiedAttackMove.setName(String.format("%s @%+d",modifiedAttackMove.getName(), atkMod.getStage(Stat.ATK)));
        else if(modifiedAttackMove.isSpecial() && atkMod.getStage(Stat.SPA) != 0)
    		modifiedAttackMove.setName(String.format("%s @%+d",modifiedAttackMove.getName(), atkMod.getStage(Stat.SPA)));
        
        // (retrieving stats)
        int attackerAtk = attacker.getBattleTowerStatValue(Stat.ATK);
        int attackerSpa = attacker.getBattleTowerStatValue(Stat.SPA);
        
        int defenderDef = defender.getBattleTowerStatValue(Stat.DEF);
        int defenderSpd = defender.getBattleTowerStatValue(Stat.SPD);
        
        // (retrieving items)
        Item attackerItem = attacker.getHeldItem();
        boolean hasAttackerItem = attackerItem != null;
        Item defenderItem = defender.getHeldItem();
        boolean hasDefenderItem = defenderItem != null;
        
        // Huge Power, Pure Power
        if(attacker.getAbility() == Ability.HUGE_POWER || attacker.getAbility() == Ability.PURE_POWER) {
        	attackerAtk *= 2;
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attacker.getAbility()));
    	}
        
        // Badge boosts
        if(!isBattleTower) { 
        	// TODO : game checks for 'gBattleTypeFlags & BATTLE_TYPE_TRAINER', so maybe no badge boosts against encounters ?
        	// See : https://github.com/pret/pokeruby/blob/a3228d4c86494ee25aff60fc037805ddc1d47d32/src/calculate_base_damage.c#L85
	        //attackerAtk = attacker.applyBadgeBoostIfPossible(Stat.ATK, attackerAtk);
	        //attackerSpa = attacker.applyBadgeBoostIfPossible(Stat.SPA, attackerSpa);
	        //defenderDef = defender.applyBadgeBoostIfPossible(Stat.DEF, defenderDef);
	        //defenderSpd = defender.applyBadgeBoostIfPossible(Stat.SPD, defenderSpd);
        	attackerAtk = attacker.getStatValue(Stat.ATK);
        	attackerSpa = attacker.getStatValue(Stat.SPA);
        	defenderDef = defender.getStatValue(Stat.DEF);
        	defenderSpd = defender.getStatValue(Stat.SPD);
        }
        
        // Type boosting items
        if (hasAttackerItem) {
        	ItemHoldEffect attackerItemHoldEffect = attackerItem.getHoldEffect();
        	Type moveType = modifiedAttackMove.getType();
        	if(attackerItemHoldEffect.isTypeBoosting(moveType)) {
        		int effectParam = attackerItem.getHoldEffectParam();
        		if(modifiedAttackMove.isPhysical()) // TODO: may be different for Gen 4
        			attackerAtk = attackerAtk * (100 + effectParam) / 100; // TODO: hardcoded
        		else if (modifiedAttackMove.isSpecial())
        			attackerSpa = attackerSpa * (100 + effectParam) / 100; // TODO: hardcoded
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));
        	}
        }
        
        // Choice Band
        if (hasAttackerItem && attackerItem.getHoldEffect() == ItemHoldEffect.CHOICE_BAND) {
        	attackerAtk = attackerAtk * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));
    	}
        
        // Attacker Soul Dew
        if (hasAttackerItem && attackerItem.getHoldEffect() == ItemHoldEffect.SOUL_DEW && !isBattleTower
        		&& (attacker.getSpecies() == Species.getSpeciesByName("LATIOS") || attacker.getSpecies() == Species.getSpeciesByName("LATIAS"))) { // TODO: hardcoded
        	attackerSpa = attackerSpa * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));
    	}
        
        // Defender Soul Dew
        if (hasDefenderItem && defenderItem.getHoldEffect() == ItemHoldEffect.SOUL_DEW && !isBattleTower
        		&& (defender.getSpecies() == Species.getSpeciesByName("LATIOS") || defender.getSpecies() == Species.getSpeciesByName("LATIAS"))) { // TODO: hardcoded
        	defenderSpd = defenderSpd * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s -y%s",modifiedAttackMove.getName(), defenderItem));
    	}
        
        // Attacker Deep Sea Tooth
        if (hasAttackerItem && attackerItem.getHoldEffect() == ItemHoldEffect.DEEP_SEA_TOOTH
        		&& attacker.getSpecies() == Species.getSpeciesByName("CLAMPERL")) { // TODO: hardcoded
        	attackerSpa *= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));
    	}
        
        // Defender Deep Sea Scale
        if (hasDefenderItem && defenderItem.getHoldEffect() == ItemHoldEffect.DEEP_SEA_SCALE
        		&& defender.getSpecies() == Species.getSpeciesByName("CLAMPERL")) { // TODO: hardcoded
        	defenderSpd *= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s -y%s",modifiedAttackMove.getName(), defenderItem));
    	}
        
        // Attacker Light Ball
        if (hasAttackerItem && attackerItem.getHoldEffect() == ItemHoldEffect.LIGHT_BALL
        		&& attacker.getSpecies() == Species.getSpeciesByName("PIKACHU")) { // TODO: hardcoded
        	attackerSpa *= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));
    	}
        
        // Defender Metal Powder
        if (hasDefenderItem && defenderItem.getHoldEffect() == ItemHoldEffect.METAL_POWDER
        		&& defender.getSpecies() == Species.getSpeciesByName("DITTO")) { // TODO: hardcoded
        	defenderDef *= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s -y%s",modifiedAttackMove.getName(), defenderItem));
    	}
        
        // Attacker Thick Club
        if (hasAttackerItem && attackerItem.getHoldEffect() == ItemHoldEffect.THICK_CLUB
        		&& (attacker.getSpecies() == Species.getSpeciesByName("CUBONE") || attacker.getSpecies() == Species.getSpeciesByName("MAROWAK"))) { // TODO: hardcoded
        	attackerAtk *= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attackerItem));	
        }
        
        // Defender Thick Fat
        if(defender.getAbility() == Ability.THICK_FAT 
        		&& (modifiedAttackMove.getType() == Type.FIRE || modifiedAttackMove.getType() == Type.ICE)) {
        	attackerSpa /= 2; // TODO: hardcoded
        	if(modifiedAttackMove.isSpecial())
        		modifiedAttackMove.setName(String.format("%s -y%s",modifiedAttackMove.getName(), defender.getAbility()));
        }
        
        // Attacker Hustle
        if(attacker.getAbility() == Ability.HUSTLE) {
        	attackerAtk = attackerAtk * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attacker.getAbility()));
        }
        
        // TODO: Plus & Minus
        
        // Attacker Guts
        if(attacker.getAbility() == Ability.GUTS && atkMod.getStatus1() != Status.NONE) {
        	attackerAtk = attackerAtk * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s +x%s",modifiedAttackMove.getName(), attacker.getAbility()));
        }
        
        // Defender Marvel Scale
        if(defender.getAbility() == Ability.MARVEL_SCALE && defMod.getStatus1() != Status.NONE) {
        	defenderDef = defenderDef * 150 / 100; // TODO: hardcoded
        	if(modifiedAttackMove.isPhysical())
        		modifiedAttackMove.setName(String.format("%s -y%s",modifiedAttackMove.getName(), defender.getAbility()));
        }
        
        // Attacker Mud Sport
        if(modifiedAttackMove.getType() == Type.ELECTRIC && (atkMod.hasStatus2_3(Status.MUDSPORT) || defMod.hasStatus2_3(Status.MUDSPORT))) {
        	modifiedAttackMove.setPower(modifiedAttackMove.getPower() / 2); // TODO: hardcoded
        	modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.MUDSPORT));
        }
        
        // Attacker Water Spout
        if(modifiedAttackMove.getType() == Type.FIRE && (atkMod.hasStatus2_3(Status.WATERSPORT) || defMod.hasStatus2_3(Status.WATERSPORT))) {
        	modifiedAttackMove.setPower(modifiedAttackMove.getPower() / 2); // TODO: hardcoded
        	modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.WATERSPORT));
        }
        
        // Attacker Overgrow, Blaze, Torrent, Swarm
        if(atkMod.isHPThirdOrLess(attacker.getStatValue(Stat.HP))) {
	        if(modifiedAttackMove.getType() == Type.GRASS && attacker.getAbility() == Ability.OVERGROW
	        || modifiedAttackMove.getType() == Type.FIRE && attacker.getAbility() == Ability.BLAZE
	        || modifiedAttackMove.getType() == Type.WATER && attacker.getAbility() == Ability.TORRENT
	        ||modifiedAttackMove.getType() == Type.BUG && attacker.getAbility() == Ability.SWARM) {
	        	modifiedAttackMove.setPower(modifiedAttackMove.getPower() * 150 / 100); // TODO: hardcoded
	        	modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), attacker.getAbility()));
	        }
        }
        
        // Defender Explosion
        if (modifiedAttackMove.getEffect() == MoveEffect.EXPLOSION)
        	defenderDef /= 2; // TODO: hardcoded
        
        // Base damage
        int damage = Integer.MIN_VALUE;
        int damageHelper = Integer.MIN_VALUE;
        if(modifiedAttackMove.isPhysical()){
        	if(isCrit) {
        		if(atkMod.getStage(Stat.ATK) > 0)
        			damage = atkMod.modStat(Stat.ATK, attackerAtk);
        		else
        			damage = attackerAtk;
        	}
        	else
        		damage = atkMod.modStat(Stat.ATK, attackerAtk);
        	
        	damage *= modifiedAttackMove.getPower();
        	damage *= (2 * attacker.getLevel() / 5 + 2);
        	
        	if(isCrit) {
        		if(defMod.getStage(Stat.DEF) < 0)
        			damageHelper = defMod.modStat(Stat.DEF, defenderDef);
        		else
        			damageHelper = defenderDef;
        	}
        	else
        		damageHelper = defMod.modStat(Stat.DEF, defenderDef);
        	
        	damage = damage / damageHelper;
        	damage /= 50;
        	
        	// Attacker Burn
        	if(atkMod.getStatus1() == Status.BURN && attacker.getAbility() != Ability.GUTS) {
        		damage /= 2;
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.BURN));
	        }
        	
        	// Defender Reflect
        	if(defMod.hasStatus2_3(Status.REFLECT) && !isCrit) {
        		if(isDoubleBattle) // TODO: Only if defender side has 2 mons alive, make sure isDoubleBattle is really for this, or change the logic
        			damage = 2 * (damage / 3);
        		else
        			damage /= 2;
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.REFLECT));
        	}
        	
        	// Moves hitting both enemies		
        	// In 3G : BOTH_ENEMIES with 2 targets = 50%, 
    		//         BOTH_ENEMIES with 1 target = 100%,
    		//         ALL_EXCEPT_USER = 100%
        	if(isDoubleBattle && modifiedAttackMove.getMoveTarget() == MoveTarget.BOTH_ENEMIES) { // TODO: Only if defender side has 2 mons alive, make sure isDoubleBattle is really for this, or change the logic
        		damage /= 2;
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), MoveTarget.BOTH_ENEMIES));
        	}
        	
        	// Damage of at least 1
        	if (damage == 0)
        		damage = 1;
        }
        else if (modifiedAttackMove.getType() == Type.MYSTERY || modifiedAttackMove.getType() == Type.NONE)
        	damage = 0;
        else { // Special types
        	if(isCrit) {
        		if(atkMod.getStage(Stat.SPA) > 0)
        			damage = atkMod.modStat(Stat.SPA, attackerSpa);
        		else
        			damage = attackerSpa;
        	}
        	else
        		damage = atkMod.modStat(Stat.SPA, attackerSpa);
        	
        	damage *= modifiedAttackMove.getPower();
        	damage *= (2 * attacker.getLevel() / 5 + 2);
        	
        	if(isCrit) {
        		if(defMod.getStage(Stat.SPD) < 0)
        			damageHelper = defMod.modStat(Stat.SPD, defenderSpd);
        		else
        			damageHelper = defenderSpd;
        	}
        	else
        		damageHelper = defMod.modStat(Stat.SPD, defenderSpd);
        	
        	damage = damage / damageHelper;
        	damage /= 50;
        	
        	// Defender Light Screen
        	if(defMod.hasStatus2_3(Status.LIGHTSCREEN) && !isCrit) {
        		if(isDoubleBattle) // TODO: Only if defender side has 2 mons alive, make sure isDoubleBattle is really for this, or change the logic
        			damage = 2 * (damage / 3);
        		else
        			damage /= 2;

        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Status.LIGHTSCREEN));
        	}
        	
        	// Moves hitting both enemies		
        	// In 3G : BOTH_ENEMIES with 2 targets = 50%, 
    		//         BOTH_ENEMIES with 1 target = 100%,
    		//         ALL_EXCEPT_USER = 100%
        	if(isDoubleBattle && modifiedAttackMove.getMoveTarget() == MoveTarget.BOTH_ENEMIES) { // TODO: Only if defender side has 2 mons alive, make sure isDoubleBattle is really for this, or change the logic
        		damage /= 2;
        		modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), MoveTarget.BOTH_ENEMIES));
    		}
        	
        	// Inverting the weather checks here to handle CLOUD_NINE/AIR_LOCK weather effects in move names
        	// The original code is in comment below
        	
        	boolean isAttackerNegatingWeather = attacker.getAbility() == Ability.CLOUD_NINE || attacker.getAbility() == Ability.AIR_LOCK;
        	boolean isDefenderNegatingWeather = defender.getAbility() == Ability.CLOUD_NINE || defender.getAbility() == Ability.AIR_LOCK;
        	
        	// Rainy
    		if(atkMod.getWeather() == Weather.RAIN) {
    			if(modifiedAttackMove.getType() == Type.FIRE) {
    				if(isAttackerNegatingWeather) {
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), attacker.getAbility()));
    				} else if (isDefenderNegatingWeather){
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), defender.getAbility()));
    				} else {
    					damage /= 2;
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), Weather.RAIN));
    				}
    			}
    			else if (modifiedAttackMove.getType() == Type.WATER) {
    				if(isAttackerNegatingWeather) {
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), attacker.getAbility()));
    				} else if (isDefenderNegatingWeather){
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), defender.getAbility()));
    				} else {
    					damage = damage * 15 / 10;
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), Weather.RAIN));
    				}
    			}
    		}
    		
    		// Any weather except sun weakens solar beam
    		if(modifiedAttackMove.getEffect() == MoveEffect.SOLARBEAM 
    				&& atkMod.getWeather() != Weather.NONE && atkMod.getWeather() != Weather.SUN) {
    			if(isAttackerNegatingWeather) {
					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), attacker.getAbility()));
				} else if (isDefenderNegatingWeather){
					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), defender.getAbility()));
				} else {
					damage /= 2;
					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), atkMod.getWeather()));
				}
    		}
    		
    		// Sunny
    		if(atkMod.getWeather() == Weather.SUN) {
    			if(modifiedAttackMove.getType() == Type.FIRE) {
    				if(isAttackerNegatingWeather) {
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), attacker.getAbility()));
    				} else if (isDefenderNegatingWeather){
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), defender.getAbility()));
    				} else {
    					damage = damage * 15 / 10;
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), atkMod.getWeather()));
    				}
    			}
    			else if (modifiedAttackMove.getType() == Type.WATER) {
    				if(isAttackerNegatingWeather) {
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), attacker.getAbility()));
    				} else if (isDefenderNegatingWeather){
    					modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), defender.getAbility()));
    				} else {
    					damage /= 2;
    					modifiedAttackMove.setName(String.format("%s -%s",modifiedAttackMove.getName(), atkMod.getWeather()));
    				}
    			}
    		}
        	
        	/*
        	// Are effects of weather negated with cloud nine or air lock
        	if(attacker.getAbility() != Ability.CLOUD_NINE && attacker.getAbility() != Ability.AIR_LOCK
        			&& defender.getAbility() != Ability.CLOUD_NINE && defender.getAbility() != Ability.AIR_LOCK) {
        		// Rainy
        		if(atkMod.getWeather() == Weather.RAIN) {
        			if(modifiedAttackMove.getType() == Type.FIRE) {
        				damage /= 2;
        			}
        			else if (modifiedAttackMove.getType() == Type.WATER)
        				damage = damage * 15 / 10;
        		}
        		
        		// Any weather except sun weakens solar beam
        		if(modifiedAttackMove.getEffect() == MoveEffect.SOLARBEAM 
        				&& atkMod.getWeather() != Weather.NONE && atkMod.getWeather() != Weather.SUN)
        			damage /= 2;
        		
        		// Sunny
        		if(atkMod.getWeather() == Weather.SUN) {
        			if(modifiedAttackMove.getType() == Type.FIRE)
        				damage = damage * 15 / 10;
        			else if (modifiedAttackMove.getType() == Type.WATER)
        				damage /= 2;
        		}
        	}
        	*/
        	
        	// Attacker Flash Fire
        	if(atkMod.hasStatus2_3(Status.FLASH_FIRE) && modifiedAttackMove.getType() == Type.FIRE) {
        		damage = damage * 15 / 10;
        		modifiedAttackMove.setName(String.format("%s +%s",modifiedAttackMove.getName(), Status.FLASH_FIRE));
        	}
        	
        	// No Damage at least 1 for special moves wtf
        }
        
        damage += 2;
        return damage;
        // CalculateBaseDamage routine returns damage + 2 here
        
        // **************************** //
        // >> (End CalculateBaseDamage) //
        // ***######################### //
    }
	
    // ****************** //
    //        GEN 4       //
	// MAIN DAMAGE METHOD //
    // ****************** //
    
    // Using smogon as a reference:
    // https://github.com/smogon/damage-calc/blob/master/calc/src/mechanics/gen4.ts 
    
	/**
	 * Calculate one damage value based on a provided roll, in Gen 4.
	 * @param move the used move.
	 * @param attacker the attacker Pokemon.
	 * @param defender the defender Pokemon.
	 * @param atkMod the attacker's stat modifiers.
	 * @param defMod the defender's stat modifiers.
	 * @param roll the provided roll.
	 * @param isCrit if the move is a critical hit.
	 * @param extra_multiplier the desired extra multiplier
	 * @param isBattleTower if the battle occurs in Battle Tower.
	 * @param isDoubleBattle if the battle is a double battle.
	 * @param param an optional parameter used in specific cases.
	 * @return the calculated damage value.
	 * @throws ToolInternalException 
	 * @throws UnsupportedOperationException 
	 */
    private static int damageGen4(Move move, Pokemon attacker, Pokemon defender,
                              StatModifier atkMod, StatModifier defMod, int roll,
                              boolean isCrit, int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) throws UnsupportedOperationException, ToolInternalException {
    	
    	//if(move.getName().equalsIgnoreCase("TACKLE") && attacker.getSpecies().getHashName().equalsIgnoreCase("Starly")
    	//		&& attacker.getLevel() == 5)
    	//	System.out.println("in");
    	//
    	
        MoveClass moveClass = move.getMoveClass();
        MoveEffect moveEffect = move.getEffect();
        Type moveType = move.getType();
        int movePower = move.getPower();
        
        Weather weather = atkMod.getWeather();
        
        Ability attackerAbility = attacker.getAbility();
        Ability defenderAbility = defender.getAbility();
        
        Type attackerType1 = attacker.getSpecies().getType1();
        Type attackerType2 = attacker.getSpecies().getType2();
        Type defenderType1 = defender.getSpecies().getType1();
        Type defenderType2 = defender.getSpecies().getType2();
                
        // Check Air Lock
        if(attackerAbility == Ability.AIR_LOCK
        || defenderAbility == Ability.AIR_LOCK) {
        	weather = Weather.NONE;
        	move.setName(String.format("%s %s%s",move.getName(), attackerAbility == Ability.AIR_LOCK ? "x" : "y",Ability.AIR_LOCK)); // TODO: find better way
        }
        
        // Check Forecast
        if(attackerAbility == Ability.FORECAST) {
        	switch(weather) {
        	case SUN:  attackerType1 = Type.FIRE;   attackerType2 = Type.NONE; break;
        	case RAIN: attackerType1 = Type.WATER;  attackerType2 = Type.NONE; break;
        	case HAIL: attackerType1 = Type.ICE;    attackerType2 = Type.NONE; break;
        	default:   attackerType1 = Type.NORMAL; attackerType2 = Type.NONE; break;
        	}
        	move.setName(String.format("%s [x%s %s]",move.getName(),attackerAbility,attackerType1)); // TODO: find better way
        }
        if(defenderAbility == Ability.FORECAST) { // TODO: find a way to change Pokemon type directly somehow
        	switch(weather) {
          	case SUN:  defenderType1 = Type.FIRE;   defenderType2 = Type.NONE; break;
        	case RAIN: defenderType1 = Type.WATER;  defenderType2 = Type.NONE; break;
        	case HAIL: defenderType1 = Type.ICE;    defenderType2 = Type.NONE; break;
        	default:   defenderType1 = Type.NORMAL; defenderType2 = Type.NONE; break;
        	}
        	move.setName(String.format("%s [y%s %s]",move.getName(),defenderAbility,defenderType1)); // TODO: find better way
        }
        
        // Check item | Beware : Klutz + Iron Ball doesn't negate speed drop, but negates grounding (that's why we keep the Iron Ball here)
        Item attackerItem = attacker.getHeldItem();
        Item defenderItem = defender.getHeldItem();
        if(attackerAbility == Ability.KLUTZ && attackerItem != null) {
        	switch(attackerItem.getHoldEffect()) {
        	case SPEED_DOWN_GROUNDED: // Iron Ball
        	case EXP_UP_SPEED_DOWN: // Macho Brace
        	case LVLUP_SPEED_EV_UP: // Power Anklet
        	case LVLUP_SPDEF_EV_UP: // Power Band
        	case LVLUP_DEF_EV_UP:   // Power Belt
        	case LVLUP_ATK_EV_UP:   // Power Bracer
        	case LVLUP_SPATK_EV_UP: // Power Lens
        	case LVLUP_HP_EV_UP:    // Power Weight
        		break;
        	default:
        		attackerItem = null;
            	move.setName(String.format("%s -%s",move.getName(), attackerAbility)); // TODO: find better way
        		break;
        	}
        }
        if(defenderAbility == Ability.KLUTZ && defenderItem != null) {
        	switch(defenderItem.getHoldEffect()) {
        	case SPEED_DOWN_GROUNDED: // Iron Ball
        	case EXP_UP_SPEED_DOWN: // Macho Brace
        	case LVLUP_SPEED_EV_UP: // Power Anklet
        	case LVLUP_SPDEF_EV_UP: // Power Band
        	case LVLUP_DEF_EV_UP:   // Power Belt
        	case LVLUP_ATK_EV_UP:   // Power Bracer
        	case LVLUP_SPATK_EV_UP: // Power Lens
        	case LVLUP_HP_EV_UP:    // Power Weight
        		break;
        	default:
        		defenderItem = null;
            	move.setName(String.format("%s +%s",move.getName(), defenderAbility)); // TODO: find better way
        		break;
        	}
        }
        	
        Item attackerOldItem = attacker.getHeldItem();
        attacker.setItem(attackerItem);
        int attackerSpeed = atkMod.getFinalSpeed(attacker);
        attacker.setItem(attackerOldItem);
        
        Item defenderOldItem = defender.getHeldItem();
        defender.setItem(defenderItem);
        int defenderSpeed = defMod.getFinalSpeed(defender);
        defender.setItem(defenderOldItem);
        
        if (moveClass == MoveClass.STATUS && moveEffect != MoveEffect.NATURE_POWER)
            return 0;
        
        if(attackerAbility == Ability.MOLD_BREAKER && defenderAbility.isIgnorable()) {
        	defenderAbility = Ability.NONE;
        	move.setName(String.format("%s x%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        if(defenderAbility == Ability.MOLD_BREAKER  && attackerAbility.isIgnorable()) {
        	attackerAbility = Ability.NONE;
        	move.setName(String.format("%s y%s",move.getName(), defenderAbility)); // TODO: find better way
        }
        
        boolean discardCrit = defenderAbility == Ability.BATTLE_ARMOR || defenderAbility == Ability.SHELL_ARMOR;
        if(discardCrit) {
        	isCrit = false;
        	move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        }
        
        
        switch(moveEffect) {
        case WEATHER_BALL:
        	switch(weather) {
        	case SUN :
        		moveType = Type.FIRE;
        		movePower *= 2;
            	move.setName(String.format("%s +%s",move.getName(), moveType)); // TODO: find better way
        		break;
        	case RAIN:
        		moveType = Type.WATER;
        		movePower *= 2;
            	move.setName(String.format("%s +%s",move.getName(), moveType)); // TODO: find better way
        		break;
        	case SANDSTORM:
        		moveType = Type.ROCK;
        		movePower *= 2;
            	move.setName(String.format("%s +%s",move.getName(), moveType)); // TODO: find better way
        		break;
        	case HAIL:
        		moveType = Type.ICE;
        		movePower *= 2;
            	move.setName(String.format("%s +%s",move.getName(), moveType)); // TODO: find better way
        		break;
        	default: 
        		break;
        	}
        	break;
        	
        case JUDGMENT:
    		if(attackerItem == null)
    			break;
    		boolean isModifiedType = false;
    		switch(attackerItem.getHoldEffect()) {
    		case ARCEUS_BUG:      moveType = Type.BUG;      isModifiedType = true; break;
    		case ARCEUS_DARK:     moveType = Type.DARK;     isModifiedType = true; break;
    		case ARCEUS_DRAGON:   moveType = Type.DRAGON;   isModifiedType = true; break;
    		case ARCEUS_ELECTRIC: moveType = Type.ELECTRIC; isModifiedType = true; break;
    		case ARCEUS_FIGHT:    moveType = Type.FIGHTING; isModifiedType = true; break;
    		case ARCEUS_FIRE:     moveType = Type.FIRE;     isModifiedType = true; break;
    		case ARCEUS_FLYING:   moveType = Type.FLYING;   isModifiedType = true; break;
    		case ARCEUS_GHOST:    moveType = Type.GHOST;    isModifiedType = true; break;
    		case ARCEUS_GRASS:    moveType = Type.GRASS;    isModifiedType = true; break;
    		case ARCEUS_GROUND:   moveType = Type.GROUND;   isModifiedType = true; break;
    		case ARCEUS_ICE:      moveType = Type.ICE;      isModifiedType = true; break;
    		case ARCEUS_POISON:   moveType = Type.POISON;   isModifiedType = true; break;
    		case ARCEUS_PSYCHIC:  moveType = Type.PSYCHIC;  isModifiedType = true; break;
    		case ARCEUS_ROCK:     moveType = Type.ROCK;     isModifiedType = true; break;
    		case ARCEUS_STEEL:    moveType = Type.STEEL;    isModifiedType = true; break;
    		case ARCEUS_WATER:    moveType = Type.WATER;    isModifiedType = true; break;
    		default:
    			break;
    		}
    		if(isModifiedType) {
            	move.setName(String.format("%s %s",move.getName(), moveType)); // TODO: find better way
    		}
    		break;
    	
    	case NATURAL_GIFT:
    		if(attackerItem != null && attackerItem.getNaturalGiftPower() > 0) {
    			movePower = attackerItem.getNaturalGiftPower();
    			moveType = attackerItem.getNaturalGiftType();
            	move.setName(String.format("%s %s %s",move.getName(), moveType, movePower)); // TODO: find better way
    		}
    		break;
    		
        case HIDDEN_POWER:
            moveType = attacker.getIVs().getHiddenPowerType();
            movePower = attacker.getIVs().getHiddenPowerPower();
        	move.setName(String.format("%s %s %s",move.getName(), moveType, movePower)); // TODO: find better way
            break;
        	
        default:
        	break;
        }
        
        
        if(attackerAbility == Ability.NORMALIZE) {
        	moveType = Type.NORMAL;
        	move.setName(String.format("%s %s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        
        boolean isGhostRevealed = attackerAbility == Ability.SCRAPPY || defMod.hasStatus2_3(Status.FORESIGHT);
        boolean isGrounded = defenderItem != null && defenderItem.getHoldEffect() == ItemHoldEffect.SPEED_DOWN_GROUNDED && defenderAbility != Ability.KLUTZ // Iron Ball 
        		          || defMod.hasStatus2_3(Status.GROUNDED); // Gravity
        
        boolean isImmune = Type.isImmune(moveType, defenderType1, defenderType2, isGhostRevealed, isGrounded);
        
        if(isGhostRevealed) {
        	if(attackerAbility == Ability.SCRAPPY) {
            	move.setName(String.format("%s %s",move.getName(), attackerAbility)); // TODO: find better way
        	} else {
            	move.setName(String.format("%s %s",move.getName(), Status.FORESIGHT)); // TODO: find better way
        	}
        }
        
        if(isGrounded) {
        	if(defenderItem != null) {
            	move.setName(String.format("%s %s",move.getName(), "IRON BALL")); // TODO: find better way
        	} else {
            	move.setName(String.format("%s %s",move.getName(), "GRAVITY")); // TODO: find better way
        	}
        }
        
        if(isImmune)
        	return 0;
        
        boolean isSuperEffective = Type.isSuperEffective(moveType, defenderType1, defenderType2, isGhostRevealed, isGrounded);
        boolean isNotVeryEffective = Type.isNotVeryEffective(moveType, defenderType1, defenderType2, isGhostRevealed, isGrounded);
        //String moveName = move.getName();
        boolean ignoresWonderGuard = moveType == Type.MYSTERY || moveType == Type.NONE || moveEffect == MoveEffect.FIRE_FANG;
        if(!ignoresWonderGuard && defenderAbility == Ability.WONDER_GUARD && !isSuperEffective
        || moveType == Type.FIRE && defenderAbility == Ability.FLASH_FIRE
        || moveType == Type.WATER && (defenderAbility == Ability.DRY_SKIN || defenderAbility == Ability.WATER_ABSORB)
        || moveType == Type.ELECTRIC && (defenderAbility == Ability.MOTOR_DRIVE || defenderAbility == Ability.VOLT_ABSORB)
        || moveType == Type.GROUND && !isGrounded && defenderAbility == Ability.LEVITATE
        || defenderAbility == Ability.SOUNDPROOF && move.isSoundMove()) {
        	move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        	return 0;
        }
        	
        // There should be no more immunity from now on
        
        switch(moveEffect) {
        case LEVEL_DAMAGE: // Night Shade, Seismic Toss
        	return attacker.getLevel();
        	
        case FIXED_20: // Sonicboom
        	return 20; // TODO: hardcoded
        	
        case FIXED_40: // Dragon Rage
        	return 40; // TODO: hardcoded
        
        case BRINE:
    		if(defMod.isHPHalfOrLess(defender.getStatValue(Stat.HP))) {
    			movePower *= 2;
        		move.setName(String.format("%s %s",move.getName(), "+yHPlow")); // TODO: find better way
    		}
    		break;
    		
    	case STRONGER_HIGH_HP: // Eruption, Water Spout
    		movePower = (int)Math.max(1, Math.floor((movePower * atkMod.getCurrHP()) / attacker.getStatValue(Stat.HP)));
    		move.setName(String.format("%s %s",move.getName(), movePower)); // TODO: find better way
    		break;
    		
    	case FACADE:
    		switch(atkMod.getStatus1()) {
    		case POISON:
    		case BURN:
    		case PARALYSIS:
    		case TOXIC:
    			movePower *= 2;
        		move.setName(String.format("%s +%s",move.getName(), atkMod.getStatus1())); // TODO: find better way
    			break;
    		default:
    			break;
    		}
    		break;	
    		
    	case STRONGER_LOW_HP: // Flail, Reversal
    		int p = (int)Math.floor((64 * atkMod.getCurrHP()) / attacker.getStatValue(Stat.HP));
    		movePower = p <= 1 ? 200 : p <= 5 ? 150 : p <= 12 ? 100 : p <= 21 ? 80 : p <= 42 ? 40 : 20;
    		// move name is already handled
    		// move.setName(String.format("%s %s",move.getName(), movePower));
    		break; 		
    		
    	case FLING:
    		// TODO : not implemented
    		move.setName(String.format("%s %s",move.getName(), "(not implemented)")); // TODO: find better way
    		return 0;
    		// break;	
    		
    	case STRONGER_HEAVIER: // Grass Knot, Low Kick
        	int w = defender.getSpecies().getWeight();
        	movePower = w >= 2000 ? 120 : w >= 1000 ? 100 : w >= 500 ? 80 : w >= 250 ? 60 : w >= 100 ? 40 : 20;
    		move.setName(String.format("%s %s",move.getName(), movePower)); // TODO: find better way
        	break;	
        	
    	case GYRO_BALL:
    		movePower = (int)Math.min(150, Math.floor((25 * defenderSpeed) / attackerSpeed));
    		move.setName(String.format("%s %s",move.getName(), movePower)); // TODO: find better way
    		break;       	
        	
    	case PAYBACK:
    		// TODO : not entirely implemented
    		// Bulbapedia states "Its power doubles to 100 if the user moves after the target, if the target switches out, or if the opponent uses an item."
    		// move.setName(String.format("%s %s",move.getName(), "(not fully implemented")); // TODO: find better way
    		break;    		
    		
    	case PUNISHMENT:
    		int atkStage = defMod.getStage(Stat.ATK);
    		int defStage = defMod.getStage(Stat.DEF);
    		int spaStage = defMod.getStage(Stat.SPA);
    		int spdStage = defMod.getStage(Stat.SPD);
    		int speStage = defMod.getStage(Stat.SPE);
    		int boosts = ((atkStage > 0) ? atkStage : 0)
    				   + ((defStage > 0) ? defStage : 0)
    				   + ((spaStage > 0) ? spaStage : 0)
    				   + ((spdStage > 0) ? spdStage : 0)
    				   + ((speStage > 0) ? speStage : 0);
    		movePower = (int)Math.min(200, 60 + 20 * boosts);
    		move.setName(String.format("%s %s",move.getName(), movePower)); // TODO: find better way
    		break;    		
        	
    	case WAKE_UP_SLAP:
    		if(defMod.getStatus1() == Status.SLEEP) {
    			movePower *= 2;
        		move.setName(String.format("%s +y%s",move.getName(), Status.SLEEP)); // TODO: find better way
    		}
    		break;        	

    	case NATURE_POWER:
    		// TODO : not implemented
    		move.setName(String.format("%s %s",move.getName(), "(not implemented")); // TODO: find better way
    		break;
    		
    	case CRUSH_GRIP: // Crush Grip, Wring Out
    		movePower = (int)Math.floor((defMod.getCurrHP() * 120) / defender.getStatValue(Stat.HP)) + 1;
    		move.setName(String.format("%s %s",move.getName(), movePower)); // TODO: find better way
    		break;

        default:
    		break;
        }
        

        // Adding stat stages to move name now | TODO: that's maybe too low, but i don't know yet how to do this properly
        if(move.isPhysical() && atkMod.getStage(Stat.ATK) != 0)
    		move.setName(String.format("%s @%+d",move.getName(), atkMod.getStage(Stat.ATK)));
        else if(move.isSpecial() && atkMod.getStage(Stat.SPA) != 0)
        	move.setName(String.format("%s @%+d",move.getName(), atkMod.getStage(Stat.SPA)));
        
        
        // TODO : Helping Hand
        
        // Boosting items
        if(attackerItem != null) {
        	ItemHoldEffect attackerItemEffect = attackerItem.getHoldEffect();
        	int effectParam = attackerItem.getHoldEffectParam();
        	
            if(attackerItemEffect.isTypeBoosting(moveType) // Type boosting items
    	    || attackerItemEffect == ItemHoldEffect.POWER_UP_PHYS && move.isPhysical() // Muscle Band
    	    || attackerItemEffect == ItemHoldEffect.POWER_UP_SPEC && move.isSpecial() // Wise Glasses
    	    || (attackerItemEffect == ItemHoldEffect.DIALGA_BOOST // Adamant Orb
    	        && attacker.getSpecies() == Species.getSpeciesByName("DIALGA")
    	        && (moveType == Type.STEEL || moveType == Type.DRAGON))
    	    || (attackerItemEffect == ItemHoldEffect.PALKIA_BOOST // Lustrous Orb
    	        && attacker.getSpecies() == Species.getSpeciesByName("PALKIA") 
    	        && (moveType== Type.WATER || moveType == Type.DRAGON))
    	    || (attackerItemEffect == ItemHoldEffect.GIRATINA_BOOST // TODO: Griseous Orb
    	        && (attacker.getSpecies() == Species.getSpeciesByName("GIRATINA") || attacker.getSpecies() == Species.getSpeciesByName("GIRATINA ORIGIN"))
    	        && (moveType == Type.GHOST || moveType == Type.DRAGON))
    	    ) {
            	movePower = movePower * (100 + effectParam) / 100; // TODO: hardcoded
        		move.setName(String.format("%s +%s",move.getName(), attackerItem)); // TODO: find better way
            }
        }
        
        // Rivalry
        if(attacker.getAbility() == Ability.RIVALRY) {
        	if(attacker.getGender() == defender.getGender() && attacker.getGender() != Gender.GENDERLESS) {
            	movePower = movePower * (100 + 25) / 100;
            	move.setName(String.format("%s +%s",move.getName(), attackerItem));
        	} else if (attacker.getGender() != defender.getGender() && attacker.getGender() != Gender.GENDERLESS
        		&& defender.getGender() != Gender.GENDERLESS) {
        		movePower = movePower * (100 - 25) / 100;
            	move.setName(String.format("%s -%s",move.getName(), attackerItem));
        	}
        }
        
        // Reckless
        if(attackerAbility == Ability.RECKLESS
        && (moveEffect == MoveEffect.RECOIL_HIT 
            || moveEffect == MoveEffect.RECOIL
            || moveEffect == MoveEffect.RECOIL_HIT_HARD
            || moveEffect == MoveEffect.RECOIL_IF_MISS
            || moveEffect == MoveEffect.JUMP_KICK)
        ) {
        	movePower = movePower * (100 + 20) / 100;  // TODO: hardcoded
    		move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        // Iron Fist
        // https://github.com/pret/pokeheartgold/blob/866d850ffe510ab7e73a8c2e3e4cdc48e2526b45/asm/overlay_12_0224E4FC_s.s#L1246
        else if(attackerAbility == Ability.IRON_FIST && move.isFistMove()){
        	movePower = movePower * (100 + 20) / 100;  // TODO: hardcoded
    		move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        // Overgrow, Blaze, Torrent, Swarm
        else if(atkMod.isHPThirdOrLess(attacker.getStatValue(Stat.HP))
        && (moveType == Type.GRASS && attackerAbility == Ability.OVERGROW
              || moveType == Type.FIRE && attackerAbility == Ability.BLAZE
              || moveType == Type.WATER && attackerAbility == Ability.TORRENT
              || moveType == Type.BUG && attackerAbility == Ability.SWARM)) {
        	movePower = movePower * (100 + 50) / 100; // TODO: hardcoded
    		move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        // Technician
        if(attackerAbility == Ability.TECHNICIAN && movePower <= 60) {
        	movePower = movePower * (100 + 50) / 100; // TODO: hardcoded
    		move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        // Heatproof, Thick Fat
        if(defenderAbility == Ability.HEATPROOF && moveType == Type.FIRE
        || defenderAbility == Ability.THICK_FAT && (moveType == Type.FIRE || moveType == Type.ICE)
        ) {
        	movePower /= 2; // TODO: hardcoded
    		move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        }
        
        // Dry Skin
        if(defenderAbility == Ability.DRY_SKIN && moveType == Type.FIRE) {
        	movePower = movePower * (100 + 25) / 100; // TODO: hardcoded
    		move.setName(String.format("%s +y%s",move.getName(), defenderAbility)); // TODO: find better way
        }
        
        
        // Updating move (useful for displaying move names properly through side effects)
        move.setType(moveType);
        move.setPower(movePower);
        
        
        /*
         * ATTACK / SPECIAL ATTACK
         */
        
        boolean isPhysical = moveClass == MoveClass.PHYSICAL;
        Stat attackerStat = isPhysical ? Stat.ATK : Stat.SPA;
        boolean isAttackerSimple = attackerAbility == Ability.SIMPLE;
        int attack;

        int rawAttack = isPhysical ? attacker.getStatValue(Stat.ATK) : attacker.getStatValue(Stat.SPA);
        int	attackBoost = isPhysical ? atkMod.getStage(Stat.ATK) : atkMod.getStage(Stat.SPA);
        
        // Attacker stat stages 
        if (attackBoost == 0 || (isCrit && attackBoost < 0))
            attack = rawAttack;
        else if (defenderAbility == Ability.UNAWARE) {
            attack = rawAttack;
    		move.setName(String.format("%s %sy%s",
    				move.getName(), attackBoost > 0 ? "-" : "+", defenderAbility)); // TODO: find better way
        }
        else {
            attack = atkMod.modStat(attackerStat, rawAttack, isAttackerSimple);
            if(isAttackerSimple && attackBoost != 0)
            	move.setName(String.format("%s %sx%s",move.getName(), attackBoost > 0 ? "+" : "-",attackerAbility)); // TODO: find better way
        }
        
        // Attacker ability
        if (isPhysical && (attackerAbility == Ability.PURE_POWER || attackerAbility == Ability.HUGE_POWER)) {
            attack *= 2;
        	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        } else if (weather == Weather.SUN
        && attackerAbility == (isPhysical ? Ability.FLOWER_GIFT : Ability.SOLAR_POWER)) {
        	attack = attack * (100 + 50) / 100; // TODO: hardcoded
        	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        } else if (atkMod.hasStatus2_3(Status.FLOWER_GIFT) && weather == Weather.SUN && isPhysical) {
        	attack = attack * (100 + 50) / 100; // TODO: hardcoded
        	move.setName(String.format("%s +%s",move.getName(), Status.FLOWER_GIFT)); // TODO: find better way
        } else if (isPhysical && (attackerAbility == Ability.HUSTLE 
        	                      || (attackerAbility == Ability.GUTS && atkMod.getStatus1() != Status.NONE))
        ) {
        	attack = attack * (100 + 50) / 100; // TODO: hardcoded
        	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        } else if (isPhysical && attackerAbility == Ability.SLOW_START) { // TODO : test if ability is activated ?
        	attack /= 2;
        	move.setName(String.format("%s -%s",move.getName(), attackerAbility)); // TODO: find better way
        }        	
        // TODO : Plus, Minus
        
        // Attacker item
        if(attackerItem != null) {
        	ItemHoldEffect attackerItemEffect = attackerItem.getHoldEffect();
        	
	        if((isPhysical ? attackerItemEffect== ItemHoldEffect.CHOICE_ATK // Choice Band
	        		       : attackerItemEffect == ItemHoldEffect.CHOICE_SPATK)  // Choice Specs
	        || !isPhysical && attackerItemEffect == ItemHoldEffect.LATI_SPECIAL // Soul Dew
	                       && (attacker.getSpecies() == Species.getSpeciesByName("LATIOS") || attacker.getSpecies() == Species.getSpeciesByName("LATIAS"))
	        ) {
	        	attack = attack * (100 + 50) / 100; // TODO: hardcoded
	        	move.setName(String.format("%s +%s",move.getName(), attackerItem)); // TODO: find better way
	        } else if(attackerItemEffect == ItemHoldEffect.PIKA_SPATK_UP && attacker.getSpecies() == Species.getSpeciesByName("PIKACHU") // Light Ball
	               || isPhysical && attackerItemEffect == ItemHoldEffect.CUBONE_ATK_UP &&  // Thick Club
	                  (attacker.getSpecies() == Species.getSpeciesByName("CUBONE") || attacker.getSpecies() == Species.getSpeciesByName("MAROWAK"))
	               || attackerItemEffect == ItemHoldEffect.CLAMPERL_SPATK && attacker.getSpecies() == Species.getSpeciesByName("CLAMPERL") // Deep Sea Tooth
	               && !isPhysical
	        ) {
	        	attack *= 2;
	        	move.setName(String.format("%s +%s",move.getName(), attackerItem)); // TODO: find better way
	        }
        }
        
        /*
         * DEFENSE / SPECIAL DEFENSE
         */
        
        Stat defenderStat = isPhysical ? Stat.DEF : Stat.SPD;
        boolean isDefenderSimple = defenderAbility == Ability.SIMPLE;
        int defense;

        int rawDefense = isPhysical ? defender.getStatValue(Stat.DEF) : defender.getStatValue(Stat.SPD);
        int defenseBoost = isPhysical ? defMod.getStage(Stat.DEF) : defMod.getStage(Stat.SPD);
        
        // Defender stat stages
        if (defenseBoost == 0 || (isCrit && defenseBoost > 0))
        	defense = rawDefense;
        else if (attackerAbility == Ability.UNAWARE) {
        	defense = rawDefense;
    		move.setName(String.format("%s %sx%s",move.getName(), defenseBoost < 0 ? "-" : "+", attackerAbility)); // TODO: find better way
        } else {
        	defense = defMod.modStat(defenderStat, rawDefense, isDefenderSimple);
        	if(isDefenderSimple && defenseBoost != 0)
        		move.setName(String.format("%s %sy%s",move.getName(), defenseBoost > 0 ? "-" : "+", defenderAbility)); // TODO: find better way
        }
        
        // Defender ability
        if (defenderAbility == Ability.MARVEL_SCALE && defMod.getStatus1() != Status.NONE && isPhysical) {
            defense = defense * (100 + 50) / 100;
        	move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        } else if (defenderAbility == Ability.FLOWER_GIFT && weather == Weather.SUN && !isPhysical) {
        	defense = defense * (100 + 50) / 100;
        	move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        } else if (defMod.hasStatus2_3(Status.FLOWER_GIFT) && weather == Weather.SUN && !isPhysical) {
        	defense = defense * (100 + 50) / 100;
        	move.setName(String.format("%s -%s",move.getName(), Status.FLOWER_GIFT)); // TODO: find better way
        }
        
        // Defender item
        if(defenderItem != null) {
        	ItemHoldEffect defenderItemEffect = defenderItem.getHoldEffect();
        	
	        if (defenderItemEffect == ItemHoldEffect.LATI_SPECIAL && !isPhysical
	            && (defender.getSpecies() == Species.getSpeciesByName("LATIOS") || defender.getSpecies() == Species.getSpeciesByName("LATIAS"))
	        ) {
	        	defense = defense * (100 + 50) / 100;
	        	move.setName(String.format("%s -%s",move.getName(), defenderItem)); // TODO: find better way
	        } else if (defenderItemEffect == ItemHoldEffect.CLAMPERL_SPDEF && !isPhysical  // Deep Sea Scale
	        		   && defender.getSpecies() == Species.getSpeciesByName("CLAMPERL")
	                || defenderItemEffect == ItemHoldEffect.DITTO_DEF_UP && isPhysical // Metal Powder
	                   && defender.getSpecies() == Species.getSpeciesByName("DITTO")
	        ) {
	            defense *= 2;
	        	move.setName(String.format("%s -%s",move.getName(), defenderItem)); // TODO: find better way
	        }
        }

        // Sandstorm special defense
        if (weather == Weather.SANDSTORM && (defenderType1 == Type.ROCK || defenderType2 == Type.ROCK) && !isPhysical) {
        	defense = defense * (100 + 50) / 100;
        	move.setName(String.format("%s -%s",move.getName(), weather)); // TODO: find better way
        }

        if (moveEffect == MoveEffect.EXPLOSION) { // Self Destruct, Explosion
            defense /= 2;
        }

        if (defense < 1) {
            defense = 1;
        }
        
        
        /*
         * DAMAGE
         */
        
        int baseDamage = ((2 * attacker.getLevel()) / 5 + 2) * movePower * attack / 50 / defense;
        
        // Burn
        if (atkMod.getStatus1() == Status.BURN && isPhysical && attackerAbility != Ability.GUTS) {
        	baseDamage /= 2;
        	move.setName(String.format("%s -x%s",move.getName(), Status.BURN)); // TODO: find better way
        }

        // Screens
        if (!isCrit) {
            int screenNumerator = isDoubleBattle ? 2 : 1;
            int screenDenominator = isDoubleBattle ? 3 : 2;
            if (isPhysical && defMod.hasStatus2_3(Status.REFLECT)) {
            	baseDamage = baseDamage * screenNumerator / screenDenominator;
            	move.setName(String.format("%s -%s",move.getName(), Status.REFLECT)); // TODO: find better way
            } else if (!isPhysical && defMod.hasStatus2_3(Status.LIGHTSCREEN)) {
                baseDamage = baseDamage * screenNumerator / screenDenominator;
            	move.setName(String.format("%s -%s",move.getName(), Status.LIGHTSCREEN)); // TODO: find better way
            }
        }
        
        // Move hitting several targets
		// In 4G : BOTH_ENEMIES avec 2 target = 75%, 
		//         BOTH_ENEMIES avec 1 target = 100%, 
		//         ALL_EXCEPT_USER avec 2+ target = 75%,
		//         ALL_EXCEPT_USER avec 1 target = 100%,
        if (isDoubleBattle && (move.getMoveTarget() == MoveTarget.BOTH_ENEMIES || move.getMoveTarget() == MoveTarget.ALL_EXCEPT_USER)) {
        	baseDamage = baseDamage * 3 / 4;
        	move.setName(String.format("%s -%s",
        			move.getName(), 
        			move.getMoveTarget() == MoveTarget.BOTH_ENEMIES ? "2ENEMIES" : "2OR3TARGETS")); // TODO: find better way
        }
        
        // Weather
        if (weather == Weather.SUN && moveType == Type.FIRE
        ||  weather == Weather.RAIN && moveType == Type.WATER
        ) {
        	baseDamage = baseDamage * (100 + 50) / 100;
        	move.setName(String.format("%s +%s",move.getName(), weather)); // TODO: find better way
        } else if(weather == Weather.SUN && moveType == Type.WATER
        	   || weather == Weather.RAIN && moveType == Type.FIRE
        	   || (weather == Weather.RAIN || weather == Weather.SANDSTORM || weather == Weather.HAIL) && move.matchesAny("SOLAR BEAM")
        ) {
        	baseDamage /= 2;
        	move.setName(String.format("%s -%s",move.getName(), weather)); // TODO: find better way
        }
        
        // Flash Fire
        if (attackerAbility == Ability.FLASH_FIRE && atkMod.hasStatus2_3(Status.FLASH_FIRE) && moveType == Type.FIRE) {
        	baseDamage = baseDamage * (100 + 50) / 100;
        	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        baseDamage += 2;
        
        // Crit
        if (isCrit) {
            if (attackerAbility == Ability.SNIPER) {
            	baseDamage *= 3;
            	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
            } else {
            	baseDamage *= 2;
            }
        }

        // Life Orb
        if (attackerItem != null && attackerItem.getHoldEffect() == ItemHoldEffect.HP_DRAIN_ON_ATK) { // Life Orb
            baseDamage = baseDamage * (100 + 30) / 100;
        	move.setName(String.format("%s +%s",move.getName(), attackerItem)); // TODO: find better way
        }
        
        // Pursuit
        if (move.matchesAny("Pursuit") && defMod.hasStatus2_3(Status.SWITCHING_OUT)) {
            // technician negates switching boost, thanks DaWoblefet
            if (attackerAbility != Ability.TECHNICIAN) {
            	baseDamage *= 2;
            	move.setName(String.format("%s +%s",move.getName(), Status.SWITCHING_OUT)); // TODO: find better way
            }
        }
        
        int stabMod = 2;
        int stabModDenom = 2;
        if (moveType == attackerType1 || moveType == attackerType2) {
        	if (attackerAbility == Ability.ADAPTABILITY){
        		stabMod = 4;
            	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        	} else {
        		stabMod = 3;
        	}
        }

        int filterMod = 4;
        int filterModDenominator = 4;
        if ((defenderAbility == Ability.FILTER || defenderAbility == Ability.SOLID_ROCK) && isSuperEffective) {
        	filterMod = 3;
        	move.setName(String.format("%s -%s",move.getName(), defenderAbility)); // TODO: find better way
        }
        
        int ebeltMod = 10;
        int ebeltModDenominator = 10;
        if (attackerItem != null && attackerItem.getHoldEffect() == ItemHoldEffect.POWER_UP_SE && isSuperEffective) { // Expert Belt
        	ebeltMod = 12;
        	move.setName(String.format("%s +%s",move.getName(), attackerItem)); // TODO: find better way
        }
        
        int tintedMod = 1;
        if (attackerAbility == Ability.TINTED_LENS && isNotVeryEffective) {
        	tintedMod = 2;
        	move.setName(String.format("%s +%s",move.getName(), attackerAbility)); // TODO: find better way
        }
        
        int berryMod = 2;
        int berryModDenominator = 2;
        if (defenderItem != null) {
        	ItemHoldEffect defenderItemEffect = defenderItem.getHoldEffect();
        	if(moveType == Type.NORMAL   && defenderItemEffect== ItemHoldEffect.WEAKEN_NORMAL
	        || moveType == Type.BUG      && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_BUG
	        || moveType == Type.DARK     && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_DARK
	        || moveType == Type.DRAGON   && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_DRAGON
	        || moveType == Type.ELECTRIC && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_ELECTRIC
	        || moveType == Type.FIGHTING && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_FIGHT
	        || moveType == Type.FIRE     && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_FIRE
	        || moveType == Type.FLYING   && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_FLYING
	        || moveType == Type.GHOST    && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_GHOST
	        || moveType == Type.GRASS    && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_GRASS
	        || moveType == Type.GROUND   && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_GROUND
	        || moveType == Type.ICE      && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_ICE
	        || moveType == Type.POISON   && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_POISON
	        || moveType == Type.PSYCHIC  && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_PSYCHIC
	        || moveType == Type.ROCK     && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_ROCK
	        || moveType == Type.STEEL    && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_STEEL
	        || moveType == Type.WATER    && defenderItemEffect== ItemHoldEffect.WEAKEN_SE_WATER
        	) {
        	berryMod = 1;
        	move.setName(String.format("%s -%s",move.getName(), defenderItem)); // TODO: find better way
        	}
        }
        
        int damage = baseDamage * roll / 100;
        damage = damage * stabMod / stabModDenom;
        damage = Type.applyTypeEffectiveness(damage, moveType, Type.getType1ByPrecedence(defenderType1, defenderType2), isGhostRevealed, isGrounded);
        damage = Type.applyTypeEffectiveness(damage, moveType, Type.getType2ByPrecedence(defenderType1, defenderType2), isGhostRevealed, isGrounded);
        damage = damage * filterMod / filterModDenominator;
        damage = damage * ebeltMod / ebeltModDenominator;
        damage = damage * tintedMod;
        damage = damage * berryMod / berryModDenominator;
        damage = (int)Math.max(1, damage);
        
        return damage;
    }

    
    // ********************* //
    // HELPER DAMAGE METHODS //
    // ********************* //
    
    /*
    public static int minDamage(Move attack, Pokemon attacker,
                                Pokemon defender, StatModifier atkMod, StatModifier defMod,
                                int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
        return damage(attack, attacker, defender, atkMod, defMod, MIN_ROLL,
                false, extra_multiplier, isBattleTower, isDoubleBattle);
    }
    */

    /*
    public static int maxDamage(Move attack, Pokemon attacker,
                                Pokemon defender, StatModifier atkMod, StatModifier defMod,
                                int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
        return damage(attack, attacker, defender, atkMod, defMod, MAX_ROLL,
                false, extra_multiplier, isBattleTower, isDoubleBattle);
    }
    */

    /*
    public static int minCritDamage(Move attack, Pokemon attacker,
                                    Pokemon defender, StatModifier atkMod, StatModifier defMod,
                                    int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
        return damage(attack, attacker, defender, atkMod, defMod, MIN_ROLL,
                true, extra_multiplier, isBattleTower, isDoubleBattle);
    }
    */

    /*
    public static int maxCritDamage(Move attack, Pokemon attacker,
                                    Pokemon defender, StatModifier atkMod, StatModifier defMod,
                                    int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
        return damage(attack, attacker, defender, atkMod, defMod, MAX_ROLL,
                true, extra_multiplier, isBattleTower, isDoubleBattle);
    }
    */
    
    /*
    public static int minPsywaveDamage(Move attack, Pokemon attacker,
	            Pokemon defender, StatModifier atkMod, StatModifier defMod,
	            int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
    	return damage(attack, attacker, defender, atkMod, defMod, MIN_PSYWAVE_ROLL,
    			false, extra_multiplier, isBattleTower, isDoubleBattle);
	}
	*/
	
    /*
	public static int maxPsywaveDamage(Move attack, Pokemon attacker,
	            Pokemon defender, StatModifier atkMod, StatModifier defMod,
	            int extra_multiplier, boolean isBattleTower, boolean isDoubleBattle) {
		return damage(attack, attacker, defender, atkMod, defMod, MAX_PSYWAVE_ROLL,
				false, extra_multiplier, isBattleTower, isDoubleBattle);
	}
	*/
    
    
    
    // ******************************** //
    // HELPER STRING FORMATTING METHODS //
    // ******************************** //
    
    public static void appendBattleIntroSummary(StringBuilder sb, Pokemon p1, Pokemon p2, BattleOptions options) throws ToolInternalException {
        sb.append(String.format("%s vs %s", p1.levelNameNatureAbilityItemStr(), p2.levelNameNatureAbilityItemStr()));

        // Don't show exp for tower pokes (minor thing since exp isn't added anyway)
        if(options.isBattleTower())
        	return;
        
    	boolean isItemBoosted = p1.getHeldItem() != null && p1.getHeldItem().isBoostingExperience();
    	boolean isTradeBoosted = p1.hasBoostedExp();
    	String sep = isItemBoosted && isTradeBoosted ? "+" : "";
    	String startBracket = isItemBoosted || isTradeBoosted ? " (" : "";
    	String endBracket = isItemBoosted || isTradeBoosted ? ")" : "";
    	String itemStr = String.format("%s%s%s%s%s", startBracket, isItemBoosted ? p1.getHeldItem().getDisplayName() : "",sep, isTradeBoosted ? "TRADE" : "", endBracket);
    	//int expGiven = options.getNumberOfParticipants() == 0 ? 0 : p2.expGivenWithoutEXPBoost(options.getNumberOfParticipants());
    	//expGiven = (expGiven * 3) / (isTradeBoosted ? 2 : 3); // TODO : duplicate code from the Pokemon.gainExp method
    	//expGiven = (expGiven * 3) / (isItemBoosted ? 2 : 3);
    	//int expGiven = options.getNumberOfParticipants() == 0 ? 0 : p1.earnedExpFrom(p2, options.getNumberOfParticipants());
    	/*
        sb.append(String.format("          >>> EXP GIVEN: %d%s%s", 
        		expGiven, 
        		options.getNumberOfParticipants() > 1 ? String.format(" (split in %d)", options.getNumberOfParticipants()) : "",
        		expGiven == 0 ? "" : options.isPostponedExp() ? " (POSTPONED)" : itemStr));
		*/
    	int expGiven = p1.earnedExpFrom(p2, options.getCurrentNumberOfParticipants());
    	//if(options.isCurrentPostponedExp())
    	//	expGiven = 0;
    	
        sb.append(String.format("          >>> EXP GIVEN: %d%s%s", 
        		expGiven, 
        		options.getCurrentNumberOfParticipants() > 1 ? String.format(" (was split in %d)", options.getCurrentNumberOfParticipants()) : "",
        		expGiven == 0 ? "" : options.isCurrentPostponedExp() ? " (POSTPONED)" : itemStr));

    }
    
    public static void appendPokemonSummary(StringBuilder sb, Pokemon p, StatModifier mod) {
    	sb.append(String.format("%s (%s) ", p.getDisplayName(), p.getInlineTrueStatsStr()));
        if (mod.hasMods() || p.hasBadgeBoost())
            sb.append(String.format("%s ", mod.summary(p)));
        if (p.getHeldItem() != null)
        	sb.append(String.format("<%s> ", p.getHeldItem().getDisplayName()));
        if (mod.getWeather() != Weather.NONE)
        	sb.append(String.format("~%s~ ", mod.getWeather()));
    }
    
    public static void appendFormattedMoveName(StringBuilder sb, Move move) {
    	sb.append(move.getName());
    }
    /*
    public static void appendFormattedMoveName(StringBuilder sb, Move move, Pokemon p1,
            Pokemon p2, StatModifier mod1, StatModifier mod2,
            int _extra_multiplier, boolean isBattleTower, boolean isDoubleBattle, Object param) {
    	Move m = new Move(move);
    	
        // Move name
    	switch(m.getEffect()) {
    	//case RAGE:
    	case FURY_CUTTER:
    	case ROLLOUT:
            sb.append(m.getBoostedName(_extra_multiplier));
            break;
            
    	case HIDDEN_POWER:
    		Type type = p1.getIVs().getHiddenPowerType();
	        int power = p1.getIVs().getHiddenPowerPower();
	        m.setType(type);
	        m.setPower(power);
	        sb.append(String.format("%s [%s %d]", m.getName(), type, power)); // TODO : hardcoded
	        break;
	        
    	case MAGNITUDE:
    		power = m.getPower();
    		int magnitude = -1;
    		int percent = 0;
    		
    		if (power == 10) {//TODO : hardcoded
    			magnitude = 4;
    			percent = 5;
    		} else if (power == 30) {
    			magnitude = 5;
    			percent = 10;
    		} else if (power == 50) {
    			magnitude = 6;
    			percent = 20;
    		} else if (power == 70) {
    			magnitude = 7;
    			percent = 30;
    		} else if (power == 90) {
    			magnitude = 8;
    			percent = 20;
    		} else if (power == 110) {
    			magnitude = 9;
    			percent = 10;
    		} else {
    			magnitude = 10;
    			percent = 5;
    		}
    		sb.append(String.format("%s %d (%d%%)", m.getName(), magnitude, percent)); // TODO : hardcoded
	        break;
	        
    	case FLAIL:
    		int minScale, maxScale;
        	power = m.getPower();
        	//System.out.println(power);
        	if (power == 20) { //TODO: hardcoded
        		minScale = 33;
        		maxScale = 48;
        	} else if (power == 40) {
        		minScale = 17;
        		maxScale = 32;
        	} else if (power == 80) {
        		minScale = 10;
        		maxScale = 16;
        	} else if (power == 100) {
        		minScale = 5;
        		maxScale = 9;
        	} else if (power == 150) {
        		minScale = 2;
        		maxScale = 4;
        	} else {
        		minScale = 0;
        		maxScale = 1;
        	}
        	
        	int fullHP = p1.getHP();
        	int minHP = 0;
        	int maxHP = fullHP;
        	
        	for(int hp = 1; hp <= fullHP; hp++) {
        		int scale = hp * 48 / fullHP;
        		if(scale == minScale && minHP == 0)
        			minHP = hp;
        		if(scale > maxScale) {
        			maxHP = hp - 1;
        			break;
        		}
        	}
    		sb.append(String.format("%s %d (HP:%d-%d)", m.getName(), power, minHP, maxHP)); // TODO : hardcoded
    		break;
    		
    	case PRESENT:
    		power = m.getPower();
    		percent = -1;
    		if(power == 40) //TODO: hardcoded
    			percent = 102;
    		else if (power == 80)
    			percent = 178 - 102;
    		else // 120
    			percent = 204 - 178;
    		sb.append(String.format("%s %d (%d%%)", m.getName(), power, 100 * percent / 256)); // TODO : hardcoded
    	
    	case PURSUIT:
    		boolean isSwitchOut = mod2.hasStatus2_3(Status.SWITCHING_OUT);
    		sb.append(String.format("%s %s", m.getName(), isSwitchOut ? "SWITCH" : "")); // TODO : hardcoded
    		break;
    		
    	default:
    		sb.append(m.getName());
    		break;
    	}

        // Various modifiers
		if(m.getPower() > 1) { //TODO : hardcoded value
			Item attackerItem = p1.getHeldItem();
			Item defenderItem = p2.getHeldItem();
			boolean hasAttackerItem = attackerItem != null;
			boolean hasDefenderItem = defenderItem != null;
			
			String underwaterBonusStr = " +UW"; //TODO : hardcoded
			String itemBonusStr = " +ITEM"; // TODO: hardcoded
			String itemMalusStr = " -ITEM"; // TODO: hardcoded
			String mudSportMalusStr = " -" + Status.MUDSPORT;
			String waterSpoutMalusStr = " -" + Status.WATERSPOUT;
			
			if (mod1.hasMods() || p1.hasBadgeBoost()) {
				if (m.isPhysical()) {
					if (p1.hasAtkBadge())
						sb.append("*");
					if (mod1.getAtkStage() != 0)
						sb.append(String.format(" @%+d", mod1.getAtkStage()));
				} else if (m.isSpecial()) {
					if (p1.hasSpaBadge())
						sb.append("*");
					if (mod1.getSpaStage() != 0)
						sb.append(String.format(" @%+d", mod1.getSpaStage()));
				}
			}
			
			
	        if (m.getName().equalsIgnoreCase("SURF") && mod2.hasStatus2_3(Status.UNDERWATER)) // TODO : hardcoded
	        	sb.append(underwaterBonusStr);
			
			
			// Type boosting items
	        if (hasAttackerItem) {
	        	ItemHoldEffect attackerItemHoldEffect = attackerItem.getHoldEffect();
	        	Type moveType = m.getType();
	        	if(attackerItemHoldEffect.isTypeBoosting(moveType))
	        		sb.append(itemBonusStr);
	        }
	        
	        // Choice Band
	        if (hasAttackerItem && m.isPhysical() && attackerItem.getHoldEffect() == ItemHoldEffect.CHOICE_BAND)
        		sb.append(itemBonusStr);
	        
	        // Attacker Soul Dew
	        if (hasAttackerItem && m.isSpecial() && attackerItem.getHoldEffect() == ItemHoldEffect.SOUL_DEW && !isBattleTower
	        		&& (p1.getSpecies().getHashName().equalsIgnoreCase("LATIOS") || p1.getSpecies().getHashName().equalsIgnoreCase("LATIAS"))) // TODO: hardcoded
        		sb.append(itemBonusStr);
	        
	        // Defender Soul Dew
	        if (hasDefenderItem && m.isSpecial() && defenderItem.getHoldEffect() == ItemHoldEffect.SOUL_DEW && !isBattleTower
	        		&& (p2.getSpecies().getHashName().equalsIgnoreCase("LATIOS") || p2.getSpecies().getHashName().equalsIgnoreCase("LATIAS"))) // TODO: hardcoded
        		sb.append(itemMalusStr);
	        
	        // Attacker Deep Sea Tooth
	        if (hasAttackerItem && m.isSpecial() && attackerItem.getHoldEffect() == ItemHoldEffect.DEEP_SEA_TOOTH
	        		&& p1.getSpecies().getHashName().equalsIgnoreCase("CLAMPERL")) // TODO: hardcoded
        		sb.append(itemBonusStr);
	        
	        // Defender Deep Sea Scale
	        if (hasDefenderItem && m.isSpecial() && defenderItem.getHoldEffect() == ItemHoldEffect.DEEP_SEA_SCALE
	        		&& p2.getSpecies().getHashName().equalsIgnoreCase("CLAMPERL")) // TODO: hardcoded
        		sb.append(itemMalusStr);
	        
	        // Attacker Light Ball
	        if (hasAttackerItem && m.isSpecial() && attackerItem.getHoldEffect() == ItemHoldEffect.LIGHT_BALL
	        		&& p1.getSpecies().getHashName().equalsIgnoreCase("PIKACHU")) // TODO: hardcoded
        		sb.append(itemBonusStr);
	        
	        // Defender Metal Powder
	        if (hasDefenderItem && m.isPhysical() && defenderItem.getHoldEffect() == ItemHoldEffect.METAL_POWDER
	        		&& p2.getSpecies().getHashName().equalsIgnoreCase("DITTO")) // TODO: hardcoded
        		sb.append(itemMalusStr);
	        
	        // Attacker Thick Club
	        if (hasAttackerItem && m.isPhysical() && attackerItem.getHoldEffect() == ItemHoldEffect.THICK_CLUB
	        		&& (p1.getSpecies().getHashName().equalsIgnoreCase("CUBONE") || p1.getSpecies().getHashName().equalsIgnoreCase("MAROWAK"))) // TODO: hardcoded
        		sb.append(itemBonusStr);
	        
	        // Defender Thick Fat
	        if(p2.getAbility() == Ability.THICK_FAT 
	        		&& (m.getType() == Type.FIRE || m.getType() == Type.ICE))
	        	sb.append(" -" + p2.getAbility()); // TODO: hardcoded
	        
	        // Attacker Hustle
	        if(m.isPhysical() && p1.getAbility() == Ability.HUSTLE)
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        
	        // TODO: Plus & Minus
	        
	        // Attacker Guts
	        if(m.isPhysical() && p1.getAbility() == Ability.GUTS && mod1.getStatus1() != Status.NONE)
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        
	        // Defender Marvel Scale
	        if(m.isSpecial() && p2.getAbility() == Ability.MARVEL_SCALE && mod2.getStatus1() != Status.NONE)
	        	sb.append(" -" + p2.getAbility()); // TODO: hardcoded
	        
	        
	        // Attacker Overgrow, Blaze, Torrent, Swarm
	        if(m.getType() == Type.GRASS && p1.getAbility() == Ability.OVERGROW && mod1.isHPHalfOrLess(p1.getHP()))
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        if(m.getType() == Type.FIRE && p1.getAbility() == Ability.BLAZE && mod1.isHPHalfOrLess(p1.getHP()))
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        if(m.getType() == Type.WATER && p1.getAbility() == Ability.TORRENT && mod1.isHPHalfOrLess(p1.getHP()))
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        if(m.getType() == Type.BUG && p1.getAbility() == Ability.SWARM && mod1.isHPHalfOrLess(p1.getHP()))
	        	sb.append(" +" + p1.getAbility()); // TODO: hardcoded
	        
	        
	        // Field modifiers
	        
	        // Attacker Mud Sport
	        if(m.getType() == Type.ELECTRIC && mod1.hasStatus2_3(Status.MUDSPORT))
        		sb.append(mudSportMalusStr);
	        
	        // Attacker Water Spout
	        if(m.getType() == Type.FIRE && mod1.hasStatus2_3(Status.WATERSPOUT))
        		sb.append(waterSpoutMalusStr);
	     
	        if(m.isPhysical() && mod1.getStatus1() == Status.BURN && p1.getAbility() != Ability.GUTS)
	        	sb.append(" -" + Status.BURN); // TODO: hardcoded
	        
	        // Double target in double battle
	        if(m.getMoveTarget() == MoveTarget.BOTH_ENEMIES && isDoubleBattle)
	        	sb.append(" -DOUBLE"); //TODO : hardcoded
	        
			// Weather
	        if (mod1.getWeather() != Weather.NONE) {
	        	if(!(p1.getAbility() == Ability.CLOUD_NINE || p1.getAbility() == Ability.AIR_LOCK
	        			|| p2.getAbility() == Ability.CLOUD_NINE || p2.getAbility() == Ability.AIR_LOCK)) {
	
	        		// Rainy
	        		if(mod1.getWeather() == Weather.RAIN) {
	        			if(m.getType() == Type.FIRE)
	        				sb.append(String.format(" -%s", mod1.getWeather()));
	        			else if (m.getType() == Type.WATER)
	        				sb.append(String.format(" +%s", mod1.getWeather()));
	        		}
	        		
	        		// Any weather except sun weakens solar beam
	        		if(m.getEffect() == MoveEffect.SOLARBEAM 
	        				&& !(mod1.getWeather() == Weather.NONE || mod1.getWeather() == Weather.SUN))
	        			sb.append(String.format(" -%s", mod1.getWeather()));
	        		
	        		// Sunny
	        		if(mod1.getWeather() == Weather.SUN) {
	        			if(m.getType() == Type.FIRE)
	        				sb.append(String.format(" +%s", mod1.getWeather()));
	        			else if (m.getType() == Type.WATER)
	        				sb.append(String.format(" -%s", mod1.getWeather()));
	        		}
	        	}
	        }
        } 
        if (m.isPhysical() && mod2.hasStatus2_3(Status.REFLECT)) {
        	sb.append(" -R");
        } else if (!m.isPhysical() && mod2.hasStatus2_3(Status.LIGHTSCREEN)) {
        	sb.append(" -LS");
        }
    }
    */
    
    
    // *********************** //
    // MAIN FORMATTING METHODS //
    // *********************** //
    
    // printout of move damages between the two pokemon
    // assumes you are p1
    public static String battleSummary(Pokemon p1, Pokemon p2, BattleOptions options) throws UnsupportedOperationException, ToolInternalException {
        StringBuilder sb = new StringBuilder();
        String endl = Constants.endl;
        StatModifier mod1 = options.getStatModifier(Side.PLAYER);
        StatModifier mod2 = options.getStatModifier(Side.ENEMY);

        appendBattleIntroSummary(sb, p1, p2, options);
        sb.append(endl);
        
        // Player side
        appendPokemonSummary(sb, p1, mod1);
        sb.append(endl);

        appendMainDamagesSummary(sb, p1, p2, mod1, mod2, options.isBattleTower(), options.isDoubleBattleSplittingDamage(), options.getVerbose());
        sb.append(endl);
        
        
        // Opponent side
        appendPokemonSummary(sb, p2, mod2);
        sb.append(endl);

        appendMainDamagesSummary(sb, p2, p1, mod2, mod1, options.isBattleTower(), options.isDoubleBattleSplittingDamage(), options.getVerbose());
        sb.append(endl);

        // Speed information
        appendSpeedInfo(sb, p1, p2, mod1, mod2);
        
        return sb.toString();
    }
    
    // used for the less verbose option
    /*
    public static String shortBattleSummary(Pokemon p1, Pokemon p2, BattleOptions options) {
        StringBuilder sb = new StringBuilder();
        String endl = Constants.endl;

        StatModifier mod1 = options.getMod1();
        StatModifier mod2 = options.getMod2();

        appendBattleIntroSummary(sb, p1, p2, options);
        sb.append(endl);
        
        appendPokemonSummary(sb, p1, mod1);
        sb.append(endl);

        appendMainDamagesSummary(sb, p1, p2, mod1, mod2, options.isBattleTower(), options.isDoubleBattle());
        sb.append(endl);
        
        appendPokemonSummary(sb, p2, mod2);
        sb.append(endl);

        sb.append(p2.getMoveset().toString());
        sb.append(endl);
        
        return sb.toString();
    }
    */

       
    
    // ******************************** //
    // HELPER DAMAGE FORMATTING METHODS //
    // ******************************** //
    
    /*
    public static void appendVerboseDamagesSummary(StringBuilder sb, Pokemon p1, Pokemon p2, BattleOptions options) {
    	damagesSummaryCore(sb, p1, p2, options.getMod1(), options.getMod2(), options.isBattleTower(), options.isDoubleBattle(), true);
    }
    */
    public static void appendMainDamagesSummary(StringBuilder sb, Pokemon p1, Pokemon p2, StatModifier mod1, StatModifier mod2, 
    		boolean isBattleTower, boolean isDoubleBattle, VerboseLevel verboseLevel) throws UnsupportedOperationException, ToolInternalException {
    	damagesSummaryCore(sb, p1, p2, mod1, mod2, isBattleTower, isDoubleBattle, verboseLevel);
    }
    
    //TODO: fuzzy, hacky, idk ... but at least the logic stays within a single method
    private static void damagesSummaryCore(StringBuilder sb, Pokemon p1, Pokemon p2, StatModifier mod1, StatModifier mod2, 
    		boolean isBattleTower, boolean isDoubleBattle, VerboseLevel verboseLevel) throws UnsupportedOperationException, ToolInternalException {
    	String endl = Constants.endl;
    	
    	// First, append attacker move calcs
        for (Move move : p1.getMoveset()) {
        	int maxDamage = 0; 
        	int initialMovePower = move.getPower();
        	String initialName = move.getName();
        	Move moveCopy = new Move(move); // copy
        	
        	switch (moveCopy.getEffect()) {
        	case FURY_CUTTER:
        		for (int _extra_multiplier : new Integer[] {0, 1, 2, 3, 4}) { //TODO: hardcoded
        			moveCopy.setName(initialName);
        			moveCopy.appendName(_extra_multiplier + 1);
        			moveCopy.setPower(initialMovePower * (1 << _extra_multiplier));
        			maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
                }
        		break;
        		
        	case ROLLOUT: // Gen 3 Rollout, Ice Ball
        	case INCREASING_HIT: // Gen 4 Rollout, Ice Ball
        		for (int _extra_multiplier : new Integer[] {0, 1, 2, 3, 4}) { //TODO: hardcoded
        			moveCopy.setName(initialName);
        			int newPower = initialMovePower * (1 << _extra_multiplier);
        			moveCopy.appendName(_extra_multiplier + 1);
        			if(mod1.hasStatus2_3(Status.DEFENSE_CURL)) {
        				newPower *= 2;
            			moveCopy.appendName(String.format("+%s", Status.DEFENSE_CURL));
        			}
        			moveCopy.setPower(newPower);
        			maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
                }
        		break;
        		
        	case MAGNITUDE:
                for (int power : new Integer[] {10, 30, 50, 70, 90, 110, 150}) { //TODO: hardcoded
                	int percent = power ==  10 ?  5 :
                				  power ==  30 ? 10 :
                				  power ==  50 ? 20 :
        			              power ==  70 ? 30 :
        		                  power ==  90 ? 20 : 
        			              power == 110 ? 10 : 5;
        			moveCopy.setName(String.format("%s %s (%d%%)", initialName, power, percent));
                    moveCopy.setPower(power);
                    maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
                }
                break;
                
        	case FLAIL: // Gen 3 Flail, Reversal
            	int oldHP = mod1.getCurrHP();
        		for(int power : new Integer[]{20, 40, 80, 100, 150, 200}) { //TODO: hardcoded
            		int minScale, maxScale;
                	if (power == 20) { //TODO: hardcoded
                		maxScale = 48;
                		minScale = 33;
                	} else if (power == 40) {
                		maxScale = 32;
                		minScale = 17;
                	} else if (power == 80) {
                		maxScale = 16;
                		minScale = 10;
                	} else if (power == 100) {
                		maxScale =  9;
                		minScale =  5;
                	} else if (power == 150) {
                		maxScale =  4;
                		minScale =  2;
                	} else {
                		maxScale =  1;
                		minScale =  0;
                	}
                	
                	int fullHP = p1.getStatValue(Stat.HP);
                	int minHP = 0;
                	int maxHP = fullHP;
                	
                	for(int hp = 1; hp <= fullHP; hp++) {
                		int scale = hp * 48 / fullHP;
                		if(scale == minScale && minHP == 0)
                			minHP = hp;
                		if(scale > maxScale) {
                			maxHP = hp - 1;
                			break;
                		}
                	}
                	mod1.setCurrHP(maxHP);
                	String hpString = minHP == maxHP ? String.format("%d", minHP) : String.format("%d-%d", minHP, maxHP);
                	moveCopy.setName(String.format("%s %s (%sHP)", initialName, power, hpString));
            		moveCopy.setPower(power);
            		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
        		}
        		mod1.setCurrHP(oldHP);
            	break;
            		
        	case STRONGER_LOW_HP: // Gen 4 Flail, Reversal
            	oldHP = mod1.getCurrHP();
            	for(int power : new Integer[]{20, 40, 80, 100, 150, 200}) { //TODO: hardcoded
            		int minScale, maxScale;
            		
                	if (power == 20) { //TODO: hardcoded
                		maxScale = 64;
                		minScale = 43;
                	} else if (power == 40) {
                		maxScale = 42;
                		minScale = 22;
                	} else if (power == 80) {
                		maxScale = 21;
                		minScale = 13;
                	} else if (power == 100) {
                		maxScale = 12;
                		minScale =  6;
                	} else if (power == 150) {
                		maxScale =  5;
                		minScale =  2;
                	} else {
                		maxScale =  1;
                		minScale =  0;
                	}
                	
                	int fullHP = p1.getStatValue(Stat.HP);
                	int minHP = 0;
                	int maxHP = fullHP;
                	
                	for(int hp = 1; hp <= fullHP; hp++) {
                		int scale = hp * 64 / fullHP;
                		if(scale == minScale && minHP == 0)
                			minHP = hp;
                		if(scale > maxScale) {
                			maxHP = hp - 1;
                			break;
                		}
                	}
                	mod1.setCurrHP(maxHP);
                	String hpString = minHP == maxHP ? String.format("%d", minHP) : String.format("%d-%d", minHP, maxHP);
        			moveCopy.setName(String.format("%s (%sHP)", move.getBoostedName(power), hpString));
            		moveCopy.setPower(power);
            		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
            	}
        		mod1.setCurrHP(oldHP);
                break;
                
        	case PRESENT:
            	for(int power : new Integer[]{40, 80, 120}) { //TODO: hardcoded
        			moveCopy.setName(move.getBoostedName(power));
            		moveCopy.setPower(power);
            		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
            	}
                //TODO : handle healing
                break;
                
        	case PURSUIT:
        		boolean oldHasSwitch = mod2.hasStatus2_3(Status.SWITCHING_OUT);
        		
        		mod2.removeStatus2_3(Status.SWITCHING_OUT);
        		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
    			
    			mod2.addStatus2_3(Status.SWITCHING_OUT);
    			//moveCopy.setName(moveCopy.getName() + " SWITCH");
    			maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
    			
    			if(!oldHasSwitch) 
    				mod2.removeStatus2_3(Status.SWITCHING_OUT);
                break;
                
        	case RETURN:
        	{
        		int bp = Happiness.getReturnBP(p1.getHappiness());
        		moveCopy.setPower(bp);
        		moveCopy.setName(moveCopy.getBoostedName(bp));
        		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
    			break;
        	}
    			
        	case FRUSTRATION:
        	{
        		int bp = Happiness.getFrustrationBP(p1.getHappiness());
        		moveCopy.setPower(bp);
        		moveCopy.setName(moveCopy.getBoostedName(bp));
        		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
    			break;
        	}
                               
        	default:
	        	// Default print
        		maxDamage = calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
            	break;
            	
        	} // end switch effect
        	
        	// As a side effect of previous damage calculation call, the true move's type is in the move copy
        	Type trueMoveType = moveCopy.getType();
        	boolean isDealingDamage = maxDamage > 0;
        	
        	Move moveCopy2 = new Move(move); // copy

			// Print again with some modifiers
        	switch(p1.getAbility()) {
        	case BLAZE:
        		int oldHP = mod1.getCurrHP();
        		if(trueMoveType == Type.FIRE && isDealingDamage) {
        			mod1.setCurrHP(1);
        			calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy2, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
        		}
        		mod1.setCurrHP(oldHP);
        		break;
        		
        	case TORRENT:
        		oldHP = mod1.getCurrHP();
        		if(trueMoveType == Type.WATER && isDealingDamage) {
        			mod1.setCurrHP(1);
        			calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy2, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
        		}
        		mod1.setCurrHP(oldHP);
        		break;
        		
        	case OVERGROW:
        		oldHP = mod1.getCurrHP();
        		if(trueMoveType == Type.GRASS && isDealingDamage) {
        			mod1.setCurrHP(1);
        			calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy2, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
        		}
        		mod1.setCurrHP(oldHP);
        		break;
        		
        	case SWARM:
        		oldHP = mod1.getCurrHP();
        		if(trueMoveType == Type.BUG && isDealingDamage) {
        			mod1.setCurrHP(1);
        			if(moveCopy2.getEffect() == MoveEffect.FURY_CUTTER) { //TODO: find a way to not repeat ?
                		for (int _extra_multiplier : new Integer[] {0, 1, 2, 3, 4}) { //TODO: hardcoded
                			moveCopy2.appendName(_extra_multiplier + 1);
                			moveCopy2.setPower(initialMovePower * (1 << _extra_multiplier));
                			calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy2, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
                        }
        			} else {
        				calculateDamageAndPrintBasedOnVerboseLevel(sb, moveCopy2, p1, p2, mod1, mod2, 1, isBattleTower, isDoubleBattle, verboseLevel);
        			}
        		}
        		mod1.setCurrHP(oldHP);
        		break;
        		
        	default:
            	break;
        	} // end ability
        	
        } // end for
        
        if(verboseLevel == VerboseLevel.NONE)
        	return;
        
        // Then, append residuals
        boolean canPoison = false;
        boolean canToxic = false;
        boolean canBurn = false;
        boolean canTrap = false;
        boolean canConfuse = false;
        boolean canSeed = false;
        boolean canNightmare = false;
        boolean canCurse = false;
        boolean canSandstorm = mod1.getWeather() == Weather.SANDSTORM;
        boolean canHail = mod1.getWeather() == Weather.HAIL || p2.getAbility() == Ability.SNOW_WARNING;
        for (Move move : p2.getMoveset()) {
        	switch(move.getEffect()) {
        	case POISON:
        	case POISON_HIT:
        	case POISON_TAIL:
        	case TWINEEDLE:
        	case POISON_DOUBLE_HIT:
        	case POISON_HIT_HIGH_CRIT:
        		canPoison = true;
        		break;
        		
        	case TOXIC:
        	case POISON_FANG:
        	case TOXIC_HIT:
        		canToxic = true;
        		break;
        		
        	case TOXIC_SPIKES:
        		canPoison = true;
        		canToxic = true;
        		break;
        	
        	case BURN_HIT:
        	case WILL_O_WISP:
        	case TRI_ATTACK:
        	case BURN_HIT_THAW:
        	case BURN:
        	case BURN_HIT_HIGH_CRIT:
        		canBurn = true;
        		break;
        		
        	case TRAP:
        	case TRAP_HIT:
        		canTrap = true;
        		break;
        		
        	case CONFUSE:
        	case CONFUSE_HIT:
        	case CONFUSE_ALL:
        	case SWAGGER:
        		canConfuse = true;
        		break;
        		
        	case LEECH_SEED:
        		canSeed = true;
        		break;
        		
        	case NIGHTMARE:
        		canNightmare = true;
        		break;
        		
        	case CURSE:
        		canCurse = true;
        		break;
        		
        	case SANDSTORM:
        		canSandstorm = true;
        		break;
        	
        	case HAIL:
        		canHail = true;
        		break;
        		
        	default: 
        		break;
        	}
        	/* Missing : 
			METRONOME, //TODO
			SPIKES, //TODO
			
			NATURE_POWER, //TODO
			SECRET_POWER, //TODO
			*/
        }
    	
    	// poison
    	//if (p1.getSpecies().getType1() != Type.POISON && p1.getSpecies().getType2() != Type.POISON 
    	//		&& p1.getSpecies().getType1() != Type.STEEL && p1.getSpecies().getType2() != Type.STEEL) { //TODO : hardcoded
        	
    		if (canPoison) { //TODO : hardcoded
        		sb.append(String.format("(poisoned: %d)", p1.getStatValue(Stat.HP) / 8)); //TODO : hardcoded
        		sb.append(endl);
        	}
        	
    		if (canToxic) {
        		int minTxcDmg = Math.max(1, p1.getStatValue(Stat.HP) / 16);
        		ArrayList<Integer> dmgs = new ArrayList<>();
        		for(int i = 1; i <= 15; i++)
        			dmgs.add(i * minTxcDmg);
        		sb.append(String.format("(badly poisoned: %s)", dmgs)); //TODO : hardcoded
        		sb.append(endl);
    		}
    	//}
    	
    	// burn
    	//if (p1.getSpecies().getType1() != Type.FIRE && p1.getSpecies().getType2() != Type.FIRE) {
    		if (canBurn) {
    			sb.append(String.format("(burned: %d)", p1.getStatValue(Stat.HP) / 8)); //TODO : hardcoded
        		sb.append(endl);
    		}
    	//}
    	
    	// trap
    	if (canTrap) {
    		sb.append(String.format("(trapped: %d)", p1.getStatValue(Stat.HP) / 16)); //TODO : hardcoded
    		sb.append(endl);
    	}
    	
    	// confuse
    	if(canConfuse) { //TODO : hardcoded
    		Move selfHitMove = new Move(Move.getMoveByName("POUND"));
    		selfHitMove.setType(Type.NONE);
    		selfHitMove.setPower(40);
    		
    		TreeMap<Integer, Long> map = new TreeMap<>();
    		int maxDmg = Settings.game.isGen3() ?
    				damageGen3(selfHitMove, p1, p1, mod1, mod1, MAX_ROLL, false, 1, isBattleTower, isDoubleBattle)
    				: damageGen4(selfHitMove, p1, p1, mod1, mod1, MAX_ROLL, false, 1, isBattleTower, isDoubleBattle);
        	for(int roll = MIN_ROLL; roll <= MAX_ROLL; roll++) {
        		int dmg = maxDmg * roll / MAX_ROLL;
    			if (!map.containsKey(dmg))
    				map.put(dmg, (long) 1);
    			else
    				map.put(dmg, 1 + map.get(dmg));
        	}
			sb.append("(confused: "); //TODO : hardcoded
			boolean isFirstEntry = true;
			for(Map.Entry<Integer, Long> entry : map.entrySet()) {
				if (!isFirstEntry)
					sb.append(", ");
				int dmg = entry.getKey();
				int mult = entry.getValue().intValue();
				sb.append(String.format("%dx%d", dmg, mult));
				isFirstEntry = false;
			}
			sb.append(")"); //TODO : hardcoded
    		sb.append(endl);
    	}
    	
    	// leech seed
    	//if (p1.getSpecies().getType1() != Type.GRASS && p1.getSpecies().getType2() != Type.GRASS) {
        	if  (canSeed) {
        		sb.append(String.format("(seeded: %d)", p1.getStatValue(Stat.HP) / 8)); //TODO : hardcoded
        		sb.append(endl);
        	}
    	//}
    	
    	// nightmare
    	if (canNightmare) {
    		sb.append(String.format("(nightmared: %d)", p1.getStatValue(Stat.HP) / 4)); //TODO : hardcoded
    		sb.append(endl);
    	}
    	
    	// curse
    	if (canCurse) {
    		sb.append(String.format("(cursed: %d)", p1.getStatValue(Stat.HP) / 4)); //TODO : hardcoded
    		sb.append(endl);
    	}
    	
    	// hail
    	if(canHail) {
    		sb.append(String.format("(hail: %d)", p1.getStatValue(Stat.HP) / 16)); //TODO : hardcoded
    		sb.append(endl);
    	}    	
    	
    	// sandstorm
    	if(canSandstorm) {
    		sb.append(String.format("(hail: %d)", p1.getStatValue(Stat.HP) / 16)); //TODO : hardcoded
    		sb.append(endl);
    	}
    }
    
    public static void printDamageWithIVvariationIfApplicable(StringBuilder sb, Move move, Pokemon p1, Pokemon p2, StatModifier mod1, StatModifier mod2, int extra_modifier, boolean isBattleTower, boolean isDoubleBattle, boolean isCrit) throws UnsupportedOperationException, ToolInternalException {
    	Pokemon p1_in_use, p2_in_use, modifiedPoke;
    	boolean isAttackerVariation;
    	// Check who has IV variation (i.e. where the player is)
    	if(mod1.isIVvariation()) {
    		p1_in_use = new Pokemon(p1);
    		p2_in_use = p2;
    		isAttackerVariation = true;
    		modifiedPoke = p1_in_use;
    	} else if (mod2.isIVvariation()) {
    		p1_in_use = p1;
    		p2_in_use = new Pokemon(p2);
    		isAttackerVariation = false;
    		modifiedPoke = p2_in_use;
		} else {
			// No one has IV variation
			return;
		}
    	
    	Stat stat;
    	Nature[] natures;
    	if(isAttackerVariation) {
    		stat = move.isPhysical() ? Stat.ATK : Stat.SPA;
    		natures =  move.isPhysical() ? new Nature[] {Nature.MODEST, Nature.DOCILE, Nature.ADAMANT}  // Atk : minus, neutral, bonus
			                             : new Nature[] {Nature.ADAMANT, Nature.DOCILE, Nature.MODEST}; // Spa : minus, neutral, bonus
    	} else {
    		stat = move.isPhysical() ? Stat.DEF : Stat.SPD;
    		natures =  move.isPhysical() ? new Nature[] {Nature.GENTLE, Nature.DOCILE, Nature.LAX}  // Def : minus, neutral, bonus
                                         : new Nature[] {Nature.LAX, Nature.DOCILE, Nature.GENTLE}; // Spd : minus, neutral, bonus
    	}
    	
    	String[] names = new String[] {"-", "", "+"}; // TODO : hardcoded

    	//if(move.matchesAny("Strength") && p2.getSpecies() == Species.getSpeciesByName("FLAREON"))
    	//	System.out.println("printDamageWithIVvariationIfApplicable");
    	
    	
		sb.append(String.format("\t%s DAMAGE VARIATION", isCrit ? "CRIT": "NORMAL"));
		sb.append(Constants.endl);
    	for(int k = 0; k < natures.length; k++) {
    		sb.append(String.format("\t%s%1s", stat, names[k]));
    		sb.append(Constants.endl);
    		
    		Nature nature = natures[k];
    		
    		int last_low_iv = 0;
    		// boolean isDifferentDamages = false;
    		Damages previousDamages = null;
    		int iv = 0;
    		int hp = p2.getStatValue(Stat.HP);
    		for(iv = ContainerType.IV.getMinPerStat(); iv <= ContainerType.IV.getMaxPerStat() + 1; iv++) {
	    		boolean isFirstIV = iv == ContainerType.IV.getMinPerStat();
	    		boolean isLastIV = iv > ContainerType.IV.getMaxPerStat();
    			
	    		Damages damages = previousDamages;
	    		if(!isLastIV) {
	    			modifiedPoke.getIVs().put(stat, iv);
	    			modifiedPoke.setNature(nature);
	    			damages = new Damages(move, p1_in_use, p2_in_use, mod1, mod2, extra_modifier, isBattleTower, isDoubleBattle);
	    			damages.capDamagesWithHP(hp);
	    		}

	    		boolean isDifferentDamages = !damages.equals(previousDamages) || isLastIV;
	    		
	    		// Print previous IV range
	    		if(!isFirstIV && isDifferentDamages) {
	    			boolean isSingleIV = last_low_iv == iv - 1;
	    			String rangeStr = isSingleIV ? String.format("%5d", last_low_iv) : String.format("%2d-%2d", last_low_iv, iv - 1);
	    			sb.append(String.format("\t%s : ", rangeStr));
	    			if(isCrit)
	    				previousDamages.appendCritDamages(sb);
	    			else
	    				previousDamages.appendNormalDamages(sb);
	    			
	    			last_low_iv = iv;
	    		}
	    		
	    		previousDamages = damages;
	    		
	    		if(isLastIV)
	    			break;
	    		
	    		boolean isGuaranteedKO = damages.lowestDamage() >= p2.getStatValue(Stat.HP) || isCrit && damages.lowestCritDamage() >= p2.getStatValue(Stat.HP);
	    		if(isGuaranteedKO)
	    			iv = ContainerType.IV.getMaxPerStat();
    		}
    		sb.append(Constants.endl);
    		
    		
    		/*
    		int last_low_iv = 0;
    		Damages previousDamages = null;
    		int iv;
	    	for(iv = ContainerType.IV.getMinPerStat(); iv <= ContainerType.IV.getMaxPerStat(); iv++) {
	    		modifiedPoke.getIVs().put(stat, iv);
	    		modifiedPoke.setNature(nature);
	    		Damages damages = new Damages(move, p1_in_use, p2_in_use, mod1, mod2, extra_modifier, isBattleTower, isDoubleBattle);
	    		
	    		boolean isLastIV = iv == ContainerType.IV.getMaxPerStat();
	    		boolean isDifferentDamages = !damages.equals(previousDamages);
	    		
	    		// Short-circuiting guaranteed KOs
	    		if(damages.lowestDamage() >= p2.getStatValue(Stat.HP) || isCrit && damages.lowestCritDamage() >= p2.getStatValue(Stat.HP)) {
	    			iv = ContainerType.IV.getMaxPerStat();
	    			isLastIV = true;
	    			previousDamages = damages;
	    			isDifferentDamages = false;
	    		}
	    			
	    		
	    		if(previousDamages != null && isDifferentDamages || isLastIV) {
	    			String ivStr;
	    			if(!isLastIV && last_low_iv == iv-1) // There's a single IV with this damage spread
	    				ivStr =  String.format("%5d", last_low_iv);
	    			else
	    				ivStr = String.format("%2d-%2d", last_low_iv, isLastIV ? iv : iv - 1);
	    				
	    			sb.append(String.format("\t%s : ", ivStr));
	    			if(isCrit)
	    				previousDamages.appendCritDamages(sb);
	    			else
	    				previousDamages.appendNormalDamages(sb);
	    			last_low_iv = iv;
	    		}
	    		
	    		// Handle solo last IV
	    		if(isLastIV && isDifferentDamages) {
	    			sb.append(String.format("\t%5d : ", iv));
	    			damages.appendNormalDamages(sb);
	    		}
	    		
	    		previousDamages = damages;
	    	}
    		sb.append(Constants.endl);
    		*/
    	}
    }
    
    /**
     * Prints speed information : current speed comparison with the opponent, and with full IV+nature variation.
     */
    public static void appendSpeedInfo(StringBuilder sb, Pokemon player, Pokemon opponent, StatModifier playerMod, StatModifier opponentMod) {
        int minIV = ContainerType.IV.getMinPerStat();
		int maxIV = ContainerType.IV.getMaxPerStat();
		String endl = Constants.endl;
		
    	Pokemon pSpeedCopy = new Pokemon(player);
    	int currentSpeed = playerMod.getFinalSpeed(player);
    	int oppSpeed = opponentMod.getFinalSpeed(opponent);
    	
    	sb.append(String.format("SPEED INFO (vs. %s %s SPE)%s", opponent.getDisplayName(), oppSpeed, endl));
    	
    		// Check if player is always slower or always faster, whatever nature it has
    	pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMinPerStat()); // min iv
		pSpeedCopy.setNature(Nature.BRAVE); // minus spe
    	int meMinSpeed = playerMod.getFinalSpeed(pSpeedCopy);
    	
    	pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMaxPerStat()); //max iv
		pSpeedCopy.setNature(Nature.TIMID); // bonus spe
    	int meMaxSpeed = playerMod.getFinalSpeed(pSpeedCopy);
    	
    	if(meMinSpeed > oppSpeed) {
    		sb.append(">> Player is always faster than enemy.");
    		sb.append(endl);
    		sb.append(endl);
    	} else if(meMaxSpeed < oppSpeed) {
    		sb.append(">> Player is always slower than enemy.");
    		sb.append(endl);
    		sb.append(endl);
    	} else {  // There exist speed thresholds
    		// Current speed comparison
    		if(currentSpeed > oppSpeed) {
    			sb.append("~~ Player is currently faster than enemy.");
        		sb.append(endl);
    		} else if (currentSpeed < oppSpeed) {
    			sb.append("~~ Player is currently slower than enemy.");
        		sb.append(endl);
    		} else {
    			sb.append("~~ Player is currently speedtied with enemy.");
        		sb.append(endl);
    		}
    		
    		// Speed IV and nature variation
    		String[] names = new String[] {"-", "", "+"}; // TODO : hardcoded
        	Nature[] natures = new Nature[] {Nature.BRAVE, Nature.HARDY, Nature.TIMID}; // Spe : minus, neutral, bonus
        	for(int k = 0; k < natures.length; k++) { 
        		Nature nature = natures[k];
        		String statStr = String.format(">> %s%1s : ", Stat.SPE, names[k]);
        		
        		pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMinPerStat());
        		pSpeedCopy.setNature(nature);
            	meMinSpeed = playerMod.getFinalSpeed(pSpeedCopy);
            	
        		pSpeedCopy.getIVs().put(Stat.SPE, ContainerType.IV.getMaxPerStat());
        		pSpeedCopy.setNature(nature);
            	meMaxSpeed = playerMod.getFinalSpeed(pSpeedCopy);
            	
            	sb.append(statStr);
        		
            		// Checking always slower/faster for this nature
            	if(meMaxSpeed < oppSpeed) {
            		sb.append("Player always slower than enemy.");
            		sb.append(endl);
                    continue;
            	} else if (meMinSpeed > oppSpeed) {
            		sb.append("Player always faster than enemy.");
            		sb.append(endl);
            		sb.append(endl);
            		break;
            	}
            	
            		// If there exist speed thresholds for this nature
            	else if (meMinSpeed <= oppSpeed && meMaxSpeed >= oppSpeed) {    	                    		
                    int minIvToSpeedtie = Integer.MAX_VALUE, minIvToOutspeed = Integer.MAX_VALUE;
                    for (int sIV = minIV; sIV <= maxIV; sIV++) {
                    	pSpeedCopy.getIVs().put(Stat.SPE, sIV);
                		pSpeedCopy.setNature(nature);
                        int mySpd = playerMod.getFinalSpeed(pSpeedCopy);
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
                    	
                    	sb.append(String.format("%s to outspeed, %s to speedtie.", outspeedStr, speedtieStr));
                        //Main.append(" to outspeed: " + outspeedIV + ", to speedtie: " + tieIV);
                    } else if (minIvToOutspeed != Integer.MAX_VALUE) {
                    	minIvToOutspeed = (int)Math.max(0, minIvToOutspeed);
                    	String outspeedStr = minIvToOutspeed == 31 ? String.format("%d", minIvToOutspeed) : String.format("%d-%d", (int)minIvToOutspeed, 31);
                    	
                    	sb.append(String.format("%s to outspeed.", outspeedStr));
                    	//Main.append(" to outspeed: " + outspeedIV);
                    } else {
                    	maxIvToSpeedtie = (int)Math.min(31, maxIvToSpeedtie);
                    	String speedtieStr = minIvToSpeedtie == maxIvToSpeedtie || maxIvToSpeedtie == 0 ?
                    			String.format("%d", minIvToSpeedtie) : String.format("%d-%d", minIvToSpeedtie, maxIvToSpeedtie);
                    	
                    	sb.append(String.format("%s to speedtie.", speedtieStr));
                        //Main.append(" to speedtie: " + tieIV);
                    }
                    //Main.appendln(" with the same nature)");
                    sb.append(endl);

            	} // end speed thresholds
            } // end for 
            sb.append(endl);
    	}
    }
    
    
    
    /**
     * (Return the highest damage for side-effect purposes.)
     * @throws ToolInternalException 
     * @throws UnsupportedOperationException 
     */
    private static int calculateDamageAndPrintBasedOnVerboseLevel(StringBuilder sb, Move move, Pokemon p1, Pokemon p2, StatModifier mod1, StatModifier mod2, 
    		int _extra_multiplier, boolean isBattleTower, boolean isDoubleBattle, VerboseLevel verboseLevel) throws UnsupportedOperationException, ToolInternalException {
		Damages damages = new Damages(move, p1, p2, mod1, mod2, _extra_multiplier, isBattleTower, isDoubleBattle);
		if (verboseLevel == VerboseLevel.SOME)
			damages.appendBasicMoveInfo(sb);
		else if (verboseLevel == VerboseLevel.MOST)
        	damages.appendAllMoveInfo(sb);
		
		if (verboseLevel == VerboseLevel.EVERYTHING)
        	damages.appendDetailledPercentDamages(sb);
		
		return damages.highestDamage();
    }
}