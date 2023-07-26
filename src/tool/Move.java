package tool;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tool.exc.ToolInternalException;

public class Move {	
	private static HashMap<IgnoreCaseString, Move> movesByName;
	
	public static Move getMoveByName(String name) {
		// System.out.println(name+" "+movesByName.containsKey(name)); //TODO
        return movesByName.get(new IgnoreCaseString(name));
    }
	
	public static void initMoves(Game game) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		movesByName = new LinkedHashMap<IgnoreCaseString, Move>();
		
        BufferedReader in;
    	System.out.println("/resources/"+game.getMovesFilename()); // TODO
        in = new BufferedReader(new InputStreamReader(Move.class
                .getResource("/resources/"+game.getMovesFilename()).openStream())); // TODO : handle custom files ?
        
        if(game.isGen3())          
            initMovesGen3(in);
        else if (game.isGen4())
        	initMovesGen4(in);
        else
        	throw new ToolInternalException(Move.class.getEnclosingMethod(), game, "");
    }
	
	private static void initMovesGen3(BufferedReader in) throws IOException, ParseException {
		int currentIndex = 0;
		JSONParser jsonParser = new JSONParser();
		
		Move move = null;
        JSONArray array = (JSONArray) jsonParser.parse(in);
        for(Object moveObj : array) {
        	JSONObject moveDic = (JSONObject) moveObj;
        	
        	String name = (String) moveDic.get("name");
        	int index = currentIndex;
        	MoveEffect effect = MoveEffect.valueOf((String) moveDic.get("move_effect"));
        	int power = ((Long) moveDic.get("base_power")).intValue();
        	Type type = Type.valueOf((String) moveDic.get("type"));
        	int accuracy = ((Long) moveDic.get("accuracy")).intValue();
        	int pp = ((Long) moveDic.get("pp")).intValue();
        	int effectChance = ((Long) moveDic.get("secondary_effect_chance")).intValue();
        	MoveTarget target = MoveTarget.valueOf((String) moveDic.get("target"));
        	int priority = ((Long) moveDic.get("priority")).intValue();
        	MoveClass moveClass = MoveClass.getGen3MoveClassFromType(type);
        	
        	JSONArray flagsArray = (JSONArray) moveDic.get("flags");
        	ArrayList<String> moveFlagStrings = new ArrayList<>();
        	for(Object flagObj: flagsArray)
        		moveFlagStrings.add((String) flagObj);
        	
        	move = new Move(name, index, moveClass, effect, power, type, accuracy, pp, effectChance,
            		target, priority, moveFlagStrings);
        	
        	movesByName.put(new IgnoreCaseString(name), move);
        	
        	currentIndex++;
        }
	}
	
	private static void initMovesGen4(BufferedReader in) throws IOException, ParseException {
		int currentIndex = 0;
		JSONParser jsonParser = new JSONParser();
		
    	Move move = null;
        JSONArray array = (JSONArray) jsonParser.parse(in);
        for(Object moveObj : array) {
        	JSONObject moveDic = (JSONObject) moveObj;
        	
        	String name = (String) moveDic.get("name");
        	int index = currentIndex;
        	MoveEffect effect = MoveEffect.valueOf((String) moveDic.get("effect"));
        	MoveClass moveClass = MoveClass.valueOf((String) moveDic.get("class"));
        	int power = ((Long) moveDic.get("power")).intValue();
        	Type type = Type.valueOf((String) moveDic.get("type"));
        	int accuracy = ((Long) moveDic.get("accuracy")).intValue();
        	int pp = ((Long) moveDic.get("pp")).intValue();
        	int effectChance = ((Long) moveDic.get("effectChance")).intValue();
        	MoveTarget target = MoveTarget.getGen4MoveTargetFromInt(((Long) moveDic.get("unk8")).intValue());
        	int priority = ((Long) moveDic.get("priority")).intValue();
        	
        	ArrayList<String> moveFlagStrings = new ArrayList<>();
        	
        	move = new Move(name, index, moveClass, effect, power, type, accuracy, pp, effectChance,
            		target, priority, moveFlagStrings);
        	
        	movesByName.put(new IgnoreCaseString(name), move);
        	
        	currentIndex++;
        }
	}
	
	public static Set<Entry<IgnoreCaseString, Move>> entrySet(){
		return movesByName.entrySet();
	}
	
	public static void printMoves() {
		for(Map.Entry<IgnoreCaseString, Move> entry : movesByName.entrySet()) {
			System.out.println(entry.getKey()+"="+entry.getValue());
		}
	}
	
	// TODO: Only for testing
	public static void main(String[] args) {
		try {
			initMoves(Game.RUBY);
			printMoves();
			
			initMoves(Game.DIAMOND);
			printMoves();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private String name;
    private final String unmodifiedName; // used for more reliable move recognition
    private int index;
    private MoveClass moveClass;
    private MoveEffect effect;
    private int power;
    private Type type;
    private int accuracy;
    private int pp;
    private int effectChance;
    private MoveTarget target;
    private int priority;
    private EnumSet<MoveFlag> moveFlags;

    private Move(String name, int index, MoveClass moveClass, MoveEffect effect, int power, Type type, int accuracy, int pp, int effectChance,
    		MoveTarget target, int priority, List<String> moveFlagStrings) {
        this.name = name;
        this.unmodifiedName = name;
        this.index = index;
        this.moveClass = moveClass;
        this.effect = effect;
        this.power = power;
        this.type = type;
        this.accuracy = accuracy;
        this.pp = pp;
        this.effectChance = effectChance;
        this.target = target;
        this.priority = priority;
        this.moveFlags = MoveFlag.getMoveFlags(moveFlagStrings);   
    }
    
    public Move(Move m) { // Copy constructor
    	this.name = m.name;
    	this.unmodifiedName = m.unmodifiedName;
        this.index = m.index;
        this.moveClass = m.moveClass;
        this.effect = m.effect;
        this.power = m.power;
        this.type = m.type;
        this.accuracy = m.accuracy;
        this.pp = m.pp;
        this.effectChance = m.effectChance;
        this.target = m.target;
        this.priority = m.priority;
        this.moveFlags = EnumSet.copyOf(m.moveFlags);
    }

    public String getName() {
        return name;
    }
    
    public int getIndex() {
    	return index;
    }

    public String getBoostedName(int i) {
        return name + " " + i;
    }

    public MoveEffect getEffect() {
        return effect;
    }

    public int getPower() {
        return power;
    }

    public Type getType() {
        return type;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPP() {
        return pp;
    }

    public int getEffectChance() {
        return effectChance;
    }
    
    public MoveTarget getMoveTarget() {
    	return target;
    }
    
    public int getPriority() {
    	return priority;
    }
    
    public Set<MoveFlag> getMoveFlags(){
    	return moveFlags;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public boolean isPhysical() {
    	return moveClass == MoveClass.PHYSICAL || this.matchesAny("Struggle");
    }
    
    public boolean isSpecial() {
    	return !isPhysical() && type !=Type.NONE && type != Type.MYSTERY;
    }
    
    public MoveClass getMoveClass() {
    	return moveClass;
    }
    
    @Override
    public String toString() {
    	return String.format("{'%s' id:%d '%s' eff:'%s' pow:%d '%s' acc:%d pp:%d effChance:%d target:'%s' prio:%d flags:%s}", 
    			name, index, moveClass.toString(), effect.toString(), power, type.toString(), accuracy, pp, effectChance, target.toString(), priority,
    			moveFlags.toString()
    			);
    }
    
    public boolean isIncreasedCritRatio() {
    	return this.effect.isIncreasedCritRatio();
    }
    
    /**
     * Returns true if the move matches any of the move in the list. This is robust to name or reference changes.
     */
    @SuppressWarnings("unlikely-arg-type")
	public boolean matchesAny(String... names) {
    	IgnoreCaseString ics = new IgnoreCaseString(unmodifiedName);
    	for(String name : names) {
    		if(ics.equals(name))
    			return true;
    	}
    	
    	return false;
    }
    
    // Contact moves : https://bulbapedia.bulbagarden.net/wiki/Contact#Moves_that_make_contact
    // TODO: store it in moves json instead, separately or with moves
    private static final String[] contactsStrArr = new String[] {
			"Pound", 
			"Karate Chop", 
			"Double Slap", 
			"Comet Punch", 
			"Mega Punch", 
			"Fire Punch", 
			"Ice Punch", 
			"Thunder Punch", 
			"Scratch", 
			"Vise Grip", 
			"Guillotine", 
			"Cut", 
			"Wing Attack", 
			"Fly", 
			"Bind", 
			"Slam", 
			"Vine Whip", 
			"Stomp", 
			"Double Kick", 
			"Mega Kick", 
			"Jump Kick", 
			"Rolling Kick", 
			"Headbutt", 
			"Horn Attack", 
			"Fury Attack", 
			"Horn Drill", 
			"Tackle", 
			"Body Slam", 
			"Wrap", 
			"Take Down", 
			"Thrash", 
			"Double-Edge", 
			"Bite", 
			"Peck", 
			"Drill Peck", 
			"Submission", 
			"Low Kick", 
			"Counter", 
			"Seismic Toss", 
			"Strength", 
			"Petal Dance", 
			"Dig", 
			"Quick Attack", 
			"Rage", 
			"Bide", 
			"Lick", 
			"Waterfall", 
			"Clamp", 
			"Skull Bash", 
			"Constrict", 
			"High Jump Kick", 
			"Leech Life", 
			"Dizzy Punch", 
			"Crabhammer", 
			"Fury Swipes", 
			"Hyper Fang", 
			"Super Fang", 
			"Slash", 
			"Struggle", 
			"Triple Kick", 
			"Thief", 
			"Flame Wheel", 
			"Flail", 
			"Reversal", 
			"Mach Punch", 
			"Feint Attack", 
			"Outrage", 
			"Rollout", 
			"False Swipe", 
			"Spark", 
			"Fury Cutter", 
			"Steel Wing", 
			"Return", 
			"Frustration", 
			"Dynamic Punch", 
			"Megahorn", 
			"Pursuit", 
			"Rapid Spin", 
			"Iron Tail", 
			"Metal Claw", 
			"Vital Throw", 
			"Cross Chop", 
			"Crunch", 
			"Extreme Speed", 
			"Rock Smash", 
			"Fake Out", 
			"Facade", 
			"Focus Punch", 
			"Smelling Salts", 
			"Superpower", 
			"Revenge", 
			"Brick Break", 
			"Knock Off", 
			"Endeavor", 
			"Dive", 
			"Arm Thrust", 
			"Blaze Kick", 
			"Ice Ball", 
			"Needle Arm", 
			"Poison Fang", 
			"Crush Claw", 
			"Meteor Mash", 
			"Astonish", 
			"Shadow Punch", 
			"Sky Uppercut", 
			"Aerial Ace", 
			"Dragon Claw", 
			"Bounce", 
			"Poison Tail", 
			"Covet", 
			"Volt Tackle", 
			"Leaf Blade", 
			"Wake-Up Slap", 
			"Hammer Arm", 
			"Gyro Ball", 
			"Pluck", 
			"U-turn", 
			"Close Combat", 
			"Payback", 
			"Assurance", 
			"Trump Card", 
			"Wring Out", 
			"Punishment", 
			"Last Resort", 
			"Sucker Punch", 
			"Flare Blitz", 
			"Force Palm", 
			"Poison Jab", 
			"Night Slash", 
			"Aqua Tail", 
			"X-Scissor", 
			"Dragon Rush", 
			"Drain Punch", 
			"Brave Bird", 
			"Giga Impact", 
			"Bullet Punch", 
			"Avalanche", 
			"Shadow Claw", 
			"Thunder Fang", 
			"Ice Fang", 
			"Fire Fang", 
			"Shadow Sneak", 
			"Zen Headbutt", 
			"Rock Climb", 
			"Power Whip", 
			"Cross Poison", 
			"Iron Head", 
			"Grass Knot", 
			"Bug Bite", 
			"Wood Hammer", 
			"Aqua Jet", 
			"Head Smash", 
			"Double Hit", 
			"Crush Grip", 
			"Shadow Force", 
			"Storm Throw", 
			"Heavy Slam", 
			"Flame Charge", 
			"Low Sweep", 
			"Foul Play", 
			"Chip Away", 
			"Sky Drop", 
			"Circle Throw", 
			"Acrobatics", 
			"Retaliate", 
			"Dragon Tail", 
			"Wild Charge", 
			"Drill Run", 
			"Dual Chop", 
			"Heart Stamp", 
			"Horn Leech", 
			"Sacred Sword", 
			"Razor Shell", 
			"Heat Crash", 
			"Steamroller", 
			"Tail Slap", 
			"Head Charge", 
			"Gear Grind", 
			"Bolt Strike", 
			"V-create", 
			"Flying Press", 
			"Fell Stinger", 
			"Phantom Force", 
			"Draining Kiss", 
			"Play Rough", 
			"Nuzzle", 
			"Hold Back", 
			"Infestation", 
			"Power-Up Punch", 
			"Dragon Ascent", 
			"Catastropika", 
			"First Impression", 
			"Darkest Lariat", 
			"Ice Hammer", 
			"High Horsepower", 
			"Solar Blade", 
			"Throat Chop", 
			"Anchor Shot", 
			"Lunge", 
			"Fire Lash", 
			"Power Trip", 
			"Smart Strike", 
			"Trop Kick", 
			"Dragon Hammer", 
			"Brutal Swing", 
			"Malicious Moonsault", 
			"Soul-Stealing 7-Star Strike", 
			"Pulverizing Pancake", 
			"Psychic Fangs", 
			"Stomping Tantrum", 
			"Accelerock", 
			"Liquidation", 
			"Spectral Thief", 
			"Sunsteel Strike", 
			"Zing Zap", 
			"Multi-Attack", 
			"Plasma Fists", 
			"Searing Sunraze Smash", 
			"Let's Snuggle Forever", 
			"Zippy Zap", 
			"Floaty Fall", 
			"Sizzly Slide", 
			"Veevee Volley", 
			"Double Iron Bash", 
			"Jaw Lock", 
			"Bolt Beak", 
			"Fishious Rend", 
			"Body Press", 
			"Snap Trap", 
			"Behemoth Blade", 
			"Behemoth Bash", 
			"Breaking Swipe", 
			"Branch Poke", 
			"Spirit Break", 
			"False Surrender", 
			"Steel Roller", 
			"Grassy Glide", 
			"Skitter Smack", 
			"Lash Out", 
			"Flip Turn", 
			"Triple Axel", 
			"Dual Wingbeat", 
			"Wicked Blow", 
			"Surging Strikes", 
			"Thunderous Kick", 
			"Dire Claw", 
			"Psyshield Bash", 
			"Stone Axe", 
			"Wave Crash", 
			"Headlong Rush", 
			"Ceaseless Edge", 
			"Axe Kick", 
			"Jet Punch", 
			"Spin Out", 
			"Population Bomb", 
			"Ice Spinner", 
			"Glaive Rush", 
			"Triple Dive", 
			"Mortal Spin", 
			"Kowtow Cleave", 
			"Aqua Step", 
			"Raging Bull", 
			"Collision Course", 
			"Electro Drift", 
			"Pounce", 
			"Trailblaze", 
			"Hyper Drill", 
			"Rage Fist", 
			"Bitter Blade", 
			"Double Shock", 
			"Comeuppance "
	};

    private static final Set<IgnoreCaseString> contacts = new HashSet<>();
    static {
    	for(String contactStr : contactsStrArr)
    		contacts.add(new IgnoreCaseString(contactStr));
    	
    	// Gen 3 exceptions
    	if(Settings.game.isGen3()) {
    		contacts.add(new IgnoreCaseString("AncientPower"));
    		contacts.add(new IgnoreCaseString("Overheat"));
    		contacts.remove(new IgnoreCaseString("Covet"));
    		contacts.remove(new IgnoreCaseString("Faint Attack"));
    		contacts.remove(new IgnoreCaseString("Fake Out")); 
    	}
    }
    public boolean makesContact() {
    	return contacts.contains(new IgnoreCaseString(this.unmodifiedName));
    }
    
}
