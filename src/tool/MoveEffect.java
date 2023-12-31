package tool;
public enum MoveEffect {
	// GEN3
	// Source : https://github.com/pret/pokeruby/blob/0ea1e7620cc5fea1e651974442052ba9c52cdd13/include/constants/battle_move_effects.h
	// Regex : #define\s*EFFECT_(\w*)\s*.*\s*
	// Replacement : \1,\n
	HIT,
	SLEEP,
	POISON_HIT,
	ABSORB,
	BURN_HIT,
	FREEZE_HIT,
	PARALYZE_HIT,
	EXPLOSION,
	DREAM_EATER,
	MIRROR_MOVE,
	ATTACK_UP,
	DEFENSE_UP,
	SPEED_UP,
	SPECIAL_ATTACK_UP,
	SPECIAL_DEFENSE_UP,
	ACCURACY_UP,
	EVASION_UP,
	ALWAYS_HIT,
	ATTACK_DOWN,
	DEFENSE_DOWN,
	SPEED_DOWN,
	SPECIAL_ATTACK_DOWN,
	SPECIAL_DEFENSE_DOWN,
	ACCURACY_DOWN,
	EVASION_DOWN,
	HAZE,
	BIDE,
	RAMPAGE,
	ROAR,
	MULTI_HIT,
	CONVERSION,
	FLINCH_HIT,
	RESTORE_HP,
	TOXIC,
	PAY_DAY,
	LIGHT_SCREEN,
	TRI_ATTACK,
	REST,
	OHKO,
	RAZOR_WIND,
	SUPER_FANG,
	DRAGON_RAGE,
	TRAP,
	HIGH_CRITICAL,
	DOUBLE_HIT,
	RECOIL_IF_MISS,
	MIST,
	FOCUS_ENERGY,
	RECOIL,
	CONFUSE,
	ATTACK_UP_2,
	DEFENSE_UP_2,
	SPEED_UP_2,
	SPECIAL_ATTACK_UP_2,
	SPECIAL_DEFENSE_UP_2,
	ACCURACY_UP_2,
	EVASION_UP_2,
	TRANSFORM,
	ATTACK_DOWN_2,
	DEFENSE_DOWN_2,
	SPEED_DOWN_2,
	SPECIAL_ATTACK_DOWN_2,
	SPECIAL_DEFENSE_DOWN_2,
	ACCURACY_DOWN_2,
	EVASION_DOWN_2,
	REFLECT,
	POISON,
	PARALYZE,
	ATTACK_DOWN_HIT,
	DEFENSE_DOWN_HIT,
	SPEED_DOWN_HIT,
	SPECIAL_ATTACK_DOWN_HIT,
	SPECIAL_DEFENSE_DOWN_HIT,
	ACCURACY_DOWN_HIT,
	EVASION_DOWN_HIT,
	SKY_ATTACK,
	CONFUSE_HIT,
	TWINEEDLE,
	VITAL_THROW,
	SUBSTITUTE,
	RECHARGE,
	RAGE,
	MIMIC,
	METRONOME,
	LEECH_SEED,
	SPLASH,
	DISABLE,
	LEVEL_DAMAGE,
	PSYWAVE,
	COUNTER,
	ENCORE,
	PAIN_SPLIT,
	SNORE,
	CONVERSION_2,
	LOCK_ON,
	SKETCH,
	UNUSED_60,
	SLEEP_TALK,
	DESTINY_BOND,
	FLAIL,
	SPITE,
	FALSE_SWIPE,
	HEAL_BELL,
	QUICK_ATTACK,
	TRIPLE_KICK,
	THIEF,
	MEAN_LOOK,
	NIGHTMARE,
	MINIMIZE,
	CURSE,
	UNUSED_6E,
	PROTECT,
	SPIKES,
	FORESIGHT,
	PERISH_SONG,
	SANDSTORM,
	ENDURE,
	ROLLOUT,
	SWAGGER,
	FURY_CUTTER,
	ATTRACT,
	RETURN,
	PRESENT,
	FRUSTRATION,
	SAFEGUARD,
	THAW_HIT,
	MAGNITUDE,
	BATON_PASS,
	PURSUIT,
	RAPID_SPIN,
	SONICBOOM,
	UNUSED_83,
	MORNING_SUN,
	SYNTHESIS,
	MOONLIGHT,
	HIDDEN_POWER,
	RAIN_DANCE,
	SUNNY_DAY,
	DEFENSE_UP_HIT,
	ATTACK_UP_HIT,
	ALL_STATS_UP_HIT,
	UNUSED_8D,
	BELLY_DRUM,
	PSYCH_UP,
	MIRROR_COAT,
	SKULL_BASH,
	TWISTER,
	EARTHQUAKE,
	FUTURE_SIGHT,
	GUST,
	FLINCH_HIT_2,
	SOLARBEAM,
	THUNDER,
	TELEPORT,
	BEAT_UP,
	FLY,
	DEFENSE_CURL,
	SOFTBOILED,
	FAKE_OUT,
	UPROAR,
	STOCKPILE,
	SPIT_UP,
	SWALLOW,
	UNUSED_A3,
	HAIL,
	TORMENT,
	FLATTER,
	WILL_O_WISP,
	MEMENTO,
	FACADE,
	FOCUS_PUNCH,
	SMELLINGSALT,
	FOLLOW_ME,
	NATURE_POWER,
	CHARGE,
	TAUNT,
	HELPING_HAND,
	TRICK,
	ROLE_PLAY,
	WISH,
	ASSIST,
	INGRAIN,
	SUPERPOWER,
	MAGIC_COAT,
	RECYCLE,
	REVENGE,
	BRICK_BREAK,
	YAWN,
	KNOCK_OFF,
	ENDEAVOR,
	ERUPTION,
	SKILL_SWAP,
	IMPRISON,
	REFRESH,
	GRUDGE,
	SNATCH,
	LOW_KICK,
	SECRET_POWER,
	DOUBLE_EDGE,
	TEETER_DANCE,
	BLAZE_KICK,
	MUD_SPORT,
	POISON_FANG,
	WEATHER_BALL,
	OVERHEAT,
	TICKLE,
	COSMIC_POWER,
	SKY_UPPERCUT,
	BULK_UP,
	POISON_TAIL,
	WATER_SPORT,
	CALM_MIND,
	DRAGON_DANCE,
	CAMOUFLAGE,
	
