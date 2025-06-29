package tool;

import tool.exception.ToolInternalException;

public enum MoveTarget {
	//GEN 3
	SELECTED_POKEMON(0),
	SPECIAL(1<<0),
	RANDOM(1<<2),
	BOTH_ENEMIES(1<<3),
	USER(1<<4),
	ALL_EXCEPT_USER(1<<5),
	ENEMY_SIDE(1<<6),
	
	//GEN 4
	USER_SIDE(32),
	ENTIRE_FIELD(64),
	ENEMY_FIELD(128),
	HELPING_HAND(256),
	ACUPRESSURE(512),
	ME_FIRST(1024),
	;
	
	private int flagValue;
	
	private MoveTarget(int flagValue) {
		this.flagValue = flagValue;
	}
	
	public int getFlagValue() {
		return flagValue;
	}
	
	public static MoveTarget getGen3MoveTargetFromInt(int i) throws ToolInternalException {
		switch(i) {
		case 0:    return SELECTED_POKEMON;
		case 1<<0: return SPECIAL;
		case 1<<2: return RANDOM;
		case 1<<3: return BOTH_ENEMIES;
		case 1<<4: return USER;
		case 1<<5: return ALL_EXCEPT_USER;
		case 1<<6: return ENEMY_SIDE;
		default:   throw new ToolInternalException(i, null);
		}
	}
	
	public static MoveTarget getGen4MoveTargetFromInt(int i) throws ToolInternalException {
		switch(i) {
		case 0:    return SELECTED_POKEMON;
		case 1:    return SPECIAL;
		case 2:    return RANDOM;
		case 4:    return BOTH_ENEMIES;
		case 8:    return ALL_EXCEPT_USER;
		case 16:   return USER;
		case 32:   return USER_SIDE;
		case 64:   return ENTIRE_FIELD;
		case 128:  return ENEMY_FIELD;
		case 256:  return HELPING_HAND;
		case 512:  return ACUPRESSURE;
		case 1024: return ME_FIRST;
		default:   throw new ToolInternalException(i, null);
		}
	}
}
