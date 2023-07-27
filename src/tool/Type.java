package tool;
import java.util.EnumMap;

import tool.exception.ToolInternalException;

public enum Type {
	//TODO : handle effectiveness and Type.MYSTERY properly
    NORMAL, FIGHTING, FLYING, POISON, GROUND, ROCK, BUG, GHOST, FIRE, WATER, GRASS, ELECTRIC, PSYCHIC, ICE, DRAGON, STEEL, DARK, MYSTERY, NONE;
	
	public static final int SUPER_EFFECTIVE_MULT = 20;
    public static final int NEUTRAL_MULT = 10, NEUTRAL_TWO_TYPES_MULT = NEUTRAL_MULT * NEUTRAL_MULT;
    public static final int NOT_VERY_EFFECTIVE_MULT = 5;
    public static final int IMMUNE_MULT = 0;
    
    public static final int EFFECTIVENESS_DENOM = 10;
    
	/**
	 * Applies type effectiveness to damage.
	 * Accounts for whether ghosts can be hit or not, or if gravity is applied.
	 */
    public static int applyTypeEffectiveness(int damage, Type atk, Type def, boolean ghostRevealed, boolean isGravity) {
    	damage = damage * effectivenessMultiplier(atk, def, ghostRevealed, isGravity) / EFFECTIVENESS_DENOM;
    	return damage;
    }

	/**
	 * Retrieves type effectiveness numerator (based on the fact denominator is 10).
	 * Accounts for whether ghosts can be hit or not, or if gravity is applied.
	 */
    private static int effectivenessMultiplier(Type atkType, Type defType, boolean ghostRevealed, boolean isGravity) {
        if (atkType == NONE // TODO : is it handling Struggle properly now ?
        ||  defType == NONE // No second type
        || (atkType == Type.NORMAL || atkType == Type.FIGHTING) && defType == Type.GHOST && ghostRevealed // can hit Ghost with Normal/Fighting
        ||  atkType == Type.GROUND && defType == Type.FLYING && isGravity // can hit Flying with Ground
        ) { 
            return NEUTRAL_MULT; // TODO : hardcoded (could become desync with the type effectiveness table)
        } else {
            int val = typeTable[atkType.typeIndex()][defType.typeIndex()];
            return val;
        }
    }
    
    /**
     * Returns true if the atk type can't hit the combination of def1 and def2 types.
	 * Accounts for whether ghosts can be hit or not, or if gravity is applied.
     */
    public static boolean isImmune(Type atk, Type def1, Type def2, boolean ghostRevealed, boolean isGravity) {
    	return IMMUNE_MULT == effectivenessMultiplier(atk, def1, ghostRevealed, isGravity) * effectivenessMultiplier(atk, def2, ghostRevealed, isGravity);
    }
    
    /**
     * Returns true if the atk type is super effective against the combination of def1 and def2 types.
	 * Accounts for whether ghosts can be hit or not, or if gravity is applied.
     */
    public static boolean isSuperEffective(Type atk, Type def1, Type def2, boolean ghostRevealed, boolean isGravity) {
    	return effectivenessMultiplier(atk, def1, ghostRevealed, isGravity) * effectivenessMultiplier(atk, def2, ghostRevealed, isGravity) > NEUTRAL_TWO_TYPES_MULT;
    }
    
    /**
     * Returns true if the atk type is not very effective against the combination of def1 and def2 types (immune returns false).
	 * Accounts for whether ghosts can be hit or not, or if gravity is applied.
     */
    public static boolean isNotVeryEffective(Type atk, Type def1, Type def2, boolean ghostRevealed, boolean isGravity) {
    	return !isImmune(atk, def1, def2, ghostRevealed, isGravity)
    			&& effectivenessMultiplier(atk, def1, ghostRevealed, isGravity) * effectivenessMultiplier(atk, def2, ghostRevealed, isGravity) < NEUTRAL_TWO_TYPES_MULT;
    }
    

    /** 
     * Returns index associated with this type.
     * Type.NONE returns -1.
     */
    public int typeIndex() {
        switch (this) {
        case NORMAL:   return 0;
        case FIGHTING: return 1;
        case FLYING:   return 2;
        case POISON:   return 3;
        case GROUND:   return 4;
        case ROCK:     return 5;
        case BUG:      return 6;
        case GHOST:    return 7;
        case FIRE:     return 8;
        case WATER:    return 9;
        case GRASS:    return 10;
        case ELECTRIC: return 11;
        case PSYCHIC:  return 12;
        case ICE:      return 13;
        case DRAGON:   return 14;
        case STEEL:    return 15;
        case DARK:     return 16;
        case MYSTERY:  return 7; //TODO
        default:       return -1; // NONE
            
        }
    }
    
    public static Type getHiddenPowerTypeFromInt(int i) throws ToolInternalException {
    	switch(i) {
    	case 0 : return FIGHTING;
    	case 1 : return FLYING;
    	case 2 : return POISON;
    	case 3 : return GROUND;
    	case 4 : return ROCK;
    	case 5 : return BUG;
    	case 6 : return GHOST;
    	case 7 : return STEEL;
    	case 8 : return FIRE;
    	case 9 : return WATER;
    	case 10: return GRASS;
    	case 11: return ELECTRIC;
    	case 12: return PSYCHIC;
    	case 13: return ICE;
    	case 14: return DRAGON;
    	case 15: return DARK;
    	default:
    		throw new ToolInternalException(Type.class.getEnclosingMethod(), Integer.valueOf(i), "This index shouldn't be encountered.");
    	}
    }

