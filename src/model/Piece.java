package model;

public class Piece {
    private char id;
    private int rowStart;
    private int colStart;
    private int length;
    private boolean isVertical;
    private boolean isPrimary;
    
    /* Konstruktor Piece */
    public Piece(char id, int rowStart, int colStart, int length, boolean isVertical, boolean isPrimary) {
        this.id = id;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.length = length;
        this.isVertical = isVertical;
        this.isPrimary = isPrimary;
    }
    
    /* CCtor */
    public Piece(Piece other) {
        this.id = other.id;
        this.rowStart = other.rowStart;
        this.colStart = other.colStart;
        this.length = other.length;
        this.isVertical = other.isVertical;
        this.isPrimary = other.isPrimary;
    }
    
   /* Cek piece nempatin cell atau ga */
    public boolean occupies(int row, int col) {
        if (isVertical) {
            return col == colStart && row >= rowStart && row < rowStart + length;
        } else {
            return row == rowStart && col >= colStart && col < colStart + length;
        }
    }
    
   /* Move piece */
    public void move(int steps) {
        if (isVertical) {
            rowStart += steps;
        } else {
            colStart += steps;
        }
    }
    
    /* Getter dan Setter */
    public char getId() {
        return id;
    }
    
    public void setId(char id) {
        this.id = id;
    }
    
    public int getRowStart() {
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }
    
    public int getColStart() {
        return colStart;
    }
    
    public void setColStart(int colStart) {
        this.colStart = colStart;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public boolean isVertical() {
        return isVertical;
    }
    
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }
    
    public boolean isHorizontal() {
        return !isVertical;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public int getEnd() {
        return isVertical ? rowStart + length - 1 : colStart + length - 1;
    }
    
    public int getRow() {
        return rowStart;
    }
    
    public void setRow(int row) {
        this.rowStart = row;
    }
    
    public int getCol() {
        return colStart;
    }
    
    public void setCol(int col) {
        this.colStart = col;
    }
    
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
