package tool;

import java.util.Set;

import tool.exc.ToolInternalException;

public class Happiness {
	public enum HappinessEvent{
		MASSAGE_RIBBON_SYNDICATE_MUCH_MORE(Game.DPPtGameSet, 30, 30, 30),
		MASSAGE_RIBBON_SYNDICATE_MORE(Game.DPPtGameSet, 10, 10, 10),
		MASSAGE_RIBBON_SYNDICATE_LITTLE_MORE(Game.DPPtGameSet, 5, 5, 5),
		
		GROOMING_HAIRCUT_BROTHERS_DELIGHTED(Game.HGSSGameSet, 10, 10, 10),
		GROOMING_HAIRCUT_BROTHERS_HAPPY(Game.HGSSGameSet, 5, 5, 5),
		GROOMING_HAIRCUT_BROTHERS_LITTLE_HAPPIER(Game.HGSSGameSet, 1, 1, 1),
		
		GROOMING_DAISY_HGSS(Game.FRLGGameSet, 10, 10, 10),
		
		// Pomeg Berry, Kelpsy Berry, Qualot Berry, Hondew Berry, Grepa Berry, and Tamato Berry
		HAPPINESS_BERRY(Game.emeraldUpToHGSSGameSet, 10, 5, 2),
		
		LEVEL_UP(Game.gen34GameSet, 5, 3, 2),
		
		// Gen3+ : HP Up, Protein, Iron, Calcium, Zinc, Carbos, PP Up, PP Max, and Rare Candy
		VITAMIN(Game.gen34GameSet, 5, 3, 2),
		
		GROOMING_DAISY_FRLG(Game.FRLGGameSet, 3, 3, 1),
		
		MASSAGE_VEILSTONE_CITY(Game.DPPtGameSet, 3, 3, 3),
		
		GYM_LEADER_E4_CHAMPION(Game.gen34GameSet, 3, 2, 1),

		WINNING_CONTEST(Game.gen4GameSet, 3, 2, 1),
		
		//WALKING_128(Game.gen34GameSet, 1, 1, 1), // 50%
		
		LEARN_TM_HM(Game.gen34GameSet, 1, 1, 0),
		
		// Gen 3 : X Attack, X Defend, X Speed, X Special, X Accuracy, Dire Hit, and Guard Spec.
		// Gen 4+ : X Attack, X Defend, X Speed, X Special, X Sp. Def, X Accuracy, Dire Hit, and Guard Spec.
		BATTLE_ITEM(Game.gen34GameSet, 1, 1, 0),
		
		// opp level <= player level +30 | In Gen 3, poison KO in battles doesn't count
		FAINTING_WITH_OPP_LOW_LEVEL(Game.gen34GameSet, -1, -1, -1),
		
		// opp level > player level +30 | In Gen 3, poison KO in battles doesn't count
		FAINTING_WITH_OPP_HIGH_LEVEL(Game.gen34GameSet, -5, -5, -10),
		
		SURVIVING_POISON_1_HP(Game.gen4GameSet, -5, -5, -10),

		ENERGY_POWDER_HEAL_POWDER(Game.gen34GameSet, -5, -5, -10),

		ENERGY_ROOT(Game.gen34GameSet, -10, -10, -15),

		REVIVAL_HERB(Game.gen34GameSet, -15, -15, -20),
		;
		
		public static final int LOW_MID = 100, MID_HIGH = 200; // 0-99=low, 100-199=mid, 200-255=high
		
		private final Set<Game> validGames;
		private final int low;
		private final int mid;
		private final int high;
		
		private HappinessEvent(Set<Game> validGames, int low, int mid, int high) {
			this.validGames = validGames;
			this.low = low;
			this.mid = mid;
			this.high = high;
		}

		public Set<Game> getValidGames() {
			return validGames;
		}

		public int getLow() {
			return low;
		}

		public int getMid() {
			return mid;
		}

