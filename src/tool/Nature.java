package tool;

import java.util.ArrayList;

public enum Nature {
	HARDY  (null, null),
	LONELY (Stat.ATK, Stat.DEF),
	BRAVE  (Stat.ATK, Stat.SPE),
	ADAMANT(Stat.ATK, Stat.SPA),
	NAUGHTY(Stat.ATK, Stat.SPD),
	BOLD   (Stat.DEF, Stat.ATK),
	DOCILE (null, null),
	RELAXED(Stat.DEF, Stat.SPE),
	IMPISH (Stat.DEF, Stat.SPA),
	LAX    (Stat.DEF, Stat.SPD),
	TIMID  (Stat.SPE, Stat.ATK),
	HASTY  (Stat.SPE, Stat.DEF),
	SERIOUS(null, null),
	JOLLY  (Stat.SPE, Stat.SPA),
	NAIVE  (Stat.SPE, Stat.SPD),
	MODEST (Stat.SPA, Stat.ATK),
	MILD   (Stat.SPA, Stat.DEF),
	QUIET  (Stat.SPA, Stat.SPE),
	BASHFUL(null, null),
	RASH   (Stat.SPA, Stat.SPD),
	CALM   (Stat.SPD, Stat.ATK),
	GENTLE (Stat.SPD, Stat.DEF),
	SASSY  (Stat.SPD, Stat.SPE),
	CAREFUL(Stat.SPD, Stat.SPA),
	QUIRKY (null, null);
	
	public static final Nature DEFAULT = HARDY;
	public static final String INCREASED_NATURE_STR = "+";
	public static final String NEUTRAL_NATURE_STR = " ";
	public static final String DECREASED_NATURE_STR = "-";
	
	private static int INCREASE_MULT = 11;
	private static int DECREASE_MULT = 9;
	private static int DIV = 10;
	
	
	private final Stat increased;
	private final Stat decreased;
	
	private Nature(Stat increased, Stat decreased) {
		this.increased = increased;
		this.decreased = decreased;
	}
	
	public Stat getIncreased() {
		return increased;
	}
	
	public Stat getDecreased() {
		return decreased;
	}
	

	public static int decreaseStat(int value) {
		return value * DECREASE_MULT / DIV;
	}
	
	public static int increaseStat(int value) {
		return value * INCREASE_MULT / DIV;
	}
	
	
	/**
	 * Returns the modified stat value based on this nature.
	 */
	public int getAlteredStat(Stat stat, int value) {
		if(this.getIncreased() == null)
			return value;
		else if(this.getIncreased() == stat) 
			return increaseStat(value);
		else if(this.getDecreased() == stat)
			return decreaseStat(value);
		return value;
	}
	
	/**
	 * Returns the nature associated with a personality value (common to both Gen 3 and 4).
	 */
	public static Nature getNatureFromPersonalityValue(int personalityValue) {
		return Nature.values()[personalityValue % 25];
	}
	
	/**
	 * Returns the nature associated with the string. Returns null if there's no correspondence.
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static Nature getNatureFromString(String value) {
		IgnoreCaseString ics = new IgnoreCaseString(value);
        for(Nature n : values())
            if(ics.equals(n.name())) return n;
        return null;
    }
	
	public static ArrayList<Nature> getNaturesIncreasing(Stat stat) {
		ArrayList<Nature> natures = new ArrayList<>();
		for(Nature n : values()) {
			if(n.getIncreased() == stat)
				natures.add(n);
		}
		return natures;
	}
	
	public static ArrayList<Nature> getNaturesDecreasing(Stat stat) {
		ArrayList<Nature> natures = new ArrayList<>();
		for(Nature n : values()) {
			if(n.getDecreased() == stat)
				natures.add(n);
		}
		return natures;
	}
}
