package tool;

public enum Language {
	ENGLISH("English", "EN"),
	FRENCH("French", "FR");

	private String name;
	private String shortName;
	public static Language default_ = ENGLISH;
	
	private Language(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public String getLangExtensionString() {
		return "_" + this.shortName;
	}
	/**
	 * Returns the language associated with the string, or null if there's no correspondence.
	 */
	public static Language getLanguageFromStr(String str) {
		for(Language lang : values()) {
			if(lang.getName().equalsIgnoreCase(str) || lang.getShortName().equalsIgnoreCase(str))
				return lang;
		}
		
		return null;
	}
}
