package tool;
public class Settings {
    public static Game game = Game.RUBY;
    public static Language lang = Language.ENGLISH;
    
    public static VerboseLevel verboseLevel = VerboseLevel.NONE;
    public static boolean showStatsOnLevelUp = false;
    public static boolean showStatRangesOnLevelUp = false;
    public static boolean defaultIvVariation = false;
    public static boolean overallChanceKO = false;
    public static boolean showGuarantees = false;
    // public static boolean hasAmuletCoin = false; // TODO: maybe will keep this in the end
    public static int money = 3000;

	private static final String resourcesFolderName = "/resources/";
	
	public static String getResourceRelativePathName(String resource) {
		return String.format("%s%s", resourcesFolderName, resource);
	}
}
