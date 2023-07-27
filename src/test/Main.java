package test;

import tool.Game;
import tool.Trainer;

public class Main {
	
	public static void main(String[] args) {
		try {
			tool.Initialization.init(Game.EMERALD);
			
			Trainer norman_1 = Trainer.getTrainerByName("NORMAN_1");
			System.out.println(norman_1.getParty());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
