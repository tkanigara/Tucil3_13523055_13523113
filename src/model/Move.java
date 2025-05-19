package model;

/* Kelas sebagai representasi move */
public class Move {
    private int pieceIndex;     
    private int targetRow;      
    private int targetCol;      
    private int fromRow;        
    private int fromCol;        
    
    /* Konstruktor move */
    public Move(int pieceIndex, int fromRow, int fromCol, int targetRow, int targetCol) {
        this.pieceIndex = pieceIndex;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }
    
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