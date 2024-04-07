package tool;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import tool.exception.ToolInternalException;

/**
 * A container keeping track of certain stat-related values (for example IVs, EVs, stat values, stat stages, etc.).
 * Can be iterated over to retrieve stat-related values in a specific order, either to follow internal mechanisms or to provide user-friendly display purposes.
 */
public class StatsContainer implements Iterable<Integer> {
	
	/**
	 * A type to specialize a stats container. 
	 * Can be iterated over to retrieve stats in a specific order, either to follow internal mechanisms or to provide user-friendly display purposes.
	 */
	static enum ContainerType implements Iterable<Stat> {
		IV(Stat.evCompliantStats,             0,  31,                 Integer.MAX_VALUE,   31),
		EV(Stat.evCompliantStats,             0, 252,                 510,                  0),
		STATS(Stat.pokemonMenuStats,          0, Integer.MAX_VALUE,   Integer.MAX_VALUE,    0),
		STAT_STAGES(Stat.stagesStats,        -6,   6,                 Integer.MAX_VALUE,    0),
		STAT_INCREMENTS(Stat.stagesStats,   -12,  12,                 Integer.MAX_VALUE,    0),
		;
		
		private LinkedHashSet<Stat> stats;
		private int minPerStat, maxPerStat;
		private int maxTotal;
		private int defaultValue;
		
		private ContainerType(LinkedHashSet<Stat> stats, int minPerStat, int maxPerStat, int maxTotal, int defaultValue) {
			this.stats = stats; // no copy
			this.minPerStat = minPerStat;
			this.maxPerStat = maxPerStat;
			this.maxTotal = maxTotal;
			this.defaultValue = defaultValue;
		}

		public int getMinPerStat() {
			return minPerStat;
		}

		public int getMaxPerStat() {
			return maxPerStat;
		}

		public int getMaxTotal() {
			return maxTotal;
		}

		public int getDefaultValue() {
			return defaultValue;
		}

		
		public boolean isCompatibleWith(ContainerType o){
			return this.stats == o.stats; // Only a reference checking | TODO: why not 'this == o' ?
		}
		
		/**
		 * Returns the maximum value to be put, following restrictions for a single stat only.
		 */
		public int boundStatOnly(int statValue) {
			if(statValue > maxPerStat)
				return maxPerStat;
			if(statValue < minPerStat)
				return minPerStat;
			
			return statValue;
		}
		
		public boolean isInStatBound(int i) {
			return minPerStat <= i && i <= maxPerStat;
		}
		

		@Override
		public Iterator<Stat> iterator() {
			return stats.iterator();
		}
	}
	
	
	private EnumMap<Stat, Integer> container;
	private ContainerType containerType;
    
    public StatsContainer(ContainerType containerType, int fixedValue) { // TODO: how to avoid going through stats twice ?
    	this(containerType);
    	for(Stat stat : containerType)
    		this.put(stat, fixedValue);
    }
    
    public StatsContainer(ContainerType containerType) {
    	this.containerType = containerType;
    	this.container = new EnumMap<Stat, Integer>(Stat.class);
    	for(Stat stat : containerType)
    		this.container.put(stat, containerType.getDefaultValue()); // bypass checks
    }
    
    public StatsContainer(StatsContainer o) { // copy constructor
    	this(o.getContainerType());
    	for(Stat stat : o.containerType)
    		this.container.put(stat, o.get(stat)); // bypass checks, as the other object has already been through these
    }
    
    /** Creates custom IVs */
    public StatsContainer(int hp, int atk, int def, int spa, int spd, int spe) {
    	this(ContainerType.IV);
    	this.put(Stat.HP , hp);
    	this.put(Stat.ATK, atk);
    	this.put(Stat.DEF, def);
    	this.put(Stat.SPA, spa);
    	this.put(Stat.SPD, spd);
    	this.put(Stat.SPE, spe);
    }
    
    public void changeContainerType(ContainerType newType) throws IllegalArgumentException {
    	if(!this.containerType.isCompatibleWith(newType)) {
    		throw new IllegalArgumentException(String.format("Can't change container type '%s' to '%s'.", 
    				this.containerType, newType));
    	}
    	
    	this.containerType = newType;
    	
    	for(Stat stat : containerType)
    		this.put(stat, this.get(stat)); // Going through checks with new type
    }
    
    public Integer get(Stat stat) {
    	return this.container.get(stat);
    }
    
    /**
     * Puts the value inside the container, throwing if it's not within container constraints.
     * Returns the previous value, or null if there was no previous mapping.
     */
    public Integer put(Stat stat, int value) throws IllegalArgumentException {
    	if(value != bound(stat, value))
    		throw new IllegalArgumentException(String.format("Tried putting value '%d' in stat '%s' within '%s', breaking allowed ranges of '%d-%d' per stat and '0-%d' for total.", 
				value, stat, StatModifier.class.getName(), containerType.minPerStat, containerType.maxPerStat, containerType.maxTotal));

    	return this.container.put(stat, value);
    }
    
    /**
     * Puts the bounded value inside the container, returning the effectively put value.
     */
    public int putWithBound(Stat stat, int value) {
    	// Should never throw if bound is properly coded ...
    	int boundValue = this.bound(stat, value);
    	this.container.put(stat, boundValue);
    	return boundValue;
    }
    
