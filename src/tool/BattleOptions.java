package tool;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;

import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;

public class BattleOptions {
	public static final int MIN_NUMBER_OF_BATTLERS_PER_FIGHT = 1, MAX_NUMBER_OF_BATTLERS_PER_FIGHT = 12;
	public static final int MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT = 0, MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT = 6; // 0 means no exp
	public static final int MIN_BATCH_SIZE = 1, MAX_BATCH_SIZE = 2;

	private int currentOpponentIndex = -1; // -1 allows to update stuff at the beginning of any needed loop, instead of at the end
    private ArrayList<Integer> sxps = new ArrayList<Integer>();
    
    private EnumMap<Stat, ArrayList<Integer>> xStages = new EnumMap<Stat, ArrayList<Integer>>(Stat.class);
    private EnumSet<Stat> xStagesAreForced = EnumSet.noneOf(Stat.class);
    private ArrayList<Status> xStatus1List = new ArrayList<Status>();
    private ArrayList<EnumSet<Status>> xStatuses2_3List = new ArrayList<EnumSet<Status>>();
    private int xCurrentHP = 0;
    private CurrentHPid xCurrentHPid = CurrentHPid.FULL;
    private Trainer xPartner = null;

    private EnumMap<Stat, ArrayList<Integer>> yStages = new EnumMap<Stat, ArrayList<Integer>>(Stat.class);
    private EnumSet<Stat> yStagesAreForced = EnumSet.noneOf(Stat.class);
    private ArrayList<Status> yStatus1List = new ArrayList<Status>();
    private ArrayList<EnumSet<Status>> yStatuses2_3List = new ArrayList<EnumSet<Status>>();
    private int yCurrentHP = 0;
    private CurrentHPid yCurrentHPid = CurrentHPid.FULL;
    private Trainer yPartner = null;
    
    private ArrayList<ArrayList<Integer>> order = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Weather> weathers = new ArrayList<Weather>();
    
    private boolean isBattleTower = false;
    private boolean forcingMultiTargetDamage = false;
    private boolean forcingSingleTargetDamage = false;
    private boolean isForcedDoubleBattle = false;
    private boolean isForcedSingleBattle = false;
    private boolean isSingleTrainerForcedToBeFoughtAsDouble = false;
    
    private boolean printStatRangesOnLvl = false;
    private boolean printStatsOnLvl = false;
    private StatModifier mod1 = new StatModifier();
    private StatModifier mod2 = new StatModifier();
    
    private boolean isPostponedExp = false;
    
    private String scenarioName = null;
    private boolean isBacktrackingAfterBattle = false;
    
    private boolean isIVvariation = false;
    
    private VerboseLevel verbose = VerboseLevel.NONE;

    
    public BattleOptions(boolean isInseparableDoubleBattle) throws ToolInternalException {
        this();
        this.setSingleTrainerForcedToBeFoughtAsDouble(isInseparableDoubleBattle);
    }
    
    public BattleOptions() throws ToolInternalException {
    	// TODO: Don't really know yet how to avoid setting custom values.
    	setSxps(1); 
    	setWeather(Weather.NONE); // TODO: same
    	for(Side side : Side.values()) {
    		setStatus1(side, Status.noStatus1());
    		setStatuses2_3(side, Status.noStatus2_3());
    		for(Stat stat : ContainerType.STAT_INCREMENTS) {
    			for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
    				setStage(side, stat, ContainerType.STAT_INCREMENTS.getDefaultValue(), false); // TODO: hardcoded
    		}
    	}
    }

    /*
	public int getParticipants() {
		return participants;
	}
	*/

	public ArrayList<Integer> getSxps() {
		return sxps;
	}

