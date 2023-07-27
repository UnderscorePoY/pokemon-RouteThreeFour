package tool;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Item {
	public static final Item DEFAULT = null;
	private static HashMap<IgnoreCaseString, Item> itemsByName;
	
	
	/**
	 * Returns the item associated with the string, or null if there's no correspondence.
	 */
	public static Item getItemByName(String name) {
        return itemsByName.get(new IgnoreCaseString(name));
    }
	
	public static void initItems(Game game) throws FileNotFoundException, IOException, ParseException {
		itemsByName = new LinkedHashMap<IgnoreCaseString, Item>();
		
		JSONParser jsonParser = new JSONParser();
        BufferedReader in;
        
        String itemsResourcePathName = Settings.getResourceRelativePathName(game.getItemsFilename());
    	System.out.println(String.format("INFO: Items loaded from '%s'", itemsResourcePathName));
        in = new BufferedReader(new InputStreamReader(Trainer.class.getResource(
        		itemsResourcePathName).openStream())); // TODO : handle custom files ?
        JSONArray itemsArray = (JSONArray) jsonParser.parse(in);
        for(Object itemObj : itemsArray) {
			JSONObject itemDic = (JSONObject) itemObj;
        	
			// All gens
			String displayName = "";
			int price = 0;
			String holdEffect = ItemHoldEffect.NONE.toString();
			int holdEffectParam = 0;
			
			displayName = ((String) itemDic.get("name"));
			if(itemDic.containsKey("price"))
				price = ((Long) itemDic.get("price")).intValue();
			if(itemDic.containsKey("holdEffect"))
				holdEffect = (String) itemDic.get("holdEffect");
			if(itemDic.containsKey("holdEffectParam"))
				holdEffectParam = ((Long) itemDic.get("holdEffectParam")).intValue();
        	
        	// Gen 4
        	String naturalGiftType = Type.NONE.toString();
        	int naturalGiftPower = 0;
        	if(itemDic.containsKey("naturalGiftType"))
        		naturalGiftType = (String)itemDic.get("naturalGiftType");
        	if(itemDic.containsKey("naturalGiftPower"))
        		naturalGiftPower = ((Long)itemDic.get("naturalGiftPower")).intValue();
        	
        	// Useless data ?
        	//int itemId = ((Long) itemDic.get("itemId")).intValue();
        	//int importance = ((Long) itemDic.get("importance")).intValue();
        	//int pocket = ((Long) itemDic.get("pocket")).intValue();
        	//int type = ((Long) itemDic.get("type")).intValue();
        	//int secondaryId = ((Long) itemDic.get("secondaryId")).intValue();
        	
        	Item item = new Item(displayName, price, holdEffect, holdEffectParam, 
        			naturalGiftType, naturalGiftPower);
        			//itemId, importance, pocket, type, secondaryId);
        	
        	itemsByName.put(new IgnoreCaseString(displayName), item);
        }
    }
	
	private static void printItems() {
		for(Map.Entry<IgnoreCaseString, Item> entry : itemsByName.entrySet()) {
			System.out.println(entry.getKey().toString()+"="+entry.getValue().toString());
		}
	}
	
	public static void main(String[] args) {
		try {
			Item.initItems(Game.RUBY);
			printItems();
			
			Item.initItems(Game.PEARL);
			printItems();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String displayName;
	//private int itemId;
	private int price;
	private ItemHoldEffect holdEffect;
	private int holdEffectParam;
	private Type naturalGiftType;
	private int naturalGiftPower;
	//private int importance;
	//private int pocket;
	//private int type;
	//private int secondaryId;
	
	public Item(String displayName, int price, String holdEffect, int holdEffectParam, 
			String naturalGiftType, int naturalGiftPower) {
			//int itemId, int importance, int pocket, int type, int secondaryId) {
		this.displayName = displayName;
		this.price = price;
		this.holdEffect = ItemHoldEffect.valueOf(holdEffect);
		this.holdEffectParam = holdEffectParam;
		this.naturalGiftType = Type.valueOf(naturalGiftType);
		this.naturalGiftPower = naturalGiftPower;
		//this.itemId = itemId;
		//this.importance = importance;
		//this.pocket = pocket;
		//this.type = type;
		//this.secondaryId = secondaryId;	
	}

	public String getDisplayName() {
		return displayName;
	}

	/*
	public int getItemId() {
		return itemId;
	}
	*/

	public int getPrice() {
		return price;
	}

	public ItemHoldEffect getHoldEffect() {
		return holdEffect;
	}

	public int getHoldEffectParam() {
		return holdEffectParam;
	}
	
	/*
	public int getImportance() {
		return importance;
	}

	public int getPocket() {
		return pocket;
	}

	public int getType() {
		return type;
	}

	public int getSecondaryId() {
		return secondaryId;
	}
	*/
	
	public int getBuyPrice() {
		return getPrice();
	}
	
	public int getSellPrice() {
		return getPrice() / 2;
	}
	
	
	public String detailledStr() {
		return String.format("{'%s' price:%d '%s' %d naturalGift: '%s' %d}", 
				displayName, price, holdEffect, holdEffectParam, naturalGiftType, naturalGiftPower
				);
		/*
		return String.format("{'%s' id:%d price:%d '%s' %d imp:%d pocket:%d type:%d secId:%d}", 
				displayName, itemId, price, holdEffect, holdEffectParam, importance, pocket, type, secondaryId
				);
		*/
	}
	
	@Override
	public String toString() {
		return displayName;
	}

	public int getNaturalGiftPower() {
		return naturalGiftPower;
	}

	public void setNaturalGiftPower(int naturalGiftPower) {
		this.naturalGiftPower = naturalGiftPower;
	}

	public Type getNaturalGiftType() {
		return naturalGiftType;
	}

	public void setNaturalGiftType(Type naturalGiftType) {
		this.naturalGiftType = naturalGiftType;
	}
	
	public boolean isBoostingExperience() {
		switch(this.getHoldEffect()) {
		case LUCKY_EGG:
		case EXP_UP: 
			return true;
		default:
			return false;
		}
	}
	
	public boolean isMoneyBoosting() {
		return getHoldEffect().isMoneyBoosting();
	}
	
	public boolean isBoostingAllEVs(Stat stat) {
		return this.getHoldEffect() == ItemHoldEffect.MACHO_BRACE;
	}
	
	public boolean isBoostingSpecificEv(Stat stat) {
		switch(this.getHoldEffect()) {
		case LVLUP_HP_EV_UP: return stat == Stat.HP;
		case LVLUP_ATK_EV_UP: return stat == Stat.ATK;
		case LVLUP_DEF_EV_UP: return stat == Stat.DEF;
		case LVLUP_SPATK_EV_UP: return stat == Stat.SPA;
		case LVLUP_SPDEF_EV_UP: return stat == Stat.SPD;
		case LVLUP_SPEED_EV_UP: return stat == Stat.SPE;
		default: return false;
		}
	}
	
	public boolean isBoostingHappiness() {
		return getHoldEffect().isBoostingHappiness();
	}
	
}
