package tool;
import java.util.Locale;

import tool.exception.ToolInternalException;

public abstract class GameAction {
	abstract void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException;

    public static final GameAction unequip = new GameAction() {
        void performAction(Pokemon p) { p.setItem(null); }
    };
    
    public static final GameAction setBattleTowerFlag = new GameAction() {
        void performAction(Pokemon p) { Constants.isBattleTower = true; }
    };
    
    public static final GameAction unsetBattleTowerFlag = new GameAction() {
        void performAction(Pokemon p) { Constants.isBattleTower = false; }
    };
    
    // Other modifiers
    /* TODO maybe keep this still ?
    public static final GameAction setAmuletCoin = new GameAction() {
        void performAction(Pokemon p) {
        	Settings.hasAmuletCoin = true;
        }
    };
    public static final GameAction unsetAmuletCoin = new GameAction() {
        void performAction(Pokemon p) {
        	Settings.hasAmuletCoin = false;
        }
    };
    */
	
    public static final GameAction setPokerus = new GameAction() {
        void performAction(Pokemon p) {
        	p.setPokerus(true);
        }
    };
    
    public static final GameAction unsetPokerus = new GameAction() {
        void performAction(Pokemon p) {
        	p.setPokerus(false);
        }
    };

    public static final GameAction setBoostedExp = new GameAction() {
        void performAction(Pokemon p) {
        	p.setBoostedExp(true);
        }
    };

    public static final GameAction unsetBoostedExp = new GameAction() {
        void performAction(Pokemon p) {
        	p.setBoostedExp(false);
        }
    };
   
    
    //not really a game action, but it's a nice hack?
    public static final GameAction printMoney = new GameAction() {
        void performAction(Pokemon p) { 
            Main.appendln(String.format(Locale.US, "MONEY: %,d", Settings.money));
        }
    };
    public static final GameAction printAllStats = new GameAction() {
        void performAction(Pokemon p) { 
            Main.appendln(p.getDetailledStatsStr(true));
            Main.appendln(p.levelAndExperienceNeededToLevelUpStr());
        }
    };
    public static final GameAction printAllStatsNoBoost = new GameAction() {
        void performAction(Pokemon p) { 
            Main.appendln(p.getDetailledStatsStr(false));
            Main.appendln(p.levelAndExperienceNeededToLevelUpStr());
        }
    };
    public static final GameAction printStatRanges = new GameAction() {
        void performAction(Pokemon p) { Main.appendln(p.statRanges(true)); }
    };
    public static final GameAction printStatRangesNoBoost = new GameAction() {
        void performAction(Pokemon p) { Main.appendln(p.statRanges(false)); }
    };
    
    public static final GameAction updateEVs = new GameAction() {
        void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
        	Main.appendln("Updating stats after PC withrawal.");
        	p.updateEVsAndCalculateStats();
        	printAllStatsNoBoost.performAction(p);
        }
    };

}

class EatConsumable extends GameAction {
	private Consumable consumable;
	private int quantity;
	public EatConsumable(Consumable consumable, int quantity) {
		this.consumable = consumable;
		this.quantity = quantity; 
	}
	@Override
	void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
		for(int i = 1 ; i <= quantity; i++) {
			Boolean isConsumed = null;
			switch(consumable) {
			case RARE_CANDY: isConsumed = p.eatRareCandy();                   break;
			case HP_UP:      isConsumed = p.eatEVBoostingItem(Stat.HP) != 0;  break;
			case PROTEIN:    isConsumed = p.eatEVBoostingItem(Stat.ATK) != 0; break;
			case IRON:       isConsumed = p.eatEVBoostingItem(Stat.DEF) != 0; break;
			case CALCIUM:    isConsumed = p.eatEVBoostingItem(Stat.SPA) != 0; break;
			case ZINC:       isConsumed = p.eatEVBoostingItem(Stat.SPD) != 0; break;
			case CARBOS:     isConsumed = p.eatEVBoostingItem(Stat.SPE) != 0; break;
			}
			
			if(isConsumed) {
				Main.appendln(""); 
				Main.appendln(String.format("Using a %s.", consumable.getDisplayName())); 
				printAllStats.performAction(p);
			} else {
				Main.appendln(String.format("WARNING : %s #%d had no effect.", consumable.getDisplayName(), i)); // TODO : better warning
			}
		}
	}
}


/**
 * Game action representing acquiring one or several badge boosts.
 */
class EarnBadge extends GameAction {
	private Stat[] badges;
	public EarnBadge(Stat... badges) { this.badges = badges; }
	@Override
	void performAction(Pokemon p) { for (Stat badge : badges) p.setBadge(badge); }
}

/* TODO : make this work with new implementation
class ChangeReturnPower extends GameAction {
    private int power;
    private int MIN_RETURN_POWER = 1;
    private int MAX_RETURN_POWER = 102;
    ChangeReturnPower(int newPower) { power = Math.min(MAX_RETURN_POWER, Math.max(MIN_RETURN_POWER, newPower)); }
    @Override
    void performAction(Pokemon p) { Move.RETURN.setPower(power); }
}
*/

class LearnMove extends GameAction {
    private Move move;
    LearnMove(Move m) { move = m; }
    public Move getMove() { return move; }
    @Override
    void performAction(Pokemon p) { 
    	p.getMoveset().addMove(move);
    }
}


class UnlearnMove extends GameAction {
    private Move move;
    UnlearnMove(Move m) { move = m; }
    public Move getMove() { return move; }
    @Override
    void performAction(Pokemon p) {
    	p.getMoveset().delMove(move);
    }
}

class Evolve extends GameAction {
    private Species target;
    Evolve(Species s) { target = s; }
    @Override
    void performAction(Pokemon p) throws UnsupportedOperationException, ToolInternalException {
        p.evolve(target);
        p.updateEVsAndCalculateStats();
        GameAction.printAllStatsNoBoost.performAction(p);
    }
}

class AddMoney extends GameAction {
	private int money;
	AddMoney(int m) {money = m;}
	@Override
	void performAction(Pokemon p) {
		Settings.money += money;
	}
}

class Equip extends GameAction {
	private Item item;
	Equip(Item i) { item = i; }
	@Override
	void performAction(Pokemon p) {
		p.setItem(item);
	}
}

class SetHappiness extends GameAction {
	private int happiness;
	SetHappiness(int h) { happiness = h; }
	@Override
	void performAction(Pokemon p) throws ToolInternalException {
		p.setHappiness(happiness);
	}
}
