package tool;

public class IgnoreCaseString {
	private String original;
	private String comparisonString;
	
	public IgnoreCaseString(String s) {
		this.original = s;
		this.comparisonString = tighten(s);
	}
	
	private static String tighten(String s) {
		return s.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
	}
	
	public String getComparisonString() {
		return comparisonString;
	}
	
	public String getOriginalString() {
		return original;
	}
	
	/**
	 * Returns true if, and only if, one of the following conditions is true : 
	 * 1) the other object has the same reference as this 
	 * 2) the other object is a string and both string names match when performing case-insensitive comparison on the strings with only A-Za-z0-9 characters are kept
	 * 3) the other object is an IgnoreCaseString and 2) applies
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (getComparisonString() == null || o == null)
			return false;
		if (o.getClass() == String.class) {
			String os = (String) o;
			return getComparisonString().equalsIgnoreCase(tighten(os));
		}
		if (getClass() == o.getClass()) {
			IgnoreCaseString cs = (IgnoreCaseString) o;
			return getComparisonString().equalsIgnoreCase(cs.getComparisonString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getComparisonString().hashCode();
	}
	
	@Override
	public String toString() {
		return getComparisonString();
	}
	
	// test
	@SuppressWarnings("unlikely-arg-type")
	public static void main(String[] args) {
		IgnoreCaseString moveStr = new IgnoreCaseString("MUD_SLAP");
		System.out.println(moveStr.equals("MUDSLAP"));
	}
	
}
