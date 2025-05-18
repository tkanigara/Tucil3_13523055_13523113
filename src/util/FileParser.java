// package util;

// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.Set;
// import model.Board;
// import model.Piece;

// public class FileParser {
//     public Board parseFile(String filename) throws IOException {
//         BufferedReader reader = new BufferedReader(new FileReader(filename));
        
//         // Read board dimensions
//         String[] dimensions = reader.readLine().split("\\s+");
//         int rows = Integer.parseInt(dimensions[0]);
//         int cols = Integer.parseInt(dimensions[1]);
        
//         // Read number of pieces
//         int numPieces = Integer.parseInt(reader.readLine());
        
//         // Read the grid configuration
//         ArrayList<String> gridRows = new ArrayList<>();
//         for (int i = 0; i < rows; i++) {
//             String line = reader.readLine();
//             gridRows.add(line);
//         }
        
//         // Create the board
//         Board board = new Board(rows, cols);
        
//         // Process the exit first - check if it's on any edge
//         int exitRow = -1;
//         int exitCol = -1;
//         boolean exitFound = false;
        
//         // Check for top exit
//         String topExitLine = reader.readLine();
//         if (topExitLine != null && topExitLine.contains("K")) {
//             exitRow = -1;
//             exitCol = topExitLine.indexOf('K');
//             board.setExit(exitRow, exitCol);
//             exitFound = true;
//             System.out.println("DEBUG: Found exit at top edge at column " + exitCol);
//         }
        
//         // Check for bottom exit (might be after all grid rows)
//         if (!exitFound && reader.ready()) {
//             String bottomExitLine = reader.readLine();
//             if (bottomExitLine != null && bottomExitLine.contains("K")) {
//                 exitRow = rows;
//                 exitCol = bottomExitLine.indexOf('K');
//                 board.setExit(exitRow, exitCol);
//                 exitFound = true;
//                 System.out.println("DEBUG: Found exit at bottom edge at column " + exitCol);
//             }
//         }
        
//         // Adjust grid for left/right exits
//         char[][] grid = new char[rows][cols];
//         for (int r = 0; r < rows; r++) {
//             String line = r < gridRows.size() ? gridRows.get(r) : "";
            
//             // Handle left exit
//             boolean hasLeftExit = false;
//             if (line.length() > 0 && line.charAt(0) == 'K') {
//                 exitRow = r;
//                 exitCol = -1;
//                 board.setExit(exitRow, exitCol);
//                 exitFound = true;
//                 hasLeftExit = true;
//                 System.out.println("DEBUG: Found exit at left edge at row " + exitRow);
                
//                 // Remove K from the line
//                 line = line.substring(1);
//             }
            
//             // Check for right exit (at position cols)
//             if (line.length() > cols && line.charAt(cols) == 'K') {
//                 exitRow = r;
//                 exitCol = cols;
//                 board.setExit(exitRow, exitCol);
//                 exitFound = true;
//                 System.out.println("DEBUG: Found exit at right edge at row " + exitRow);
                
//                 // Remove K from the line by taking only the first cols characters
//                 line = line.substring(0, cols);
//             }
            
//             // Fill the grid row
//             int offset = 0;
//             if (line.length() > 0 && line.charAt(0) == ' ' && !hasLeftExit) {
//                 offset = 1; // Skip leading space
//             }
            
//             for (int c = 0; c < cols && c + offset < line.length(); c++) {
//                 grid[r][c] = line.charAt(c + offset);
//             }
//         }
        
//         // Check inside grid for exit (K) if not found on edges
//         if (!exitFound) {
//             for (int r = 0; r < rows; r++) {
//                 for (int c = 0; c < cols; c++) {
//                     if (grid[r][c] == 'K') {
//                         exitRow = r;
//                         exitCol = c;
//                         board.setExit(exitRow, exitCol);
//                         exitFound = true;
//                         System.out.println("DEBUG: Found exit inside board at (" + r + "," + c + ")");
                        
//                         // Replace K with dot
//                         grid[r][c] = '.';
//                         break;
//                     }
//                 }
//                 if (exitFound) break;
//             }
//         }
        
//         // Process pieces
//         Set<Character> processedChars = new HashSet<>();
//         processedChars.add('.');
//         processedChars.add('K');
        
//         // First pass: process all pieces
//         for (int r = 0; r < rows; r++) {
//             for (int c = 0; c < cols; c++) {
//                 char pieceChar = grid[r][c];
                
//                 // Skip empty cells and already processed piece characters
//                 if (pieceChar == '.' || pieceChar == ' ' || processedChars.contains(pieceChar)) {
//                     continue;
//                 }
                
//                 // Found a new piece, determine its orientation and length
//                 processedChars.add(pieceChar);
//                 boolean isPrimary = (pieceChar == 'P');
                
//                 // Check horizontal orientation first
//                 int length = 1;
//                 boolean isHorizontal = false;
                
//                 // Look right
//                 for (int i = c + 1; i < cols; i++) {
//                     if (grid[r][i] == pieceChar) {
//                         length++;
//                         isHorizontal = true;
//                     } else {
//                         break;
//                     }
//                 }
                
//                 // If not horizontal, check vertical
//                 if (length == 1) {
//                     // Look down
//                     for (int i = r + 1; i < rows; i++) {
//                         if (grid[i][c] == pieceChar) {
//                             length++;
//                         } else {
//                             break;
//                         }
//                     }
//                 }
                
