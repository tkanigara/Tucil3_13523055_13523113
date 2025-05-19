package util;

import model.Board;

public class BoardPrinter {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";    
    private static final String GREEN = "\u001B[32m";   
    private static final String YELLOW = "\u001B[33m"; 
    
    /* Print board awal */
    public static void printInitialBoard(Board board) {
        System.out.println("\nInitial Board:");
        printBoardWithBorders(board, ' ');
    }

    public static void printBoardAfterMove(Board board, int moveCount, char pieceId, String direction) {
        System.out.println("\nMove " + moveCount + ": " + pieceId + " " + direction);
        printBoardWithBorders(board, pieceId);
    }
    
    public static void printBoardAfterMove(Board board, int moveCount, char pieceId, String direction, int distance) {
        if (distance > 1) {
            System.out.println("\nMove " + moveCount + ": " + pieceId + " " + direction + " " + distance + " cells");
        } else {
            System.out.println("\nMove " + moveCount + ": " + pieceId + " " + direction);
        }
        printBoardWithBorders(board, pieceId);
    }
      
    private static void printBoardWithBorders(Board board, char movedPiece) {
        int rows = board.getRows();
        int cols = board.getCols();
        char[][] grid = board.getGrid();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        boolean needTopBorder = (exitRow == -1);
        boolean needBottomBorder = (exitRow == rows);
        boolean needLeftBorder = (exitCol == -1);
        boolean needRightBorder = (exitCol == cols);
        
        System.out.print("+");
        for (int c = 0; c < cols; c++) {
            if (needTopBorder && c == exitCol) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("-");
            }
        }
        System.out.println("+");
        
        for (int r = 0; r < rows; r++) {
            if (needLeftBorder && r == exitRow) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("|");
            }
            
            for (int c = 0; c < cols; c++) {
                char cell = grid[r][c];
                
                if (cell == 'P') {
                    // Primary car warna merah
                    System.out.print(RED + cell + RESET);
                } else if (cell != '.' && cell == movedPiece) {
                    // Mobil yg gerak warna kuning
                    System.out.print(YELLOW + cell + RESET);
                } else {
                    System.out.print(cell);
                }
            }
            
            if (needRightBorder && r == exitRow) {
                System.out.print(GREEN + "K" + RESET);
            } else {
                System.out.print("|");
            }
            
            System.out.println();
        }
        
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