package tool;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import tool.exception.ToolInternalException;

public class Initialization {
    public static void init(Game game, Language lang) 
    		throws FileNotFoundException, IOException, ParseException, ToolInternalException {
        System.out.println(String.format("Loading game data for Pokémon %s (%s) ...", game.getDisplayName(), lang.getName()));
        
    	Settings.game = game;
    	Settings.lang = lang;
		Item.initItems(game);
		Move.initMoves(game);
		Learnset.initLearnsets(game);
		Species.initSpecies(game, lang);
		Trainer.initTrainers(game, lang);

        System.out.println("Succesfully loaded.");
    }
    
    public static void init(Game game) 
    		throws FileNotFoundException, IOException, ParseException, ToolInternalException {
    	init(game, Language.default_);
    }
}
