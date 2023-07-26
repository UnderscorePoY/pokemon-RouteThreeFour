package tool;

public enum BattleHeldItem { // TODO: class has no use, delete ?
	// type boosting items
	BLACKBELT(Type.FIGHTING),
	BLACKGLASSES(Type.DARK),
	CHARCOAL(Type.FIRE),
	DRAGONSCALE(Type.DRAGON),
	HARDSTONE(Type.ROCK),
	MAGNET(Type.ELECTRIC),
	METALCOAT(Type.STEEL),
	MIRACLESEED(Type.GRASS),
	MYSTICWATER(Type.WATER),
	NEVERMELTICE(Type.ICE),
	PINKBOW(Type.NORMAL),
	POLKADOTBOW(Type.NORMAL),
	SILKSCARF(Type.NORMAL),
	POISONBARB(Type.POISON),
	SHARPBEAK(Type.FLYING),
	SILVERPOWDER(Type.BUG),
	SOFTSAND(Type.GROUND),
	SPELLTAG(Type.GHOST),
	TWISTEDSPOON(Type.PSYCHIC),
	
	// species boosting items
	LIGHTBALL(Type.NONE),
	METALPOWDER(Type.NONE),
	THICKCLUB(Type.NONE),
	
	// exp boosting item
	LUCKYEGG(Type.NONE);
	
	public Type type;
	
	private BattleHeldItem(Type t) {
		this.type = t;
	}
}