    /** Doesn't return, puts ivs with no checks */
    public void putAssumingValidIVs(StatsContainer ivs) {
    	for(Stat stat : Stat.pokemonMenuStats)
    		this.container.put(stat , ivs.get(stat));
    }
    
    /**
     * Adds the bounded value inside the container, returning the effectively added value.
     */
    public int addWithBound(Stat stat, int increment) {
    	return putWithBound(stat, get(stat) + increment);
    }
    
    
    
    public int getStatsTotal() {
    	int total = 0;
    	for(int value : this)
    		total += value;
    	
    	return total;
    }
    
    
  
	/**
	 * Returns the maximum value possible within container constraints, maximum being the input.
	 */
	public int bound(Stat stat, int statValue) {
		statValue = containerType.boundStatOnly(statValue);
		
		int newTotal = getStatsTotal() - (get(stat) == null ? containerType.getDefaultValue(): get(stat)) + statValue;
		if(newTotal > containerType.maxTotal) {
			int overflow = newTotal - containerType.maxTotal;
			return statValue - overflow;
		}
		
		return statValue;
	}
	

	public static final String STATS_FORMAT = "%7s";
	public static final String BOOSTED_STR = "*";
	/*
	private String getStatsFormat() {
		StringBuilder sb = new StringBuilder();
		for(@SuppressWarnings("unused") Stat stat : containerType) {
			sb.append(STATS_FORMAT);
		}
		return sb.toString();
	}
	*/

	public String getStatsHeaderStr(Set<Stat> boosts, Nature nature) {
		StringBuilder sb = new StringBuilder();
		for(Stat stat : containerType) {
			String statModifiedStr = String.format("%s%s%s",
					boosts.contains(stat) ? BOOSTED_STR : "",
					stat,
					nature.getIncreased() == stat ? Nature.INCREASED_NATURE_STR : 
				    nature.getDecreased() == stat ? Nature.DECREASED_NATURE_STR : Nature.NEUTRAL_NATURE_STR);			
			sb.append(String.format(STATS_FORMAT, statModifiedStr));
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns a string of the values in this container, in the order of the provided container type.
	 */
	public String getStatsValuesStr(ContainerType type) {
		StringBuilder sb = new StringBuilder();
		
		switch(containerType) {
		case EV: sb.append("EV"); break;
		case IV: sb.append("IV"); break;
		default: sb.append("  "); break;
		}
		
		for(Stat stat : type)
			sb.append(String.format(STATS_FORMAT, get(stat)));
		
		return sb.toString();
	}
	
	public String getStatsValuesStr() {
		return getStatsValuesStr(containerType);
	}
	
    private static final String INLINE_STATS_SEPARATOR = "/";
    public String getInlineStatsStr() {
    	StringBuilder sb = new StringBuilder();
    	for(Stat stat : containerType)
    		sb.append(String.format("%d%s", get(stat), INLINE_STATS_SEPARATOR));
    	
    	return sb.substring(0, sb.length() - 1);
    }
    
    
    public Type getHiddenPowerType() throws UnsupportedOperationException, ToolInternalException {
    	if(containerType != ContainerType.IV)
    		throw new UnsupportedOperationException(String.format("Can't call '%s' if the container is not '%s'.", 
    				StatModifier.class.getEnclosingMethod().getName(), ContainerType.IV));
    	
    	int _hp  = get(Stat.HP)  & 1;
    	int _atk = get(Stat.ATK) & 1;
    	int _def = get(Stat.DEF) & 1;
    	int _spe = get(Stat.SPE) & 1;
    	int _spa = get(Stat.SPA) & 1;
    	int _spd = get(Stat.SPD) & 1;
    	
    	int typeId = (_hp + 2*_atk + 4*_def + 8*_spe + 16*_spa + 32*_spd) * 15 / 63;
    	return Type.getHiddenPowerTypeFromInt(typeId);
    }
    
    public int getHiddenPowerPower() throws UnsupportedOperationException {
    	if(containerType != ContainerType.IV)
    		throw new UnsupportedOperationException(String.format("Can't call '%s' if the container is not '%s'.", 
    				StatModifier.class.getEnclosingMethod().getName(), ContainerType.IV));
    	
    	int _hp  = (get(Stat.HP)  >> 1) & 1;
    	int _atk = (get(Stat.ATK) >> 1) & 1;
    	int _def = (get(Stat.DEF) >> 1) & 1;
    	int _spe = (get(Stat.SPE) >> 1) & 1;
    	int _spa = (get(Stat.SPA) >> 1) & 1;
    	int _spd = (get(Stat.SPD) >> 1) & 1;
    	
    	int power = (_hp + 2*_atk + 4*_def + 8*_spe + 16*_spa + 32*_spd) * 40 / 63 + 30 ;
    	return power;
    }

	public ContainerType getContainerType() {
		return containerType;
	}

	@Override
	public Iterator<Integer> iterator() {
		return container.values().iterator();
	}
	
	@Override
	public String toString() {
		return container.toString();
	}
}
