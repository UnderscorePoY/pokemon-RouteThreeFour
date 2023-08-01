package tool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Ability {
	// GEN 3
	// Source : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/include/constants/abilities.h#L4
	// Regex : #define\s*ABILITY_(\w*)\s\d+\s*
	// Replacement : \1,\n
	
	NONE,
	STENCH,
	DRIZZLE,
	SPEED_BOOST,
	BATTLE_ARMOR,
	STURDY,
	DAMP,
	LIMBER,
	SAND_VEIL,
	STATIC,
	VOLT_ABSORB,
	WATER_ABSORB,
	OBLIVIOUS,
	CLOUD_NINE,
	COMPOUND_EYES,
	INSOMNIA,
	COLOR_CHANGE,
	IMMUNITY,
	FLASH_FIRE,
	SHIELD_DUST,
	OWN_TEMPO,
	SUCTION_CUPS,
	INTIMIDATE,
	SHADOW_TAG,
	ROUGH_SKIN,
	WONDER_GUARD,
	LEVITATE,
	EFFECT_SPORE,
	SYNCHRONIZE,
	CLEAR_BODY,
	NATURAL_CURE,
	LIGHTNING_ROD,
	SERENE_GRACE,
	SWIFT_SWIM,
	CHLOROPHYLL,
	ILLUMINATE,
	TRACE,
	HUGE_POWER,
	POISON_POINT,
	INNER_FOCUS,
	MAGMA_ARMOR,
	WATER_VEIL,
	MAGNET_PULL,
	SOUNDPROOF,
	RAIN_DISH,
	SAND_STREAM,
	PRESSURE,
	THICK_FAT,
	EARLY_BIRD,
	FLAME_BODY,
	RUN_AWAY,
	KEEN_EYE,
	HYPER_CUTTER,
	PICKUP,
	TRUANT,
	HUSTLE,
	CUTE_CHARM,
	PLUS,
	MINUS,
	FORECAST,
	STICKY_HOLD,
	SHED_SKIN,
	GUTS,
	MARVEL_SCALE,
	LIQUID_OOZE,
	OVERGROW,
	BLAZE,
	TORRENT,
	SWARM,
	ROCK_HEAD,
	DROUGHT,
	ARENA_TRAP,
	VITAL_SPIRIT,
	WHITE_SMOKE,
	PURE_POWER,
	SHELL_ARMOR,
	CACOPHONY,
	AIR_LOCK,
	
	// GEN 4
	TANGLED_FEET,
	MOTOR_DRIVE,
	RIVALRY,
	STEADFAST,
	SNOW_CLOAK,
	GLUTTONY,
	ANGER_POINT,
	UNBURDEN,
	HEATPROOF,
	SIMPLE,
	DRY_SKIN,
	DOWNLOAD,
	IRON_FIST,
	POISON_HEAL,
	ADAPTABILITY,
	SKILL_LINK,
	HYDRATION,
	SOLAR_POWER,
	QUICK_FEET,
	NORMALIZE,
	SNIPER,
	MAGIC_GUARD,
	NO_GUARD,
	STALL,
	TECHNICIAN,
	LEAF_GUARD,
	KLUTZ,
	MOLD_BREAKER,
	SUPER_LUCK,
	AFTERMATH,
	ANTICIPATION,
	FOREWARN,
	UNAWARE,
	TINTED_LENS,
	FILTER,
	SLOW_START,
	SCRAPPY,
	STORM_DRAIN,
	ICE_BODY,
	SOLID_ROCK,
	SNOW_WARNING,
	HONEY_GATHER,
	FRISK,
	RECKLESS,
	MULTITYPE,
	FLOWER_GIFT,
	BAD_DREAMS,
	;
	
	/**
	 * Returns the ability associated with the string, or null if there's no correspondence.
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static Ability getAbilityFromString(String value) {
		IgnoreCaseString ics = new IgnoreCaseString(value);
        for(Ability v : values())
            if(ics.equals(v.name())) return v;
        return null;
    }
	
    // Gen 3 Emerald : https://github.com/pret/pokeemerald/blob/master/src/pokemon.c#L2288-L2292
	// Gen 4 DP : https://github.com/pret/pokediamond/blob/master/arm9/src/pokemon.c#L341-L346
	// Gen 4 HGSS : https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L241-L249
	/**
	 * Returns the ability from a personality value (common to Gen 3 and 4).
	 */
    public static Ability getAbilityFromPersonalityValue(Species species, int personalityValue) {
    	if(species.getAbility2() == Ability.NONE)
    		return species.getAbility1();
    	
    	return (personalityValue & 1) == 0 ? species.getAbility1() : species.getAbility2();
    }
	
	public int getScritStage() {
		if (this == SUPER_LUCK)
			return 1;
		else
			return 0;
	}
	
	@Override
	public String toString() {
		return this.name().replace("_", " "); // TODO : better handling, or enough ?
	}
	
	public String noSpaces() {
		return this.name().replace("_", ""); // TODO : better handling, or enough ?
	}
	
	/**
	 * Abilities bypassed by Mold Breaker.
	 */
	private static Set<Ability> ignorableAbilites = new HashSet<Ability>();
	static {
		Collections.addAll(ignorableAbilites,
				BATTLE_ARMOR,
				CLEAR_BODY,
				DAMP, DRY_SKIN,
				FILTER, FLASH_FIRE, FLOWER_GIFT,
				HEATPROOF, HYPER_CUTTER, 
				IMMUNITY, INNER_FOCUS, INSOMNIA, 
				KEEN_EYE, 
				LEAF_GUARD, LEVITATE, LIGHTNING_ROD, LIMBER,
				MAGMA_ARMOR, MARVEL_SCALE, MOTOR_DRIVE,
				OBLIVIOUS, OWN_TEMPO, 
				SAND_VEIL, SHELL_ARMOR, SHIELD_DUST, SIMPLE, SNOW_CLOAK, SOLID_ROCK, SOUNDPROOF, STICKY_HOLD, STORM_DRAIN, STURDY, SUCTION_CUPS,
				TANGLED_FEET, THICK_FAT,
				UNAWARE, 
				VITAL_SPIRIT, VOLT_ABSORB,
				WATER_ABSORB, WATER_VEIL, WHITE_SMOKE, WONDER_GUARD);
	}
	
	/**
	 * Returns true if, and only if, this ability is bypassed by Mold Breaker.
	 */
	public boolean isIgnorable() {
		return ignorableAbilites.contains(this);
	}

	
	public boolean avoidsRecoil() {
		return this == ROCK_HEAD || this == MAGIC_GUARD;
	}

	boolean ignoresWeather() {
		return this == AIR_LOCK || this == CLOUD_NINE;
	}
}
