package tool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A trainer class. Contains base money yields, internal identifier and trainer type (male, female, double). Only used for Gen 4.
 */
public class TrainerClass {
	private static HashMap<IgnoreCaseString, TrainerClass> trainerClassesByName;
	
	public static TrainerClass getTrainerClassByName(String name) {
        return trainerClassesByName.get(new IgnoreCaseString(name));
    }
	
	public static void initTrainerClasses(Game game) throws IOException, ParseException {
		trainerClassesByName = new LinkedHashMap<IgnoreCaseString, TrainerClass>();
		
		JSONParser jsonParser = new JSONParser();
        BufferedReader in;
    	System.out.println("/resources/"+game.getTrainersFilename()); // TODO
        in = new BufferedReader(new InputStreamReader(Trainer.class
                .getResource("/resources/"+game.getTrainerClassesFilename()).openStream())); // TODO : handle custom files ?

        JSONArray trainers = (JSONArray) jsonParser.parse(in);
        for(Object trainer : trainers) {
        	JSONObject trainerDic = (JSONObject) trainer;
        	String trainerClass = (String) trainerDic.get("trainerClass");
        	int id = (int)((Long) trainerDic.get("id")).intValue();
        	int money = (int)((Long) trainerDic.get("money")).intValue();
        	int genderOrCount = (int)((Long) trainerDic.get("genderOrCount")).intValue();
        	
        	trainerClassesByName.put(
        			new IgnoreCaseString(trainerClass), 
        			new TrainerClass(trainerClass, id, money, genderOrCount));
        }
	}
	
    private String trainerClass;
	private int id;
	private int money;
	private int genderOrCount;
	
	private TrainerClass(String trainerClass, int id, int money, int genderOrCount) {
		this.trainerClass = trainerClass;
		this.id = id;
		this.money = money;
		this.genderOrCount = genderOrCount;
	}

	public String getTrainerClass() {
		return trainerClass;
	}

	public int getId() {
		return id;
	}

	public int getMoney() {
		return money;
	}

	public int getGenderOrCount() {
		return genderOrCount;
	}
}
