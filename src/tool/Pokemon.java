package tool;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import tool.Happiness.HappinessEvent;
import tool.StatsContainer.ContainerType;
import tool.exception.ToolInternalException;

public class Pokemon implements Battleable {
	public static final int MAX_LEVEL = 100;
	
    private Species species;
    private Gender gender;
    private int level;
    private Nature nature;
    private Ability ability;
    private StatsContainer ivs;
    private StatsContainer evs;
    private StatsContainer evs_used;
    private StatsContainer statsWithoutAnyBadgeBoosts;
    private StatsContainer statsWithApplicableBadgeBoosts;
    private StatsContainer statsWithForcedBadgeBoosts;

    private int totalExp;
    private Moveset moves;
    private boolean wild;
    private Item heldItem = null;
    private boolean hasPokerus = false;
    private boolean hasBoostedExp = false;
    private Set<Stat> badges = new HashSet<Stat>();
    private int happiness;
    private boolean isInLuxuryBall = false;
      
    /**
     * Creates a Pokemon. Constructor compatible with default trainers (no item, default moveset).
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, int fixedIV) {
    	this(species, gender, level, nature, ability, fixedIV, Moveset.defaultMoveset(species, level), Item.DEFAULT);
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with item, default moveset trainers.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, int fixedIV, Item heldItem) {
    	this(species, gender, level, nature, ability, fixedIV, Moveset.defaultMoveset(species, level), heldItem);
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with no item, custom moveset trainers.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, int fixedIV, Moveset moves) {
    	this(species, gender, level, nature, ability, fixedIV, moves, Item.DEFAULT);
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with all trainers (aside from Battle Tower, etc.).
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, int fixedIV, Moveset moves, Item heldItem) {
    	this(species, gender, level, nature, ability, new StatsContainer(ContainerType.IV, fixedIV), moves, heldItem);
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with minimal-option wilds.
     */
    public Pokemon(Species species, int level) {
    	this(species, Gender.predominantGender(species), level, Nature.DEFAULT, species.getAbility1());
    }
    
    
    /**
     * Creates a Pokemon. Constructor compatible with non-custom-ivs wilds.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability) {
    	this(species, gender, level, nature, ability, new StatsContainer(ContainerType.IV));
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with all wilds.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, StatsContainer ivs) {
    	this(species, gender, level, nature, ability, ivs, false, false);
        this.wild = true;
    }
    
    /**
     * Creates a Pokemon. Constructor compatible with trainers with custom IVs.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, StatsContainer ivs, Moveset moveset, Item heldItem) {
    	this(species, gender, level, nature, ability, ivs, false, false);
    	this.setMoveset(moveset);
    	this.setItem(heldItem);
    }
    
    
    /**
     * Creates a Pokemon. Constructor compatible with starting player Pokemon.
     */
    public Pokemon(Species species, Gender gender, int level, Nature nature, Ability ability, StatsContainer ivs, boolean hasBoostedExp, boolean hasPokerus) {
        this.species = species;
        this.gender = gender;
        this.level = level;
        this.nature = nature;
        this.ability = ability;
        this.ivs = ivs;
        this.hasBoostedExp = hasBoostedExp;
        this.hasPokerus = hasPokerus;
        
        this.evs = new StatsContainer(ContainerType.EV);
        this.evs_used = new StatsContainer(ContainerType.EV);
        this.statsWithoutAnyBadgeBoosts = new StatsContainer(ContainerType.STATS);
        this.statsWithApplicableBadgeBoosts = new StatsContainer(ContainerType.STATS);
        this.statsWithForcedBadgeBoosts = new StatsContainer(ContainerType.STATS);
        this.moves = Moveset.defaultMoveset(species, level);
        this.wild = false;
        this.happiness = species.getFriendship();
        
        this.calculateStats();
        this.setExpForLevel();
    }
    
