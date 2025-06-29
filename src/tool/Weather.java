package tool;
import java.util.ArrayList;

public enum Weather {
	NONE     ("",            "NONE", "0"),
	RAIN     ("RAIN",        "RAIN"),
	SUN      ("SUN",         "SUN"), 
	SANDSTORM("SANDSTORM",   "SANDSTORM"), 
	HAIL     ("HAIL",        "HAIL"),
	;
	
	private String displayName;
	private ArrayList<String> tokens;
	
	public static Weather default_ = NONE;
	
	private Weather(String displayName, String... tokens) {
		this.displayName = displayName;
		this.tokens = new ArrayList<String>();
		for(String token : tokens)
			this.tokens.add(token);
	}
	
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
	public static Weather getWeatherFromString(String str) throws IllegalArgumentException {
		for(Weather weather : values()) {
			for(String token : weather.getTokens()) {
				if(token.equalsIgnoreCase(str))
					return weather;
			}
		}
		throw new IllegalArgumentException();
	}

	public ArrayList<String> getTokens() {
		return tokens;
	}
	
	public static String getAllWeathersString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID : tags to trigger it");
		sb.append(Constants.endl);
		
		for(Weather weather : values()) {			
			sb.append(String.format("%s : ", weather.getDisplayName()));
			sb.append(String.format("%s : ", weather.getDisplayName()));
			for(int i = 0;  i < weather.getTokens().size(); i++) {
				sb.append(weather.getTokens().get(i));				
				if(i < weather.getTokens().size() - 1)
					sb.append(",");
			}
			sb.append(Constants.endl);
		}
		
		return sb.toString();
	}
}
