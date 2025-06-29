package tool;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * The in-battle stat modifiers applied to a Pokémon (caused by e.g. X Attack).
 */
public class StatModifier {		
    private StatsContainer stages;
    private int currentHP;
    private Status status1;
    private EnumSet<Status> statuses2_3;
    private Weather weather;
    private boolean isIVvariation;
    private int returnOffset;
    private int returnMaxAdded;
    
    private HashSet<Object> immuneTo = new HashSet<>();
    
    private int SIMPLE_STAT_STAGE_MULT = 2;
    private int DEFAULT_STAT_STAGE_MULT = 1;

    public StatModifier() {
    	this.stages = new StatsContainer(StatsContainer.ContainerType.STAT_INCREMENTS);
    	this.currentHP = 0;
    	this.status1 = Status.noStatus1();
    	this.statuses2_3 = Status.noStatus2_3();
    	this.weather = Weather.NONE;
    	this.isIVvariation = false;
    	this.returnOffset = 0;
    	this.returnMaxAdded = 0;
    }

    public StatModifier(int atk, int def, int spa, int spd, int spe) {
    	this();
    	this.stages.put(Stat.ATK, atk);
    	this.stages.put(Stat.DEF, def);
    	this.stages.put(Stat.SPA, spa);
    	this.stages.put(Stat.SPD, spd);
    	this.stages.put(Stat.SPE, spe);
    }

    public StatModifier(int atk, int def, int spa, int spd, int spe, int acc, int eva) {
        this(atk, def, spa, spd, spe);
        this.stages.put(Stat.ACC, acc);
        this.stages.put(Stat.EVA, eva);
    }

    /*
    public StatModifier(int atk, int def, int spa, int spd, int spe, int acc, int eva) {
        this(atk, def, spa, spd, spe, acc);
        this.eva = eva;
    }
    */

    /*
    // used to keep the stage between -6 and +6
    private static int bound(int stage) {
        if (stage < MIN_STAGE)
            return MIN_STAGE;
        else if (stage > MAX_STAGE)
            return MAX_STAGE;
        else
            return stage;
    }
    */

    /*
    // in gen 1, accuracy/evasion stages are done the same as other stats
    private static double accEvaMultiplier(int stage) {
        return ((double) acc_eva_multipliers[stage + 6]) / acc_eva_divisors[stage + 6];
    }

    //multiplier for atk,def,spc,spd
    private static double normalStatMultiplier(int stage) {
        return ((double) normal_multipliers[stage + 6]) / normal_divisors[stage + 6];
    }
    */

    private static int[] normal_multipliers = new int[] { 2, 2, 2, 2, 2, 2, 2, 3, 4, 5, 6, 7, 8};
    private static int[] normal_divisors    = new int[] { 8, 7, 6, 5, 4, 3, 2, 2, 2, 2, 2, 2, 2};
    private static int[] acc_eva_multipliers = new int[] {  33,  36,  43,  50,  60,  75, 100, 133, 166, 200, 250, 266, 300};
    private static int[] acc_eva_divisors    = new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
    

    private static int applyStatStage(Stat stat, int valueToMod, int stage) {
    	if(Stat.primaryStagesStats.contains(stat))
    		return valueToMod * normal_multipliers[stage + 6] / normal_divisors[stage + 6];
    	else if(Stat.secondaryStagesStats.contains(stat))
    		return valueToMod * acc_eva_multipliers[stage + 6] / acc_eva_divisors[stage + 6];
    
    	// Should never be called if the class is coded properly ...
    	throw new IllegalArgumentException(String.format("Received wrong stat '%s' in '%s'.", 
    				stat, StatModifier.class.getEnclosingMethod().getName()));
    }

    
    public int getStage(Stat stat) {
    	return this.getStages().get(stat);
    }
    
    /**
     * Sets the stage, throwing if it's not within container constraints.
     * Returns the previous value, or null if there was none.
     */
    public Integer setStage(Stat stat, int stage) {
    	return this.stages.put(stat, stage);
    }
    
    /**
     * Sets the stage, possibly modified by the container constraints.
     * Returns the previous value.
     */
    public int setStageWithBound(Stat stat, int stage) {
    	return this.stages.putWithBound(stat, stage);
    }
     
    public Status getStatus1() {
    	return status1;
    }
    
    public void setStatus1(Status status1) {
    	this.status1 = status1;
    }
    
