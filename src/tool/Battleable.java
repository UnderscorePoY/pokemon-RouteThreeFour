package tool;

import tool.exc.ToolInternalException;

public interface Battleable {
    //makes pokemon p get exp from this object
    void battle(Pokemon p, BattleOptions options) throws ToolInternalException;
}
