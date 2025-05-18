package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private int rows;            // Visible rows (excluding border)
    private int cols;            // Visible columns (excluding border)
    private char[][] grid;       // Grid with border included
    private int exitRow;         // Exit row position
    private int exitCol;         // Exit column position
    private ArrayList<Piece> pieces;
    
    // Constants for border positions
    private static final int BORDER_SIZE = 1;
    
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        // Create grid with invisible border (add 2 rows, 2 columns)
        this.grid = new char[rows + 2*BORDER_SIZE][cols + 2*BORDER_SIZE];
        this.pieces = new ArrayList<>();
        
        // Initialize grid with empty cells
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        // Default exit values (invalid position)
        this.exitRow = -1;
        this.exitCol = -1;
    }
    
    public Board(Board other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.exitRow = other.exitRow;
        this.exitCol = other.exitCol;
        
        // Deep copy the grid with border
        this.grid = new char[rows + 2*BORDER_SIZE][cols + 2*BORDER_SIZE];
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, cols + 2*BORDER_SIZE);
        }
        
        // Deep copy the pieces
        this.pieces = new ArrayList<>();
        for (Piece piece : other.pieces) {
            this.pieces.add(new Piece(piece));
        }
    }
    
    public void setExit(int row, int col) {
        this.exitRow = row;
        this.exitCol = col;
        
        // The physical exit position in the grid with border
        int borderRow, borderCol;
        
        // Convert logical exit position to grid position with border
        if (row == -1) { // Top exit
            borderRow = 0;
            borderCol = col + BORDER_SIZE;
        } else if (row == rows) { // Bottom exit
            borderRow = rows + BORDER_SIZE;
            borderCol = col + BORDER_SIZE;
        } else if (col == -1) { // Left exit
            borderRow = row + BORDER_SIZE;
            borderCol = 0;
        } else if (col == cols) { // Right exit
            borderRow = row + BORDER_SIZE;
            borderCol = cols + BORDER_SIZE;
        } else { // Inside the board
            borderRow = row + BORDER_SIZE;
            borderCol = col + BORDER_SIZE;
        }
        
        // Mark exit in the grid with the border
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
        
        // Adjust for border
        int gridRow = row + BORDER_SIZE;
        int gridCol = col + BORDER_SIZE;
        
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? gridRow : gridRow + i;
            int c = isHorizontal ? gridCol + i : gridCol;
            
            // Check if the cell is within the actual visible board area
            if (r >= BORDER_SIZE && r < rows + BORDER_SIZE && 
                c >= BORDER_SIZE && c < cols + BORDER_SIZE) {
                // Don't overwrite the exit marker
                if (grid[r][c] != 'K') {
                    grid[r][c] = symbol;
                }
            }
        }
    }
    
    public void updateGrid() {
        // Reset grid - only reset the visible portion plus exits
        for (int i = 0; i < rows + 2*BORDER_SIZE; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        // Re-mark exit in the grid
        if (exitRow != -1 || exitCol != -1) {
            // Convert logical exit position to grid position with border
            int borderRow, borderCol;
            
            if (exitRow == -1) { // Top exit
                borderRow = 0;
                borderCol = exitCol + BORDER_SIZE;
            } else if (exitRow == rows) { // Bottom exit
                borderRow = rows + BORDER_SIZE;
                borderCol = exitCol + BORDER_SIZE;
            } else if (exitCol == -1) { // Left exit
                borderRow = exitRow + BORDER_SIZE;
                borderCol = 0;
            } else if (exitCol == cols) { // Right exit
                borderRow = exitRow + BORDER_SIZE;
                borderCol = cols + BORDER_SIZE;
            } else { // Inside the board
                borderRow = exitRow + BORDER_SIZE;
                borderCol = exitCol + BORDER_SIZE;
            }
            
            grid[borderRow][borderCol] = 'K';
        }
        
        // Place all pieces
        for (Piece piece : pieces) {
            updatePieceInGrid(piece);
        }
    }
    
    // Method to move a piece to a specific position (regardless of distance)
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
    
    // The original method is kept for compatibility
    public boolean movePiece(Piece piece, int direction) {
        // direction: 1 = right/down, -1 = left/up
        int newRow = piece.getRow();
        int newCol = piece.getCol();
        
        if (piece.isHorizontal()) {
            newCol += direction;
        } else {
            newRow += direction;
        }
        
        return movePieceTo(piece, newRow, newCol);
    }
    
    public boolean canPlacePiece(Piece piece, int newRow, int newCol) {
        int length = piece.getLength();
        boolean isHorizontal = piece.isHorizontal();
        boolean isPrimary = piece.isPrimary();
        
        // Convert logical position to grid position with border
        int gridRow = newRow + BORDER_SIZE;
        int gridCol = newCol + BORDER_SIZE;
        
        // Check if the new position is valid
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? gridRow : gridRow + i;
            int c = isHorizontal ? gridCol + i : gridCol;
            
            // Check if the piece would be outside the valid play area
            boolean outsideBoard = r < BORDER_SIZE || r >= rows + BORDER_SIZE || 
                                  c < BORDER_SIZE || c >= cols + BORDER_SIZE;
            
            if (outsideBoard) {
                // Special case: primary piece at an exit
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
            
            // Skip checking the current piece's own positions
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
                // Check if the cell is empty or is the exit (only for primary piece)
                if (grid[r][c] != '.' && !(isPrimary && grid[r][c] == 'K')) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // Method to check if there's a clear path for sliding
    private boolean isClearPath(Piece piece, int targetRow, int targetCol) {
        int currentRow = piece.getRow();
        int currentCol = piece.getCol();
        boolean isHorizontal = piece.isHorizontal();
        
        // For horizontal pieces, check if all cells between current and target are empty
        if (isHorizontal) {
            int minCol = Math.min(currentCol, targetCol);
            int maxCol = Math.max(currentCol, targetCol);
            
            // Check all cells in between (excluding start position)
            for (int c = minCol + 1; c <= maxCol; c++) {
                if (c == currentCol) continue; // Skip the current position
                
                // Create a temporary board state to check this position
                Board tempBoard = new Board(this);
                Piece tempPiece = tempBoard.getPieces().get(pieces.indexOf(piece));
                
                if (!tempBoard.movePieceTo(tempPiece, currentRow, c)) {
                    return false;
                }
            }
        } 
        // For vertical pieces, check if all cells between current and target are empty
        else {
            int minRow = Math.min(currentRow, targetRow);
            int maxRow = Math.max(currentRow, targetRow);
            
            // Check all cells in between (excluding start position)
            for (int r = minRow + 1; r <= maxRow; r++) {
                if (r == currentRow) continue; // Skip the current position
                
                // Create a temporary board state to check this position
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
        // Find the primary piece
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
        
        // Check if the primary piece is at the exit
        if (isHorizontal) {
            // Exit on right edge
            if (exitCol == cols && row == exitRow && col + length - 1 == cols - 1) {
                return true;
            }
            // Exit on left edge
            else if (exitCol == -1 && row == exitRow && col == 0) {
                return true;
            }
        } else {
            // Exit on top edge
            if (exitRow == -1 && col == exitCol && row == 0) {
                return true;
            }
            // Exit on bottom edge
            else if (exitRow == rows && col == exitCol && row + length - 1 == rows - 1) {
                return true;
            }
        }
        
        return false;
    }
    
    // New method to get all possible states with sliding moves
    public List<Board> getNextStates() {
        List<Board> nextStates = new ArrayList<>();
        
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            boolean isHorizontal = piece.isHorizontal();
            int currentRow = piece.getRow();
            int currentCol = piece.getCol();
            
            // Find all possible positions for this piece
            if (isHorizontal) {
                // Try all possible horizontal positions
                for (int newCol = 0; newCol <= cols - piece.getLength(); newCol++) {
                    // Skip current position
                    if (newCol == currentCol) continue;
                    
                    // Check if we can move to this position directly (slide)
                    if (isClearPath(piece, currentRow, newCol)) {
                        Board newBoard = new Board(this);
                        Piece newPiece = newBoard.getPieces().get(i);
                        
                        if (newBoard.movePieceTo(newPiece, currentRow, newCol)) {
                            nextStates.add(newBoard);
                        }
                    }
                }
            } else {
                // Try all possible vertical positions
                for (int newRow = 0; newRow <= rows - piece.getLength(); newRow++) {
                    // Skip current position
                    if (newRow == currentRow) continue;
                    
                    // Check if we can move to this position directly (slide)
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
        // Return only the visible part of the grid (without border)
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
        
        // Compare only the visible grid configurations
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
        
        // Hash only the visible part of the grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result = 31 * result + grid[i + BORDER_SIZE][j + BORDER_SIZE];
            }
        }
        
        return result;
    }
}