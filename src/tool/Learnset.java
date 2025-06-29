package tool;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tool.exception.ToolInternalException;

/**
 * A representation of a sequence of moves and levels which a species learns moves at.
 */
public class Learnset {
	private static HashMap<IgnoreCaseString, Learnset> learnsetsByName;
	private static final int LEVEL_INDEX = 0, MOVENAME_INDEX = 1; // For Gen 3
	
	public static Learnset getLearnsetByName(String name) {
        return learnsetsByName.get(new IgnoreCaseString(name));
    }
	
	public static void initLearnsets(Game game) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
		learnsetsByName = new LinkedHashMap<IgnoreCaseString, Learnset>();
		
        BufferedReader in;
        String learnsetsResourcePathName = Settings.getResourceRelativePathName(game.getLearnsetsFilename());
        in = new BufferedReader(new InputStreamReader(Learnset.class.getResource(
        		learnsetsResourcePathName).openStream())); // TODO : handle custom files ?
        
        if(game.isGen3())
        	initLearnsetsGen3(in);
        else if (game.isGen4())
        	initLearnsetsGen4(in);
        else { // TODO : clean
        	// throw new ToolInternalException(Learnset.class.getEnclosingMethod(), game, "");
        	// throw new ToolInternalException(Learnset.class.getMethods()[0].getName(), game, "");
        	throw new ToolInternalException(game, "");
        }

        System.out.println(String.format("INFO: Learnsets loaded from '%s'", learnsetsResourcePathName));
    }
	
	private static void initLearnsetsGen3(BufferedReader in) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();

        JSONObject learnsetsDic = (JSONObject) jsonParser.parse(in);
        for(Object learnsetEntryObj : learnsetsDic.entrySet()) {
            Learnset learnset = new Learnset();
            
        	@SuppressWarnings("unchecked")
			Map.Entry<Object, Object> learnsetEntry = (Map.Entry<Object, Object>) learnsetEntryObj;
        	String speciesName = (String) learnsetEntry.getKey();
        	JSONArray levelMovesArray = (JSONArray) learnsetEntry.getValue();
        	for(Object levelMoveObj : levelMovesArray) {
        		JSONArray levelMoveArray = (JSONArray) levelMoveObj;
        		int level = ((Long) levelMoveArray.get(LEVEL_INDEX)).intValue();
        		String moveName = (String) levelMoveArray.get(MOVENAME_INDEX);
        		Move move = Move.getMoveByName(moveName);
        		LevelMove levelMove = new LevelMove(level, move);
        		learnset.add(levelMove);
        	}
        	
        	learnsetsByName.put(new IgnoreCaseString(speciesName), learnset);
        }
	}

	private static void initLearnsetsGen4(BufferedReader in) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();

    	JSONArray learnsetList = (JSONArray) jsonParser.parse(in);
    	for(Object learnsetObj : learnsetList) {
            Learnset learnset = new Learnset();
            
    		JSONObject learnsetDic = (JSONObject) learnsetObj;
    		String speciesName = (String) learnsetDic.get("species");
    		JSONArray moves = (JSONArray) learnsetDic.get("moves");
    		for(Object moveObj : moves) {
    			JSONObject moveJSONObj = (JSONObject) moveObj;
    			String moveName = (String) moveJSONObj.get("move");
    			Move move = Move.getMoveByName(moveName);
    			int level = ((Long) moveJSONObj.get("level")).intValue();
    			LevelMove levelMove = new LevelMove(level, move);
    			learnset.add(levelMove);
    		}
    		
    		learnsetsByName.put(new IgnoreCaseString(speciesName), learnset);
    	}
	}

	
	private static void printLearnsets() {
		for(Map.Entry<IgnoreCaseString, Learnset> entry : learnsetsByName.entrySet()) {
			System.out.println(entry.getKey()+"="+entry.getValue());
		}
	}
	
	public static Set<Entry<IgnoreCaseString, Learnset>> entrySet(){
		return learnsetsByName.entrySet();
	}
	

    private ArrayList<LevelMove> levelMoves;
    
    public Learnset() {
        levelMoves = new ArrayList<LevelMove>();
    }
            
    public Learnset(List<LevelMove> new_levelMoves) {
        if (new_levelMoves == null) {
            levelMoves = new ArrayList<LevelMove>();
        } else {
        	levelMoves = new ArrayList<LevelMove>(new_levelMoves);
        }
    }
    
    public boolean add(LevelMove levelMove) {
    	return levelMoves.add(levelMove);
    }
    
    public ArrayList<LevelMove> getLevelMoves(){
    	return levelMoves;
    }
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	for (LevelMove levelMove : levelMoves) {
    		sb.append(levelMove);
    		sb.append("\n");
    	}
    	return sb.toString();
    }
    
}
