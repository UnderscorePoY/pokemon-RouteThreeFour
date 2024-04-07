package test.initialization;

import java.util.Map;

import tool.BattleOptions;
import tool.Game;
import tool.IgnoreCaseString;
import tool.Pokemon;
import tool.Trainer;

public class Main {
    public static void main(String[] args) {
    	try {
    		//BattleOptions placeholderOptions = new BattleOptions();
	    	for (Game game : new Game[] {Game.HEARTGOLD}) {
	    		tool.Initialization.init(game);
	    		for(Map.Entry<IgnoreCaseString, Trainer> entry: Trainer.getTrainers().entrySet()) {
	    			System.out.print(entry.getKey());
	    			System.out.print("=");
	    			System.out.println(String.format("%s %s %d$%s ", 
	    					entry.getValue().getTrainerClass(),
	    					entry.getValue().getTrainerName(),
	    					//entry.getValue().getAiFlags(),
	    					entry.getValue().getReward(entry.getValue().getParty().get(0)),
	    					(entry.getValue().getItems() != null && !entry.getValue().getItems().isEmpty()) ? entry.getValue().getItems().toString(): ""
	    					));
	    			//System.out.println(String.format("%d$", entry.getValue().getReward(entry.getValue().getParty().get(0))));
	    			//System.out.println(":");
	    			for(Pokemon poke: entry.getValue().getParty())
	    				System.out.println(poke.allInfoStr());
	    			System.out.println();
	    		}
	    	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
