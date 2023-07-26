package tool;
import java.util.ArrayList;

public enum CurrentHPid {
	FULL        ("FULL",    "MAX", "FULL"),
	HALF        ("HALF",    "HALF"),
	THIRD       ("THIRD",   "THIRD"),
	CUSTOM_VALUE(""); // No tokens to be compared to
	;
	
	private String displayName;
	private ArrayList<String> tokens;
	
	private CurrentHPid(String displayName, String... tokens) {
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
	
	public static CurrentHPid getCurrentHPidFromString(String str) throws IllegalArgumentException {
		for(CurrentHPid id : values()) {
			if(id.getTokens() == null)
				continue;
			
			for(String token : id.getTokens()) {
				if(token.equalsIgnoreCase(str))
					return id;
			}
		}
		
		return CUSTOM_VALUE;
	}
	
	public ArrayList<String> getTokens() {
		return tokens;
	}
	
	public static String getAllIDsString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID : tags to trigger it");
		sb.append(Constants.endl);
		
		for(CurrentHPid id : values()) {
			if(id.getTokens() == null)
				continue;
			
			sb.append(String.format("%s : ", id.getDisplayName()));
			for(int i = 0;  i < id.getTokens().size(); i++) {
				sb.append(id.getTokens().get(i));				
				if(i < id.getTokens().size() - 1)
					sb.append(",");
			}
			sb.append(Constants.endl);
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
