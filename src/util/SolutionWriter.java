package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import model.Board;

/**
 * Utility class for writing solution steps to a file.
 */
public class SolutionWriter {
    private PrintWriter fileWriter;
    private boolean captureOutput;
    private StringWriter stringWriter;
    private PrintWriter capturedWriter;
    
    /**
     * Create a new SolutionWriter
     * 
     * @param filePath Path to the output file
     * @throws IOException If the file cannot be created
     */
    public SolutionWriter(String filePath) throws IOException {
        
        // Create the file writer
        this.fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
        
        // Set up capturing output
        this.captureOutput = true;
        this.stringWriter = new StringWriter();
        this.capturedWriter = new PrintWriter(stringWriter);
    }
    
    /**
     * Write the initial board state
     * 
     * @param board The initial board
     */
    public void writeInitialBoard(Board board) {
        // Capture the output
        if (captureOutput) {
            // Redirect BoardPrinter output to our StringWriter
            StringWriter tempWriter = new StringWriter();
            PrintWriter tempPrinter = new PrintWriter(tempWriter);
            
            // Write the header
            tempPrinter.println("Initial Board:");
            
            // Capture the board display
            String boardDisplay = getBoardWithBordersString(board, ' ');
            tempPrinter.println(boardDisplay);
            
            // Get the captured text
            tempPrinter.flush();
            String outputText = tempWriter.toString();
            
            // Write to file
            fileWriter.println(outputText);
            
            // Also print to console
            System.out.print(outputText);
        }
    }
    
    /**
     * Write a board state after a move
     * 
     * @param board The board state
     * @param moveCount The move number
     * @param pieceId The ID of the piece that was moved
     * @param direction The direction the piece was moved
     * @param distance The number of cells the piece was moved
     */
    public void writeBoardAfterMove(Board board, int moveCount, char pieceId, String direction, int distance) {
        if (captureOutput) {
            // Redirect output to our StringWriter
            StringWriter tempWriter = new StringWriter();
            PrintWriter tempPrinter = new PrintWriter(tempWriter);
            
            // Write the move description
            if (distance > 1) {
                tempPrinter.println("\nMove " + moveCount + ": " + pieceId + " " + direction + " " + distance + " cells");
            } else {
                tempPrinter.println("\nMove " + moveCount + ": " + pieceId + " " + direction);
            }
            
            // Capture the board display
            String boardDisplay = getBoardWithBordersString(board, pieceId);
            tempPrinter.println(boardDisplay);
            
            // Get the captured text
            tempPrinter.flush();
            String outputText = tempWriter.toString();
            
            // Write to file
            fileWriter.println(outputText);
            
            // Also print to console
            System.out.print(outputText);
        }
    }
    
    /**
     * Write solution statistics
     * 
     * @param steps Number of steps in the solution
     * @param nodesVisited Number of nodes visited during search
     * @param executionTime Execution time in seconds
     */
    public void writeStatistics(int steps, int nodesVisited, double executionTime) {
        String statsText = "Jumlah langkah: " + steps + "\n" +
                          "Jumlah node yang diperiksa: " + nodesVisited + "\n" +
                          "Waktu eksekusi: " + executionTime + " detik";
        
        // Write to file
        fileWriter.println(statsText);
        
        // Also print to console
        System.out.println(statsText);
        
        // Close the file writer
        fileWriter.close();
    }
    
    /**
     * Write to file that no solution was found
     * 
     * @param nodesVisited Number of nodes visited during search
     * @param executionTime Execution time in seconds
     */
    public void writeNoSolution(int nodesVisited, double executionTime) {
        String statsText = "Tidak ada solusi yang ditemukan!\n" +
                          "Jumlah node yang diperiksa: " + nodesVisited + "\n" +
                          "Waktu eksekusi: " + executionTime + " detik";
        
        // Write to file
        fileWriter.println(statsText);
        
        // Also print to console
        System.out.println(statsText);
        
        // Close the file writer
        fileWriter.close();
    }
    
    /**
     * Get the string representation of a board with borders
     * 
     * @param board The board to display
     * @param movedPiece The piece that was moved (for highlighting)
     * @return String representation of the board with borders
     */
    private String getBoardWithBordersString(Board board, char movedPiece) {
        // This reimplements BoardPrinter.printBoardWithBorders but returns a string instead of printing
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        int rows = board.getRows();
        int cols = board.getCols();
        char[][] grid = board.getGrid();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        // Check if we need borders with exit markers
        boolean needTopBorder = (exitRow == -1);
        boolean needBottomBorder = (exitRow == rows);
        boolean needLeftBorder = (exitCol == -1);
        boolean needRightBorder = (exitCol == cols);
        
        // Print top border with exit if needed
        pw.print("+");
        for (int c = 0; c < cols; c++) {
            if (needTopBorder && c == exitCol) {
                pw.print("K");
            } else {
                pw.print("-");
            }
        }
        pw.println("+");
        
        // Print board contents with left and right borders
        for (int r = 0; r < rows; r++) {
            // Print left border
            if (needLeftBorder && r == exitRow) {
                pw.print("K");
            } else {
                pw.print("|");
            }
            
            // Print the row with colored pieces
            for (int c = 0; c < cols; c++) {
                char cell = grid[r][c];
                pw.print(cell);
            }
            
            // Print right border
            if (needRightBorder && r == exitRow) {
                pw.print("K");
            } else {
                pw.print("|");
            }
            
            pw.println();
        }
        
        // Print bottom border with exit if needed
        pw.print("+");
        for (int c = 0; c < cols; c++) {
            if (needBottomBorder && c == exitCol) {
                pw.print("K");
            } else {
                pw.print("-");
            }
        }
        pw.println("+");
        
        pw.flush();
        return sw.toString();
    }
}