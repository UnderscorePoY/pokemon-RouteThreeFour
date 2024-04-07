package tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.parser.ParseException;

import tool.DamageCalculator.Damages;
import tool.exception.ToolInternalException;

/**
 * The class is too slow to be used in practice (33s per Pokémon).
 * @deprecated
 */
public class HiddenPowerVariationData {
	public class ListIVs extends ArrayList<Integer> {
		private static final long serialVersionUID = 1L;
		
		@Override
		public int hashCode() {
			if(this.isEmpty())
				return -1;
			if(this.size() == 1)
				return this.get(0);
			else
				return 32*this.get(this.size()-1) + this.get(0);
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			if(this == o)
				return true;
			ListIVs lst = (ListIVs) o;
			return this.hashCode() == lst.hashCode();
		}

		/** Compares lower values. If this is smaller, the result is negative. */
		public int compareTo(ListIVs o) {
			return this.get(0) - o.get(0);
		}
	}
	
	// natureMod->type->[ivs]->(minPow,maxPow)->dmg
	private EnumMap<Nature.Mod, EnumMap<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>>> data = new EnumMap<>(Nature.Mod.class);
	private Pokemon attacker;
	private Pokemon defender;
	private StatModifier atkMod;
	private StatModifier defMod;
	private int extra_modifier;
	private boolean isBattleTower;
	private boolean isDoubleBattle;
	private Move move = Move.getMoveByName("HIDDEN POWER");
	private int defHp;
	