    public Pokemon(Pokemon p) { // Copy constructor
        this.species = p.species;
        this.gender = p.gender;
        this.level = p.level;
        this.nature = p.nature;
        this.ability = p.ability;
        this.ivs = new StatsContainer(p.ivs);
        this.evs = new StatsContainer(p.evs);
        this.evs_used = new StatsContainer(p.evs_used);
        this.statsWithoutAnyBadgeBoosts = new StatsContainer(p.statsWithoutAnyBadgeBoosts);
        this.statsWithApplicableBadgeBoosts = new StatsContainer(p.statsWithApplicableBadgeBoosts);
        this.statsWithForcedBadgeBoosts = new StatsContainer(p.statsWithForcedBadgeBoosts);
        this.totalExp = p.totalExp;
        this.moves = new Moveset(p.moves);
        this.wild = p.wild;
        this.heldItem = p.heldItem;
        this.hasPokerus = p.hasPokerus;
        this.hasBoostedExp = p.hasBoostedExp;
        this.badges = new HashSet<Stat>(p.badges);
        this.happiness = p.getHappiness();
    }

    /*TODO Battle Tower poke
    public Pokemon(Species s, int newLevel, Nature nat, Moveset moves, IVs ivs, int hp, int atk, int def, int spe, int spA, int spD) {
        species = s;
        level = newLevel;
        nature = nat;
        this.ivs = ivs;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.spa = spA;
        this.spd = spD;
        this.spe = spe;
        this.moves = moves;
        this.battleTower = true;
        setExpForLevel();
    }
    */

    /*
    // TODO constructor which accepts EVs
    public void setZeroEVs() {
        ev_hp = ev_hp_used = 0;
        ev_atk = ev_atk_used = 0;
        ev_def = ev_def_used = 0;
        ev_spa = ev_spa_used = 0;
        ev_spd = ev_spd_used = 0;
        ev_spe = ev_spe_used = 0;
    }
    */
    
	/**
	 * Returns stat value without badge boost. Used in Battle Tower battles in Gen 3, and in Gen 4.
	 */
	public int getTrueStatValue(Stat stat) {
		return statsWithoutAnyBadgeBoosts.get(stat);
	}
	
	/**
	 * Returns stat value with applicable badge boost. Used in overworld battles in Gen 3.
	 */
	public int getStatValue(Stat stat) {
		return statsWithApplicableBadgeBoosts.get(stat);
	}
	

    // call this to update your stats
    // automatically called on level ups/rare candies, but not just from gaining
    // stat EV
    public void updateEVsAndCalculateStats() {
    	updateEVs();
    	calculateStats();
    }
    
    
    
    public void calculateStats() {
    	for(Stat stat : statsWithoutAnyBadgeBoosts.getContainerType()) {
    		int trueStatValue = calculateNonBoostedStat(stat, ivs.get(stat), nature);
    		statsWithoutAnyBadgeBoosts.put(stat, trueStatValue);
    		statsWithApplicableBadgeBoosts.put(stat, applyBadgeBoostIfPossible(stat, trueStatValue));
    		statsWithForcedBadgeBoosts.put(stat, applyBadgeBoost(trueStatValue));
    	}
    		
		/*
        hp = calcHPWithIV(ivs.getHPIV());
        atk = calcAtkWithIV(ivs.getAtkIV(), nature);
        def = calcDefWithIV(ivs.getDefIV(), nature);
        spa = calcSpaWithIV(ivs.getSpaIV(), nature);
        spd = calcSpdWithIV(ivs.getSpdIV(), nature);
        spe = calcSpeWithIV(ivs.getSpeIV(), nature);
    	*/
    }
    
    public void updateEVs() {
    	for(Stat stat : evs.getContainerType())
    		evs_used.put(stat, evs.get(stat));
    }

    private int calculateNonBoostedStat(Stat stat, int iv, Nature nature) {
    	switch(stat) {
    	case HP : return calcHPStat(iv);
    	default : return calcNonHPStat(stat, iv, nature);
    	}
    }
    
    private int calcHPStat(int iv) {
        return calcStatNumerator(iv, species.getBaseStat(Stat.HP), getEVused(Stat.HP)) * level / 100
                + level + 10;
    }
    
