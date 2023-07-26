package tool;
public enum VerboseLevel {
	NONE, SOME, MOST, EVERYTHING;
	
	/**
	 * Gets verbose level from String, performing case-insensitive comparison.
	 * @throws IllegalArgumentException
	 */
	public static VerboseLevel fromString(String verboseStr) throws IllegalArgumentException {
		for(VerboseLevel verboseLevel : values()) {
			if(verboseLevel.name().equalsIgnoreCase(verboseStr)) {
				return verboseLevel;
			}
		}
		
		throw new IllegalArgumentException();
	}
	
	/*
	public static VerboseLevel getVerboseLevelFromExternalString(String verboseStr) throws IndexOutOfBoundsException, IllegalArgumentException {
		// Match as number first
		VerboseLevel verboseLevel;
		if (verboseStr.matches("[0-9]+")) {
			Integer verboseLevelInt = Integer.parseInt(verboseStr);
			verboseLevel = values()[verboseLevelInt];
		} else {
			verboseLevel = VerboseLevel.fromString(verboseStr);
		}
		
		return verboseLevel;
	}
	*/
	
	public static String allStrings() {
		StringBuffer sb = new StringBuffer();
		for(VerboseLevel verboseLevel : values()) {
			sb.append(String.format("%s/%d,", verboseLevel.name(), verboseLevel.ordinal()));
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
