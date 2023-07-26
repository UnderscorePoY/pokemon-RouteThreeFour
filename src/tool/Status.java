package tool;
import java.util.ArrayList;
import java.util.EnumSet;

public enum Status {
	// Status1 (non stackable)
	NONE     ("",      "NONE", "0"),
	SLEEP    ("SLP",   "SLEEP", "SLP"), 
	POISON   ("PSN",   "POISON", "PSN"), 
	BURN     ("BRN",   "BURN", "BRN"),
	FREEZE   ("FRZ",   "FREEZE", "FRZ"), 
	PARALYSIS("PRZ",   "PARALYSIS", "PRZ"),
	TOXIC    ("TXC",   "TOXIC", "TXC"),
	
	// Status2 (stackable)
	CONFUSED    ("CONFUSED",       "CONFUSED"),
	WRAPPED     ("WRAPPED",        "WRAPPED", "WRAP"), 
	NIGHTMARE   ("NIGHTMARE",      "NIGHTMARE"), 
	CURSED      ("CURSED",         "CURSED", "CURSE"),
	FORESIGHT   ("FORESIGHT",      "FORESIGHT"),
	DEFENSE_CURL("DEFENSE CURL",   "DEFENSECURL"),
	
	// Status3 (stackable)
	LEECH_SEED("LEECH SEED",    "LEECHSEED"), 
	MINIMIZED ("MINIMIZED",     "MINIMIZED", "MINIMIZE"),
	ROOTED    ("ROOTED",        "ROOTED"),
	CHARGED_UP("CHARGED UP",    "CHARGEDUP", "CHARGED"),
	MUDSPORT  ("MUD SPORT",     "MUDSPORT"), 
	WATERSPORT("WATER SPORT",   "WATERSPORT"),
	UNDERWATER("UNDERWATER",    "UNDERWATER"),
	TRACED    ("TRACED",        "TRACED", "TRACE"),
	
	UNBURDEN     ("UNBURDEN",        "UNBURDEN"), 
	GROUNDED     ("GROUNDED",        "GROUNDED"), // TODO: gravity impacts accuracy and forbids some move to be used : https://bulbapedia.bulbagarden.net/wiki/Gravity_(move). This is not the case for the iron Ball, so might need to separate both status
	FLOWER_GIFT  ("FLOWER GIFT",     "FLOWERGIFT"), 
	SWITCHING_OUT("SWITCHING OUT",   "SWITCHINGOUT", "SWITCHOUT", "SWITCH"), 
	FLASH_FIRE   ("FLASH FIRE",      "FLASHFIRE"),
	
	// Side_Status (stackable)
	REFLECT    ("REFLECT",       "REFLECT", "R"),
	LIGHTSCREEN("LIGHTSCREEN",   "LIGHTSCREEN", "LS"),
	SPIKES     ("SPIKES",        "SPIKES"),
	TAILWIND   ("TAILWIND",      "TAILWIND");

	private String displayName;
	private ArrayList<String> tokens;
	
	private Status(String displayName, String... tokens) {
		this.displayName = displayName;
		this.tokens = new ArrayList<String>();
		for(String token : tokens)
			this.tokens.add(token);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public static Status noStatus1() {
		return NONE;
	}
	
	public static EnumSet<Status> noStatus2_3(){
		return EnumSet.noneOf(Status.class);
		/*
		EnumMap<Status, Boolean> statuses = new EnumMap<Status, Boolean>(Status.class);
		for(int i = CONFUSED.ordinal() ; i < Status.values().length ; i++) {
			statuses.put(Status.values()[i], false);
		}
		
		return statuses;
		*/
	}
	
	public boolean isStatus1() {
		return ordinal() <= Status.TOXIC.ordinal();
	}
	
	public boolean isStatus2_3() {
		return ordinal() >= Status.CONFUSED.ordinal();
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	public static Status getStatusFromString(String str) throws IllegalArgumentException {
		for(Status status : values()) {
			for(String token : status.getTokens()) {
				if(token.equalsIgnoreCase(str))
					return status;
			}
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Both start and end are included.
	 */
	private static String getAllStatusAsStringFromTo(Status start, Status end) {
		int startOrdinal = start == null ? 0 : start.ordinal();
		int endOrdinal = end == null ? Status.values().length - 1 : end.ordinal();
		
		StringBuffer sb = new StringBuffer();
		sb.append("STATUS : tags to trigger it");
		sb.append(Constants.endl);
		
		for(int statusIdx = startOrdinal; statusIdx <= endOrdinal; statusIdx++) {
			
			Status status = Status.values()[statusIdx];
			sb.append(String.format("%s : ", status.getDisplayName()));
			for(int i = 0; i < status.getTokens().size(); i++) {
				sb.append(status.getTokens().get(i));
				if(i < status.getTokens().size() - 1)
					sb.append(",");
			}
			sb.append(Constants.endl);
		}
		
		return sb.toString();
	}
	
	public static String getAllStatus1AsString() {
		return getAllStatusAsStringFromTo(null, Status.TOXIC);
	}
	
	public static String getAllStatus23AsString() {
		return getAllStatusAsStringFromTo(Status.CONFUSED, null);
	}

	public ArrayList<String> getTokens() {
		return tokens;
	}
}
