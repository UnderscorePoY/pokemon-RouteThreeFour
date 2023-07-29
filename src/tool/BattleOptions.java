package tool;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;
import tool.exception.route.RouteParserException;

public class BattleOptions {
	public static final int MIN_NUMBER_OF_BATTLERS_PER_FIGHT = 1, MAX_NUMBER_OF_BATTLERS_PER_FIGHT = 12;
	public static final int MIN_NUMBER_OF_EXP_SHARERS_PER_FIGHT = 0, MAX_NUMBER_OF_EXP_SHARERS_PER_FIGHT = 6; // 0 means no exp
	public static final int DEFAULT_NB_OF_EXP_SHARERS = 1;
	public static final int MIN_BATCH_SIZE = 1, MAX_BATCH_SIZE = 2;

	/**
	 * Index compatible with array/list indexing. BEWARE of miss-by-one errors.
	 */
	private int currentOpponentIndex = -1; // -1 allows to update stuff at the beginning of any needed loop, instead of at the end
    private ArrayList<Integer> sxps = new ArrayList<Integer>();
    private boolean isSxpConstant = false;
    
    private EnumMap<Stat, ArrayList<Integer>> xStages = new EnumMap<Stat, ArrayList<Integer>>(Stat.class);
    private EnumSet<Stat> xStageIsConstant = EnumSet.noneOf(Stat.class);
    private EnumSet<Stat> xStagesAreForced = EnumSet.noneOf(Stat.class);
    private ArrayList<Status> xStatus1List = new ArrayList<Status>();
    private boolean isXStatus1Constant = false;
    private ArrayList<EnumSet<Status>> xStatuses2_3List = new ArrayList<EnumSet<Status>>();
    private boolean isXStatuses2_3Constant = false;
    private int xCurrentHP = 0;
    private CurrentHPid xCurrentHPid = CurrentHPid.FULL;
    private Trainer xPartner = null;
    private boolean xHasUsedSingleTimeAbility = false;

    private EnumMap<Stat, ArrayList<Integer>> yStages = new EnumMap<Stat, ArrayList<Integer>>(Stat.class);
    private EnumSet<Stat> yStageIsConstant = EnumSet.noneOf(Stat.class);
    private EnumSet<Stat> yStagesAreForced = EnumSet.noneOf(Stat.class);
    private ArrayList<Status> yStatus1List = new ArrayList<Status>();
    private boolean isYStatus1Constant = false;
    private ArrayList<EnumSet<Status>> yStatuses2_3List = new ArrayList<EnumSet<Status>>();
    private boolean isYStatuses2_3Constant = false;
    private int yCurrentHP = 0;
    private CurrentHPid yCurrentHPid = CurrentHPid.FULL;
    private Trainer yPartner = null;
    private boolean yHasUsedSingleTimeAbility = false; // TODO: not needed ?
    
    private ArrayList<ArrayList<Integer>> order = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Weather> weathers = new ArrayList<Weather>();
    private boolean isWeatherConstant = false;
    
    private boolean isBattleTower = false;
    private boolean forcingMultiTargetDamage = false;
    private boolean forcingSingleTargetDamage = false;
    private boolean isForcedDoubleBattle = false;
    private boolean isForcedSingleBattle = false;
    private boolean isSingleTrainerForcedToBeFoughtAsDouble = false;
    
    
    private StatModifier mod1 = new StatModifier();
    private StatModifier mod2 = new StatModifier();
    
    private ArrayList<ArrayList<Pokemon>> newPartySeparatedInBatches;
    
    //private boolean isPostponedExp = false;
    
    /**
     * Potential indices are from 1 to max number of battlers
     */
    private Set<Integer> postponeExpSet = new HashSet<Integer>();
    
    private String scenarioName = null;
    private boolean isBacktrackingAfterBattle = false;

    private VerboseLevel verbose = Settings.verboseLevel;
    private boolean printStatsOnLvl = Settings.showStatsOnLevelUp;
    private boolean printStatRangesOnLvl = Settings.showStatRangesOnLevelUp;
    private boolean isIVvariation = Settings.defaultIvVariation;
    

    
    public BattleOptions(boolean isInseparableDoubleBattle) throws ToolInternalException {
        this();
        this.setSingleTrainerForcedToBeFoughtAsDouble(isInseparableDoubleBattle);
    }
    