	// GEN 4
	NORMAL_HIT,
//	SLEEP,
//	POISON_HIT,
	DRAIN_HP,
//	BURN_HIT,
//	FREEZE_HIT,
//	PARALYZE_HIT,
//	EXPLOSION,
//	DREAM_EATER,
//	MIRROR_MOVE,
//	ATTACK_UP,
//	DEFENSE_UP,
//	SPEED_UP, // unused
	SPATK_UP,
	SPDEF_UP, // unused
//	ACCURACY_UP, // unused
//	EVASION_UP,
//	ALWAYS_HIT,
//	ATTACK_DOWN,
//	DEFENSE_DOWN,
//	SPEED_DOWN,
	SPATK_DOWN, // unused
	SPDEF_DOWN, // unused
//	ACCURACY_DOWN,
//	EVASION_DOWN,
	STATS_RESET,
//	BIDE,
//	RAMPAGE,
	SWITCH_FOE,
	MULTIHIT_2_5,
//	CONVERSION,
//	FLINCH_HIT,
//	RESTORE_HP,
//	TOXIC,
	PAYDAY,
//	LIGHT_SCREEN,
//	TRI_ATTACK,
	SLEEP_USER,
//	OHKO,
//	RAZOR_WIND,
//	SUPER_FANG,
	FIXED_40,
	TRAP_HIT,
	HIGH_CRIT,
//	DOUBLE_HIT,
	JUMP_KICK,
//	MIST,
	CRIT_CHANCE_UP,
	RECOIL_HIT,
//	CONFUSE,
	ATTACK_2UP,
	DEFENSE_2UP,
	SPEED_2UP,
	SPATK_2UP,
	SPDEF_2UP,
	ACCURACY_2UP, // unused
	EVASION_2UP, // unused
//	TRANSFORM,
	ATTACK_2DOWN,
	DEFENSE_2DOWN,
	SPEED_2DOWN,
	SPATK_2DOWN, // unused
	SPDEF_2DOWN,
	ACCURACY_2DOWN, // unused
	EVASION_2DOWN, // unused
//	REFLECT,
//	POISON,
//	PARALYZE,
//	ATTACK_DOWN_HIT,
//	DEFENSE_DOWN_HIT,
//	SPEED_DOWN_HIT,
	SPATK_DOWN_HIT,
	SPDEF_DOWN_HIT,
//	ACCURACY_DOWN_HIT,
//	EVASION_DOWN_HIT, // unused
//	SKY_ATTACK,
//	CONFUSE_HIT,
	POISON_DOUBLE_HIT,
//	VITAL_THROW,
//	SUBSTITUTE,
	HIT_RECHARGE,
//	RAGE,
//	MIMIC,
//	METRONOME,
//	LEECH_SEED,
	NONE,
//	DISABLE,
//	LEVEL_DAMAGE,
	RANDOM_DAMAGE,
//	COUNTER,
//	ENCORE,
//	PAIN_SPLIT,
//	SNORE,
//	CONVERSION_2,
	ENSURE_NEXT_HIT,
//	SKETCH,
	UNK096, // unused
//	SLEEP_TALK,
//	DESTINY_BOND,
	STRONGER_LOW_HP,
//	SPITE,
//	FALSE_SWIPE,
	HEAL_ALL_STATUS,
	PRIORITY_HIT,
//	TRIPLE_KICK,
	STEAL_ITEM,
	PREVENT_ESCAPE,
//	NIGHTMARE,
//	MINIMIZE,
//	CURSE,
	UNK110, // unused
//	PROTECT,
//	SPIKES,
	IDENTIFY,
//	PERISH_SONG,
//	SANDSTORM,
//	ENDURE,
	INCREASING_HIT,
//	SWAGGER,
//	FURY_CUTTER,
//	ATTRACT,
//	RETURN,
//	PRESENT,
//	FRUSTRATION,
//	SAFEGUARD,
	BURN_HIT_THAW,
//	MAGNITUDE,
//	BATON_PASS,
//	PURSUIT,
//	RAPID_SPIN,
	FIXED_20,
	UNK131, // unused
	RESTORE_HP_DAYTIME,
	UNK133, // unused
	UNK134, // unused
//	HIDDEN_POWER, // unused
	RAIN,
	SUNNY,
//	DEFENSE_UP_HIT,
//	ATTACK_UP_HIT,
	STATS_UP_HIT,
	UNK141, // unused
//	BELLY_DRUM,
//	PSYCH_UP,
//	MIRROR_COAT,
//	SKULL_BASH,
//	TWISTER,
//	EARTHQUAKE,
	HIT_LATER,
//	GUST,
	STOMP,
	SOLAR_BEAM,
//	THUNDER,
//	TELEPORT,
//	BEAT_UP,
//	FLY,
//	DEFENSE_CURL,
	UNK157, // unused
//	FAKE_OUT,
//	UPROAR,
//	STOCKPILE,
//	SPIT_UP,
//	SWALLOW,
	UNK163, // unused
//	HAIL,
//	TORMENT,
//	FLATTER,
	BURN,
//	MEMENTO,
//	FACADE,
//	FOCUS_PUNCH,
	SMELLING_SALTS,
//	FOLLOW_ME,
//	NATURE_POWER,
//	CHARGE,
//	TAUNT,
//	HELPING_HAND,
	SWAP_ITEMS,
//	ROLE_PLAY,
//	WISH,
//	ASSIST,
//	INGRAIN,
//	SUPERPOWER,
//	MAGIC_COAT,
//	RECYCLE,
//	REVENGE,
//	BRICK_BREAK,
//	YAWN,
//	KNOCK_OFF,
//	ENDEAVOR,
	STRONGER_HIGH_HP,
//	SKILL_SWAP,
//	IMPRISON,
//	REFRESH,
//	GRUDGE,
//	SNATCH,
	STRONGER_HEAVIER,
//	SECRET_POWER,
	RECOIL_HIT_HARD,
	CONFUSE_ALL,
	BURN_HIT_HIGH_CRIT,
//	MUD_SPORT,
	TOXIC_HIT,
//	WEATHER_BALL,
	HIT_USER_SPATK_2_DOWN,
	ATTACK_DEFENSE_DOWN,
	DEFENSE_SPDEF_UP,
//	SKY_UPPERCUT,
	ATTACK_DEFENSE_UP,
	POISON_HIT_HIGH_CRIT,
//	WATER_SPORT,
	SPATK_SPDEF_UP,
	ATTACK_SPEED_UP,
//	CAMOUFLAGE,
	ROOST,
	GRAVITY,
	MIRACLE_EYE,
	WAKE_UP_SLAP,
	HAMMER_ARM,
	GYRO_BALL,
	HEALING_WISH,
	BRINE,
	NATURAL_GIFT,
	FEINT,
	BUG_BITE,
	TAILWIND,
	ACUPRESSURE,
	METAL_BURST,
	U_TURN,
	CLOSE_COMBAT,
	PAYBACK,
	ASSURANCE,
	EMBARGO,
	FLING,
	PSYCHO_SHIFT,
	TRUMP_CARD,
	HEAL_BLOCK,
	CRUSH_GRIP,
	POWER_TRICK,
	GASTRO_ACID,
	LUCKY_CHANT,
	ME_FIRST,
	COPYCAT,
	POWER_SWAP,
	GUARD_SWAP,
	PUNISHMENT,
	LAST_RESORT,
	WORRY_SEED,
	SUCKER_PUNCH,
	TOXIC_SPIKES,
	HEART_SWAP,
	AQUA_RING,
	MAGNET_RISE,
	FLARE_BLITZ,
	STRUGGLE,
	DIVE,
	DIG,
	SURF,
	DEFOG,
	TRICK_ROOM,
	BLIZZARD,
	WHIRLPOOL,
	VOLT_TACKLE,
	BOUNCE,
	UNK264, // unused
	CAPTIVATE,
	STEALTH_ROCK,
	CHATTER,
	JUDGMENT,
	HEAD_SMASH,
	LUNAR_DANCE,
	SEED_FLARE,
	SHADOW_FORCE,
	FIRE_FANG,
	ICE_FANG,
	THUNDER_FANG,
	CHARGE_BEAM,
	;
	