		public int getHigh() {
			return high;
		}
		
		public boolean isAuthorized() {
			return validGames.contains(Settings.game);
		}
		
		private int getBaseBoost(int happiness) throws ToolInternalException {
			if(!isInBound(happiness))
				throw new ToolInternalException(Happiness.class.getEnclosingMethod(), Integer.valueOf(happiness), "");
			
			return (happiness < LOW_MID) ? low : (happiness < MID_HIGH) ? mid : high;
		}
		
		public int getFinalHappiness(int happiness, boolean isItemBoosting, boolean isInLuxuryBall, boolean isMetLocation) throws ToolInternalException {
			if(!isAuthorized())
				throw new ToolInternalException(Happiness.class.getEnclosingMethod(), Settings.game, "Non-existing happiness event.");
			
			int boost = getBaseBoost(happiness);
			
			if(boost > 0) {
				switch(Settings.game) {
				
				// E : https://github.com/pret/pokeemerald/blob/master/src/pokemon.c#L4676
				case RUBY:
				case SAPPHIRE:
				case EMERALD:
				case LEAFGREEN:
				case FIRERED:
					if(isItemBoosting) boost = boost * 150 / 100;
					if(isInLuxuryBall) boost++;
					if(isMetLocation) boost++;
					break;
					
				// Gen 4 : https://bulbapedia.bulbagarden.net/wiki/Friendship#Generation_IV
				// - DP : https://github.com/pret/pokediamond/blob/master/arm9/src/scrcmd_party.c#L303
				//        https://github.com/pret/pokediamond/blob/master/arm9/src/pokemon.c#L2052
                //        => order contradicts Bulbapedia | is the battle item thing real ?
				case DIAMOND:
				case PEARL:
				case PLATINUM:
					if(this == MASSAGE_RIBBON_SYNDICATE_LITTLE_MORE
					|| this == MASSAGE_RIBBON_SYNDICATE_MORE
					|| this == MASSAGE_RIBBON_SYNDICATE_MUCH_MORE)
						break;
					
					if(isItemBoosting && this != BATTLE_ITEM) boost = boost * 150 / 100;
					if(isInLuxuryBall) boost++;
					// Egg/Met location is broken in Gen 4 ... ?
					//if(isMetLocation) boost++;
					break;
				
				// - HGSS : https://github.com/pret/pokeheartgold/blob/decd3cd653c535358a7f01a68cbc27a5823601a6/src/use_item_on_mon.c#L613-L647
				case HEARTGOLD:
				case SOULSILVER:
					if(this == MASSAGE_RIBBON_SYNDICATE_LITTLE_MORE
					|| this == MASSAGE_RIBBON_SYNDICATE_MORE
					|| this == MASSAGE_RIBBON_SYNDICATE_MUCH_MORE)
						break;
					
					if(isInLuxuryBall) boost++;
					// Egg/Met location is broken in Gen 4 ... ?
					//if(isMetLocation) boost++;
					if(isItemBoosting) boost = boost * 150 / 100;
					break;
				}
				
			} else if (boost < 0) {
				// empty ?
			}
			
			int potentialTotalHappiness = happiness + boost;
			// DP has herbal medicine friendship underflow in battles, not implemented.
			int effectiveTotalHappiness = Happiness.bound(potentialTotalHappiness);
			return effectiveTotalHappiness;
		}
	}
	
	public static final int MIN = 0, MAX = 255;

	public static int bound(int happiness) {
		if(happiness < MIN)
			return MIN;
		
		if(happiness > MAX)
			return MAX;
		
		return happiness;
	}

	public static boolean isInBound(int happiness) {
		return MIN <= happiness && happiness <= MAX;
	}
	
	public static int getReturnBP(int happiness) {
		return (int)Math.max(1, happiness * 10 / 25);
	}
	
	public static int getFrustrationBP(int happiness) {
		return getReturnBP(MAX - happiness);
	}
}
