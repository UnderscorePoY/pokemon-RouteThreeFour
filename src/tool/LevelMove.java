package tool;

/**
 * A move associated with a level at which a species learns it.
 */
public class LevelMove {
    private final int level;
    private final Move move;
    
    public LevelMove(int a_level, Move a_move){
        level = a_level;
        move = a_move;
    }

    public String toString() {
        return String.format("%d %s", level, move.getName());
    }
    public Move getMove() {
        return move;
    }
    public int getLevel() {
        return level;
    }
}