    private static final int __i = IMMUNE_MULT, nve = NOT_VERY_EFFECTIVE_MULT, __n = NEUTRAL_MULT, _se = SUPER_EFFECTIVE_MULT;
    // typeTable[i][j] is type i's effectiveness against type j
    private static final int[][] typeTable = { // TODO : externalize type table ? Would be useful for rom hacks
    		               //NOR, FIG, FLY, POI, GRO, ROC, BUG, GHO, FIR, WAT, GRA, ELE, PSY, ICE, DRA, STE, DAR
            /* NORMAL */   { __n, __n, __n, __n, __n, nve, __n, __i, __n, __n, __n, __n, __n, __n, __n, nve, __n },
            /* FIGHTING */ { _se, __n, nve, nve, __n, _se, nve, __i, __n, __n, __n, __n, nve, _se, __n, _se, _se },
            /* FLYING */   { __n, _se, __n, __n, __n, nve, _se, __n, __n, __n, _se, nve, __n, __n, __n, nve, __n },
            /* POISON */   { __n, __n, __n, nve, nve, nve, __n, nve, __n, __n, _se, __n, __n, __n, __n, __i, __n },
            /* GROUND */   { __n, __n, __i, _se, __n, _se, nve, __n, _se, __n, nve, _se, __n, __n, __n, _se, __n },
            /* ROCK */     { __n, nve, _se, __n, nve, __n, _se, __n, _se, __n, __n, __n, __n, _se, __n, nve, __n },
            /* BUG */      { __n, nve, nve, nve, __n, __n, __n, nve, nve, __n, _se, __n, _se, __n, __n, nve, _se },
            /* GHOST */    { __i, __n, __n, __n, __n, __n, __n, _se, __n, __n, __n, __n, _se, __n, __n, nve, nve },
            /* FIRE */     { __n, __n, __n, __n, __n, nve, _se, __n, nve, nve, _se, __n, __n, _se, nve, _se, __n },
            /* WATER */    { __n, __n, __n, __n, _se, _se, __n, __n, _se, nve, nve, __n, __n, __n, nve, __n, __n },
            /* GRASS */    { __n, __n, nve, nve, _se, _se, nve, __n, nve, _se, nve, __n, __n, __n, nve, nve, __n },
            /* ELECTRIC */ { __n, __n, _se, __n, __i, __n, __n, __n, __n, _se, nve, nve, __n, __n, nve, __n, __n },
            /* PSYCHIC */  { __n, _se, __n, _se, __n, __n, __n, __n, __n, __n, __n, __n, nve, __n, __n, nve, __i },
            /* ICE */      { __n, __n, _se, __n, _se, __n, __n, __n, nve, nve, _se, __n, __n, nve, _se, nve, __n },
            /* DRAGON */   { __n, __n, __n, __n, __n, __n, __n, __n, __n, __n, __n, __n, __n, __n, _se, nve, __n },
            /* STEEL */    { __n, __n, __n, __n, __n, _se, __n, __n, nve, nve, __n, nve, __n, _se, __n, nve, __n },
            /* DARK */     { __n, nve, __n, __n, __n, __n, __n, _se, __n, __n, __n, __n, _se, __n, __n, nve, nve }, 
    };

    public boolean isGen3PhysicalType() {
        return (this.typeIndex() >= Type.NORMAL.typeIndex() && this.typeIndex() <= Type.GHOST.typeIndex())
                || this == Type.STEEL || this == Type.NONE;
    }
    
    // Used to apply effectiveness multiplier in the correct order
    static EnumMap<Type, Integer> typeEffectivenessPrecedenceRules = new EnumMap<Type, Integer>(Type.class);
    static {
    	typeEffectivenessPrecedenceRules.put(Type.NORMAL, 0);
    	typeEffectivenessPrecedenceRules.put(Type.FIRE, 1);
    	typeEffectivenessPrecedenceRules.put(Type.WATER, 2);
    	typeEffectivenessPrecedenceRules.put(Type.ELECTRIC, 3);
    	typeEffectivenessPrecedenceRules.put(Type.GRASS, 4);
    	typeEffectivenessPrecedenceRules.put(Type.ICE, 5);
    	typeEffectivenessPrecedenceRules.put(Type.FIGHTING, 6);
    	typeEffectivenessPrecedenceRules.put(Type.POISON, 7);
    	typeEffectivenessPrecedenceRules.put(Type.GROUND, 8);
    	typeEffectivenessPrecedenceRules.put(Type.FLYING, 9);
    	typeEffectivenessPrecedenceRules.put(Type.PSYCHIC, 10);
    	typeEffectivenessPrecedenceRules.put(Type.BUG, 11);
    	typeEffectivenessPrecedenceRules.put(Type.ROCK, 12);
    	typeEffectivenessPrecedenceRules.put(Type.GHOST, 13);
    	typeEffectivenessPrecedenceRules.put(Type.DRAGON, 14);
    	typeEffectivenessPrecedenceRules.put(Type.DARK, 15);
    	typeEffectivenessPrecedenceRules.put(Type.STEEL, 16);
    	
    	typeEffectivenessPrecedenceRules.put(Type.NONE, 99);
    }
     
    /**
     * Retrieves first defender type to apply type effectiveness to.
     */
    public static Type getType1ByPrecedence(Type type1, Type type2) {
    	if (typeEffectivenessPrecedenceRules.get(type1) > typeEffectivenessPrecedenceRules.get(type2))
    		return type2;
    	return type1;
    }
    
    /**
     * Retrieves second defender type to apply type effectiveness to.
     */
    public static Type getType2ByPrecedence(Type type1, Type type2) {
    	if (typeEffectivenessPrecedenceRules.get(type1) <= typeEffectivenessPrecedenceRules.get(type2))
    		return type2;
    	return type1;
    }

}