    public BattleOptions() throws ToolInternalException {
    	// Only makes sure there's no null
    	for(Stat stat : Stat.stagesStats) {
    		xStages.put(stat, new ArrayList<Integer>());
    		yStages.put(stat, new ArrayList<Integer>());
    	}
    }

    /*
	public int getParticipants() {
		return participants;
	}
	*/
    
    public boolean isPostponedExp(int index) {
    	return postponeExpSet.contains(index);
    }
    
    

    /**
     * Performs another set of sanity checks, and updates options which were awaiting the final battle info.
     * Throws errors if a check doesn't pass.
     */
    public void compileAndValidate(Battleable opponent) throws RouteParserException, ToolInternalException {
    	int nbOfBattlers = getTotalNbOfEnemyBattlers(opponent);
    	
    	// Preparing data that were waiting for total nb of battlers
		this.updateStages(nbOfBattlers);
		this.updateOrderAndPostponedExp(opponent);
		// TODO: At this point, the postponedExp indices are in sync with the new order, 
		//         but this new order is not already applied to the Battleable. 
		//       Currently, the Battleable order is updated in the Battle.doBattle routine, by fear of :
		//        - introducing side effects in the party management, 
		//        - having a trainer party filled with the yPartner party in addition with a non-null yPartner
		//       There's probably a better way to do all this.
		
		if(opponent instanceof Trainer && ((Trainer)opponent).getTrainerName().equals("TATE&LIZA"))
			System.out.println("BattleOptions.compileAndValidate");
		
		this.updateSxps(opponent, nbOfBattlers);
		this.updateStatus1List(nbOfBattlers);
		this.updateStatus2_3List(nbOfBattlers);
		this.updateWeathers(nbOfBattlers);		
		
		// Verify options have the correct length
		// - stat stages
		{
	    	for(Side side : Side.values()) {
	    		for(Stat stat : Stat.stagesStats) {
	    			int numberOfStages = this.getStages(side, stat).size();
	    			if(numberOfStages != nbOfBattlers) {
	    				throw new RouteParserException(String.format("in %s's %s stat stages, received '%s' parameters in total, expected '%s'.", 
	    						side, stat, numberOfStages, nbOfBattlers));
	    			}
	    		}
	    	}
		}
    	
    	// - shared exp
		{
	    	int numberOfSxps = this.getSxps().size();
			if(numberOfSxps != nbOfBattlers)
	    		throw new RouteParserException(String.format("in shared exp, received '%s' parameters in total, expected '%s'.", numberOfSxps, nbOfBattlers));	
		}
		
    	// - order
		{
	    	ArrayList<ArrayList<Integer>> order = this.getOrder();
	    	int nbOfOrder = 0;
	    	for(ArrayList<Integer> batch : order) {
	    		for(int index : batch) {
	    			nbOfOrder++;
	    			if(index > nbOfBattlers)
	    	    		throw new RouteParserException(String.format("in enemy order, received '%s' as parameter, maximum possible index is '%s'.", index, nbOfBattlers));
	    		}
	    	}
	    	if(nbOfOrder != nbOfBattlers)
	    		throw new RouteParserException(String.format("in enemy order, received '%s' parameters in total, expected '%s'.", nbOfOrder, nbOfBattlers));
		}

		// - status 1
		{
			for(Side side : Side.values()) {
				int nbOfStatus1 = this.getStatus1List(side).size();
				if(nbOfStatus1 != nbOfBattlers)
    	    		throw new RouteParserException(String.format("in %s primary status, received '%s' parameters in total, expected '%s'.", side, nbOfStatus1, nbOfBattlers));
			}
		}
		
		// - status 2_3
		{
			for(Side side : Side.values()) {
				int nbOfStatus2_3 = this.getStatuses2_3List(side).size();
				if(nbOfStatus2_3 != nbOfBattlers)
    	    		throw new RouteParserException(String.format("in %s seondary status, received '%s' parameters in total, expected '%s'.", side, nbOfStatus2_3, nbOfBattlers));
			}
		}
		
		// - weather
		{
			int nbOfWeather = getWeathers().size();
			if(nbOfWeather != nbOfBattlers)
				throw new RouteParserException(String.format("in weather, received '%s' parameters in total, expected '%s'.", nbOfWeather, nbOfBattlers));
		}
    }
    
