package tool;

public enum MoveClass {
	PHYSICAL, SPECIAL, STATUS;
	
	public static MoveClass getGen3MoveClassFromType(Type type) {
		if(type.isGen3PhysicalType())
			return PHYSICAL;
		else
			return SPECIAL;
	}
}
