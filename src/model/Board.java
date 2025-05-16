// package model;

// import java.util.ArrayList;
// import java.util.List;

// /**
//  * Represents the game board for the Rush Hour puzzle.
//  */
// public class Board {
//     private int rows;          // Number of rows in the board
//     private int cols;          // Number of columns in the board
//     private List<Piece> pieces; // List of pieces on the board
//     private Piece primaryPiece; // Reference to the primary piece
//     private int exitRow;       // Row coordinate of the exit
//     private int exitCol;       // Column coordinate of the exit
//     private char[][] grid;     // Grid representation of the board
//       /**
//      * Create a new board with the specified dimensions
//      * 
//      * @param rows Number of rows
//      * @param cols Number of columns
//      */
//     public Board(int rows, int cols) {
//         this.rows = rows;
//         this.cols = cols;
//         this.pieces = new ArrayList<>();
//         this.grid = new char[rows][cols];
        
//         // Initialize with default exit position
//         this.exitRow = 0;
//         this.exitCol = 0;
        
//         // Initialize grid with empty cells
//         for (int i = 0; i < rows; i++) {
//             for (int j = 0; j < cols; j++) {
//                 grid[i][j] = '.';
//             }
//         }
//     }
    
//     /**
//      * Create a deep copy of an existing board
//      * 
//      * @param other The board to copy
//      */
//     public Board(Board other) {
//         this.rows = other.rows;
//         this.cols = other.cols;
//         this.exitRow = other.exitRow;
//         this.exitCol = other.exitCol;
        
//         // Deep copy of pieces
//         this.pieces = new ArrayList<>();
//         for (Piece piece : other.pieces) {
//             Piece newPiece = new Piece(piece);
//             this.pieces.add(newPiece);
//             if (piece.isPrimary()) {
//                 this.primaryPiece = newPiece;
//             }
//         }
        
//         // Regenerate grid
//         this.grid = new char[rows][cols];
//         updateGrid();
//     }
    
//     /**
//      * Add a piece to the board
//      * 
//      * @param piece The piece to add
//      */
//     public void addPiece(Piece piece) {
//         pieces.add(piece);
//         if (piece.isPrimary()) {
//             primaryPiece = piece;
//         }
        
//         // Update grid with the new piece
//         updatePieceInGrid(piece);
//     }
//       /**
//      * Set the exit location
//      * 
//      * @param row Exit row
//      * @param col Exit column
//      */
//     public void setExit(int row, int col) {
//         this.exitRow = row;
//         this.exitCol = col;
        
//         // Only mark exit in grid if it's within board bounds
//         if (row >= 0 && row < rows && col >= 0 && col < cols) {
//             grid[row][col] = 'K'; // Mark exit in grid
//         }
//     }
//       /**
//      * Update the grid representation of the board
//      */
//     private void updateGrid() {
//         // Reset grid
//         for (int i = 0; i < rows; i++) {
//             for (int j = 0; j < cols; j++) {
//                 grid[i][j] = '.';
//             }
//         }
        
//         // Mark exit only if it's within bounds
//         if (exitRow >= 0 && exitRow < rows && exitCol >= 0 && exitCol < cols) {
//             grid[exitRow][exitCol] = 'K';
//         }
        
//         // Add pieces to grid
//         for (Piece piece : pieces) {
//             updatePieceInGrid(piece);
//         }
//     }
//       /**
//      * Update a specific piece in the grid
//      * 
//      * @param piece The piece to update in the grid
//      */
//     private void updatePieceInGrid(Piece piece) {
//         char id = piece.getId();
//         if (piece.isVertical()) {
//             for (int r = piece.getRowStart(); r < piece.getRowStart() + piece.getLength(); r++) {
//                 if (r >= 0 && r < rows && piece.getColStart() >= 0 && piece.getColStart() < cols) {
//                     // Don't overwrite the exit marker
//                     if (grid[r][piece.getColStart()] != 'K') {
//                         grid[r][piece.getColStart()] = id;
//                     }
//                 }
//             }
//         } else {
//             for (int c = piece.getColStart(); c < piece.getColStart() + piece.getLength(); c++) {
//                 if (piece.getRowStart() >= 0 && piece.getRowStart() < rows && c >= 0 && c < cols) {
//                     // Don't overwrite the exit marker
//                     if (grid[piece.getRowStart()][c] != 'K') {
//                         grid[piece.getRowStart()][c] = id;
//                     }
//                 }
//             }
//         }
//     }
    
