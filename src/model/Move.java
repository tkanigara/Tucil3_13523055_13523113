package model;

/**
 * Represents a move in the Rush Hour puzzle.
 */
public class Move {
    private int pieceIndex;     // Index of the piece being moved
    private int targetRow;      // Target row position
    private int targetCol;      // Target column position
    private int fromRow;        // Starting row position
    private int fromCol;        // Starting column position
    
    /**
     * Create a new move
     * 
     * @param pieceIndex Index of the piece to move
     * @param fromRow    Starting row position
     * @param fromCol    Starting column position
     * @param targetRow  Target row position
     * @param targetCol  Target column position
     */
    public Move(int pieceIndex, int fromRow, int fromCol, int targetRow, int targetCol) {
        this.pieceIndex = pieceIndex;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }
    
    /**
     * Get the movement direction (for display)
     * 
     * @param isVertical Whether the piece is vertical
     * @return String description of the direction
     */
    public String getDirection(boolean isVertical) {
        if (isVertical) {
            if (targetRow < fromRow) {
                return "atas";
            } else {
                return "bawah";
            }
        } else {
            if (targetCol < fromCol) {
                return "kiri";
            } else {
                return "kanan";
            }
        }
    }
    
    /**
     * Get the magnitude of displacement
     * 
     * @param isVertical Whether the piece is vertical
     * @return The number of cells moved
     */
    public int getDistance(boolean isVertical) {
        if (isVertical) {
            return Math.abs(targetRow - fromRow);
        } else {
            return Math.abs(targetCol - fromCol);
        }
    }
    
    public int getPieceIndex() {
        return pieceIndex;
    }
    
    public int getTargetRow() {
        return targetRow;
    }
    
    public int getTargetCol() {
        return targetCol;
    }
    
    public int getFromRow() {
        return fromRow;
    }
    
    public int getFromCol() {
        return fromCol;
    }
    
    @Override
    public String toString() {
        return "Move piece " + pieceIndex + " from (" + fromRow + "," + fromCol + ") to (" + 
               targetRow + "," + targetCol + ")";
    }
}