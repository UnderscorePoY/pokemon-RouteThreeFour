package tool;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import tool.exception.ToolInternalException;

public class Initialization {
    public static void init(Game game) throws FileNotFoundException, IOException, ParseException, ToolInternalException {
    	Settings.game = game;
		Item.initItems(game);
		Move.initMoves(game);
		Learnset.initLearnsets(game);
		Species.initSpecies(game);
		Trainer.initTrainers(game);
    }
}