    public boolean hasStatus2_3(Status status) {
    	if(!status.isStatus2_3()) {
    		throw new IllegalArgumentException(String.format("Method '%s' received status '%s'.",
    				StatModifier.class.getEnclosingMethod().getName(), status));
    	}
    	return statuses2_3.contains(status);
    }

    
    public void setStatuses2_3(EnumSet<Status> statuses2_3) {
    	this.statuses2_3 = statuses2_3;
    }
    
    public void addStatus2_3(Status status) {
    	statuses2_3.add(status);
    }
    
    public void removeStatus2_3(Status status) {
    	statuses2_3.remove(status);
    }


    public String summary(Pokemon p) {
    	StringBuffer sb = new StringBuffer();
    	sb.append(getStageStr(p));
        sb.append(getStatus1Str());
        sb.append(getStatuses2_3Str());
        
        return sb.toString();
    }
    
    private String getStageStr(Pokemon p) {
    	StringBuffer sb = new StringBuffer();
    	if(hasStageMods() || p.hasBadgeBoost()) {
    		StringBuffer sbPrimary = new StringBuffer();
    		for(Stat stat : Stat.primaryStagesStats) {
    			sbPrimary.append(String.format("%s%d", p.hasBadge(stat) ? "*" : "", getStage(stat)));
				sbPrimary.append("/");
    		}
    		
    		StringBuffer sbSecondary = new StringBuffer();
    		for(Stat stat : Stat.secondaryStagesStats) {
    			sbSecondary.append(String.format("%d", getStage(stat)));
				sbSecondary.append("/");
    		}
    		
    		return String.format("[%s|%s]", sbPrimary.substring(0, sbPrimary.length() - 1), sbSecondary.substring(0, sbSecondary.length() - 1));
    	}
    	
    	if(getStage(Stat.SPE) != 0 || p.hasBadge(Stat.SPE)) { // TODO: not sure tbh
    		sb.append(String.format("Final speed: %d ", this.getFinalSpeed(p)));
    	}
    	
    	return sb.toString();
    }
    
    private String getStatus1Str() {
    	if(hasStatus1())
    		return String.format("<%s> ", status1);
    	else
    		return "";
    }
    
    private String getStatuses2_3Str() {
    	StringBuffer sb = new StringBuffer();
    	for(Status status23 : statuses2_3) {
			sb.append(String.format("%s+", status23.getDisplayName()));
    	}
    	
    	return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
    }
    
    private boolean hasStageMods() {
    	for(Stat stat : getStages().getContainerType()) {
    		if(getStage(stat) != getStages().getContainerType().getDefaultValue()) // Fancy writing for 'non 0' check
    			return true;
    	}
    	
    	return false;
    }
    
    private boolean hasStatus1() {
    	return status1 != Status.NONE;
    }
    
    private boolean hasAnyStatus2_3() {
    	return !statuses2_3.isEmpty();
    }
    
    public boolean hasMods() {
    	return hasStageMods() || hasStatus1() || hasAnyStatus2_3();
    }

    /**
     * Applies stat stage to value. Accounts for Simple. Doesn't apply any badge boost.
     */
    public int modStat(Stat stat, int valueToMod, boolean isSimple) {
    	int mult = isSimple ? SIMPLE_STAT_STAGE_MULT : DEFAULT_STAT_STAGE_MULT;
    	
    	return applyStatStage(stat, valueToMod, getStage(stat) * mult);
    }
    
    /**
     * Applies stat stage to value. Doesn't apply any badge boost, nor Simple.
     */
    public int modStat(Stat stat, int valueToMod) {
    	return modStat(stat, valueToMod, false);
    }
    
    /**
     * Applies stat stage to (potentially) badge-boosted Pokémon speed.
     */
    public int modSpe(Pokemon p) {
        return modStat(Stat.SPE, p.getStatValue(Stat.SPE), p.getAbility() == Ability.SIMPLE);
    }
    
    /**
     * Applies stat stage, nature and iv to badge-boosted speed.
     */
    public int modSpeWithIVandNature(Pokemon p, int iv, Nature nature) {
        return modStat(Stat.SPE, p.getSpeedValueWithIVandNature(iv, nature), p.getAbility() == Ability.SIMPLE);
    }
    