	public HiddenPowerVariationData(Pokemon attacker, Pokemon defender, StatModifier atkMod, StatModifier defMod, 
			int extra_modifier, boolean isBattleTower, boolean isDoubleBattle) {
		this.attacker = attacker;
		this.defender = defender;
		this.atkMod = atkMod;
		this.defMod = defMod;
		this.extra_modifier = extra_modifier;
		this.isBattleTower = isBattleTower;
		this.isDoubleBattle = isDoubleBattle;
		this.defHp = defender.getStatValue(Stat.HP);
		
		try {
			this.calc();
		} catch (UnsupportedOperationException | ToolInternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void calc() throws UnsupportedOperationException, ToolInternalException {
		Comparator<ListIVs> comparator = new Comparator<ListIVs>() {
		    @Override
		    public int compare(ListIVs one, ListIVs two) {
		        return one.compareTo(two);
		    }
		};
		
		for(Nature.Mod mod : Nature.Mod.values()) {
			data.put(mod, new EnumMap<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>>(Type.class));
			
			for(Type type : Type.values()) {
				switch(type) {
				case NORMAL:
				case MYSTERY:
				case NONE:
					continue;
				default: break;
				}
				data.get(mod).put(type, new TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>(comparator));
				TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>> map = data.get(mod).get(type);
				
				LinkedHashMap<Damages, LinkedHashMap<Pair<Integer, Integer>, ListIVs>> allIVs = calcAllIVs(mod, type);
				
				for(Map.Entry<Damages, LinkedHashMap<Pair<Integer, Integer>, ListIVs>> entry : allIVs.entrySet()) {
					Damages dmg = entry.getKey();
					LinkedHashMap<Pair<Integer, Integer>, ListIVs> powIVs = entry.getValue();
					for(Map.Entry<Pair<Integer, Integer>, ListIVs> ent : powIVs.entrySet()) {
						Pair<Integer, Integer> minMaxPow = ent.getKey();
						ListIVs ivs = ent.getValue();
						
						if(map.containsKey(ivs))
							map.get(ivs).put(minMaxPow, dmg);
						else {
							LinkedHashMap<Pair<Integer, Integer>, Damages> m = new LinkedHashMap<>();
							m.put(minMaxPow, dmg);
							map.put(ivs, m);
						}
					}
				}
			}
		}
	}
	
	private LinkedHashMap<Damages, LinkedHashMap<Pair<Integer, Integer>, ListIVs>> calcAllIVs(Nature.Mod mod, Type type) throws UnsupportedOperationException, ToolInternalException {
		// dmg->(minPow,maxPow)->[ivs]
		LinkedHashMap<Damages, LinkedHashMap<Pair<Integer, Integer>, ListIVs>> _rolls = new LinkedHashMap<>();
		
		for(int iv=0; iv<=31; iv++) {
			LinkedHashMap<Damages, Pair<Integer, Integer>> allPowers = calcAllPowers(mod, type, iv);
			if(allPowers.isEmpty())
				continue;
			
			for(Map.Entry<Damages, Pair<Integer, Integer>> entry : allPowers.entrySet()) {
				Damages dmg = entry.getKey();
				Pair<Integer, Integer> minMaxPow = entry.getValue();
				if(_rolls.containsKey(dmg)) {
					if(_rolls.get(dmg).containsKey(minMaxPow)) {
						_rolls.get(dmg).get(minMaxPow).add(iv);
					} else {
						ListIVs lst= new ListIVs();
						lst.add(iv);
						_rolls.get(dmg).put(minMaxPow, lst);
					}
				} else {
					LinkedHashMap<Pair<Integer, Integer>, ListIVs> range = new LinkedHashMap<>();
					ListIVs lst = new ListIVs();
					lst.add(iv);
					range.put(minMaxPow, lst);
					_rolls.put(dmg, range);
				}
			}
		}
		
		return _rolls;
	}
	
	private LinkedHashMap<Damages, Pair<Integer, Integer>> calcAllPowers(Nature.Mod mod, Type type, int iv) throws UnsupportedOperationException, ToolInternalException {
		// dmg->(minPow,maxPow)
		LinkedHashMap<Damages, Pair<Integer, Integer>> _rolls = new LinkedHashMap<>();
		
		Nature nature;
		if(Settings.game.isGen3() && type.isGen3PhysicalType())
			switch(mod) {
			case INCREASING: nature = Nature.ADAMANT; break;
			case DECREASING: nature = Nature.MODEST; break;
			default: nature = Nature.HARDY; break;
			}
		else {
			switch(mod) {
			case INCREASING: nature = Nature.MODEST; break;
			case DECREASING: nature = Nature.ADAMANT; break;
			default: nature = Nature.HARDY; break;
			}
		}
		
		for(int power=30; power<=70; power++) {
			StatsContainer ivs = HiddenPowerData.getCompatibleIVs(type, iv, power);
			if(ivs == null)
				continue;
			
			attacker.getIVs().putAssumingValidIVs(ivs);
			attacker.setNature(nature);
			Damages damages = new Damages(move, attacker, defender, atkMod, defMod, extra_modifier, isBattleTower, isDoubleBattle);
			if(!damages.hasDamage())
				continue;
			
			damages.capDamagesWithHP(defHp);
					
			if(_rolls.containsKey(damages))
				_rolls.get(damages).setRight(power);
			else
				_rolls.put(damages, new Pair<Integer, Integer>(power, power));
		}
		
		/*
		for(Map.Entry<Damages, Pair<Integer, Integer>> entry : _rolls.entrySet()) {
			Damages damages = entry.getKey();
			Pair<Integer, Integer> minMaxPow = entry.getValue();
			data.get(type).get(iv).put(minMaxPow, damages);
		}
		*/
		
		return _rolls;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		//EnumMap<Nature.Mod, EnumMap<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>>>
		for(Map.Entry<Nature.Mod, EnumMap<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>>> entry: data.entrySet()) {
			Nature.Mod mod = entry.getKey();
			EnumMap<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>> typeMap = entry.getValue();
			for(Map.Entry<Type, TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>>> ent : typeMap.entrySet()) {
				Type type = ent.getKey();
				TreeMap<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>> ivsMap = ent.getValue();
				for(Map.Entry<ListIVs, LinkedHashMap<Pair<Integer, Integer>, Damages>> e : ivsMap.entrySet()) {
					ListIVs ivs = e.getKey();
					LinkedHashMap<Pair<Integer, Integer>, Damages> powDmgMap = e.getValue();
					for(int i=0; i<ivs.size(); i++) {
						if(i>0)
							sb.append(",");
						sb.append(ivs.get(i));
					}
					sb.append(String.format(" %s : ", (Settings.game.isGen3() && type.isGen3PhysicalType())?Stat.ATK:Stat.SPA));
					for(Map.Entry<Pair<Integer, Integer>, Damages> f : powDmgMap.entrySet()) {
						Pair<Integer, Integer> minMaxPow = f.getKey();
						Damages dmg = f.getValue();
						sb.append(String.format("%s : %s\n", 
								(minMaxPow.getLeft() == minMaxPow.getRight())?
										String.format("%5d",minMaxPow.getLeft()):
										String.format("%2d-%2d", minMaxPow.getLeft(),minMaxPow.getRight()),
								dmg));
					}
				}
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws UnsupportedOperationException, ToolInternalException, FileNotFoundException, IOException, ParseException {
		tool.Initialization.init(Game.DIAMOND);
		HiddenPowerVariationData data = new HiddenPowerVariationData(
			new Pokemon(Species.getSpeciesByName("TURTWIG"), Gender.MALE, 9, Nature.ADAMANT, Ability.OVERGROW, 0), 
			new Pokemon(Species.getSpeciesByName("STARLY"), Gender.MALE, 7, Nature.ADAMANT, Ability.KEEN_EYE, 0),
			new StatModifier(), new StatModifier(),
			1, false, false			
		);
		System.out.println(data.toString().length());
	}
}
