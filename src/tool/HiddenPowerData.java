package tool;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class HiddenPowerData {
	// type->atk/spa->power->ivs
	protected static LinkedHashMap<Type, HashMap<Integer, HashMap<Integer, StatsContainer>>> data;
	
	static {
		data = new LinkedHashMap<Type, HashMap<Integer, HashMap<Integer, StatsContainer>>>();
		
		for(Type type : Type.values()) {
			if(type == Type.NORMAL)
				continue;
			
			data.put(type, new HashMap<Integer, HashMap<Integer, StatsContainer>>(32, (float)1.0)); // TODO: hardcoded (below as well)
			
			for(int stat=0; stat<=31;stat++) {
				data.get(type).put(stat, new HashMap<Integer, StatsContainer>());
			}
		}
		
		// Populates a (type,power) table with a compatible IVs combination for each possible tuple
		// (some (type,power) combinations are impossible)
		
		StatsContainer ivs;
		try {
			for(int spa=0; spa<32; spa++) {
			//int spa=31;
				for(int atk=0; atk<(Settings.game.isGen3()?32:4); atk++) {
					for(int hp=0; hp<4; hp++) {
	    				for(int def=0; def<4; def++) {
						    for(int spd=0; spd<4; spd++) {
							    for(int spe=0; spe<4; spe++) {
							    	ivs = new StatsContainer(hp,atk,def,spa,spd,spe);
							    	Type type = ivs.getHiddenPowerType();
							    	int power = ivs.getHiddenPowerPower();
							    	int iv = (Settings.game.isGen3() && type.isGen3PhysicalType())?atk:spa;
							    	data.get(type).get(iv).put(power, ivs);
							    }
					    	}
					    }
				    }
			    }
		    }
		} catch (Exception e) {
			// Never called from the HiddenPower methods in this context
			e.printStackTrace();
		}
	}
	
	/** Returns null if the (type,stat,power) combination is impossible */
	public static StatsContainer getCompatibleIVs(Type type, int iv, int power) {
		if(!data.get(type).get(iv).containsKey(power))
			return null;
		
		return data.get(type).get(iv).get(power);
	}
}