//     /**
//      * Check if a move is valid for a specific piece
//      * 
//      * @param pieceIndex Index of the piece to move
//      * @param steps      Number of steps to move
//      * @return True if the move is valid, false otherwise
//      */
//     public boolean isValidMove(int pieceIndex, int steps) {
//         if (pieceIndex < 0 || pieceIndex >= pieces.size() || steps == 0) {
//             return false;
//         }
        
//         Piece piece = pieces.get(pieceIndex);
//         boolean isVertical = piece.isVertical();
        
//         // Check boundaries and collisions
//         if (isVertical) {
//             // Moving up (negative steps)
//             if (steps < 0) {
//                 int newStart = piece.getRowStart() + steps;
//                 if (newStart < 0) {
//                     return false; // Out of bounds
//                 }
                
//                 // Check for collisions
//                 for (int r = newStart; r < piece.getRowStart(); r++) {
//                     if (grid[r][piece.getColStart()] != '.') {
//                         return false; // Collision with another piece
//                     }
//                 }
//             } 
//             // Moving down (positive steps)
//             else {
//                 int newEnd = piece.getEnd() + steps;
//                 if (newEnd >= rows) {
//                     return false; // Out of bounds
//                 }
                
//                 // Check for collisions
//                 for (int r = piece.getEnd() + 1; r <= newEnd; r++) {
//                     if (grid[r][piece.getColStart()] != '.' && grid[r][piece.getColStart()] != 'K') {
//                         return false; // Collision with another piece
//                     }
                    
//                     // Only primary piece can move to exit
//                     if (grid[r][piece.getColStart()] == 'K' && !piece.isPrimary()) {
//                         return false;
//                     }
//                 }
//             }
//         } else {
//             // Moving left (negative steps)
//             if (steps < 0) {
//                 int newStart = piece.getColStart() + steps;
//                 if (newStart < 0) {
//                     return false; // Out of bounds
//                 }
                
//                 // Check for collisions
//                 for (int c = newStart; c < piece.getColStart(); c++) {
//                     if (grid[piece.getRowStart()][c] != '.') {
//                         return false; // Collision with another piece
//                     }
//                 }
//             } 
//             // Moving right (positive steps)
//             else {
//                 int newEnd = piece.getEnd() + steps;
//                 if (newEnd >= cols) {
//                     return false; // Out of bounds
//                 }
                
//                 // Check for collisions
//                 for (int c = piece.getEnd() + 1; c <= newEnd; c++) {
//                     if (grid[piece.getRowStart()][c] != '.' && grid[piece.getRowStart()][c] != 'K') {
//                         return false; // Collision with another piece
//                     }
                    
//                     // Only primary piece can move to exit
//                     if (grid[piece.getRowStart()][c] == 'K' && !piece.isPrimary()) {
//                         return false;
//                     }
//                 }
//             }
//         }
        
//         return true;
//     }
    
//     /**
//      * Apply a move to a specific piece
//      * 
//      * @param pieceIndex Index of the piece to move
//      * @param steps      Number of steps to move
//      * @return A new board with the move applied
//      */
//     public Board applyMove(int pieceIndex, int steps) {
//         if (!isValidMove(pieceIndex, steps)) {
//             return null;
//         }
        
//         Board newBoard = new Board(this);
//         newBoard.pieces.get(pieceIndex).move(steps);
//         newBoard.updateGrid();
        
//         return newBoard;
//     }    /**
//      * Check if the puzzle is solved (primary piece reaches the exit)
//      * 
//      * @return True if solved, false otherwise
//      */
//     public boolean isSolved() {
//         if (primaryPiece == null) {
//             return false;
//         }
        
//         // Handle case when exit is outside the board (at the right edge)
//         if (exitCol == cols) {
//             // Horizontal primary piece reaches the right edge
//             return !primaryPiece.isVertical() && 
//                    primaryPiece.getRowStart() == exitRow && 
//                    primaryPiece.getEnd() == cols - 1;
//         }
        
//         // Handle case when exit is outside the board (at the bottom edge)
//         if (exitRow == rows) {
//             // Vertical primary piece reaches the bottom edge
//             return primaryPiece.isVertical() && 
//                    primaryPiece.getColStart() == exitCol && 
//                    primaryPiece.getEnd() == rows - 1;
//         }
        
