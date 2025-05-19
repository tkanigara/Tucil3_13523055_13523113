package model;

/**
 * Represents a vehicle piece in the Rush Hour puzzle.
 */
public class Piece {
    private char id;           // Identifier character
    private int rowStart;      // Starting row position
    private int colStart;      // Starting column position
    private int length;        // Length of the piece (2 or 3 cells)
    private boolean isVertical; // Orientation: true for vertical, false for horizontal
    private boolean isPrimary; // Whether this is the primary piece (to be moved to exit)
    
    /**
     * Create a new piece
     * 
     * @param id         Character identifier
     * @param rowStart   Starting row
     * @param colStart   Starting column
     * @param length     Length of the piece
     * @param isVertical Orientation (true if vertical, false if horizontal)
     * @param isPrimary  Whether this is the primary piece
     */
    public Piece(char id, int rowStart, int colStart, int length, boolean isVertical, boolean isPrimary) {
        this.id = id;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.length = length;
        this.isVertical = isVertical;
        this.isPrimary = isPrimary;
    }
    
    /**
     * Creates a copy of an existing piece
     * 
     * @param other The piece to copy
     */
    public Piece(Piece other) {
        this.id = other.id;
        this.rowStart = other.rowStart;
        this.colStart = other.colStart;
        this.length = other.length;
        this.isVertical = other.isVertical;
        this.isPrimary = other.isPrimary;
    }
    
    /**
     * Check if this piece occupies a specific cell
     * 
     * @param row Row coordinate
     * @param col Column coordinate
     * @return True if the piece occupies the cell, false otherwise
     */
    public boolean occupies(int row, int col) {
        if (isVertical) {
            return col == colStart && row >= rowStart && row < rowStart + length;
        } else {
            return row == rowStart && col >= colStart && col < colStart + length;
        }
    }
    
    /**
     * Move this piece by the specified number of cells
     * 
     * @param steps Number of cells to move (positive for down/right, negative for up/left)
     */
    public void move(int steps) {
        if (isVertical) {
            rowStart += steps;
        } else {
            colStart += steps;
        }
    }
    
    // Getters and setters
    
    public char getId() {
        return id;
    }
    
    /**
     * Set the ID of the piece
     * 
     * @param id The new ID for the piece
     */
    public void setId(char id) {
        this.id = id;
    }
    
    public int getRowStart() {
        return rowStart;
    }
    
    /**
     * Set the starting row position of the piece
     * 
     * @param rowStart The new starting row position
     */
    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }
    
    public int getColStart() {
        return colStart;
    }
    
    /**
     * Set the starting column position of the piece
     * 
     * @param colStart The new starting column position
     */
    public void setColStart(int colStart) {
        this.colStart = colStart;
    }
    
    public int getLength() {
        return length;
    }
    
    /**
     * Set the length of the piece
     * 
     * @param length The new length of the piece
     */
    public void setLength(int length) {
        this.length = length;
    }
    
    public boolean isVertical() {
        return isVertical;
    }
    
    /**
     * Set the orientation of the piece
     * 
     * @param isVertical True for vertical orientation, false for horizontal
     */
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }
    
    /**
     * Check if the piece is oriented horizontally
     *
     * @return True if the piece is horizontal, false if vertical
     */
    public boolean isHorizontal() {
        return !isVertical;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    /**
     * Set whether this piece is the primary piece
     * 
     * @param isPrimary True if this is the primary piece, false otherwise
     */
    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    /**
     * Get the ending position of the piece
     * 
     * @return The row or column index where the piece ends
     */
    public int getEnd() {
        return isVertical ? rowStart + length - 1 : colStart + length - 1;
    }
    
    /**
     * Alias for getRowStart() - for compatibility
     * 
     * @return The starting row position
     */
    public int getRow() {
        return rowStart;
    }
    
    /**
     * Alias for setRowStart() - for compatibility
     * 
     * @param row The new starting row position
     */
    public void setRow(int row) {
        this.rowStart = row;
    }
    
    /**
     * Alias for getColStart() - for compatibility
     * 
     * @return The starting column position
     */
    public int getCol() {
        return colStart;
    }
    
    /**
     * Alias for setColStart() - for compatibility
     * 
     * @param col The new starting column position
     */
    public void setCol(int col) {
        this.colStart = col;
    }
    
    /**
     * Get the symbol character for this piece
     * 
     * @return The ID character that represents this piece on the board
     */
    public char getSymbol() {
        return id;
    }
    
    @Override
    public String toString() {
        String orientation = isVertical ? "vertical" : "horizontal";
        return "Piece " + id + " at (" + rowStart + "," + colStart + "), " + 
               "length: " + length + ", " + orientation + 
               (isPrimary ? " [PRIMARY]" : "");
    }
}