	public ArrayList<Integer> getStages(Side side, Stat stat) {
		if(stat == Stat.HP)
			throw new IllegalArgumentException(String.format("'%s' received HP as stat. Not allowed.", BattleOptions.class.getEnclosingMethod().getName()));
		
		ArrayList<Integer> stages = null;
		switch(side) {
		case PLAYER: stages = xStages.get(stat); break;
		case ENEMY:  stages = yStages.get(stat); break;
		}
		
		return stages;
	}
	
	public boolean isForcedStat(Side side, Stat stat) {
		if(stat == Stat.HP)
			throw new IllegalArgumentException(String.format("'%s' received HP as stat. Not allowed.", BattleOptions.class.getEnclosingMethod().getName()));
		
		boolean isForced = false;
		switch(side) {
		case PLAYER: isForced = xStagesAreForced.contains(stat); break;
		case ENEMY:  isForced = yStagesAreForced.contains(stat); break;
		}
		return isForced;
	}
	
	public void setForcedStat(Side side, Stat stat) {
		if(stat == Stat.HP)
			throw new IllegalArgumentException(String.format("'%s' received HP as stat. Not allowed.", BattleOptions.class.getEnclosingMethod().getName()));
		
		switch(side) {
		case PLAYER: xStagesAreForced.add(stat); break;
		case ENEMY:  yStagesAreForced.add(stat); break;
		}
	}
	
	/*
	public ArrayList<Integer> getXatks() {
		return xatks;
	}

	public ArrayList<Integer> getXdefs() {
		return xdefs;
	}

	public ArrayList<Integer> getXspas() {
		return xspas;
	}

	public ArrayList<Integer> getXspds() {
		return xspds;
	}

	public ArrayList<Integer> getXspes() {
		return xspes;
	}

	public ArrayList<Integer> getXaccs() {
		return xaccs;
	}

	public ArrayList<Integer> getXevas() {
		return xevas;
	}
	*/

	public ArrayList<Status> getStatus1List(Side side){
		ArrayList<Status> status1List = null;
		switch(side) {
		case PLAYER: status1List = xStatus1List; break;
		case ENEMY:  status1List = yStatus1List; break;
		}
		return status1List;
	}
	
	public ArrayList<EnumSet<Status>> getStatuses2_3List(Side side){
		ArrayList<EnumSet<Status>> statuses2_3List = null;
		switch(side) {
		case PLAYER: statuses2_3List = xStatuses2_3List; break;
		case ENEMY:  statuses2_3List = yStatuses2_3List; break;
		}
		return statuses2_3List;
	}
	
	/*
	public ArrayList<EnumMap<Status, Boolean>> getXstatuses2_3(){
		return xstatuses2_3;
	}	
	*/
	
	/*
	public ArrayList<Integer> getYItemsPerStat(Stat stat) {
		return yStages.get(stat);
	}
	*/
	
	/*
	public ArrayList<Integer> getYatks() {
		return yatks;
	}

	public ArrayList<Integer> getYdefs() {
		return ydefs;
	}

	public ArrayList<Integer> getYspas() {
		return yspas;
	}

	public ArrayList<Integer> getYspds() {
		return yspds;
	}

	public ArrayList<Integer> getYspes() {
		return yspes;
	}

	public ArrayList<Integer> getYaccs() {
		return yaccs;
	}

	public ArrayList<Integer> getYevas() {
		return yevas;
	}
	*/

	/*
	public ArrayList<Status> getYstatuses1(){
		return ystatuses1;
	}
	*/
	
	/*
	public ArrayList<EnumMap<Status, Boolean>> getYstatuses2_3(){
		return ystatuses2_3;
	}
	*/

	public ArrayList<ArrayList<Integer>> getOrder() {
		return order;
	}

	public ArrayList<Weather> getWeathers() {
		return weathers;
	}

	public boolean isPrintStatRangesOnLvl() {
		return printStatRangesOnLvl;
	}

	public boolean isPrintStatsOnLvl() {
		return printStatsOnLvl;
	}

	public StatModifier getStatModifier(Side side) {
		StatModifier mod = null;
		switch(side) {
		case PLAYER: mod = mod1; break;
		case ENEMY:  mod = mod2; break;
		}
		
		return mod;
	}
	

