package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private int rows;        
    private int cols;           
    private char[][] grid;      
    private int exitRow;         
    private int exitCol;         
    private ArrayList<Piece> pieces;
    
    private static final int BORDER_SIZE = 1;
    
    /* Konstruktor Board */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows + 2*BORDER_SIZE][cols + 2*BORDER_SIZE];
        this.pieces = new ArrayList<>();
        
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        this.exitRow = -1;
        this.exitCol = -1;
    }
    
    /* CCtor */
    public Board(Board other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.exitRow = other.exitRow;
        this.exitCol = other.exitCol;
        
        this.grid = new char[rows + 2*BORDER_SIZE][cols + 2*BORDER_SIZE];
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, cols + 2*BORDER_SIZE);
        }
        
        this.pieces = new ArrayList<>();
        for (Piece piece : other.pieces) {
            this.pieces.add(new Piece(piece));
        }
    }
    

    public void setExit(int row, int col) {
        this.exitRow = row;
        this.exitCol = col;
        
        int borderRow, borderCol;
        
        if (row == -1) {                        // exit atas
            borderRow = 0;
            borderCol = col + BORDER_SIZE;
        } else if (row == rows) {               // exit bawah
            borderRow = rows + BORDER_SIZE;
            borderCol = col + BORDER_SIZE;
        } else if (col == -1) {                 // exit kiri
            borderRow = row + BORDER_SIZE;
            borderCol = 0;
        } else if (col == cols) {               // exit kanan
            borderRow = row + BORDER_SIZE;
            borderCol = cols + BORDER_SIZE;
        } else {                                // case di dalem
            borderRow = row + BORDER_SIZE;
            borderCol = col + BORDER_SIZE;
        }
        
        grid[borderRow][borderCol] = 'K';
    }
    
    public void addPiece(Piece piece) {
        pieces.add(piece);
        updatePieceInGrid(piece);
    }
    
    private void updatePieceInGrid(Piece piece) {
        int row = piece.getRow();
        int col = piece.getCol();
        int length = piece.getLength();
        boolean isHorizontal = piece.isHorizontal();
        char symbol = piece.getSymbol();
        
        int gridRow = row + BORDER_SIZE;
        int gridCol = col + BORDER_SIZE;
        
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? gridRow : gridRow + i;
            int c = isHorizontal ? gridCol + i : gridCol;
            
            if (r >= BORDER_SIZE && r < rows + BORDER_SIZE && 
                c >= BORDER_SIZE && c < cols + BORDER_SIZE) {
                if (grid[r][c] != 'K') {
                    grid[r][c] = symbol;
                }
            }
        }
    }
    
    public void updateGrid() {
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        if (exitRow != -1 || exitCol != -1) {
            int borderRow, borderCol;
            
            if (exitRow == -1) {                    // exit atas
                borderRow = 0;
                borderCol = exitCol + BORDER_SIZE;
            } else if (exitRow == rows) {           // exit bawah
                borderRow = rows + BORDER_SIZE;
                borderCol = exitCol + BORDER_SIZE;
            } else if (exitCol == -1) {             // exit kiri
                borderRow = exitRow + BORDER_SIZE;
                borderCol = 0;
            } else if (exitCol == cols) {           // exit kanan
                borderRow = exitRow + BORDER_SIZE;
                borderCol = cols + BORDER_SIZE;
            } else {
                borderRow = exitRow + BORDER_SIZE;
                borderCol = exitCol + BORDER_SIZE;
            }
            
            grid[borderRow][borderCol] = 'K';
        }
        
        for (Piece piece : pieces) {
            updatePieceInGrid(piece);
        }
    }
    
    /* Move piece */
    public boolean movePieceTo(Piece piece, int targetRow, int targetCol) {
        if (canPlacePiece(piece, targetRow, targetCol)) {
            // Update piece position
            piece.setRow(targetRow);
            piece.setCol(targetCol);
            
            // Update the grid
            updateGrid();
            
            return true;
        }
        
        return false;
    }
    
    public boolean movePiece(Piece piece, int direction) {
        int newRow = piece.getRow();
        int newCol = piece.getCol();
        
        if (piece.isHorizontal()) {
            newCol += direction;
        } else {
            newRow += direction;
        }
        
        return movePieceTo(piece, newRow, newCol);
    }
    
    /* Ngecek bisa atau ga */
    public boolean canPlacePiece(Piece piece, int newRow, int newCol) {
        int length = piece.getLength();
        boolean isHorizontal = piece.isHorizontal();
        boolean isPrimary = piece.isPrimary();
        
        int gridRow = newRow + BORDER_SIZE;
        int gridCol = newCol + BORDER_SIZE;
        
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? gridRow : gridRow + i;
            int c = isHorizontal ? gridCol + i : gridCol;
            
            boolean outsideBoard = r < BORDER_SIZE || r >= rows + BORDER_SIZE || 
                                  c < BORDER_SIZE || c >= cols + BORDER_SIZE;
            
            if (outsideBoard) {
                if (isPrimary) {
                    // Top exit
                    if (exitRow == -1 && r == 0 && c == exitCol + BORDER_SIZE) {
                        continue;
                    }
                    // Bottom exit
                    if (exitRow == rows && r == rows + BORDER_SIZE && c == exitCol + BORDER_SIZE) {
                        continue;
                    }
                    // Left exit
                    if (exitCol == -1 && c == 0 && r == exitRow + BORDER_SIZE) {
                        continue;
                    }
                    // Right exit
                    if (exitCol == cols && c == cols + BORDER_SIZE && r == exitRow + BORDER_SIZE) {
                        continue;
                    }
                }
                return false;
            }
            
            boolean isOwnPosition = false;
            int origRow = piece.getRow() + BORDER_SIZE;
            int origCol = piece.getCol() + BORDER_SIZE;
            
            for (int j = 0; j < length; j++) {
                int origR = isHorizontal ? origRow : origRow + j;
                int origC = isHorizontal ? origCol + j : origCol;
                
                if (r == origR && c == origC) {
                    isOwnPosition = true;
                    break;
                }
            }
            
            if (!isOwnPosition) {
                if (grid[r][c] != '.' && !(isPrimary && grid[r][c] == 'K')) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /* Ngecek apakah cleat buat bbrp perjalanan sekaligus */
    private boolean isClearPath(Piece piece, int targetRow, int targetCol) {
        int currentRow = piece.getRow();
        int currentCol = piece.getCol();
        boolean isHorizontal = piece.isHorizontal();
        
        if (isHorizontal) {
            int minCol = Math.min(currentCol, targetCol);
            int maxCol = Math.max(currentCol, targetCol);
            
            for (int c = minCol + 1; c <= maxCol; c++) {
                if (c == currentCol) continue;
                
                Board tempBoard = new Board(this);
                Piece tempPiece = tempBoard.getPieces().get(pieces.indexOf(piece));
                
                if (!tempBoard.movePieceTo(tempPiece, currentRow, c)) {
                    return false;
                }
            }
        } 
        else {
            int minRow = Math.min(currentRow, targetRow);
            int maxRow = Math.max(currentRow, targetRow);
            
            for (int r = minRow + 1; r <= maxRow; r++) {
                if (r == currentRow) continue;
                
                Board tempBoard = new Board(this);
                Piece tempPiece = tempBoard.getPieces().get(pieces.indexOf(piece));
                
                if (!tempBoard.movePieceTo(tempPiece, r, currentCol)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public boolean isSolved() {
        Piece primaryPiece = null;
        for (Piece piece : pieces) {
            if (piece.isPrimary()) {
                primaryPiece = piece;
                break;
            }
        }
        
        if (primaryPiece == null) {
            return false;
        }
        
        int row = primaryPiece.getRow();
        int col = primaryPiece.getCol();
        int length = primaryPiece.getLength();
        boolean isHorizontal = primaryPiece.isHorizontal();
        
        if (isHorizontal) {
            // kalau exit di kanan
            if (exitCol == cols && row == exitRow && col + length - 1 == cols - 1) {
                return true;
            }
            // kalau exit di kiri
            else if (exitCol == -1 && row == exitRow && col == 0) {
                return true;
            }
        } else {
            // kalau exit di atas
            if (exitRow == -1 && col == exitCol && row == 0) {
                return true;
            }
            // kalau exit di bawah
            else if (exitRow == rows && col == exitCol && row + length - 1 == rows - 1) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Board> getNextStates() {
        List<Board> nextStates = new ArrayList<>();
        
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            boolean isHorizontal = piece.isHorizontal();
            int currentRow = piece.getRow();
            int currentCol = piece.getCol();
            
            if (isHorizontal) {
                for (int newCol = 0; newCol <= cols - piece.getLength(); newCol++) {
                    if (newCol == currentCol) continue;
                    
                    if (isClearPath(piece, currentRow, newCol)) {
                        Board newBoard = new Board(this);
                        Piece newPiece = newBoard.getPieces().get(i);
                        
                        if (newBoard.movePieceTo(newPiece, currentRow, newCol)) {
                            nextStates.add(newBoard);
                        }
                    }
                }
            } else {
                for (int newRow = 0; newRow <= rows - piece.getLength(); newRow++) {
                    if (newRow == currentRow) continue;
                    
                    if (isClearPath(piece, newRow, currentCol)) {
                        Board newBoard = new Board(this);
                        Piece newPiece = newBoard.getPieces().get(i);
                        
                        if (newBoard.movePieceTo(newPiece, newRow, currentCol)) {
                            nextStates.add(newBoard);
                        }
                    }
                }
            }
        }
        
        return nextStates;
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public int getExitRow() {
        return exitRow;
    }
    
    public int getExitCol() {
        return exitCol;
    }
    
    public ArrayList<Piece> getPieces() {
        return pieces;
    }
    
    public Piece getPrimaryPiece() {
        for (Piece piece : pieces) {
            if (piece.isPrimary()) {
                return piece;
            }
        }
        return null;
    }
    
    public char[][] getGrid() {
        char[][] visibleGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                visibleGrid[i][j] = grid[i + BORDER_SIZE][j + BORDER_SIZE];
            }
        }
        return visibleGrid;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i + BORDER_SIZE][j + BORDER_SIZE]);
            }
            if (i < rows - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Board other = (Board) obj;
        if (rows != other.rows || cols != other.cols) return false;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i + BORDER_SIZE][j + BORDER_SIZE] != other.grid[i + BORDER_SIZE][j + BORDER_SIZE]) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = rows;
        result = 31 * result + cols;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result = 31 * result + grid[i + BORDER_SIZE][j + BORDER_SIZE];
            }
        }
        
        return result;
    }
}