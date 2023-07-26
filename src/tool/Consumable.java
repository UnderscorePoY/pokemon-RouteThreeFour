package tool;

public enum Consumable { // TODO : some berries too ?
	RARE_CANDY ("Rare Candy"), 
	HP_UP("HP Up"), 
	PROTEIN("Protein"), 
	IRON("Iron"), 
	CALCIUM("Calcium"), 
	ZINC("Zinc"), 
	CARBOS("Carbos"),
	;
	
	private final String displayName;
	
	private Consumable(String name) {
		this.displayName = name;
	}

	public String getDisplayName() {
		return displayName;
	}
	
}