//         // For other cases (exit is on the edges of the board)
//         if (!primaryPiece.isVertical()) {
//             // If exit is on the right edge and primary piece extends to the right edge
//             if (exitCol == cols - 1 && primaryPiece.getEnd() == cols - 1) {
//                 return primaryPiece.getRowStart() == exitRow;
//             }
            
//             // If exit is on the left edge and primary piece extends to the left edge
//             if (exitCol == 0 && primaryPiece.getColStart() == 0) {
//                 return primaryPiece.getRowStart() == exitRow;
//             }
//         }
        
//         // For a vertical primary piece
//         if (primaryPiece.isVertical()) {
//             // If exit is on the bottom edge and primary piece extends to the bottom edge
//             if (exitRow == rows - 1 && primaryPiece.getEnd() == rows - 1) {
//                 return primaryPiece.getColStart() == exitCol;
//             }
            
//             // If exit is on the top edge and primary piece extends to the top edge
//             if (exitRow == 0 && primaryPiece.getRowStart() == 0) {
//                 return primaryPiece.getColStart() == exitCol;
//             }
//         }
        
//         return false;
//     }
    
//     /**
//      * Generate all possible next states from the current board
//      * 
//      * @return A list of possible next states
//      */
//     public List<Board> generateNextStates() {
//         List<Board> nextStates = new ArrayList<>();
        
//         for (int i = 0; i < pieces.size(); i++) {
//             Piece piece = pieces.get(i);
            
//             // Try moving in both directions
//             if (piece.isVertical()) {
//                 // Try moving up (negative steps)
//                 for (int steps = -1; isValidMove(i, steps); steps--) {
//                     nextStates.add(applyMove(i, steps));
//                 }
                
//                 // Try moving down (positive steps)
//                 for (int steps = 1; isValidMove(i, steps); steps++) {
//                     nextStates.add(applyMove(i, steps));
//                 }
//             } else {
//                 // Try moving left (negative steps)
//                 for (int steps = -1; isValidMove(i, steps); steps--) {
//                     nextStates.add(applyMove(i, steps));
//                 }
                
//                 // Try moving right (positive steps)
//                 for (int steps = 1; isValidMove(i, steps); steps++) {
//                     nextStates.add(applyMove(i, steps));
//                 }
//             }
//         }
        
//         return nextStates;
//     }
    
//     /**
//      * Get the string representation of the move (for output)
//      * 
//      * @param pieceIndex Index of the piece being moved
//      * @param steps      Number of steps the piece is moved
//      * @return String describing the move
//      */
//     public String getMoveDescription(int pieceIndex, int steps) {
//         Piece piece = pieces.get(pieceIndex);
//         char id = piece.getId();
//         String direction;
        
//         if (piece.isVertical()) {
//             direction = steps < 0 ? "atas" : "bawah";
//         } else {
//             direction = steps < 0 ? "kiri" : "kanan";
//         }
        
//         return id + "-" + direction;
//     }
    
//     /**
//      * Generate a hash code for the board (used in the search algorithms)
//      */
//     @Override
//     public int hashCode() {
//         return toString().hashCode();
//     }
    
//     /**
//      * Check if two boards are equal
//      */
//     @Override
//     public boolean equals(Object obj) {
//         if (this == obj) return true;
//         if (obj == null || getClass() != obj.getClass()) return false;
        
//         Board other = (Board) obj;
//         return toString().equals(other.toString());
//     }
    
//     /**
//      * Get string representation of the board
//      */
//     @Override
//     public String toString() {
//         StringBuilder sb = new StringBuilder();
//         for (int i = 0; i < rows; i++) {
//             for (int j = 0; j < cols; j++) {
//                 sb.append(grid[i][j]);
//             }
//             if (i < rows - 1) {
//                 sb.append("\n");
//             }
//         }
//         return sb.toString();
//     }
    
//     // Getters
    
//     public int getRows() {
//         return rows;
//     }
    
//     public int getCols() {
//         return cols;
//     }
    
//     public List<Piece> getPieces() {
//         return pieces;
//     }
    
//     public Piece getPrimaryPiece() {
//         return primaryPiece;
//     }
    
//     public int getExitRow() {
//         return exitRow;
//     }
    
