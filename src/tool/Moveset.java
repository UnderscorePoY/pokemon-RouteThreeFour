package tool;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A list of moves a Pokémon actually learned. This list can contain more than 4 moves.
 */
public class Moveset implements Iterable<Move>{
    private ArrayList<Move> moves = new ArrayList<Move>();
    
    public Moveset() {
    }
    
    public Moveset(List<Move> newMoves) {
        if (newMoves == null)
            return;          
        moves = new ArrayList<Move>(newMoves);
    }
    
    public Moveset(Moveset o) { // Copy constructor
    	this(o.moves);
    }
    
    /**
     * Returns the 4 most recently learned moves for a given species at a certain level.
     */
    public static Moveset defaultMoveset(Species species, int level){
    	return defaultMoveset(species.getDisplayName(), level);
    }
    
    /**
     * Returns the 4 most recently learned moves for a given Pokémon name at a certain level.
     */
    public static Moveset defaultMoveset(String speciesName, int level){
        ArrayList<Move> distinctMoves = new ArrayList<Move>();
        HashSet<Move> movesSet = new HashSet<Move>();
        Learnset l = Learnset.getLearnsetByName(speciesName);
        if (l == null) {
            return new Moveset();
        }
        ArrayList<LevelMove> lms = l.getLevelMoves();
        for(LevelMove lm : lms) {
            Move m = lm.getMove();
            if (!movesSet.contains(m) && lm.getLevel() <= level) {
                movesSet.add(m);
                distinctMoves.add(m);
            }
        }
        
        if (distinctMoves.size() <= 4)
            return new Moveset(distinctMoves);
        else {
            int n = distinctMoves.size();
            return new Moveset(distinctMoves.subList(n-4, n));
        }
            
    }
    
    private static final String sep = ", ";
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for(Move m : moves) {
            sb.append(String.format("%s%s", m.getName(), sep));
        }
        
        return sb.length() == 0 ? sb.toString() : sb.substring(0, sb.length() - sep.length());
    }

    @Override
    public Iterator<Move> iterator() {
        return moves.iterator();
    }
    
    public void addMove(Move m) {
        if (!moves.contains(m))
            moves.add(m);
    }
    
    public void addMove(String s) {
        addMove(Move.getMoveByName(s));
    }
    
    public boolean delMove(Move m) {
        return moves.remove(m);
    }
    
    public void delMove(String s) {
        delMove(Move.getMoveByName(s));
    }
    
    public Set<Move> getSetView(){
    	return new HashSet<Move>(moves);
    }
    
}
