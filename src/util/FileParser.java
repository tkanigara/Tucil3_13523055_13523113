// package util;

// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;
// import model.Board;
// import model.Piece;

// /**
//  * Utility class to parse input files for the Rush Hour puzzle.
//  */
// public class FileParser {
    
//     /**
//      * Parse an input file and create a Board object
//      * 
//      * @param filePath Path to the input file
//      * @return A Board object representing the initial state
//      * @throws IOException If there's an error reading the file
//      */    public Board parseFile(String filePath) throws IOException {
//         BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
//         // Read dimensions
//         String[] dimensions = reader.readLine().trim().split("\\s+");
//         int rows = Integer.parseInt(dimensions[0]);
//         int cols = Integer.parseInt(dimensions[1]);
//         System.out.println("DEBUG: Dimensions read as " + rows + "x" + cols);
        
//         // Read number of non-primary pieces
//         int nPieces = Integer.parseInt(reader.readLine().trim());
//         System.out.println("DEBUG: Number of non-primary pieces: " + nPieces);
        
//         // Create board
//         Board board = new Board(rows, cols);
        
//         // Read board configuration
//         String[] boardConfig = new String[rows];
//         for (int i = 0; i < rows; i++) {
//             boardConfig[i] = reader.readLine();
//             System.out.println("DEBUG: Read line " + i + ": " + boardConfig[i]);
//         }
        
//         reader.close();
//           // Check for exit on the edges of the board
//         // First check for exit in each row (could be on the left or right edges)
//         for (int r = 0; r < rows; r++) {
//             String line = boardConfig[r];
//             // Check left edge
//             if (line.length() > 0 && line.charAt(0) == 'K') {
//                 board.setExit(r, 0);
//                 System.out.println("DEBUG: Found exit at left edge (" + r + ",0)");
//             }
//             // Check right edge - may be beyond the defined board size
//             if (line.length() > cols && line.charAt(cols) == 'K') {
//                 board.setExit(r, cols); // Set at the exact position it was found, outside the board
//                 System.out.println("DEBUG: Found exit beyond right edge at (" + r + "," + cols + ")");
//             }
//         }
        
//         // Now check for exit in first and last columns (could be above or below the board)
//         for (int c = 0; c < cols; c++) {
//             // No need to check for exits above or below the board as they would be in separate lines
//         }
        
//         // Process board configuration
//         Map<Character, PieceInfo> pieceInfoMap = new HashMap<>();
        
//         // First pass: collect piece information
//         for (int r = 0; r < rows; r++) {
//             String line = boardConfig[r];
//             for (int c = 0; c < Math.min(cols, line.length()); c++) {
//                 char cell = line.charAt(c);
//                 System.out.println("DEBUG: Processing cell at (" + r + "," + c + "): " + cell);
                
//                 // Skip empty cells
//                 if (cell == '.') {
//                     continue;
//                 }
                
//                 // If it's the exit
//                 if (cell == 'K') {
//                     System.out.println("DEBUG: Found exit at (" + r + "," + c + ")");
//                     board.setExit(r, c);
//                     continue;
//                 }
                
//                 // If we've already seen this piece, update its info
//                 if (pieceInfoMap.containsKey(cell)) {
//                     PieceInfo info = pieceInfoMap.get(cell);
//                     info.addCoordinate(r, c);
//                 } else {
//                     // Otherwise create a new piece info
//                     PieceInfo info = new PieceInfo(cell);
//                     info.addCoordinate(r, c);
//                     pieceInfoMap.put(cell, info);
//                 }
//             }
//         }
        
//         // Second pass: create and add pieces to the board
//         for (PieceInfo info : pieceInfoMap.values()) {
//             boolean isPrimary = info.id == 'P';
//             Piece piece = info.createPiece(isPrimary);
//             board.addPiece(piece);
//         }
        
//         return board;
//     }
    
//     /**
//      * Helper class to gather information about pieces
//      */
//     private class PieceInfo {
//         char id;
//         int minRow = Integer.MAX_VALUE;
//         int minCol = Integer.MAX_VALUE;
//         int maxRow = Integer.MIN_VALUE;
//         int maxCol = Integer.MIN_VALUE;
        
//         public PieceInfo(char id) {
//             this.id = id;
//         }
        
//         public void addCoordinate(int row, int col) {
//             minRow = Math.min(minRow, row);
//             minCol = Math.min(minCol, col);
//             maxRow = Math.max(maxRow, row);
//             maxCol = Math.max(maxCol, col);
//         }
        
//         public Piece createPiece(boolean isPrimary) {
//             boolean isVertical = minCol == maxCol;
//             int length = isVertical ? (maxRow - minRow + 1) : (maxCol - minCol + 1);
//             return new Piece(id, minRow, minCol, length, isVertical, isPrimary);
//         }
//     }
// }

package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import model.Board;
import model.Piece;

public class FileParser {
    public Board parseFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        
        // Read board dimensions
        String[] dimensions = reader.readLine().split(" ");
        int rows = Integer.parseInt(dimensions[0]);
        int cols = Integer.parseInt(dimensions[1]);
        System.out.println("DEBUG: Dimensions read as " + rows + "x" + cols);
        
