package model;

/**
 * Represents a move in the Rush Hour puzzle.
 */
public class Move {
    private int pieceIndex;    // Index of the piece being moved
    private int steps;         // Number of steps to move (positive for down/right, negative for up/left)
    
    /**
     * Create a new move
     * 
     * @param pieceIndex Index of the piece to move
     * @param steps     Number of steps to move
     */
    public Move(int pieceIndex, int steps) {
        this.pieceIndex = pieceIndex;
        this.steps = steps;
    }
    
    public int getPieceIndex() {
        return pieceIndex;
    }
    
    public int getSteps() {
        return steps;
    }
    
    @Override
    public String toString() {
        return "Move piece " + pieceIndex + " by " + steps + " steps";
    }
}