	public VerboseLevel getVerbose() {
		return verbose;
	}

	/*
	public void setParticipants(int participants) {
		this.participants = participants;
	}
	*/

	public void setSxps(ArrayList<Integer> sxps) {
		this.sxps = sxps; // TODO: is it really working without copying ?
	}
	
	/**
	 * Sets a fixed number of participants for the whole fight.
	 */
	public void setSxps(int nbOfParticipants) {
		this.sxps.clear();
		for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
			this.sxps.add(nbOfParticipants);
	}
	
	/**
	 * Sets stages to a specified side and stat. Stores whether these stages are forced or not for the whole fight.
	 */
	public void setStages(Side side, Stat stat, ArrayList<Integer> stages, boolean isForced) throws ToolInternalException { // KeyException, IllegalArgumentException {
		if(stat == Stat.HP)
        	throw new ToolInternalException(BattleOptions.class.getEnclosingMethod(), stat, "Can't set in stages.");
			//throw new KeyException("BattleOptions.setXItemsPerStat received HP as stat. Not allowed.");
		if(stages == null)
        	throw new ToolInternalException(BattleOptions.class.getEnclosingMethod(), stages, "Can't set this as stages.");
			//throw new IllegalArgumentException(String.format("BattleOptions.setXItemsPerStat '%s' received null. Not allowed.", stat));
		
		EnumMap<Stat, ArrayList<Integer>> statsMap = null;
		EnumSet<Stat> forcedSet = null;
		switch(side) {
		case PLAYER: statsMap = xStages; forcedSet = xStagesAreForced; break;
		case ENEMY:  statsMap = yStages; forcedSet = yStagesAreForced; break;
		}
		
		statsMap.put(stat, stages);
		if(isForced)
			forcedSet.add(stat);
	}
	
	/**
	 * Sets a fixed stage to a specified side and stat for the whole fight. Stores whether these stages are forced or not for the whole fight.
	 */
	public void setStage(Side side, Stat stat, int stage, boolean isForced) throws ToolInternalException {
		ArrayList<Integer> stages = new ArrayList<>();
		for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
			stages.add(stage);
		
		this.setStages(side, stat, stages, isForced);
	}
	
	/**
	 * Adds a status1.
	 */
	public void addStatus1(Side side, Status status1) {
		ArrayList<Status> status1List = getStatus1List(side);
		status1List.add(status1);
	}
	
	/**
	 * Sets a status1 for the whole fight.
	 */
	public void setStatus1(Side side, Status status1) {
		ArrayList<Status> status1List = getStatus1List(side);
		for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
			status1List.add(status1);
	}
	
	/**
	 * Sets a combination of status2_3 for the whole fight.
	 */
	public void setStatuses2_3(Side side, EnumSet<Status> statuses2_3) {
		ArrayList<EnumSet<Status>> statuses2_3List = getStatuses2_3List(side);
		statuses2_3List.clear();
		
		for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
			statuses2_3List.add(statuses2_3);
	}
	
	/**
	 * Adds a combination of status2_3.
	 */
	public void addStatuses2_3(Side side, EnumSet<Status> statuses2_3) {
		ArrayList<EnumSet<Status>> statuses2_3List = getStatuses2_3List(side);
		statuses2_3List.add(statuses2_3);
	}

	public void setOrder(ArrayList<ArrayList<Integer>> order) {
		this.order = order; // TODO: is it really working without copying ?
	}
	
	/**
	 * Sets a fixed weather for the whole fight.
	 */
	public void setWeather(Weather weather) {
		this.weathers.clear();
		for(int i = 0; i < MAX_NUMBER_OF_BATTLERS_PER_FIGHT; i++)
			this.weathers.add(weather);
	}
	
	/**
	 * Adds a weather.
	 */
	public void addWeather(Weather weather) {
		this.weathers.add(weather);
	}

