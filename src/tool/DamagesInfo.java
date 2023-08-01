package tool;

public class DamagesInfo {
	private int damage;
	
    private Type attackerType1;
    private Type attackerType2;
    private Type defenderType1;
    private Type defenderType2;
    
    private Weather weather;
    
    public DamagesInfo(Pokemon attacker, Pokemon defender, Weather weather) {
    	setAttackerType1(attacker.getSpecies().getType1());
    	setAttackerType2(attacker.getSpecies().getType2());
    	setDefenderType1(defender.getSpecies().getType1());
    	setDefenderType2(defender.getSpecies().getType2());
    	
    	this.setWeather(weather);
    }

	public Weather getWeather() {
		return weather;
	}

	public void setWeather(Weather weather) {
		this.weather = weather;
	}

	public Type getAttackerType1() {
		return attackerType1;
	}

	public void setAttackerType1(Type attackerType1) {
		this.attackerType1 = attackerType1;
	}

	public Type getAttackerType2() {
		return attackerType2;
	}

	public void setAttackerType2(Type attackerType2) {
		this.attackerType2 = attackerType2;
	}

	public Type getDefenderType1() {
		return defenderType1;
	}

	public void setDefenderType1(Type defenderType1) {
		this.defenderType1 = defenderType1;
	}

	public Type getDefenderType2() {
		return defenderType2;
	}

	public void setDefenderType2(Type defenderType2) {
		this.defenderType2 = defenderType2;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}
}