//                 // Create and add the piece
//                 Piece piece = new Piece(pieceChar, r, c, length, !isHorizontal, isPrimary);
//                 board.addPiece(piece);
                
//                 System.out.println("DEBUG: Added piece " + pieceChar + " at (" + r + "," + c + "), length: " + length + 
//                                   ", " + (isHorizontal ? "horizontal" : "vertical") + 
//                                   (isPrimary ? " [PRIMARY]" : ""));
//             }
//         }
        
//         reader.close();
//         return board;
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
        String[] dimensions = reader.readLine().split("\\s+");
        int rows = Integer.parseInt(dimensions[0]);
        int cols = Integer.parseInt(dimensions[1]);
        
        // Read number of pieces
        int numPieces = Integer.parseInt(reader.readLine());
        
        // Create the board
        Board board = new Board(rows, cols);
        
        // Read all remaining lines into a list
        ArrayList<String> allLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            allLines.add(line);
        }
        reader.close();
        
        // Process the exit and board configuration
        int exitRow = -1;
        int exitCol = -1;
        boolean exitFound = false;
        char[][] grid = new char[rows][cols];
        
        // Initialize grid with dots
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = '.';
            }
        }
        
        // Check if K is in the first or last line (top or bottom exit)
        if (allLines.size() > rows) {
            // Check for top exit (K is in first line before grid)
            String firstLine = allLines.get(0);
            if (firstLine.contains("K")) {
                exitRow = -1;
                exitCol = firstLine.indexOf('K');
                exitFound = true;
                board.setExit(exitRow, exitCol);
                System.out.println("DEBUG: Found exit at top edge at column " + exitCol);
                
                // Remove the first line from processing
                allLines.remove(0);
            }
            // Check for bottom exit (K is in line after grid)
            else if (allLines.size() > rows) {
                String lastLine = allLines.get(rows);
                if (lastLine.contains("K")) {
                    exitRow = rows;
                    exitCol = lastLine.indexOf('K');
                    exitFound = true;
                    board.setExit(exitRow, exitCol);
                    System.out.println("DEBUG: Found exit at bottom edge at column " + exitCol);
                }
            }
        }
        
        // Process grid lines
        for (int r = 0; r < rows && r < allLines.size(); r++) {
            line = allLines.get(r);
            
            // Check for left exit
            if (line.length() > 0 && line.charAt(0) == 'K') {
                exitRow = r;
                exitCol = -1;
                exitFound = true;
                board.setExit(exitRow, exitCol);
                System.out.println("DEBUG: Found exit at left edge at row " + exitRow);
                
                // Remove K for grid processing
                line = line.substring(1);
            }
            
            // Check for right exit
            if (line.length() > cols && line.charAt(cols) == 'K') {
                exitRow = r;
                exitCol = cols;
                exitFound = true;
                board.setExit(exitRow, exitCol);
                System.out.println("DEBUG: Found exit at right edge at row " + exitRow);
            }
            
            // Process characters in the grid
            int startIdx = (line.length() > 0 && line.charAt(0) == ' ') ? 1 : 0;
            for (int c = 0; c < cols && startIdx + c < line.length(); c++) {
                grid[r][c] = line.charAt(startIdx + c);
                
                // If it's K inside grid, mark as exit and replace with dot
                if (grid[r][c] == 'K' && !exitFound) {
                    exitRow = r;
                    exitCol = c;
                    exitFound = true;
                    board.setExit(exitRow, exitCol);
                    grid[r][c] = '.';
                    System.out.println("DEBUG: Found exit inside grid at (" + r + "," + c + ")");
                }
            }
        }
        
        // Process pieces from the grid
        Set<Character> processedChars = new HashSet<>();
        processedChars.add('.');
        processedChars.add('K');
        processedChars.add(' ');
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char pieceChar = grid[r][c];
                
                // Skip empty cells and already processed piece characters
                if (pieceChar == '.' || pieceChar == ' ' || processedChars.contains(pieceChar)) {
                    continue;
                }
                
                // Found a new piece, determine its orientation and length
                processedChars.add(pieceChar);
                boolean isPrimary = (pieceChar == 'P');
                
                // Check horizontal orientation first
                int length = 1;
                boolean isHorizontal = false;
                
                // Look right
                for (int i = c + 1; i < cols; i++) {
                    if (grid[r][i] == pieceChar) {
                        length++;
                        isHorizontal = true;
                    } else {
                        break;
                    }
                }
                
                // If not horizontal, check vertical
                if (length == 1) {
                    // Look down
                    for (int i = r + 1; i < rows; i++) {
                        if (grid[i][c] == pieceChar) {
                            length++;
                        } else {
                            break;
                        }
                    }
                }
                
                // Create and add the piece
                Piece piece = new Piece(pieceChar, r, c, length, !isHorizontal, isPrimary);
                board.addPiece(piece);
                
                System.out.println("DEBUG: Added piece " + pieceChar + " at (" + r + "," + c + "), length: " + length + 
                                  ", " + (isHorizontal ? "horizontal" : "vertical") + 
                                  (isPrimary ? " [PRIMARY]" : ""));
            }
        }
        
        return board;
    }
}