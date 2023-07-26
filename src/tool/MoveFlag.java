package tool;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum MoveFlag {
	MAKES_CONTACT(1<<0),
	AFFECTED_BY_PROTECT(1<<1),
	AFFECTED_BY_MAGIC_COAT(1<<2),
	AFFECTED_BY_SNATCH(1<<3),
	MIRROR_MOVE_COMPATIBLE(1<<4),
	AFFECTED_BY_KINGS_ROCK(1<<5);
	
	private int flagValue;
	
	private MoveFlag(int flagValue) {
		this.flagValue = flagValue;
	}
	
	public int getFlagValue() {
		return flagValue;
	}
	
	public static MoveFlag getFlagFromString(String s) {
		return valueOf(s);
	}
	
	public static EnumSet<MoveFlag> getMoveFlags(List<String> strings){
		EnumSet<MoveFlag> set = EnumSet.noneOf(MoveFlag.class);
		for(String s : strings) {
			set.add(getFlagFromString(s));
		}
		
		return set;
	}
	
	//TODO : maybe useless ?
	public static Set<MoveFlag> getMoveFlagsFromInteger(int integer) {
		EnumSet<MoveFlag> set = EnumSet.noneOf(MoveFlag.class);
		for(MoveFlag flag: MoveFlag.values()) {
			if((integer & flag.getFlagValue()) != 0)
				set.add(flag);
		}
		
		return set;
	}
}
