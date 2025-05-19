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
    /* File parser */
    public Board parseFile(String filename) throws IOException, IllegalArgumentException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            
            String dimensionLine = reader.readLine();
            if (dimensionLine == null || dimensionLine.trim().isEmpty()) {
                throw new IllegalArgumentException("Input file is empty.");
            }
            
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
            
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Board dimensions must be positive. Got: " + rows + "x" + cols);
            }
            
            if (rows < 2 || cols < 2) {
                throw new IllegalArgumentException("Board is too small. Minimum size is 2x2.");
            }
            
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
            
            if (numPieces <= 0) {
                throw new IllegalArgumentException("Number of pieces must be positive.");
            }
            
            int maxPieces = (rows * cols) / 2;
            if (numPieces > maxPieces) {
                throw new IllegalArgumentException("Too many pieces (" + numPieces + ") for this board size.");
            }
            
            Board board = new Board(rows, cols);
            
            ArrayList<String> allLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    allLines.add(line);
                }
            }
            
            if (allLines.size() < rows) {
                throw new IllegalArgumentException("Not enough rows in the puzzle. Expected at least " + rows + " rows.");
            }
            
            int exitRow = -1;
            int exitCol = -1;
            boolean exitFound = false;
            char[][] grid = new char[rows][cols];

            int exitCount = 0;
            
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    grid[r][c] = '.';
                }
            }
            
            if (allLines.size() > rows) {
                String firstLine = allLines.get(0);
                if (firstLine.contains("K")) {
                    exitRow = -1;
                    exitCol = firstLine.indexOf('K');
                    exitFound = true;
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    if (exitCol < 0 || exitCol >= cols) {
                        throw new IllegalArgumentException("Top exit position is outside the board boundary.");
                    }
                    
                    allLines.remove(0);
                }
                else if (allLines.size() > rows) {
                    String lastLine = allLines.get(rows);
                    if (lastLine.contains("K")) {
                        exitRow = rows;
                        exitCol = lastLine.indexOf('K');
                        exitFound = true;
                        exitCount++;
                        board.setExit(exitRow, exitCol);
                        
                        if (exitCol < 0 || exitCol >= cols) {
                            throw new IllegalArgumentException("Bottom exit position is outside the board boundary.");
                        }
                    }
                }
            }
            
            for (int r = 0; r < rows && r < allLines.size(); r++) {
                line = allLines.get(r);
                
                if (line.length() > 0 && line.charAt(0) == 'K') {
                    exitRow = r;
                    exitCol = -1;
                    exitFound = true;
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    if (exitRow < 0 || exitRow >= rows) {
                        throw new IllegalArgumentException("Left exit position is outside the board boundary.");
                    }
                    
                    line = line.substring(1);
                }
                
                if (line.length() > cols && line.charAt(cols) == 'K') {
                    exitRow = r;
                    exitCol = cols;
                    exitFound = true;
                    exitCount++;
                    board.setExit(exitRow, exitCol);
                    
                    if (exitRow < 0 || exitRow >= rows) {
                        throw new IllegalArgumentException("Right exit position is outside the board boundary.");
                    }
                }
                
                int startIdx = (line.length() > 0 && line.charAt(0) == ' ') ? 1 : 0;
                for (int c = 0; c < cols && startIdx + c < line.length(); c++) {
                    grid[r][c] = line.charAt(startIdx + c);
                    
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
            
            if (!exitFound) {
                throw new IllegalArgumentException("No exit (K) found in the puzzle.");
            }

            if (exitCount>1){
                throw new IllegalArgumentException("Multiple exits (" + exitCount + ") found in puzzle. Only one exit is allowed.");
            }
            
            Set<Character> processedChars = new HashSet<>();
            processedChars.add('.');
            processedChars.add('K');
            processedChars.add(' ');
            
            boolean foundPrimaryPiece = false;
            
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    char pieceChar = grid[r][c];
                    
                    if (pieceChar == '.' || pieceChar == ' ' || processedChars.contains(pieceChar)) {
                        continue;
                    }
                    
                    processedChars.add(pieceChar);
                    boolean isPrimary = (pieceChar == 'P');
                    
                    if (isPrimary) {
                        foundPrimaryPiece = true;
                    }
                    
                    int length = 1;
                    boolean isHorizontal = false;
                    
                    for (int i = c + 1; i < cols; i++) {
                        if (grid[r][i] == pieceChar) {
                            length++;
                            isHorizontal = true;
                        } else {
                            break;
                        }
                    }
                    
                    if (length == 1) {
                        for (int i = r + 1; i < rows; i++) {
                            if (grid[i][c] == pieceChar) {
                                length++;
                            } else {
                                break;
                            }
                        }
                    }
                    
                    if (length < 2) {
                        throw new IllegalArgumentException("Piece '" + pieceChar + "' has invalid length. Minimum length is 2.");
                    }
                    
                    Piece piece = new Piece(pieceChar, r, c, length, !isHorizontal, isPrimary);
                    board.addPiece(piece);
                }
            }
            
            if (!foundPrimaryPiece) {
                throw new IllegalArgumentException("No primary piece (P) found in the puzzle.");
            }
            
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
            
            validatePrimaryPieceExitAlignment(board, exitRow, exitCol, rows, cols);
            
            return board;
            
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /* Validasi primary dan exit */
    private void validatePrimaryPieceExitAlignment(Board board, int exitRow, int exitCol, int rows, int cols) {
        Piece primaryPiece = null;
        for (Piece piece : board.getPieces()) {
            if (piece.getId() == 'P') {
                primaryPiece = piece;
                break;
            }
        }
        
        if (primaryPiece == null) {
            return;
        }
        
        boolean isAligned = false;
        String exitPosition = "";
        
        boolean isHorizontal = !primaryPiece.isVertical();
        int pRow = primaryPiece.getRow();
        int pCol = primaryPiece.getCol();
        
        if (exitRow == -1) {
            exitPosition = "top";
            isAligned = !isHorizontal;
        } else if (exitRow == rows) {
            exitPosition = "bottom";
            isAligned = !isHorizontal;
        } else if (exitCol == -1) {
            exitPosition = "left";
            isAligned = isHorizontal;
        } else if (exitCol == cols) {
            exitPosition = "right";
            isAligned = isHorizontal;
        }
        
        if (!isAligned) {
            throw new IllegalArgumentException(
                "Primary piece has incorrect orientation for " + exitPosition + " exit. " +
                "For " + exitPosition + " exit, primary piece must be " + 
                (!isHorizontal ? "horizontal." : "vertical.")
            );
        }
        
        if ((exitRow == -1 || exitRow == rows) && !isHorizontal) {
            if (exitCol != pCol) {
                throw new IllegalArgumentException(
                    "Primary piece is not aligned with the " + exitPosition + " exit. " +
                    "Exit column (" + exitCol + ") must match primary piece's column (" + pCol + ")."
                );
            }
        } else if ((exitCol == -1 || exitCol == cols) && isHorizontal) {
            if (exitRow != pRow) {
                throw new IllegalArgumentException(
                    "Primary piece is not aligned with the " + exitPosition + " exit. " +
                    "Exit row (" + exitRow + ") must match primary piece's row (" + pRow + ")."
                );
            }
        }
    }
}