//     public int getExitCol() {
//         return exitCol;
//     }
    
//     public char[][] getGrid() {
//         return grid;
//     }
    
//     /**
//      * Find a piece by its ID
//      * 
//      * @param id ID of the piece to find
//      * @return Index of the piece, or -1 if not found
//      */
//     public int findPieceIndexById(char id) {
//         for (int i = 0; i < pieces.size(); i++) {
//             if (pieces.get(i).getId() == id) {
//                 return i;
//             }
//         }
//         return -1;
//     }
// }

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
    
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        this.pieces = new ArrayList<>();
        
        // Initialize grid with empty cells
        for (int i = 0; i < rows; i++) {
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
        
        // Deep copy the grid
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, cols);
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
        
        // Only mark the exit on the grid if it's within the grid bounds
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            grid[row][col] = 'K';
        }
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
        
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? row : row + i;
            int c = isHorizontal ? col + i : col;
            
            // Check if the cell is within bounds
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                // Don't overwrite the exit marker
                if (!(r == exitRow && c == exitCol)) {
                    grid[r][c] = symbol;
                }
            }
        }
    }
    
    public void updateGrid() {
        // Reset grid
        for (int i = 0; i < rows; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        // Mark exit if it's within grid bounds
        if (exitRow >= 0 && exitRow < rows && exitCol >= 0 && exitCol < cols) {
            grid[exitRow][exitCol] = 'K';
        }
        
        // Place all pieces
        for (Piece piece : pieces) {
            updatePieceInGrid(piece);
        }
    }
    
    public boolean movePiece(Piece piece, int direction) {
        // direction: 1 = right/down, -1 = left/up
        int newRow = piece.getRow();
        int newCol = piece.getCol();
        
        if (piece.isHorizontal()) {
            newCol += direction;
        } else {
            newRow += direction;
        }
        
        if (canPlacePiece(piece, newRow, newCol)) {
            // Save the old position
            int oldRow = piece.getRow();
            int oldCol = piece.getCol();
            
            // Update piece position
            piece.setRow(newRow);
            piece.setCol(newCol);
            
            // Update the grid
            updateGrid();
            
            return true;
        }
        
        return false;
    }
    
    public boolean canPlacePiece(Piece piece, int newRow, int newCol) {
        int length = piece.getLength();
        boolean isHorizontal = piece.isHorizontal();
        
        // Check if the new position is within bounds and cells are empty
        for (int i = 0; i < length; i++) {
            int r = isHorizontal ? newRow : newRow + i;
            int c = isHorizontal ? newCol + i : newCol;
            
            // If a piece would be outside the board
            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                // Special case: if it's the primary piece at the exit
                if (piece.isPrimary() && 
                    ((exitRow == -1 && r == 0 && c == exitCol) ||        // Top edge
                     (exitCol == cols && r == exitRow && c == cols-1) ||  // Right edge
                     (exitRow == rows && r == rows-1 && c == exitCol) ||  // Bottom edge
                     (exitCol == -1 && r == exitRow && c == 0))) {        // Left edge
                    continue;
                }
                return false;            }
            
            // Skip checking the current piece's own positions
            boolean isOwnPosition = false;
            int origRow = piece.getRowStart();
            int origCol = piece.getColStart();
            
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
                if (grid[r][c] != '.' && !(piece.isPrimary() && grid[r][c] == 'K')) {
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
        int row = primaryPiece.getRowStart();
        int col = primaryPiece.getColStart();
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
    
    public List<Board> getNextStates() {
        List<Board> nextStates = new ArrayList<>();
        
        for (int i = 0; i < pieces.size(); i++) {            
            Piece piece = pieces.get(i);
            
            // Try to move the piece in both directions
            for (int direction : new int[]{-1, 1}) {
                Board newBoard = new Board(this);
                Piece newPiece = newBoard.getPieces().get(i);
                
                if (newBoard.movePiece(newPiece, direction)) {
                    nextStates.add(newBoard);
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
    
    /**
     * Get the grid representation of the board
     * 
     * @return The 2D grid array representing the current state of the board
     */
    public char[][] getGrid() {
        return grid;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j]);
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
        
        // Compare the grid configurations
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] != other.grid[i][j]) {
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
                result = 31 * result + grid[i][j];
            }
        }
        
        return result;
    }
}
