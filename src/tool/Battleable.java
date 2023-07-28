package tool;

import tool.exception.ToolInternalException;

public interface Battleable {
    /**
     * Makes pokemon p get experience from this object.
     */
    void battle(Pokemon p, BattleOptions options) throws ToolInternalException;
    
    /**
     * Returns the number of battlers.
     */
    int getNbOfBattlers();
}