    private int calcNonHPStat(Stat stat, int iv, Nature nat) {
        int statValue = calcStatNumerator(iv, species.getBaseStat(stat), getEVused(stat)) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, statValue);
    }

    /*
    private int calcAtkWithIV(int iv, Nature nat) {
        int stat = calcStatNumerator(iv, species.getBaseAtk(), ev_atk_used) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, Stat.ATK);
    }

    private int calcDefWithIV(int iv, Nature nat) {
    	int stat = calcStatNumerator(iv, species.getBaseDef(), ev_def_used) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, Stat.DEF);
    }

    private int calcSpaWithIV(int iv, Nature nat) {
    	int stat = calcStatNumerator(iv, species.getBaseSpa(), ev_spa_used) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, Stat.SPA);
    }

    private int calcSpdWithIV(int iv, Nature nat) {
    	int stat = calcStatNumerator(iv, species.getBaseSpd(), ev_spd_used) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, Stat.SPD);
    }
    
    private int calcSpeWithIV(int iv, Nature nat) {
    	int stat = calcStatNumerator(iv, species.getBaseSpe(), ev_spe_used) * level
                / 100 + 5;
        return nat.getAlteredStat(stat, Stat.SPE);
    }
	*/
    
    private int evCalc(int ev) {
        return ev / 4;
    }
    

    private int calcStatNumerator(int iv, int base, int ev) {
        return 2 * base + iv + evCalc(ev);
    }
    
    public static final int BADGE_BOOST_NUM = 11, BADGE_BOOST_DENOM = 10; // TODO: somewhere else ?
    public int applyBadgeBoost(int value) {
    	return value * BADGE_BOOST_NUM / BADGE_BOOST_DENOM;
    }
    

    /**
     * Applies badge boost only if battle tower mode is off and there's a badge boost to apply to this stat.
     */
    public int applyBadgeBoostIfPossible(Stat stat, int value) {
    	return hasBadge(stat) && !Constants.isBattleTower ? applyBadgeBoost(value) : value;
    }


    /**
     * Gives this Pokémon the lowest possible experience for its level.
     */
    private void setExpForLevel() {
        totalExp = ExpCurve.lowestExpForLevel(species.getExpCurve(), level);
    }

    public StatsContainer getIVs() {
        return ivs;
    }
    // TODO: EV setter
    
    /**
     * Returns the stat value with forced badge boost if available. Used in display only, in Gen 3.
     */
    public int getStatValueWithForcedBadgeBoost(Stat stat) {
    	return statsWithForcedBadgeBoosts.get(stat);
    }
    // No need for a setter.

    /**
     * Returns the speed value with current evs, and custom iv and nature. Primarily used for calculating speed thresholds.
     */
    public int getSpeedValueWithIVandNature(int iv, Nature nature) {
    	int spe = calculateNonBoostedStat(Stat.SPE, iv, nature);
        return applyBadgeBoostIfPossible(Stat.SPE, spe);
    }

    public Ability getAbility() {
		return ability;
	}

	public void setAbility(Ability ability) {
		this.ability = ability;
	}
	
	
    /**
     * Returns the stat value without badge boost, whether there is one available or not. Battle Tower compliant.
     */
	public int getBattleTowerStatValue(Stat stat) {
		return this.statsWithoutAnyBadgeBoosts.get(stat);
	}
	// No need for a setter
	
    /**
     * Returns the stat value with applicable badge boost. Overworld battle compliant.
     */
	public int getBattleStatValue(Stat stat) {
		return this.statsWithApplicableBadgeBoosts.get(stat);
	}
	// No need for a setter
	
    /**
     * Returns the stat value with forced badge boost.
     */
	public int getBoostedStatValue(Stat stat) {
		return this.statsWithApplicableBadgeBoosts.get(stat);
	}
	// No need for a setter
	
	/*
	// not affected by badge boosts
    public int getTrueAtk() {
        return atk;
    }

    public int getTrueDef() {
        return def;
    }

    public int getTrueSpa() {
        return spa;
    }

    public int getTrueSpd() {
        return spd;
    }

    public int getTrueSpe() {
        return spe;
    }
    */

    public int getLevel() {
        return level;
    }

    public Nature getNature() {
    	return nature;
    }
    
    /**
     * Sets nature and recalculates all stats, without updating EVs.
     */
    public void setNature(Nature nat) {
    	nature = nat;
    	calculateStats();
    }

    public void setMoveset(Moveset m) {
        moves = m;
    }

    public Moveset getMoveset() {
        return moves;
    }

    public boolean isWild() {
        return wild;
    }

    public void setWild(boolean isWild) {
        this.wild = isWild;
    }

    public int getTotalExp() {
        return totalExp;
    }

    /*
    /**
     * Returns the yielded experience if shared among a certain number of participants.
     
    public int expGivenWithoutEXPBoost(int participants) {
        return species.getBaseExp() * level / 7 * 3
                / (isWild() ? 3 : 2)
                / participants;
    }
    */
    
    public Item getHeldItem() {
    	return heldItem;
    }
    
    public void setItem(Item newItem) {
    	this.heldItem = newItem;
    }

    public String toString() { // TODO ?
        return getDetailledStatsStr(true);
    }


    /**
     * Returns a user-friendly summary of this pokémon information : level, name, nature, ability, gender, exp, stats, ivs, evs, moves.
     * If allowBoosts is true, displays stats with applicable badge boosts.
     * If allow Boosts is false, displays stats as they would appear in the in-game Pokémon menu.
     */
    public String getDetailledStatsStr(boolean allowBoosts) {
    	String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        StatsContainer stats = allowBoosts ? statsWithApplicableBadgeBoosts : statsWithoutAnyBadgeBoosts;

        sb.append(String.format("%s %s.%s", levelNameNatureAbilityItemStr(), experienceNeededToLevelUpStr(), endl));
        sb.append(String.format("Stats%s:%s",
        		!Settings.game.isGen4() ? String.format(" %s badge boosts", allowBoosts ? "with applicable": "without") : "", 
        		endl));
        sb.append(String.format("%s%s", statsWithoutAnyBadgeBoosts.getStatsHeaderStr(badges, nature), endl));
        sb.append(String.format("%s%s", stats.getStatsValuesStr(), endl));
        sb.append(String.format("%s%s", ivs.getStatsValuesStr(ContainerType.STATS), endl));
        sb.append(String.format("%s %s%s", evs_used.getStatsValuesStr(ContainerType.STATS), "(at last level up)", endl));
        sb.append(String.format("%s %s%s", evs.getStatsValuesStr(ContainerType.STATS), "(current)", endl));
        sb.append(String.format("Happiness : %s/%s%s", this.getHappiness(), Happiness.MAX, endl));
        sb.append(String.format("Moves : %s%s", moves.toString(), endl));
        
        return sb.toString();
    }
    
    /**
     * Returns an one-liner with current level and experience needed to next level.
     */
    public String levelAndExperienceNeededToLevelUpStr() {
    	return String.format("LVL: %d | %s", level, experienceNeededToLevelUpStr());
    }
    
    public String experienceNeededToLevelUpStr() {
    	return String.format("EXP needed: %d/%d", expToNextLevel(), expForLevel());
    }
    
    /*
    public String statsWithBoost() {
        String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        sb.append(levelNameNatureAbility() + " ");
        sb.append("EXP Needed: " + expToNextLevel() + "/" + expForLevel()
                + endl);
        sb.append("Stats WITH badge boosts:" + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", "HP",
                atkBadge ? "*ATK" : "ATK", defBadge ? "*DEF" : "DEF",
                spaBadge ? "*SPA" : "SPA", spdBadge ? "*SPD" : "SPD",
                speBadge ? "*SPE" : "SPE")
                + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", getHP(),
                getAtk(), getDef(), getSpa(), getSpd(), getSpe()) + endl);
        sb.append(String.format("IV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s",
                ivs.getHPIV(), ivs.getAtkIV(), ivs.getDefIV(), ivs.getSpaIV(),
                ivs.getSpdIV(), ivs.getSpeIV()) + endl);
        sb.append(String.format("EV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", ev_hp,
                ev_atk, ev_def, ev_spa, ev_spd, ev_spe) + endl);
        sb.append(moves.toString() + endl);
        return sb.toString();
    }

    public String statsWithoutBoost() {
        String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        sb.append(levelNameNatureAbility() + " ");
        sb.append("EXP Needed: " + expToNextLevel() + "/" + expForLevel()
                + endl);
        sb.append("Stats WITHOUT badge boosts:" + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", "HP",
                "ATK", "DEF", "SPA", "SPD", "SPE") + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", getHP(),
                getTrueAtk(), getTrueDef(), getTrueSpa(),getTrueSpd(), getTrueSpe())
                + endl);
        sb.append(String.format("IV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s",
                ivs.getHPIV(), ivs.getAtkIV(), ivs.getDefIV(), ivs.getSpaIV(),
                ivs.getSpdIV(), ivs.getSpeIV()) + endl);
        sb.append(String.format("EV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", ev_hp,
                ev_atk, ev_def, ev_spa, ev_spd, ev_spe) + endl);
        sb.append(moves.toString() + endl);
        return sb.toString();
    }
    */

    // utility getters
    public String levelNameNatureAbilityItemStr() {
        return String.format("L%d %s(%s) [%s] #%s#%s", 
        		level, getDisplayName(), gender.getInitial(), nature, ability,
        		heldItem != null ? String.format(" <%s>", heldItem) : "");
    }

    // only for hash
    public String pokeName() {
        return getSpecies().getHashName();
    }
    
    public String getDisplayName() {
    	return getSpecies().getDisplayName();
    }
    
    /*
    public String pokeNameFixed() {
    	return getSpecies().getName().replace("\\u2642", " M").replace("\\u2640", " F"); // TODO : hacky
    }
	*/
    
    /*
    private static final String INLINE_STATS_SEPARATOR = "/";
    public String getInlineStatsStr() {
    	StringBuilder sb = new StringBuilder();
    	for(Stat stat : s.getContainerType())
    		sb.append(String.format("%d%s", getStatValue(stat), INLINE_STATS_SEPARATOR));
    	
    	return sb.substring(0, sb.length() - 1);
    	
        //return String.format("%s/%s/%s/%s/%s/%s", getHP(), getAtk(), getDef(),
        //        getSpa(), getSpd(), getSpe());
       
    }
    */
    public String getInlineTrueStatsStr() {
    	return statsWithoutAnyBadgeBoosts.getInlineStatsStr();
    	/*
    	StringBuilder sb = new StringBuilder();
    	for(Stat stat : stats.getContainerType())
    		sb.append(String.format("%d%s", getPrimaryStatValue(stat), INLINE_STATS_SEPARATOR));
    	
    	return sb.substring(0, sb.length() - 1);
    	*/
        //return String.format("%s/%s/%s/%s/%s/%s", getHP(), getTrueAtk(), getTrueDef(),
        //        getTrueSpa(), getTrueSpd(), getTrueSpe());
    	 
    }
    

    // experience methods
    // exp needed to get to next level
    public int expToNextLevel() {
        return ExpCurve.expToNextLevel(species.getExpCurve(), level, totalExp);
    }

    // total exp needed to get from this level to next level (no partial exp)
    public int expForLevel() {
        return ExpCurve.expForLevel(species.getExpCurve(), level);
    }

    // in game actions

    // gain num exp
    public int earnedExpFrom(Pokemon other, int nbOfParticipants) throws ToolInternalException {
    	if(nbOfParticipants == 0)
    		return 0;
    	
    	int earnedExp = other.species.getBaseExp() * other.getLevel() / 7;
    	if(Settings.game.isGen3()) {
    		// https://github.com/pret/pokeemerald/blob/39192725f22351678c9cfe2c79ce025d6802110c/src/battle_script_commands.c#L3222
    		earnedExp /= nbOfParticipants;
    		
    		if(heldItem != null && heldItem.isBoostingExperience())
    			earnedExp = earnedExp * 150 / 100;
    		
    		if(!other.isWild())
    			earnedExp = earnedExp * 150 / 100;
    		
    		if(hasBoostedExp)
    			earnedExp = earnedExp * 150 / 100;
    		
    	} else if (Settings.game.isGen4()) {
    		if(!other.isWild())
    			earnedExp = earnedExp * 150 / 100;
    		
    		earnedExp /= nbOfParticipants;
    		
    		if(hasBoostedExp)
    			earnedExp = earnedExp * 150 / 100;
    		
    		if(heldItem != null && heldItem.isBoostingExperience())
    			earnedExp = earnedExp * 150 / 100;
    	} else {
    		throw new ToolInternalException(null, "earnedExpFrom", "Gen not implemented");
    	}
    	
    	return earnedExp;
    }
    
    private void gainExp(Pokemon other, int nbOfParticipants) throws ToolInternalException {
    	totalExp += earnedExpFrom(other, nbOfParticipants);
        
        // update lvl if necessary
        while (expToNextLevel() <= 0 && level < MAX_LEVEL) {
            level++;
            this.setHappiness(HappinessEvent.LEVEL_UP.getFinalHappiness(this.getHappiness(), heldItem != null && heldItem.isBoostingHappiness(), this.isInLuxuryBall(), false));
            updateEVsAndCalculateStats();
        }
    }
    
    /*
    private void gainExp(int exp) throws ToolInternalException {
    	final int TRADE_EXP_NUM = 3, TRADE_EXP_DENOM = 2; // TODO : move constants somewhere else ?
    	final int ITEM_EXP_NUM = 3, ITEM_EXP_DENOM = 2; // TODO : move constants somewhere else ?
    	
    	if(hasBoostedExp)
    		exp = exp * TRADE_EXP_NUM / TRADE_EXP_DENOM;
    	if(heldItem != null && heldItem.isBoostingExperience())
    		exp = exp * ITEM_EXP_NUM / ITEM_EXP_DENOM;
        totalExp += exp;
        
        // update lvl if necessary
        while (expToNextLevel() <= 0 && level < MAX_LEVEL) {
            level++;
            this.setHappiness(HappinessEvent.LEVEL_UP.getFinalHappiness(this.getHappiness(), heldItem != null && heldItem.isBoostingHappiness(), this.isInLuxuryBall(), false));
            updateEVsAndCalculateStats();
        }
    }
    */

    // gain stat exp from a pokemon of species s
    private void gainEvs(Species s) {
    	int DEFAULT_MULTIPLIER = 1; // TODO : move constants somewhere else ?
    	int POKERUS_MULTIPLIER = 2;
    	int EV_BOOSTING_ITEM_MULTIPLIER = 2;
    	int SPECIFIC_EV_BOOST = 4;
    	
    	int pokerusMultiplier = hasPokerus ? POKERUS_MULTIPLIER : DEFAULT_MULTIPLIER;
    	
    	for(Stat stat : evs.getContainerType()) {
    		int heldItemMultiplier = heldItem != null && heldItem.isBoostingAllEVs(stat) ? EV_BOOSTING_ITEM_MULTIPLIER : DEFAULT_MULTIPLIER;
    		int yield = s.getEvYield(stat);
    		if(heldItem != null && heldItem.isBoostingSpecificEv(stat))
    			yield += SPECIFIC_EV_BOOST;
    		
    		yield *= pokerusMultiplier * heldItemMultiplier;
    		
    		this.evs.addWithBound(stat, yield);
    	}
    }

    @Override
    public void battle(Pokemon p, BattleOptions options) throws ToolInternalException {
        // p is the one that gets leveled up
        // this is the one that dies like noob
        // be sure to gain EVs before the exp
    	// if no participants (for example a death), don't give any ev or exp
    	if (options.getNumberOfParticipants() > 0) {
	        p.gainEvs(this.getSpecies());
	        //p.gainExp(this.expGivenWithoutEXPBoost(options.getNumberOfParticipants()));
	        p.gainExp(this, options.getNumberOfParticipants());
    	}
    }

    // gains from eating stat/level boosters
    /**
     * Eats a Rare Candy, i.e. triggers a flat level up. Returns true if the Candy is effectively consumed.
     */
    public boolean eatRareCandy() throws ToolInternalException {
        if (level < 100) { // TODO : hardcoded
            level++;
            this.setHappiness(HappinessEvent.LEVEL_UP.getFinalHappiness(this.getHappiness(), heldItem != null && heldItem.isBoostingHappiness(), this.isInLuxuryBall(), false));
            setExpForLevel();
            updateEVsAndCalculateStats();
            return true;
        }
        
        return false;
    }

    private static final int EV_BOOST_FROM_VITAMIN = 10; // TODO
    private static final int EV_LIMIT_FROM_VITAMIN = 100; // TODO
    
    /**
     * Applies an EV boost in the desired stat, returning the effectively gained value.
     */
    public int eatEVBoostingItem(Stat stat) throws ToolInternalException {
    	if(getEV(stat) >= EV_LIMIT_FROM_VITAMIN)
    		return 0;
    	
    	int actuallyAdded = this.evs.addWithBound(stat, EV_BOOST_FROM_VITAMIN);
    	this.setHappiness(HappinessEvent.VITAMIN.getFinalHappiness(this.getHappiness(), heldItem != null && heldItem.isBoostingHappiness(), this.isInLuxuryBall(), false));
    	updateEVsAndCalculateStats();
    	return actuallyAdded;
    }
    
    
    public Species getSpecies() {
        return species;
    }
    
    // TODO: proper evolution
    public void evolve(Species newSpecies) {
    	// Transfer ability
        if(ability == species.getAbility1())
        	ability = newSpecies.getAbility1();
        else
        	ability = newSpecies.getAbility2();
        
        // Set new species
        species = newSpecies;
    }


    public boolean hasBadge(Stat stat) {
    	return this.badges.contains(stat);
    }
    
    public void setBadge(Stat stat) { // TODO : check stat validity ?
    	this.badges.add(stat);
    }
    

    public void setAllBadges() {
    	badges.addAll(Stat.primaryStagesStats);
    }

    public void loseAllBadges() {
    	badges.clear();
    }

    /**
     * Returns a user-friendly iv+nature variation of this Pokémon stats.
     * If allowBoosts is true, displays stats with applicable badge boosts.
     * If allowBoosts is false, displays stats as they would appear in the in-game Pokémon menu.
     */
    public String statRanges(boolean allowBoosts) {
    	final String HEADER_FORMAT = "|%-6s"; // anchored left, length 6
    	final String STAT_FORMAT = "|%4s"; // anchored right, length 4 | TODO : move somewhere else ?
        final int IV_RANGE = ContainerType.IV.getMaxPerStat() - ContainerType.IV.getMinPerStat() + 1;
        
        // isForcedBoost &= Settings.game.isGen3(); // Badge boosts only apply in Gen 3
        
        int[] possibleHPs = new int[IV_RANGE];
        EnumMap<Stat, int[]> decreasedMap = new EnumMap<Stat, int[]>(Stat.class);
        EnumMap<Stat, int[]> neutralMap = new EnumMap<Stat, int[]>(Stat.class);
        EnumMap<Stat, int[]> increasedMap = new EnumMap<Stat, int[]>(Stat.class);
        
        // Populate neutral, no badge boost
        for(Stat stat : statsWithoutAnyBadgeBoosts.getContainerType()) {
    		int[] neutral = new int[IV_RANGE];
        	for(int iv = 0; iv < IV_RANGE; iv++) {
	        	switch(stat) {
	        	case HP: possibleHPs[iv] = calcHPStat(iv); 
	        		break;
	        		
	        	default:
	        		neutral[iv] = calcNonHPStat(stat, iv, Nature.HARDY); // Neutral nature
	        		break;
	        	}
        	}
        	if(stat != Stat.HP)
        		neutralMap.put(stat, neutral);        	
        }
        
        // Populate increased & decreased, then apply badge boost to all depending on the flag
        for(Stat stat: Stat.primaryStagesStats) {
    		int[] neutral = neutralMap.get(stat);
    		int[] increased = new int[IV_RANGE];
    		int[] decreased = new int[IV_RANGE];
        	for(int i = 0; i < IV_RANGE; i++) {
        		int neutralStatValue = neutral[i];
        		increased[i] = Nature.increaseStat(neutralStatValue);
        		decreased[i] = Nature.decreaseStat(neutralStatValue);
        		
        		if(allowBoosts) {
        			increased[i] = applyBadgeBoostIfPossible(stat, increased[i]);
        			neutral[i] = applyBadgeBoostIfPossible(stat, neutral[i]);
        			decreased[i] = applyBadgeBoostIfPossible(stat, decreased[i]);
        		}
        	}
        	increasedMap.put(stat, increased);
        	decreasedMap.put(stat, decreased);
        }
        
        
        String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%s", levelNameNatureAbilityItemStr(), endl));
        sb.append(String.format("Stat ranges%s:%s", 
        		Settings.game.isGen3() ? String.format(" %s badge boosts", allowBoosts ? "with applicable" : "without") : "", 
        		endl));
        

        for(Stat stat : Stat.pokemonMenuStats) {
	        // Header
	        sb.append(String.format(HEADER_FORMAT, "IV"));
	        for(int i = 0; i < IV_RANGE; i++) {
	        	sb.append(String.format(STAT_FORMAT, i));
	        }
	    	sb.append(endl);
	    	if(stat == Stat.HP) {
		        sb.append(String.format(HEADER_FORMAT, "HP"));
		        for (int i = 0; i < IV_RANGE; i++) {
		            sb.append(String.format(STAT_FORMAT, possibleHPs[i]));
		        }
		    	sb.append(endl);
	    	} else { // Non HP
		        HashMap<String, EnumMap<Stat, int[]>> map = new LinkedHashMap<String, EnumMap<Stat, int[]>>();
		        map.put(Nature.DECREASED_NATURE_STR, decreasedMap);
		        map.put(Nature.NEUTRAL_NATURE_STR, neutralMap);
		        map.put(Nature.INCREASED_NATURE_STR, increasedMap);
		
	        	for(Map.Entry<String, EnumMap<Stat, int[]>> entry : map.entrySet()) {
	        		String natureSign = entry.getKey();
	        		EnumMap<Stat, int[]> statsMap = entry.getValue();
	        		
	        		String statNatureSign = String.format("%s%s", stat, natureSign);
		        	sb.append(String.format(HEADER_FORMAT, statNatureSign));
		        	for (int i = 0; i < IV_RANGE; i++) {
		                sb.append(String.format(STAT_FORMAT, statsMap.get(stat)[i]));
		            }
		        	sb.append(endl);
	        	}
	    	}
        	sb.append(endl);
        }

        return sb.toString();
    }

	public boolean hasPokerus() {
		return hasPokerus;
	}

	public void setPokerus(boolean hasPokerus) {
		this.hasPokerus = hasPokerus;
	}

	public boolean hasBoostedExp() {
		return hasBoostedExp;
	}

	public void setBoostedExp(boolean hasBoostedExp) {
		this.hasBoostedExp = hasBoostedExp;
	}
	
	public boolean hasBadgeBoost() {
		return !this.badges.isEmpty();
	}
	
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	
	public int getEV(Stat stat) {
		return this.evs.get(stat);
	}
	
	public int getEVused(Stat stat) {
		return this.evs_used.get(stat);
	}
	
	/**
	 * Puts the ev inside the container, throwing if it's not within container constraints.
	 * Returns the previous value, or null if there was no previous mapping.
	 */
	public Integer setEV(Stat stat, int ev) {
		return this.evs.put(stat, ev);
	}
	// no need to set evs used, because they are always updated all at once

	public int getHappiness() {
		return happiness;
	}

	public void setHappiness(int happiness) throws ToolInternalException {
		if(!Happiness.isInBound(happiness))
			throw new ToolInternalException(Pokemon.class.getEnclosingMethod(), Integer.valueOf(happiness), "");
		this.happiness = happiness;
	}
	
	public void setHappinessBound(int happiness) throws ToolInternalException {
			this.happiness = Happiness.bound(happiness);
	}
	
	public boolean isInLuxuryBall() {
		return isInLuxuryBall;
	}
}
