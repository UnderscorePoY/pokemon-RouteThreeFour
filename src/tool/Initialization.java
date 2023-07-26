package tool;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import tool.exc.ToolInternalException;

public class Initialization {
    public static void init(Game game) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
    	Settings.game = game;
		Item.initItems(game);
		Move.initMoves(game);
		Learnset.initLearnsets(game);
		Species.initSpecies(game);
		Trainer.initTrainers(game);
    }
    
    public static void main(String[] args) {
    	try {
	    	for (Game game : Game.values())
	    		init(game);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
