package test.pokemon_matcher;

public class MatchResult {
	private String trainerName;
	private String speciesName;
	private boolean isValid;
	private String field;
	private Object expected;
	private Object received;
	
	/**
	 * Valid result constructor.
	 */
	public MatchResult(String trainerName, String speciesName) {
		 this(trainerName, speciesName, true,
					null, null, null);
	}
	
	/**
	 * Invalid result constructor.
	 */
	public MatchResult(String trainerName, String speciesName,
			String field, Object expected, Object received) {
		 this(trainerName, speciesName, false,
					field, expected, received);
	}
	
	/**
	 * Generic result constructor.
	 */
	private MatchResult(String trainerName, String speciesName, boolean isValid,
			String field, Object expected, Object received) {
		this.trainerName = trainerName;
		this.speciesName = speciesName;
		this.isValid = isValid;
		this.field = field;
		this.expected = expected;
		this.received = received;
	}
	
	@Override
	public String toString() {
		if(isValid)
			return String.format("Correctly matched '%s' '%s'", trainerName, speciesName);
		else
			return String.format(">> Error: '%s' '%s' '%s', expected '%s', received '%s'.", trainerName, speciesName, field, expected, received);
	}

	public void setTrainerName(String trainerName) {
		this.trainerName = trainerName;
	}
	
}