	public boolean isIncreasedCritRatio() {
		return this == HIGH_CRITICAL || this == BLAZE_KICK || this == RAZOR_WIND || this == SKY_ATTACK // Gen 3
				|| this == HIGH_CRIT || this == BURN_HIT_HIGH_CRIT || this == POISON_HIT_HIGH_CRIT || this == RAZOR_WIND || this == SKY_ATTACK; // Gen 4 (some duplicates)
	}
	
	
	/**
	 * Returns the number by which the damage must be divided to produce the recoil damage. 
	 * Only exception is Struggle in Gen 4, where the division applies to user's maximum HP.
	 * See <a href="https://bulbapedia.bulbagarden.net/wiki/Recoil">this article</a>.
	 */
    public Integer getRecoilDivider() {
    	switch(this) {
    	// Gen 3
    	case RECOIL: // Struggle falls in this category in Gen 3
    		return 4;
    		
    	case DOUBLE_EDGE:
    		return 3;
    		
    	// Gen 4
    	case RECOIL_HIT_HARD:
    	case VOLT_TACKLE:
    	case FLARE_BLITZ:
    		return 3;
    		
    	case HEAD_SMASH:
    		return 2;
    		
    	case RECOIL_HIT:
    		return 4;
    		
    	case STRUGGLE:
    		return 4; // This is applied to user max HP instead
    		
    	default:
    		return null;
    	}
    }
    
    public boolean isRecoil() {
    	return getRecoilDivider() != null;
    }	
}
