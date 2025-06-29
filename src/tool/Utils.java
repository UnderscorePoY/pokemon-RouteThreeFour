package tool;

public class Utils {
	public static Boolean parseBoolean(String s) {
		switch(s.toLowerCase()) {
		case "true":  return true;
		case "false": return false;
		default: return null;
		}
	}
}