	public void setPrintStatRangesOnLvl() {
		this.printStatRangesOnLvl = true;
	}

	public void setPrintStatsOnLvl() {
		this.printStatsOnLvl = true;
	}

	
	public void setVerboseLevel(VerboseLevel verbose) {
		this.verbose = verbose;
	}

	public Boolean isBattleTower() {
		return isBattleTower;
	}

	public void setBattleTower(boolean isBattleTower) {
		this.isBattleTower = isBattleTower;
	}

	public boolean isForcedSingleBattle() {
		return isForcedSingleBattle;
	}
	
	public boolean isForcedDoubleBattle() {
		return isForcedDoubleBattle;
	}
	
	// Without any single/double flag, both flags are set to false.
	// Once one of them is set, they are mutually exclusive
	public void setForcedDoubleBattle() {
		this.isForcedDoubleBattle = true;
		this.isForcedSingleBattle = false;
		this.setForcedMultiTargetDamage();
	}
	
	public void setForcedSingleBattle() {
		this.isForcedDoubleBattle = false;
		this.isForcedSingleBattle = true;
		this.setForcedSingleTargetDamage();
	}
	
	
	public boolean isDoubleBattleSplittingDamage() {
		// General purpose flags have higher priority
		if(isForcedDoubleBattle)
			return true;
		
		if(isForcedSingleBattle)
			return false;
		//
		
		if(isForcedMultiTargetDamage())
			return true;
		
		if(isForcedSingleDamage())
			return false;
		
		return yPartner != null;
	}
	
	public boolean isSharingExp() {
		if(isForcedDoubleBattle())
			return true; // TODO: might need more checks, with number of participants for example ? Or is the name/purpose of this function requiring changes ?
		
		if(isForcedSingleBattle())
			return false;
		// In DP, player+player vs. 2 singles and fakedoubles are split exp.
		//        player+partner vs. 2 singles is normal exp.
		// TODO: Gen 3, Pt, HGSS
		return xPartner == null && yPartner != null || isSingleTrainerForcedToBeFoughtAsDouble(); 
	}	
	
	public boolean isMultiplyingRewardByTwo() {
		// TODO: Gen 3, Pt, HGSS
		return isSingleTrainerForcedToBeFoughtAsDouble();
	}

	public int getCurrentHP(Side side) {
		Integer currentHP = null;
		switch(side) {
		case PLAYER: currentHP = xCurrentHP; break;
		case ENEMY:  currentHP = yCurrentHP; break;
		}
		return currentHP;
	}
	
	public void setCurrentHP(Side side, int hp) {
		switch(side) {
		case PLAYER: xCurrentHP = hp; break;
		case ENEMY:  yCurrentHP = hp; break;
		}
	}
	
	public Trainer getPartner(Side side) {
		Trainer partner = null;
		switch(side) {
		case PLAYER: partner = xPartner; break;
		case ENEMY:  partner = yPartner; break;
		}
		
		return partner;
	}
	
	public void setPartner(Side side, Trainer partner) {
		switch(side) {
		case PLAYER: xPartner = partner; break;
		case ENEMY:  yPartner = partner; break;
		}
	}

	public boolean isSingleTrainerForcedToBeFoughtAsDouble() {
		return isSingleTrainerForcedToBeFoughtAsDouble;
	}

	public void setSingleTrainerForcedToBeFoughtAsDouble(boolean b) {
		this.isSingleTrainerForcedToBeFoughtAsDouble = b;
	}

	public boolean isPostponedExp() {
		return isPostponedExp;
	}

