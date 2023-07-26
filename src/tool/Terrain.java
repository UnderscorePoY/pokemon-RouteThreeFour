package tool;

public enum Terrain { //TODO : implement terrain for Secret Power
	GRASS("GRASS"), LONG_GRASS("LONG GRASS"), SAND("SAND"), UNDERWATER("UNDERWATER"), WATER("WATER"), POND("POND"),
	MOUNTAIN("MOUNTAIN"), CAVE("CAVE"), BUILDING("BUILDING"), PLAIN("PLAIN");
	
	private String terrainName;
	
	private Terrain(String terrainName) {
		this.terrainName = terrainName;
	}

	public String getTerrainName() {
		return terrainName;
	}
}