    public int getTotalNbOfEnemyBattlers(Battleable opponent) throws ToolInternalException {
    	int nbOfBattlers;
    	if(opponent instanceof Pokemon)
    		nbOfBattlers = 1;
    	else if (opponent instanceof Trainer) {
    		Trainer t = (Trainer) opponent;
    		nbOfBattlers = t.getParty().size();
    		if(this.getPartner(Side.ENEMY) != null)
    			nbOfBattlers += this.getPartner(Side.ENEMY).getParty().size();
    	} else
    		throw new ToolInternalException(Battle.class.getEnclosingMethod(), opponent.getClass(), "Invalid opponent type.");
    
    	return nbOfBattlers;
    }

    
    

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
	
	
	
	private void updateOrderAndPostponedExp(Battleable opponent) throws ToolInternalException {
		if(order.isEmpty()) {
			// Default order : each Pokemon is alone in its batch
			int totalNbOfEnemyBattlers = getTotalNbOfEnemyBattlers(opponent);
			for(int i = 1; i <= totalNbOfEnemyBattlers; i++) {
				ArrayList<Integer> batch = new ArrayList<>();
				batch.add(i);
				order.add(batch);
			}
		}
		// else it's a custom order
		
		this.updatePostponedExpSet();
	}
	
	/**
	 * Updates the set of postponed-exp indices (1-indexed) for the NEW order.
	 * Example : 1/3/2+4 results in the 3rd Pokémon in the new order to have postponed exp, thus 3 in put into the set.
	 */
	private void updatePostponedExpSet() {
		int newIndex = 0;
		for(ArrayList<Integer> batch : order) {
			// Adds any non batch-terminating index
			for(@SuppressWarnings("unused") int i : batch.subList(0, batch.size() - 1)) {
				newIndex++;
				postponeExpSet.add(newIndex);
			}
			newIndex++;
		}
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
	
	public void addSxp(int sxp) {
		sxps.add(sxp);
	}
	
	/**
	 * Sets a fixed number of participants for the whole fight.
	 */
	public void setSxp(int nbOfParticipants) {
		//this.sxps.clear();
		sxps.add(nbOfParticipants);
		isSxpConstant = true;
	}
	
	private void setSxpsFromIndex0(int nbOfParticipants) {
		int sxp = sxps.get(0);
		for(int i = 1; i < nbOfParticipants; i++)
			sxps.add(sxp);
	}
	
	public void updateSxps(Battleable opponent, int nbOfParticipants) {		
		if(!isSxpConstant && !sxps.isEmpty()) // non-constant custom sxps
			return;
		
    	if(isSxpConstant) { // constant custom sxps
			this.setSxpsFromIndex0(nbOfParticipants);
			return;
		}
    	
    	// Default share exp
		int defaultNbOfParticipants = isSingleTrainerForcedToBeFoughtAsDouble ? 2 : DEFAULT_NB_OF_EXP_SHARERS;
			
			// "Weak" override : player+player vs. double is always split exp
		if(yPartner != null && xPartner == null)
			defaultNbOfParticipants = 2; // TODO: hardcoded constant
		

			// "Weak" override : in Gen 3, player+partner vs. double is also split exp
		if(Settings.game.isGen3() && yPartner != null)
			defaultNbOfParticipants = 2; // TODO: hardcoded constant
		
			// "Strong" override
		if(isForcedDoubleBattle)
			defaultNbOfParticipants = 2; // TODO: hardcoded constant
		if(isForcedSingleBattle)
			defaultNbOfParticipants = DEFAULT_NB_OF_EXP_SHARERS; // TODO: hardcoded constant
		
		sxps.add(defaultNbOfParticipants);
		this.setSxpsFromIndex0(nbOfParticipants);
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
	
	public boolean isStageConstant(Side side, Stat stat) {
		Boolean isConstant = null;
		switch(side) {
		case PLAYER: isConstant = xStageIsConstant.contains(stat); break;
		case ENEMY:  isConstant = yStageIsConstant.contains(stat); break;
		}
		return isConstant;
	}
	
	private void setStageIsConstant(Side side, Stat stat) {
		EnumSet<Stat> map = null;
		switch(side) {
		case PLAYER: map = xStageIsConstant; break;
		case ENEMY:  map = yStageIsConstant; break;
		}
		
		map.add(stat);
	}
	
	/**
	 * Sets a fixed stage to a specified side and stat for the whole fight. Stores whether these stages are forced or not for the whole fight.
	 */
	public void setStage(Side side, Stat stat, int stage, boolean isForced) throws ToolInternalException {
		ArrayList<Integer> stages = new ArrayList<>();
		stages.add(stage);		
		this.setStages(side, stat, stages, isForced);
		setStageIsConstant(side, stat);
	}
	
	/**
	 * Update a specific stage from the 0th index once the number of battlers is known.
	 */
	private void setStageFromIndex0(Side side, Stat stat, int nbOfBattlers) throws ToolInternalException {
		ArrayList<Integer> stages = this.getStages(side, stat);
		int stage = stages.get(0);
		for(int i = 1; i < nbOfBattlers; i++) // Only add 'nbOfBattles - 1' times
			stages.add(stage);
	}
	
	/**
	 * Updates stages once the number of battlers is known.
	 */
	public void updateStages(int nbOfBattlers) throws ToolInternalException {
		for(Side side : Side.values()) {
			for(Stat stat : Stat.stagesStats) {
				if(isStageConstant(side, stat))
					this.setStageFromIndex0(side, stat, nbOfBattlers);
				else if(this.getStages(side, stat).isEmpty()) {
					this.getStages(side, stat).add(ContainerType.STAT_STAGES.getDefaultValue()); // fancy way of adding stage 0
					this.setStageFromIndex0(side, stat, nbOfBattlers);
				}
				// else it's custom stages
			}
		}
	}
	
	/*
	/**
	 * Adds a status1.
	 
	public void addStatus1(Side side, Status status1) {
		ArrayList<Status> status1List = getStatus1List(side);
		status1List.add(status1);
	}
	*/
	
	private boolean isStatus1Constant(Side side) {
		Boolean isStatus1Constant = null;
		switch(side) {
		case PLAYER: isStatus1Constant = isXStatus1Constant; break;
		case ENEMY:  isStatus1Constant = isYStatus1Constant; break;
		}
		return isStatus1Constant;
	}
	
	private void setStatus1Constant(Side side) {
		switch(side) {
		case PLAYER: isXStatus1Constant = true; break;
		case ENEMY:  isYStatus1Constant = true; break;
		}
	}
	
	/**
	 * Sets a status1 for the whole fight.
	 */
	public void setStatus1(Side side, Status status1) {
		//ArrayList<Status> status1List = getStatus1List(side);
		//for(int i = 0; i < nbOfBattlers; i++)
		//	status1List.add(status1);

		ArrayList<Status> status1List = getStatus1List(side);
		status1List.add(status1);
		setStatus1Constant(side);
	}
	
	private void populateStatus1ListFromIndex0(Side side, int nbOfBattlers) {
		ArrayList<Status> status1List = this.getStatus1List(side);
		Status status1 = status1List.get(0);
		for(int i = 1; i < nbOfBattlers; i++) {
			status1List.add(status1);
		}
	}
	
	public void updateStatus1List(int nbOfBattlers) {
		for(Side side : Side.values()) {
			if(isStatus1Constant(side)) {
				this.populateStatus1ListFromIndex0(side, nbOfBattlers);
			} else if(getStatus1List(side).isEmpty()) {
				getStatus1List(side).add(Status.noStatus1());
				this.populateStatus1ListFromIndex0(side, nbOfBattlers);
			}
			// else it's custom status1 list
		}
	}
	
	public void addStatus1(Side side, Status status1) {
		getStatus1List(side).add(status1);
	}
	
	
	private boolean isStatuses2_3Constant(Side side) {
		Boolean isStatus2_3Constant = null;
		switch(side) {
		case PLAYER: isStatus2_3Constant = isXStatuses2_3Constant; break;
		case ENEMY:  isStatus2_3Constant = isYStatuses2_3Constant; break;
		}
		return isStatus2_3Constant;
	}
	
	private void setStatuses2_3Constant(Side side) {
		switch(side) {
		case PLAYER: isXStatuses2_3Constant = true; break;
		case ENEMY:  isYStatuses2_3Constant = true; break;
		}
	}
	
	private void populateStatuses2_3ListFromIndex0(Side side, int nbOfBattlers) {
		ArrayList<EnumSet<Status>> statuses2_3List = getStatuses2_3List(side);
		EnumSet<Status> statuses2_3 = statuses2_3List.get(0);
		for(int i = 1; i < nbOfBattlers; i++)
			statuses2_3List.add(statuses2_3);
	}
	
	private void updateStatus2_3List(int nbOfBattlers) {
		for(Side side : Side.values()) {
			if(isStatuses2_3Constant(side)) {
				this.populateStatuses2_3ListFromIndex0(side, nbOfBattlers);
			} else if (getStatuses2_3List(side).isEmpty()) {
				getStatuses2_3List(side).add(Status.noStatus2_3());
				this.populateStatuses2_3ListFromIndex0(side, nbOfBattlers);
			}
			// else it's custom statuses2_3 list
		}
	}
	
	/**
	 * Sets a combination of status2_3 for the whole fight.
	 */
	public void setStatuses2_3(Side side, EnumSet<Status> statuses2_3) {
		ArrayList<EnumSet<Status>> statuses2_3List = getStatuses2_3List(side);
		statuses2_3List.add(statuses2_3);
		setStatuses2_3Constant(side);
	}
	
	
	/**
	 * Adds a combination of status2_3.
	 */
	public void addStatuses2_3(Side side, EnumSet<Status> statuses2_3) {
		ArrayList<EnumSet<Status>> statuses2_3List = getStatuses2_3List(side);
		statuses2_3List.add(statuses2_3);
	}
	

	/*
	public void setOrder(ArrayList<ArrayList<Integer>> order) {
		this.order = order; // TODO: is it really working without copying ?
	}
	*/
	
	public void addOrderBatch(ArrayList<Integer> batch) {
		order.add(batch);
	}
	
	/**
	 * Sets a fixed weather for the whole fight.
	 */
	public void setWeather(Weather weather) {
		this.weathers.add(weather);
		isWeatherConstant = true;
	}
	
	public void addWeather(Weather weather) {
		this.weathers.add(weather);
	}
	
	/*
	/**
	 * Sets weathers.
	 
	public void setWeathers(ArrayList<Weather> weathers) {
		this.weathers = weathers;
	}
	*/
	
	private void setWeatherFromIndex0(int nbOfBattlers) {
		ArrayList<Weather> weathers = getWeathers();
		Weather weather = weathers.get(0);
		for(int i = 1; i < nbOfBattlers; i++)
			weathers.add(weather);
	}
	
	public void updateWeathers(int nbOfBattlers) {
		if(isWeatherConstant)
			setWeatherFromIndex0(nbOfBattlers);
		else if(getWeathers().size() == 0) {
			getWeathers().add(Weather.NONE); // TODO: hardcoded
			setWeatherFromIndex0(nbOfBattlers);
		}
		// else it's custom weather list
	}
	
	
	/*
	/**
	 * Adds a weather.
	 
	public void addWeather(Weather weather) {
		this.weathers.add(weather);
	}
	*/

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
		
		return yPartner != null || isSingleTrainerForcedToBeFoughtAsDouble();
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

	/*
	public boolean isPostponedExp() {
		return isPostponedExp;
	}

	public void setPostponedExp(boolean isPostponedExp) {
		this.isPostponedExp = isPostponedExp;
	}
	*/

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
	
	public int getCurrentNumberOfParticipants() {
		return this.getSxps().get(this.currentOpponentIndex);
	}
	
	public boolean isCurrentPostponedExp() {
		return isPostponedExp(currentOpponentIndex + 1);
	}
	
	/*
	private void setNumberOfParticipants(int nbOfParticipants) {
		//if(currentOpponentIndex < sxps.size())
			sxps.set(this.currentOpponentIndex, nbOfParticipants);
		//sxps.add(nbOfParticipants);
	}
	*/

	public int getCurrentOpponentIndex() {
		return currentOpponentIndex;
	}
	
	/**
	 * Prepares stat modifiers for the player and the enemy based on the current enemy index.
	 * Skipping abilities refers to deactivating automatic stat increases or drops, such as Intimidate or Speed Boost.
	 */
	public void prepareStatModifiers(Pokemon player, Pokemon enemy, boolean isSkipAbilityStatModifs) {
    	int currentOpponentIndex = this.incrementCurrentOpponentIndex();
    	
    	/* **************************** */
    	/* Non side-dependent variables */
    	/* **************************** */
    	
    	/* Done in updateSxps
    	// Experience
    	this.setPostponedExp(isPostponedExperience); // TODO: put postponed exp in StatMod ?
    	
    		// Split exp if player+player vs. double split
    	int nbOfParticipants = 1; // TODO : hardcoded
    	if(yPartner != null && xPartner == null) {
    		nbOfParticipants = 2;  // TODO : hardcoded
    	}
    	
    		// Overrides natural player+player vs. double split
    	if(!sxps.isEmpty() && currentOpponentIndex < sxps.size()) {
            nbOfParticipants = this.sxps.get(currentOpponentIndex);
        }
    	
    	this.setNumberOfParticipants(nbOfParticipants);
    	*/
    	
    	
    	
    	
    	/* Done in updateWeathers
    	// Weather | TODO: Find a way to get rid of duplicate weather
		Weather weather;
        if(!weathers.isEmpty())
            weather = weathers.get(currentOpponentIndex);
        else
        	weather = Weather.NONE; // TODO: hardcoded default
        */
        
    	// TODO: Find a way to get rid of duplicate weather
    	Weather weather = weathers.get(currentOpponentIndex);
    	getStatModifier(Side.PLAYER).setWeather(weather);
    	getStatModifier(Side.ENEMY).setWeather(weather);
    	
    	
    	/* ************************ */
    	/* Side-dependent variables */
    	/* ************************ */
    	
		for(Side attackerSide : Side.values()) {
			Pokemon attacker = null, defender = null;
			Integer currentHP = null;
			Side defenderSide = null;
			switch(attackerSide) {
			case PLAYER: attacker = player; defender = enemy;  currentHP = xCurrentHP; defenderSide = Side.ENEMY;  break;
			case ENEMY:  attacker = enemy;  defender = player; currentHP = yCurrentHP; defenderSide = Side.PLAYER; break;
			}
			
			StatModifier attackerMod = this.getStatModifier(attackerSide);
			StatModifier defenderMod = this.getStatModifier(defenderSide);
			
			// IV variation for player ?
			if(attackerSide == Side.PLAYER)
				attackerMod.setIVvariation(this.isIVvariation());
			if(attackerSide == Side.ENEMY)
				defenderMod.setIVvariation(this.isIVvariation());
			
			// Overriding currentHP if necessary
			switch(getCurrentHPid(attackerSide)) { // TODO: hardcoded constants
			case FULL:  currentHP = attacker.getStatValue(Stat.HP);   break;
			case HALF:  currentHP = attacker.getStatValue(Stat.HP)/2; break;
			case THIRD: currentHP = attacker.getStatValue(Stat.HP)/3; break;
			case CUSTOM_VALUE: break;
			}
			attackerMod.setCurrHP(currentHP);
			
			
			// Applying abilities stat modifications
			if(!isSkipAbilityStatModifs) {
	            // Download
				// TODO: Doesn't properly work for Double Battles, as it seems to calculate average Def & Spd to choose which offensive stat to boost
	            if(attacker.getAbility() == Ability.DOWNLOAD) {
	            	int def = defenderMod.modStat(Stat.DEF, defender.getStatValue(Stat.DEF), defender.getAbility() == Ability.SIMPLE);
	            	int spd = defenderMod.modStat(Stat.SPD, defender.getStatValue(Stat.SPD), defender.getAbility() == Ability.SIMPLE);
	            	if(spd <= def) {
	            		if(!isForcedStat(attackerSide, Stat.SPD))
	            			incrementStatUntilBattleEnds(attackerSide, Stat.SPD);
	            	} else {
	            		if(!isForcedStat(attackerSide, Stat.DEF))
	            			incrementStatUntilBattleEnds(attackerSide, Stat.DEF);
	            	}
	            }
	            
	            // Speed Boost
	            if(attacker.getAbility() == Ability.SPEED_BOOST && !isForcedStat(attackerSide, Stat.SPE))
	    			incrementStatUntilBattleEnds(attackerSide, Stat.SPE);
				
	            if(defender.getSpecies().matchesAny("GYARADOS") && attacker.getSpecies().matchesAny("GEODUDE") && attacker.getLevel() == 8)
	            	System.out.println("BattleOptions.prepareStatModifiers");
	            
	            // Intimidate
	            if(defender.getAbility() == Ability.INTIMIDATE 
	            		&& attacker.getAbility() != Ability.HYPER_CUTTER && attacker.getAbility() != Ability.CLEAR_BODY && attacker.getAbility() != Ability.WHITE_SMOKE
	            		&& !isForcedStat(attackerSide, Stat.ATK)
	            		&& !hasUsedSingleTimeAbility(defenderSide)) {
	            	if(attackerSide == Side.PLAYER)
	            		decrementStatUntilBattleEnds(attackerSide, Stat.ATK);
	            	else if (attackerSide == Side.ENEMY)
	            		decrementEnemyStatForCurrentIndex(Stat.ATK);
	            		
	    			setHasUsedSingleTimeAbility(defenderSide, true);
	            }
			}
            
			// X Items
			for(Stat stat : Stat.stagesStats) {
				int stage = this.getStages(attackerSide, stat).get(currentOpponentIndex);
				attackerMod.setStage(stat, stage);
			}
			
			// Status
			Status status1 = getStatus1List(attackerSide).get(currentOpponentIndex);
			attackerMod.setStatus1(status1);

			EnumSet<Status> statuses2_3 = getStatuses2_3List(attackerSide).get(currentOpponentIndex);
			attackerMod.setStatuses2_3(statuses2_3);
		} // end side loop
	}
	
	public void decrementEnemyStatForCurrentIndex(Stat stat) {
		int curr = yStages.get(stat).get(currentOpponentIndex);
		int next = ContainerType.STAT_STAGES.boundStatOnly(curr - 1);
		yStages.get(stat).set(currentOpponentIndex, next);
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

	public void disablePostponedExperience() {
		postponeExpSet.remove(currentOpponentIndex + 1);
	}

	public ArrayList<ArrayList<Pokemon>> getNewPartySeparatedInBatches() {
		return newPartySeparatedInBatches;
	}
	
	private boolean hasUsedSingleTimeAbility(Side side) {
		Boolean hasUsedSingleTimeAbility = null;
		switch(side) {
		case PLAYER : hasUsedSingleTimeAbility = xHasUsedSingleTimeAbility; break;
		case ENEMY :  hasUsedSingleTimeAbility = yHasUsedSingleTimeAbility; break;
		}
		return hasUsedSingleTimeAbility;
	}
	
	private void setHasUsedSingleTimeAbility(Side side, boolean b) {
		switch(side) {
		case PLAYER: xHasUsedSingleTimeAbility = b; break;
		case ENEMY:  yHasUsedSingleTimeAbility = b; break;
		}
	}
	
	public void resetSingleTimeAbility(Side side) {
		setHasUsedSingleTimeAbility(side, false);
	}
}
