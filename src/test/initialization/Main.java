package test.initialization;

import tool.Game;

public class Main {
    public static void main(String[] args) {
    	try {
	    	for (Game game : Game.values())
	    		tool.Initialization.init(game);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