        // Create the board
        Board board = new Board(rows, cols);
        
        // Read number of non-primary pieces
        int numPieces = Integer.parseInt(reader.readLine());
        System.out.println("DEBUG: Number of non-primary pieces: " + numPieces);
        
        // For tracking processed characters
        Set<Character> processedChars = new HashSet<>();
        
        // For storing pieces before adding to board
        ArrayList<Piece> pieces = new ArrayList<>();
          // Read board configuration and additional lines for exits outside the grid
        ArrayList<String> allLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            allLines.add(line);
        }
        
        reader.close();
        
        // Process all the lines to find exits outside the grid
        int exitRow = -1;
        int exitCol = -1;
        
        // Check for exit above the grid (top edge)
        if (allLines.size() > rows) {
            String topLine = allLines.get(0);
            for (int c = 0; c < topLine.length(); c++) {
                if (topLine.charAt(c) == 'K') {
                    exitRow = -1;
                    exitCol = c;
                    System.out.println("DEBUG: Found exit at top edge at (-1," + c + ")");
                    // Remove this line from the grid configuration
                    allLines.remove(0);
                    break;
                }
            }
        }
          // Extract the actual grid configuration
        char[][] gridConfig = new char[rows][];
        for (int i = 0; i < Math.min(rows, allLines.size()); i++) {
            gridConfig[i] = allLines.get(i).toCharArray();
            System.out.println("DEBUG: Grid line " + i + ": " + allLines.get(i));
        }
        
        // Check for exit below the grid (bottom edge)
        if (allLines.size() > rows) {
            String bottomLine = allLines.get(rows);
            for (int c = 0; c < bottomLine.length(); c++) {
                if (bottomLine.charAt(c) == 'K') {
                    exitRow = rows;
                    exitCol = c;
                    System.out.println("DEBUG: Found exit at bottom edge at (" + rows + "," + c + ")");
                    break;
                }
            }
        }
        
        // Set exit on board if found outside
        if (exitRow != -1 || exitCol != -1) {
            board.setExit(exitRow, exitCol);
        } else {
            // Check inside the board for K
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols && c < gridConfig[r].length; c++) {
                    if (gridConfig[r][c] == 'K') {
                        System.out.println("DEBUG: Found exit inside board at (" + r + "," + c + ")");
                        board.setExit(r, c);
                        // Mark K as processed
                        processedChars.add('K');
                        break;
                    }
                }
            }
        }
          // Check left edge
        if (exitRow == -1 && exitCol == -1) {
            for (int r = 0; r < rows; r++) {
                if (r < gridConfig.length && gridConfig[r].length > 0 && gridConfig[r][0] == 'K') {
                    exitRow = r;
                    exitCol = -1;
                    System.out.println("DEBUG: Found exit at left edge at (" + r + ",-1)");
                    board.setExit(exitRow, exitCol);
                    processedChars.add('K');
                    break;
                }
            }
        }
        
        // Check right edge
        if (exitRow == -1 && exitCol == -1) {
            for (int r = 0; r < rows; r++) {
                if (r < gridConfig.length && gridConfig[r].length > cols && gridConfig[r][cols] == 'K') {
                    exitRow = r;
                    exitCol = cols;
                    System.out.println("DEBUG: Found exit at right edge at (" + r + "," + cols + ")");
                    board.setExit(exitRow, exitCol);
                    processedChars.add('K');
                    break;
                }
            }
        }
          // Second pass: process all pieces
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols && c < gridConfig[r].length; c++) {
                char pieceChar = gridConfig[r][c];
                
                // Skip empty cells and already processed piece characters
                if (pieceChar == '.' || pieceChar == 'K' || processedChars.contains(pieceChar)) {
                    continue;
                }
                
                // Found a new piece, determine its orientation and length
                processedChars.add(pieceChar);
                boolean isPrimary = (pieceChar == 'P');
                
                // Check horizontal orientation
                int length = 1;
                boolean isHorizontal = false;
                
                // Look right
                if (c + 1 < cols && c + 1 < gridConfig[r].length && gridConfig[r][c + 1] == pieceChar) {
                    isHorizontal = true;
                    length = 1;
                    while (c + length < cols && c + length < gridConfig[r].length && 
                           gridConfig[r][c + length] == pieceChar) {
                        length++;
                    }
                } 
                // Look down
                else if (r + 1 < rows && r + 1 < gridConfig.length && gridConfig[r + 1][c] == pieceChar) {
                    isHorizontal = false;
                    length = 1;
                    while (r + length < rows && r + length < gridConfig.length && 
                           c < gridConfig[r + length].length && gridConfig[r + length][c] == pieceChar) {
                        length++;
                    }
                }
                
                // Create and add the piece
                // The Piece constructor expects isVertical, not isHorizontal, so we need to negate isHorizontal
                Piece piece = new Piece(pieceChar, r, c, length, !isHorizontal, isPrimary);
                board.addPiece(piece);
            }
        }
        
        return board;
    }
}