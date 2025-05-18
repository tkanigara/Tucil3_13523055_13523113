package util;

import model.Board;

/**
 * Utility class for printing the Rush Hour puzzle board with proper borders and colors.
 */
public class BoardPrinter {
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";     // For primary piece (P)
    private static final String GREEN = "\u001B[32m";   // For exit (K)
    private static final String YELLOW = "\u001B[33m";  // For moved pieces
    
    /**
     * Print the initial board state
     * 
     * @param board The initial board
     */
    public static void printInitialBoard(Board board) {
        System.out.println("\nInitial Board:");
        printBoardWithBorders(board, ' '); // No moved piece in initial board
    }
    
    /**
     * Print the board after a move has been made
     * 
     * @param board     The board state
     * @param moveCount The move number
     * @param pieceId   The ID of the piece that was moved
     * @param direction The direction in which the piece was moved
     */
    public static void printBoardAfterMove(Board board, int moveCount, char pieceId, String direction) {
        System.out.println("\nMove " + moveCount + ": " + pieceId + " " + direction);
        printBoardWithBorders(board, pieceId); // Highlight the moved piece
    }
    
    /**
     * Print the board with borders, exit position, and color-coded pieces
     * 
     * @param board      The board to print
     * @param movedPiece The piece that was just moved (to highlight), or ' ' for none
     */    
    private static void printBoardWithBorders(Board board, char movedPiece) {
        int rows = board.getRows();
        int cols = board.getCols();
        char[][] grid = board.getGrid();  // This returns only the visible part
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        // Check if we need borders with exit markers
        boolean needTopBorder = (exitRow == -1);
        boolean needBottomBorder = (exitRow == rows);
        boolean needLeftBorder = (exitCol == -1);
        boolean needRightBorder = (exitCol == cols);
        
        // Print top border with exit if needed
        System.out.print("+");
        for (int c = 0; c < cols; c++) {
            if (needTopBorder && c == exitCol) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("-");
            }
        }
        System.out.println("+");
        
        // Print board contents with left and right borders
        for (int r = 0; r < rows; r++) {
            // Print left border
            if (needLeftBorder && r == exitRow) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("|");
            }
            
            // Print the row with colored pieces
            for (int c = 0; c < cols; c++) {
                char cell = grid[r][c];
                
                if (cell == 'P') {
                    // Primary piece in red
                    System.out.print(RED + cell + RESET);
                } else if (cell != '.' && cell == movedPiece) {
                    // Moved piece in yellow
                    System.out.print(YELLOW + cell + RESET);
                } else {
                    // Regular cell or other pieces
                    System.out.print(cell);
                }
            }
            
            // Print right border
            if (needRightBorder && r == exitRow) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("|");
            }
            
            System.out.println();
        }
        
        // Print bottom border with exit if needed
        System.out.print("+");
        for (int c = 0; c < cols; c++) {
            if (needBottomBorder && c == exitCol) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("-");
            }
        }
        System.out.println("+");
    }
}