    /**
     * Returns battle-friendly speed.
     */
    public int getFinalSpeed(Pokemon p) {
    	// https://github.com/smogon/damage-calc/blob/master/calc/src/mechanics/util.ts#L92
    	
    	// Badge boost, nature & stat stages
    	//int _speed = modStat(Stat.SPE, p.getStatValue(Stat.SPE), p.getAbility() == Ability.SIMPLE);
    	int _speed = modStat(Stat.SPE, p.getSpeedValueWithIVandNature(p.getIVs().get(Stat.SPE), p.getNature()), p.getAbility() == Ability.SIMPLE);
        
    	ArrayList<Integer> speedMods = new ArrayList<>();

        // Field
		if (hasStatus2_3(Status.TAILWIND)) speedMods.add(8192);


        // Ability
		if (p.getAbility() == Ability.UNBURDEN && hasStatus2_3(Status.UNBURDEN)
		||  p.getAbility() == Ability.CHLOROPHYLL && weather == Weather.SUN
		||  p.getAbility() == Ability.SWIFT_SWIM && weather == Weather.RAIN)
			speedMods.add(8192);
		else if (p.getAbility() == Ability.QUICK_FEET && getStatus1() != Status.NONE)
			speedMods.add(6144);
		else if (p.getAbility() == Ability.SLOW_START)
			speedMods.add(2048);


        // Held item
        if(p.getHeldItem() != null) {
		    switch(p.getHeldItem().getHoldEffect()) {
		    case CHOICE_SPEED: // Choice Scarf
		    	speedMods.add(6144);
		    	break;
		    	
		    case SPEED_DOWN_GROUNDED: // Iron Ball
	    	case EXP_UP_SPEED_DOWN:   // Macho Brace
	    	case LVLUP_SPEED_EV_UP:   // Power Anklet
	    	case LVLUP_SPDEF_EV_UP:   // Power Band
	    	case LVLUP_DEF_EV_UP:     // Power Belt
	    	case LVLUP_ATK_EV_UP:     // Power Bracer
	    	case LVLUP_SPATK_EV_UP:   // power Lens
	    	case LVLUP_HP_EV_UP:      // Power Weight
	    		speedMods.add(2048);
	    		break;
	    		
	    	case DITTO_SPEED_UP: // Quick Powder
	    		if(p.getSpecies() == Species.getSpeciesByName("DITTO"))
	    			speedMods.add(8192);
	    		break;
	    		
	    	default:
	    		break;
		    }
	    }
	    
	    // Apply mods
	    int mult = 4096;
	    for(int mod : speedMods) {
		     if (mod != 4096)
		    	 mult = (mult * mod + 2048) >> 12;
	    }
	    mult = (int)Math.max(Math.min(mult, 131172), 410);
	    _speed = _speed * mult / 4096;
    
	    // PRZ
	    if (getStatus1() == Status.PARALYSIS && p.getAbility() != Ability.QUICK_FEET)
	    	_speed = _speed * 25 / 100;

	    _speed = (int)Math.min(10000, _speed);
	    return Math.max(0, _speed);
    }

    
    /*
    //Returns a string with non badge-boosted stats.

    public String modStatsNoBBStr(Pokemon p) { // TODO: refactor
    	boolean isSimple = p.getAbility() == Ability.SIMPLE;
        return String.format("%s/%s/%s/%s/%s/%s", p.getHP(), 
        		modStat(Stat.ATK, p.getTrueAtk(), isSimple), 
        		modStat(Stat.DEF, p.getTrueDef(), isSimple),
        		modStat(Stat.SPA, p.getTrueSpa(), isSimple),
        		modStat(Stat.SPD, p.getTrueSpd(), isSimple),
        		modStat(Stat.SPE, p.getTrueSpe(), isSimple));
    }
    */

	public Weather getWeather() {
		return weather;
	}

	public void setWeather(Weather weather) {
		this.weather = weather;
	}

	public int getCurrHP() {
		return currentHP;
	}

	public void setCurrHP(int currHP) {
		this.currentHP = currHP;
	}
	
	public boolean isHPThirdOrLess(int maxHP) {
		return currentHP*3 <= maxHP;
	}
	
	public boolean isHPHalfOrLess(int maxHP) {
		return currentHP*2 <= maxHP;
	}

	public StatsContainer getStages() {
		return stages;
	}

	public boolean isIVvariation() {
		return isIVvariation;
	}

	public void setIVvariation(boolean isIVvariation) {
		this.isIVvariation = isIVvariation;
	}

	public boolean isImmuneTo(Object o) {
		return immuneTo.contains(o);
	}

	public int getReturnOffset() {
		return returnOffset;
	}

	public void setReturnOffset(int returnOffset) {
		this.returnOffset = returnOffset;
	}

	public int getReturnMaxAdded() {
		return returnMaxAdded;
	}

	public void setReturnMaxAdded(int returnMaxAdded) {
		this.returnMaxAdded = returnMaxAdded;
	}
}
