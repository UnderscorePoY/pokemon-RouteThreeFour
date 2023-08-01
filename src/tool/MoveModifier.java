package tool;

/**
 * An object storing a source cause of modification of a move in damage calculation.
 */
public class MoveModifier {
	public static enum Modifier {
		BONUS("+"), NEUTRAL(""), MALUS("-");
		
		private String shortName;
		
		private Modifier(String shortName) {
			this.shortName = shortName;
		}
		
		public String getShortName() {
			return shortName;
		}
	}
	
	private Modifier modifier;
	private Side side;
	private Object source;
	
	
	/**
	 * A move modifier, storing if it benefits the move or not, from which side it comes from, and the source causing the modification.
	 */
	private MoveModifier(Modifier modifier, Side side, Object source) {
		this.modifier = modifier;
		this.side = side;
		this.source = source;
	}
	
	
	/**
	 * Side-less modifier, used as info.
	 */
	public MoveModifier(Modifier modifier, Object source) {
		this(modifier, null, source);
	}
	
	/**
	 * Neutral & side-less modifier, used as info.
	 */
	public MoveModifier(Object source) {
		this(Modifier.NEUTRAL, source);
	}
	


	public Side getSide() {
		return side;
	}

	public Object getModifier() {
		return modifier;
	}
	
	@Override
	public String toString() {
		return String.format("%s%s%s", modifier, side == null ? "" : side, source);
	}
}
