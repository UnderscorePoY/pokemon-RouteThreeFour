package tool;

/**
 * A gender or a gender ratio (a gender ratio guaranteeing a certain gender is used as gender).
 */
public enum Gender {
	GENDERLESS (255,  "x",   "x", "G", "GENDERLESS", "NONE"),
	FEMALE     (254,  "F",   "F", "FEMALE"),
	FEMALE_75  (190, null,   (String[])null),
	FEMALE_50  (127, null,   (String[])null),
	FEMALE_25  ( 63, null,   (String[])null),
	FEMALE_12_5( 31, null,   (String[])null),
	MALE       (  0,  "M",   "M", "MALE");
	
	private final int value;
	private final String initial;
	private final String[] tags;
	
	private Gender(int value, String initial, String... tags) {
		this.value = value;
		this.initial = initial;
		this.tags = tags;
	}
	
	public int getValue() {
		return value;
	}

	public String getInitial() {
		return initial;
	}

	public String[] getTags() {
		return tags;
	}
	
	/**
	 * Returns the predominant gender associated with a gender ratio.
	 */
	public static Gender predominantGender(Species species) {
		switch(species.getGenderRatio())
		{
		case GENDERLESS : return GENDERLESS;
		case FEMALE:
		case FEMALE_75:
		case FEMALE_50: return FEMALE;
		default: return MALE;
		}
	}
	
	/**
	 * Returns the gender produced by a species and a personality value (common to Gen 3 and 4). <br/>
	 * HGSS : https://github.com/pret/pokeheartgold/blob/master/src/pokemon.c#L2090-L2108
	 */
	public static Gender getGenderFromSpeciesAndPersonalityValue(Species species, int personalityValue) {
		Gender speciesGenderRatio = species.getGenderRatio();
		switch(speciesGenderRatio){
		case MALE:
			return MALE;
		case FEMALE:
			return FEMALE;
		case GENDERLESS:
			return GENDERLESS;
		default:
			if(speciesGenderRatio.getValue() > (personalityValue & 0x000000FF))
				return FEMALE;
			else
				return MALE;
		}
	}
	
	/**
	 * Returns the gender from a string. Returns null if there's no matching gender.
	 */
	public static Gender getGenderFromStr(String genderStr) {
		for(Gender gender : values()) {
			if(gender.getTags() == null)
				continue;
			
			for(String tag : gender.getTags()) {
				if(tag.equalsIgnoreCase(genderStr))
					return gender;
			}
		}
		
		return null;
		/*
		if(genderStr.equalsIgnoreCase("male") || genderStr.equalsIgnoreCase("m"))
			return MALE;
		else if (genderStr.equalsIgnoreCase("female") || genderStr.equalsIgnoreCase("f"))
			return FEMALE;
		else if (genderStr.equalsIgnoreCase("genderless") || genderStr.equalsIgnoreCase("g"))
			return GENDERLESS;
		else
			return null;
		*/
	}	
}
