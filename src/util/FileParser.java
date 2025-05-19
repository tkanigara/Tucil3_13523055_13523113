package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Board;
import model.Piece;

public class FileParser {
    public Board parseFile(String filename) throws IOException, IllegalArgumentException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            
            // Check if file is empty
            String dimensionLine = reader.readLine();
            if (dimensionLine == null || dimensionLine.trim().isEmpty()) {
                throw new IllegalArgumentException("Input file is empty.");
            }
            
            // Read board dimensions
            String[] dimensions = dimensionLine.split("\\s+");
            if (dimensions.length < 2) {
                throw new IllegalArgumentException("Invalid board dimensions format. Expected: rows cols");
            }
            
            int rows, cols;
            try {
                rows = Integer.parseInt(dimensions[0]);
                cols = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid board dimensions. Expected numbers, got: " + dimensionLine);
            }
            
            // Validate board dimensions
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Board dimensions must be positive. Got: " + rows + "x" + cols);
            }
            
            if (rows < 2 || cols < 2) {
                throw new IllegalArgumentException("Board is too small. Minimum size is 2x2.");
            }
            
            // Read number of pieces
            String numPiecesLine = reader.readLine();
            if (numPiecesLine == null || numPiecesLine.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing number of pieces.");
            }
            
            int numPieces;
            try {
                numPieces = Integer.parseInt(numPiecesLine);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number of pieces. Expected a number, got: " + numPiecesLine);
            }
            
            // Validate number of pieces
            if (numPieces <= 0) {
                throw new IllegalArgumentException("Number of pieces must be positive.");
            }
            
            // Max possible pieces (assuming each piece is at least 2 units)
            int maxPieces = (rows * cols) / 2;
            if (numPieces > maxPieces) {
                throw new IllegalArgumentException("Too many pieces (" + numPieces + ") for this board size.");
            }
            
            // Create the board
            Board board = new Board(rows, cols);
            
            // Read all remaining lines into a list
            ArrayList<String> allLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    allLines.add(line);
                }
            }
            
            // Ensure enough data is available
            if (allLines.size() < rows) {
                throw new IllegalArgumentException("Not enough rows in the puzzle. Expected at least " + rows + " rows.");
            }
            
            // Process the exit and board configuration
            int exitRow = -1;
            int exitCol = -1;
            boolean exitFound = false;
            char[][] grid = new char[rows][cols];

            int exitCount = 0;
            
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
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    // Validate exit position
                    if (exitCol < 0 || exitCol >= cols) {
                        throw new IllegalArgumentException("Top exit position is outside the board boundary.");
                    }
                    
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
                        exitCount++;
                        board.setExit(exitRow, exitCol);
                        
                        // Validate exit position
                        if (exitCol < 0 || exitCol >= cols) {
                            throw new IllegalArgumentException("Bottom exit position is outside the board boundary.");
                        }
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
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    // Validate exit position
                    if (exitRow < 0 || exitRow >= rows) {
                        throw new IllegalArgumentException("Left exit position is outside the board boundary.");
                    }
                    
                    // Remove K for grid processing
                    line = line.substring(1);
                }
                
                // Check for right exit
                if (line.length() > cols && line.charAt(cols) == 'K') {
                    exitRow = r;
                    exitCol = cols;
                    exitFound = true;
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    // Validate exit position
                    if (exitRow < 0 || exitRow >= rows) {
                        throw new IllegalArgumentException("Right exit position is outside the board boundary.");
                    }
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
                        exitCount++;
                        board.setExit(exitRow, exitCol);
                        grid[r][c] = '.';
                    }
                }
            }
            
            // Validate exit was found
            if (!exitFound) {
                throw new IllegalArgumentException("No exit (K) found in the puzzle.");
            }

            if (exitCount>1){
                throw new IllegalArgumentException("Multiple exits (" + exitCount + ") found in puzzle. Only one exit is allowed.");
            }
            
            // Process pieces from the grid
            Set<Character> processedChars = new HashSet<>();
            processedChars.add('.');
            processedChars.add('K');
            processedChars.add(' ');
            
            boolean foundPrimaryPiece = false;
            
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
                    
                    if (isPrimary) {
                        foundPrimaryPiece = true;
                    }
                    
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
                    
                    // Validate piece length
                    if (length < 2) {
                        throw new IllegalArgumentException("Piece '" + pieceChar + "' has invalid length. Minimum length is 2.");
                    }
                    
                    // Create and add the piece
                    Piece piece = new Piece(pieceChar, r, c, length, !isHorizontal, isPrimary);
                    board.addPiece(piece);
                }
            }
            
            // Validate primary piece exists
            if (!foundPrimaryPiece) {
                throw new IllegalArgumentException("No primary piece (P) found in the puzzle.");
            }
            
            // Validate actual number of pieces matches the expected count
            // The count represents non-primary pieces, so we need to count them separately
            int nonPrimaryPieceCount = 0;
            for (Piece piece : board.getPieces()) {
                if (piece.getId() != 'P') {
                    nonPrimaryPieceCount++;
                }
            }
            
            if (nonPrimaryPieceCount != numPieces) {
                throw new IllegalArgumentException("Expected " + numPieces + " non-primary pieces, but found " + 
                                              nonPrimaryPieceCount + ".");
            }
            
            // Validate primary piece and exit alignment
            validatePrimaryPieceExitAlignment(board, exitRow, exitCol, rows, cols);
            
            return board;
            
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    private void validatePrimaryPieceExitAlignment(Board board, int exitRow, int exitCol, int rows, int cols) {
        Piece primaryPiece = null;
        for (Piece piece : board.getPieces()) {
            if (piece.getId() == 'P') {
                primaryPiece = piece;
                break;
            }
        }
        
        if (primaryPiece == null) {
            return; // Already checked for existence earlier
        }
        
        boolean isAligned = false;
        String exitPosition = "";
        
        // Primary piece orientation
        boolean isHorizontal = !primaryPiece.isVertical();
        int pRow = primaryPiece.getRow();
        int pCol = primaryPiece.getCol();
        int pLength = primaryPiece.getLength();
        
        // Determine exit position and check alignment
        if (exitRow == -1) {
            exitPosition = "top";
            // For top exit, primary piece should be vertical (not horizontal)
            isAligned = !isHorizontal;
        } else if (exitRow == rows) {
            exitPosition = "bottom";
            // For bottom exit, primary piece should be vertical (not horizontal)
            isAligned = !isHorizontal;
        } else if (exitCol == -1) {
            exitPosition = "left";
            // For left exit, primary piece should be horizontal (not vertical)
            isAligned = isHorizontal;
        } else if (exitCol == cols) {
            exitPosition = "right";
            // For right exit, primary piece should be horizontal (not vertical)
            isAligned = isHorizontal;
        }
        
        if (!isAligned) {
            throw new IllegalArgumentException(
                "Primary piece has incorrect orientation for " + exitPosition + " exit. " +
                "For " + exitPosition + " exit, primary piece must be " + 
                (!isHorizontal ? "horizontal." : "vertical.")
            );
        }
        
        // Additional row/column alignment check
        if ((exitRow == -1 || exitRow == rows) && !isHorizontal) {
            // For top/bottom exits with vertical primary piece, check column alignment
            if (exitCol != pCol) {
                throw new IllegalArgumentException(
                    "Primary piece is not aligned with the " + exitPosition + " exit. " +
                    "Exit column (" + exitCol + ") must match primary piece's column (" + pCol + ")."
                );
            }
        } else if ((exitCol == -1 || exitCol == cols) && isHorizontal) {
            // For left/right exits with horizontal primary piece, check row alignment
            if (exitRow != pRow) {
                throw new IllegalArgumentException(
                    "Primary piece is not aligned with the " + exitPosition + " exit. " +
                    "Exit row (" + exitRow + ") must match primary piece's row (" + pRow + ")."
                );
            }
        }
    }
}