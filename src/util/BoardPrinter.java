package util;

import model.Board;

/**
 * Utility class to print board states to the console with colors.
 */
public class BoardPrinter {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";     // For primary piece (P)
    public static final String GREEN = "\u001B[32m";   // For exit (K)
    public static final String YELLOW = "\u001B[33m";  // For the piece that was moved
    
    /**
     * Print the initial board state
     * 
     * @param board The initial board
     */
    public static void printInitialBoard(Board board) {
        System.out.println("\nPapan Awal");
        printBoardWithColor(board, ' ');
    }
    
    /**
     * Print a board state after a move
     * 
     * @param board        The board to print
     * @param moveNumber   The move number
     * @param movedPieceId The ID of the piece that was moved
     * @param direction    The direction the piece was moved
     */
    public static void printBoardAfterMove(Board board, int moveNumber, char movedPieceId, String direction) {
        System.out.println("\nGerakan " + moveNumber + ": " + movedPieceId + "-" + direction);
        printBoardWithColor(board, movedPieceId);
    }
    
    /**
     * Print a board with color highlighting
     * 
     * @param board        The board to print
     * @param movedPieceId The ID of the piece that was moved (highlight in yellow)
     */
    private static void printBoardWithColor(Board board, char movedPieceId) {
        char[][] grid = board.getGrid();
        
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                char cell = grid[i][j];
                
                if (cell == 'P') {
                    // Primary piece in red
                    System.out.print(RED + cell + RESET);
                } else if (cell == 'K') {
                    // Exit in green
                    System.out.print(GREEN + cell + RESET);
                } else if (cell != '.' && cell == movedPieceId) {
                    // Moved piece in yellow
                    System.out.print(YELLOW + cell + RESET);
                } else {
                    // Regular cell or piece
                    System.out.print(cell);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