	public void setPostponedExp(boolean isPostponedExp) {
		this.isPostponedExp = isPostponedExp;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public boolean isBacktrackingAfterBattle() {
		return isBacktrackingAfterBattle;
	}

	public void setBacktrackingAfterBattle() {
		this.isBacktrackingAfterBattle = true;
	}

	public boolean isForcedMultiTargetDamage() {
		return forcingMultiTargetDamage;
	}

	public void setForcedMultiTargetDamage() {
		this.forcingMultiTargetDamage = true;
		this.forcingSingleTargetDamage = false;
	}

	public boolean isForcedSingleDamage() {
		return forcingSingleTargetDamage;
	}

	public void setForcedSingleTargetDamage() {
		this.forcingSingleTargetDamage = true;
		this.forcingMultiTargetDamage = false;
	}
	
	public CurrentHPid getCurrentHPid(Side side) {
		CurrentHPid id = null;
		switch(side) {
		case PLAYER: id = xCurrentHPid; break;
		case ENEMY:  id = yCurrentHPid; break;
		}
		
		return id;
	}
	
	public void setCurrentHPid(Side side, CurrentHPid id) {
		switch(side) {
		case PLAYER: xCurrentHPid = id; break;
		case ENEMY:  yCurrentHPid = id; break;
		}
	}

	/**
	 * Increments the current enemy battler count, and returns the new value.
	 */
	public int incrementCurrentOpponentIndex() {
		this.currentOpponentIndex += 1;
		return this.currentOpponentIndex;
	}
	
	public int backtrackCurrentOpponentIndex(int batchSize) {
		this.currentOpponentIndex -= batchSize;
		return this.currentOpponentIndex;
	}
	
	public int getNumberOfParticipants() {
		return this.getSxps().get(this.currentOpponentIndex);
	}
	
	private int setNumberOfParticipants(int nbOfParticipants) {
		return this.getSxps().set(this.currentOpponentIndex, nbOfParticipants);
	}

	public int getCurrentOpponentIndex() {
		return currentOpponentIndex;
	}
	
	public void updateStatModifiersAndOptions(Pokemon player, Pokemon enemy, boolean isPostponedExperience) {
    	int currentOpponentIndex = this.incrementCurrentOpponentIndex();
    	
    	/* **************************** */
    	/* Non side-dependent variables */
    	/* **************************** */
    	
    	// Experience
    	this.setPostponedExp(isPostponedExperience); // TODO: put postponed exp in StatMod ?
    	
    		// Split exp if player+player vs. double split
    	int nbOfParticipants = 1; // TODO : hardcoded
    	if(yPartner != null && xPartner == null) {
    		nbOfParticipants = 2;  // TODO : hardcoded
    	}
    	
    		// Overrides natural player+player vs. double split
    	if(!sxps.isEmpty()) {
            nbOfParticipants = this.sxps.get(currentOpponentIndex);
        }
    	
    	this.setNumberOfParticipants(nbOfParticipants);
    	
    	// Weather | TODO: Find a way to get rid of duplicate weather
		Weather weather;
        if(!weathers.isEmpty())
            weather = weathers.get(currentOpponentIndex);
        else
        	weather = Weather.NONE; // TODO: hardcoded default
    	getStatModifier(Side.PLAYER).setWeather(weather);
    	getStatModifier(Side.ENEMY).setWeather(weather);
    	    	
    	
    	/* ************************ */
    	/* Side-dependent variables */
    	/* ************************ */
    	
		for(Side side : Side.values()) {
			Pokemon attacker = null, defender = null;
			Integer currentHP = null;
			Side otherSide = null;
			switch(side) {
			case PLAYER: attacker = player; defender = enemy;  currentHP = xCurrentHP; otherSide = Side.ENEMY;  break;
			case ENEMY:  attacker = enemy;  defender = player; currentHP = yCurrentHP; otherSide = Side.PLAYER; break;
			}
			
			StatModifier attackerMod = this.getStatModifier(side);
			StatModifier defenderMod = this.getStatModifier(otherSide);
			
			// IV variation for player ?
			if(side == Side.PLAYER)
				attackerMod.setIVvariation(this.isIVvariation());
			if(side == Side.ENEMY)
				defenderMod.setIVvariation(this.isIVvariation());
			
			// Overriding currentHP if necessary
			switch(getCurrentHPid(side)) { // TODO: hardcoded constants
			case FULL:  currentHP = attacker.getStatValue(Stat.HP);   break;
			case HALF:  currentHP = attacker.getStatValue(Stat.HP)/2; break;
			case THIRD: currentHP = attacker.getStatValue(Stat.HP)/3; break;
			case CUSTOM_VALUE: break;
			}
			attackerMod.setCurrHP(currentHP);
			
			
			// Applying abilities stat modifications
            	// Download
				// TODO: Doesn't properly work for Double Battles, as it seems to calculate average Def & Spd to choose which offensive stat to boost
            if(attacker.getAbility() == Ability.DOWNLOAD) {
            	int def = defenderMod.modStat(Stat.DEF, defender.getStatValue(Stat.DEF), defender.getAbility() == Ability.SIMPLE);
            	int spd = defenderMod.modStat(Stat.SPD, defender.getStatValue(Stat.SPD), defender.getAbility() == Ability.SIMPLE);
            	if(spd <= def) {
            		if(!isForcedStat(side, Stat.SPD))
            			incrementStatUntilBattleEnds(side, Stat.SPD);
            	} else {
            		if(!isForcedStat(side, Stat.DEF))
            			incrementStatUntilBattleEnds(side, Stat.DEF);
            	}
            }
            
            	// Speed Boost
            if(attacker.getAbility() == Ability.SPEED_BOOST && !isForcedStat(side, Stat.SPE))
    			incrementStatUntilBattleEnds(side, Stat.SPE);
				
            	// Intimidate | TODO: Guard Spec, more ?
            if(defender.getAbility() == Ability.INTIMIDATE 
            		&& attacker.getAbility() != Ability.HYPER_CUTTER && attacker.getAbility() != Ability.CLEAR_BODY && attacker.getAbility() != Ability.WHITE_SMOKE
            		&& !isForcedStat(side, Stat.ATK))
    			decrementStatUntilBattleEnds(side, Stat.ATK);
            
			// X Items
			for(Stat stat : Stat.values()) {
				if(stat == Stat.HP)
					continue;

				int stage;
				if(!this.getStages(side, stat).isEmpty())
					stage = this.getStages(side, stat).get(currentOpponentIndex);
				else
					stage = ContainerType.STAT_STAGES.getDefaultValue();

				attackerMod.setStage(stat, stage); //mod.setStage(stat, stage);
			}
			
			// Status
			Status status1;
			if(!getStatus1List(side).isEmpty())
				status1 = getStatus1List(side).get(currentOpponentIndex);
			else
				status1 = Status.noStatus1();
			attackerMod.setStatus1(status1);
			
			EnumSet<Status> statuses2_3;
			if(!getStatuses2_3List(side).isEmpty())
				statuses2_3 = getStatuses2_3List(side).get(currentOpponentIndex);
			else
				statuses2_3 = Status.noStatus2_3();
			attackerMod.setStatuses2_3(statuses2_3);
		} // end side loop
	}
	
	public void incrementStatUntilBattleEnds(Side side, Stat stat, int stage) {
		ArrayList<Integer> stages = getStages(side, stat);
		for(int i = 0; i < stages.size(); i++) {
			int curr = stages.get(i);
			int next = ContainerType.STAT_STAGES.boundStatOnly(curr + stage);
			stages.set(i, next);
		}
	}
	
	public void incrementStatUntilBattleEnds(Side side, Stat stat) {
		incrementStatUntilBattleEnds(side, stat, 1);
	}
	public void decrementStatUntilBattleEnds(Side side, Stat stat) {
		incrementStatUntilBattleEnds(side, stat, -1);
	}

	public void setIVvariation(boolean isIVvariation) {
		this.isIVvariation = isIVvariation;
	}
	
	public boolean isIVvariation() {
		return isIVvariation;
	}
}
