package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import model.Board;
import model.Piece;

/**
 * BoardEditor provides a graphical interface for creating Rush Hour puzzles.
 */
public class BoardEditor extends JFrame {
    // Board parameters
    private int rows = 6;
    private int cols = 6;
    private EditorPanel boardPanel;
    
    // Editor state
    private char nextPieceId = 'A';
    private boolean placingPrimaryPiece = false;
    private boolean placingExit = false;
    private boolean isVertical = false;
    private int pieceLength = 2;
    private int exitRow = -1;
    private int exitCol = -1;
    
    // UI components
    private JButton createButton;
    private JButton cancelButton;
    private JButton primaryPieceButton;
    private JButton exitButton;
    private JComboBox<String> orientationSelector;
    private JSpinner lengthSpinner;
    private JButton clearButton;
    private JButton undoButton;
    private JLabel statusLabel;
    
    // Board data
    private List<EditorPiece> placedPieces = new ArrayList<>();
    private Map<Character, Color> pieceColors = new HashMap<>();
    
    /**
     * Constructor - sets up the board editor dialog
     * @param parent Parent frame
     */
    public BoardEditor(JFrame parent) {
        super("Rush Hour Board Editor");
        setModal(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLayout(new BorderLayout());
        
        // Create UI components
        createControlPanel();
        createBoardPanel();
        createStatusBar();
        
        // Center on parent
        setLocationRelativeTo(parent);
    }
    
    /**
     * Set the JFrame to act like a modal dialog
     */
    private void setModal(JFrame parent) {
        // Make the dialog appear modal by disabling the parent
        if (parent != null) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    parent.setEnabled(false);
                }
                
                @Override
                public void windowClosed(WindowEvent e) {
                    parent.setEnabled(true);
                    parent.toFront();
                }
            });
        }
    }
    
    /**
     * Create the top control panel with buttons and selectors
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Board size panel
        JPanel sizePanel = new JPanel();
        JLabel rowsLabel = new JLabel("Rows:");
        JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(6, 3, 20, 1));
        rowsSpinner.addChangeListener(e -> {
            rows = (int) rowsSpinner.getValue();
            resetBoard();
        });
        
        JLabel colsLabel = new JLabel("Columns:");
        JSpinner colsSpinner = new JSpinner(new SpinnerNumberModel(6, 3, 20, 1));
        colsSpinner.addChangeListener(e -> {
            cols = (int) colsSpinner.getValue();
            resetBoard();
        });
        
        sizePanel.add(rowsLabel);
        sizePanel.add(rowsSpinner);
        sizePanel.add(colsLabel);
        sizePanel.add(colsSpinner);
        
        // Piece creation panel
        JPanel piecePanel = new JPanel();
        
        primaryPieceButton = new JButton("Place Primary Piece (P)");
        primaryPieceButton.addActionListener(e -> togglePrimaryPiecePlacement());
        
        exitButton = new JButton("Place Exit");
        exitButton.addActionListener(e -> toggleExitPlacement());
        
        orientationSelector = new JComboBox<>(new String[] {"Horizontal", "Vertical"});
        orientationSelector.addActionListener(e -> {
            isVertical = orientationSelector.getSelectedIndex() == 1;
            updateButtonStates();
        });
        
        JLabel lengthLabel = new JLabel("Length:");
        lengthSpinner = new JSpinner(new SpinnerNumberModel(2, 2, Math.max(rows, cols), 1));
        lengthSpinner.addChangeListener(e -> {
            pieceLength = (Integer) lengthSpinner.getValue();
            updateButtonStates();
        });
        
        clearButton = new JButton("Clear Board");
        clearButton.addActionListener(e -> clearBoard());
        
        undoButton = new JButton("Undo Last");
        undoButton.addActionListener(e -> undoLastPlacement());
        undoButton.setEnabled(false);
        
        piecePanel.add(primaryPieceButton);
        piecePanel.add(exitButton);
        piecePanel.add(new JLabel("Orientation:"));
        piecePanel.add(orientationSelector);
        piecePanel.add(lengthLabel);
        piecePanel.add(lengthSpinner);
        piecePanel.add(clearButton);
        piecePanel.add(undoButton);
        
        // Button panel for create/cancel
        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Create Puzzle");
        createButton.addActionListener(e -> createPuzzle());
        createButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        // Add all panels to control panel
        controlPanel.add(sizePanel, BorderLayout.NORTH);
        controlPanel.add(piecePanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * Create the board panel for piece placement
     */
    private void createBoardPanel() {
        boardPanel = new EditorPanel();
        boardPanel.setPreferredSize(new Dimension(600, 600));
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.add(boardPanel);
        add(new JScrollPane(centeringPanel), BorderLayout.CENTER);
    }
    
    /**
     * Create the status bar
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Click on the board to place pieces");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Toggle primary piece placement mode
     */
    private void togglePrimaryPiecePlacement() {
        // Can't place primary piece if one already exists
        if (hasPrimaryPiece()) {
            JOptionPane.showMessageDialog(this, 
                "Primary piece already placed. Clear board or remove it first.",
                "Primary Piece Exists", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        placingPrimaryPiece = !placingPrimaryPiece;
        placingExit = false;
        updateButtonStates();
    }
    
    /**
     * Toggle exit placement mode
     */
    private void toggleExitPlacement() {
        placingExit = !placingExit;
        placingPrimaryPiece = false;
        updateButtonStates();
    }
    
    /**
     * Update button states based on current editor mode
     */
    private void updateButtonStates() {
        primaryPieceButton.setBackground(placingPrimaryPiece ? Color.YELLOW : null);
        exitButton.setBackground(placingExit ? Color.YELLOW : null);
        
        // Update length spinner max value based on orientation
        int maxLength = isVertical ? rows : cols;
        SpinnerNumberModel model = (SpinnerNumberModel) lengthSpinner.getModel();
        model.setMaximum(maxLength);
        
        // If current length exceeds max, adjust it
        if (pieceLength > maxLength) {
            lengthSpinner.setValue(maxLength);
            pieceLength = maxLength;
        }
        
        // Update status text
        if (placingPrimaryPiece) {
            statusLabel.setText("Click on the board to place the primary piece (P)");
        } else if (placingExit) {
            statusLabel.setText("Click on a board edge to place the exit");
        } else {
            statusLabel.setText("Click on the board to place piece '" + nextPieceId + "'");
        }
        
        // Enable create button only if we have primary piece and exit
        createButton.setEnabled(hasPrimaryPiece() && exitRow != -1 && exitCol != -1);
    }
    
    /**
     * Check if a primary piece exists
     */
    private boolean hasPrimaryPiece() {
        for (EditorPiece piece : placedPieces) {
            if (piece.id == 'P') {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear the board completely
     */
    private void clearBoard() {
        placedPieces.clear();
        exitRow = -1;
        exitCol = -1;
        nextPieceId = 'A';
        undoButton.setEnabled(false);
        createButton.setEnabled(false);
        placingPrimaryPiece = false;
        placingExit = false;
        pieceColors.clear();
        updateButtonStates();
        boardPanel.repaint();
    }
    
    /**
     * Reset the board for a dimension change
     */
    private void resetBoard() {
        clearBoard();
        updateButtonStates(); // Update length spinner max
        boardPanel.repaint();
    }
    
    /**
     * Undo the last piece placement
     */
    private void undoLastPlacement() {
        if (!placedPieces.isEmpty()) {
            EditorPiece removed = placedPieces.remove(placedPieces.size() - 1);
            
            // If we removed the primary piece, we need to update create button state
            if (removed.id == 'P') {
                createButton.setEnabled(false);
            }
            
            // If it was a regular piece, update the next piece ID
            if (removed.id != 'P' && removed.id > 'A') {
                nextPieceId = (char) (removed.id - 1);
            }
            
            // Update button states
            undoButton.setEnabled(!placedPieces.isEmpty());
            updateButtonStates();
            boardPanel.repaint();
        }
    }
    
    /**
     * Create the puzzle and save it
     */
    private void createPuzzle() {
        // Validate that we have a primary piece and exit
        if (!hasPrimaryPiece()) {
            JOptionPane.showMessageDialog(this, 
                "You must place a primary piece (P) on the board.",
                "Missing Primary Piece", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (exitRow == -1 && exitCol == -1) {
            JOptionPane.showMessageDialog(this, 
                "You must place an exit on the board.",
                "Missing Exit", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show file save dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Puzzle");
        fileChooser.setSelectedFile(new File("custom_puzzle.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Make sure the file has .txt extension
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try {
                savePuzzleToFile(file);
                JOptionPane.showMessageDialog(this, 
                    "Puzzle saved successfully to " + file.getName(),
                    "Save Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving puzzle: " + e.getMessage(),
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Save the puzzle to a file in the format expected by the Rush Hour solver
     * 
     * @param file The file to save to
     * @throws IOException If there's an error writing to the file
     */
    private void savePuzzleToFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        
        // Write board dimensions
        writer.write(rows + " " + cols + "\n");
        
        // Write number of pieces
        writer.write(placedPieces.size() + "\n");
        
        // Create grid representation
        char[][] grid = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = '.';
            }
        }
        
        // Place pieces on grid
        for (EditorPiece piece : placedPieces) {
            if (piece.isVertical) {
                for (int r = piece.row; r < piece.row + piece.length; r++) {
                    grid[r][piece.col] = piece.id;
                }
            } else {
                for (int c = piece.col; c < piece.col + piece.length; c++) {
                    grid[piece.row][c] = piece.id;
                }
            }
        }
        
        // Write grid with exit marker 'K'
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                writer.write(grid[r][c]);
            }
            if (exitRow == r && exitCol == cols) {
                writer.write('K');
            }
            writer.write("\n");
        }
        
        writer.close();
    }
    
    /**
     * Get a color for a piece
     */
    private Color getPieceColor(char id) {
        // Primary piece is always red
        if (id == 'P') {
            return Color.RED;
        }
        
        // Get or create a color for this piece
        if (!pieceColors.containsKey(id)) {
            pieceColors.put(id, generatePastelColor());
        }
        
        return pieceColors.get(id);
    }
    
    /**
     * Generate a random pastel color
     */
    private Color generatePastelColor() {
        float hue = (float) Math.random();
        float saturation = 0.5f; // Pastels have lower saturation
        float brightness = 0.9f; // Pastels are bright
        
        return Color.getHSBColor(hue, saturation, brightness);
    }
    
    /**
     * The panel that displays the board and handles mouse input
     */
    private class EditorPanel extends JPanel {
        private static final int CELL_SIZE = 40;
        private static final int BORDER_SIZE = 20;
        private Point hoverPoint = null;
        
        public EditorPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e.getX(), e.getY());
                }
            });
            
            // For hover effect
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverPoint = e.getPoint();
                    repaint();
                }
            });
            
            // Clear hover when mouse exits
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverPoint = null;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
        }
        
        private void drawBoard(Graphics g) {
            // Calculate board position
            int startX = (getWidth() - cols * CELL_SIZE) / 2;
            int startY = (getHeight() - rows * CELL_SIZE) / 2;
            int boardWidth = cols * CELL_SIZE;
            int boardHeight = rows * CELL_SIZE;
            
            // Draw board background
            g.setColor(Color.WHITE);
            g.fillRect(startX, startY, boardWidth, boardHeight);
            
            // Draw grid
            g.setColor(Color.LIGHT_GRAY);
            for (int r = 0; r <= rows; r++) {
                g.drawLine(startX, startY + r * CELL_SIZE, startX + boardWidth, startY + r * CELL_SIZE);
            }
            for (int c = 0; c <= cols; c++) {
                g.drawLine(startX + c * CELL_SIZE, startY, startX + c * CELL_SIZE, startY + boardHeight);
            }
            
            // Draw exit marker
            g.setColor(Color.GREEN);
            if (exitRow != -1 || exitCol != -1) {
                if (exitRow == -1) { // Top exit
                    g.fillRect(startX + exitCol * CELL_SIZE, startY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE/2);
                } else if (exitRow == rows) { // Bottom exit
                    g.fillRect(startX + exitCol * CELL_SIZE, startY + rows * CELL_SIZE, CELL_SIZE, CELL_SIZE/2);
                } else if (exitCol == -1) { // Left exit
                    g.fillRect(startX - CELL_SIZE/2, startY + exitRow * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                } else if (exitCol == cols) { // Right exit
                    g.fillRect(startX + cols * CELL_SIZE, startY + exitRow * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                }
            }
            
            // Draw pieces
            for (EditorPiece piece : placedPieces) {
                // Get color for the piece
                Color pieceColor = getPieceColor(piece.id);
                
                // Draw the piece
                g.setColor(pieceColor);
                if (piece.isVertical) {
                    g.fillRect(startX + piece.col * CELL_SIZE + 2, 
                              startY + piece.row * CELL_SIZE + 2, 
                              CELL_SIZE - 4, 
                              piece.length * CELL_SIZE - 4);
                } else {
                    g.fillRect(startX + piece.col * CELL_SIZE + 2, 
                              startY + piece.row * CELL_SIZE + 2, 
                              piece.length * CELL_SIZE - 4, 
                              CELL_SIZE - 4);
                }
                
                // Draw piece ID
                g.setColor(Color.BLACK);
                Font font = new Font("Arial", Font.BOLD, CELL_SIZE / 3);
                g.setFont(font);
                
                FontMetrics metrics = g.getFontMetrics(font);
                String text = String.valueOf(piece.id);
                int textX, textY;
                
                if (piece.isVertical) {
                    textX = startX + piece.col * CELL_SIZE + (CELL_SIZE - metrics.stringWidth(text)) / 2;
                    textY = startY + piece.row * CELL_SIZE + CELL_SIZE / 2;
                } else {
                    textX = startX + piece.col * CELL_SIZE + (CELL_SIZE - metrics.stringWidth(text)) / 2;
                    textY = startY + piece.row * CELL_SIZE + (CELL_SIZE + metrics.getAscent()) / 2;
                }
                
                g.drawString(text, textX, textY);
            }
            
            // Draw preview of the piece to be placed (when hovering)
            if ((placingPrimaryPiece || !placingExit) && hoverPoint != null) {
                int mouseX = hoverPoint.x;
                int mouseY = hoverPoint.y;
                
                int gridCol = (mouseX - startX) / CELL_SIZE;
                int gridRow = (mouseY - startY) / CELL_SIZE;
                
                if (gridCol >= 0 && gridCol < cols && gridRow >= 0 && gridRow < rows) {
                    // Check if the piece would fit
                    boolean fits = true;
                    if (isVertical) {
                        fits = gridRow + pieceLength <= rows;
                    } else {
                        fits = gridCol + pieceLength <= cols;
                    }
                    
                    if (fits) {
                        // Check if space is occupied
                        boolean overlaps = false;
                        for (int i = 0; i < pieceLength; i++) {
                            int checkRow = isVertical ? gridRow + i : gridRow;
                            int checkCol = isVertical ? gridCol : gridCol + i;
                            
                            for (EditorPiece piece : placedPieces) {
                                if (piece.isVertical) {
                                    for (int r = piece.row; r < piece.row + piece.length; r++) {
                                        if (r == checkRow && piece.col == checkCol) {
                                            overlaps = true;
                                            break;
                                        }
                                    }
                                } else {
                                    for (int c = piece.col; c < piece.col + piece.length; c++) {
                                        if (piece.row == checkRow && c == checkCol) {
                                            overlaps = true;
                                            break;
                                        }
                                    }
                                }
                                if (overlaps) break;
                            }
                            if (overlaps) break;
                        }
                        
                        // Draw the preview piece with appropriate color
                        Color hoverColor;
                        if (placingPrimaryPiece) {
                            hoverColor = overlaps ? 
                                new Color(255, 0, 0, 128) : // Transparent red if overlapping
                                new Color(255, 0, 0, 64);   // More transparent red if valid
                        } else {
                            hoverColor = overlaps ? 
                                new Color(255, 0, 0, 128) : // Transparent red if overlapping
                                new Color(0, 150, 0, 64);   // Transparent green if valid
                        }
                        
                        g.setColor(hoverColor);
                        
                        if (isVertical) {
                            g.fillRect(startX + gridCol * CELL_SIZE + 2, 
                                      startY + gridRow * CELL_SIZE + 2, 
                                      CELL_SIZE - 4, 
                                      pieceLength * CELL_SIZE - 4);
                        } else {
                            g.fillRect(startX + gridCol * CELL_SIZE + 2, 
                                      startY + gridRow * CELL_SIZE + 2, 
                                      pieceLength * CELL_SIZE - 4, 
                                      CELL_SIZE - 4);
                        }
                        
                        // Show piece ID in the hover preview
                        g.setColor(Color.BLACK);
                        Font font = new Font("Arial", Font.BOLD, CELL_SIZE / 3);
                        g.setFont(font);
                        
                        FontMetrics metrics = g.getFontMetrics(font);
                        String text = String.valueOf(placingPrimaryPiece ? 'P' : nextPieceId);
                        int textX, textY;
                        
                        if (isVertical) {
                            textX = startX + gridCol * CELL_SIZE + (CELL_SIZE - metrics.stringWidth(text)) / 2;
                            textY = startY + gridRow * CELL_SIZE + CELL_SIZE / 2;
                        } else {
                            textX = startX + gridCol * CELL_SIZE + (CELL_SIZE - metrics.stringWidth(text)) / 2;
                            textY = startY + gridRow * CELL_SIZE + (CELL_SIZE + metrics.getAscent()) / 2;
                        }
                        
                        g.drawString(text, textX, textY);
                    }
                }
            }
            
            // Draw exit placement hover effect
            if (placingExit && hoverPoint != null) {
                int mouseX = hoverPoint.x;
                int mouseY = hoverPoint.y;
                
                // Check each edge for hover
                boolean validHover = false;
                
                // Top edge
                if (mouseY < startY && mouseY > startY - CELL_SIZE/2) {
                    int col = (mouseX - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        g.setColor(new Color(0, 200, 0, 128)); // Transparent green
                        g.fillRect(startX + col * CELL_SIZE, startY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE/2);
                        validHover = true;
                    }
                }
                
                // Bottom edge
                if (!validHover && mouseY > startY + boardHeight && mouseY < startY + boardHeight + CELL_SIZE/2) {
                    int col = (mouseX - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        g.setColor(new Color(0, 200, 0, 128)); // Transparent green
                        g.fillRect(startX + col * CELL_SIZE, startY + rows * CELL_SIZE, CELL_SIZE, CELL_SIZE/2);
                        validHover = true;
                    }
                }
                
                // Left edge
                if (!validHover && mouseX < startX && mouseX > startX - CELL_SIZE/2) {
                    int row = (mouseY - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        g.setColor(new Color(0, 200, 0, 128)); // Transparent green
                        g.fillRect(startX - CELL_SIZE/2, startY + row * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                        validHover = true;
                    }
                }
                
                // Right edge
                if (!validHover && mouseX > startX + boardWidth && mouseX < startX + boardWidth + CELL_SIZE/2) {
                    int row = (mouseY - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        g.setColor(new Color(0, 200, 0, 128)); // Transparent green
                        g.fillRect(startX + cols * CELL_SIZE, startY + row * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                    }
                }
            }
        }
        
        private void handleMouseClick(int x, int y) {
            // Calculate board position
            int startX = (getWidth() - cols * CELL_SIZE) / 2;
            int startY = (getHeight() - rows * CELL_SIZE) / 2;
            int boardWidth = cols * CELL_SIZE;
            int boardHeight = rows * CELL_SIZE;
            
            // Check if we're in exit placement mode
            if (placingExit) {
                // Check top edge
                if (y < startY && y > startY - CELL_SIZE/2) {
                    int col = (x - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        exitRow = -1;
                        exitCol = col;
                        placingExit = false;
                        updateButtonStates();
                        repaint();
                        return;
                    }
                }
                
                // Check bottom edge
                if (y > startY + boardHeight && y < startY + boardHeight + CELL_SIZE/2) {
                    int col = (x - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        exitRow = rows;
                        exitCol = col;
                        placingExit = false;
                        updateButtonStates();
                        repaint();
                        return;
                    }
                }
                
                // Check left edge
                if (x < startX && x > startX - CELL_SIZE/2) {
                    int row = (y - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        exitRow = row;
                        exitCol = -1;
                        placingExit = false;
                        updateButtonStates();
                        repaint();
                        return;
                    }
                }
                
                // Check right edge
                if (x > startX + boardWidth && x < startX + boardWidth + CELL_SIZE/2) {
                    int row = (y - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        exitRow = row;
                        exitCol = cols;
                        placingExit = false;
                        updateButtonStates();
                        repaint();
                        return;
                    }
                }
                
                // If we got here, the click wasn't on a valid exit location
                return;
            }
            
            // Convert to grid coordinates
            int gridCol = (x - startX) / CELL_SIZE;
            int gridRow = (y - startY) / CELL_SIZE;
            
            // Check if click is within the board
            if (gridCol < 0 || gridCol >= cols || gridRow < 0 || gridRow >= rows) {
                return;
            }
            
            // Check if the piece would fit
            if (isVertical && gridRow + pieceLength > rows) {
                statusLabel.setText("Piece doesn't fit! Try a different position or orientation.");
                return;
            }
            if (!isVertical && gridCol + pieceLength > cols) {
                statusLabel.setText("Piece doesn't fit! Try a different position or orientation.");
                return;
            }
            
            // Check if space is occupied
            for (int i = 0; i < pieceLength; i++) {
                int checkRow = isVertical ? gridRow + i : gridRow;
                int checkCol = isVertical ? gridCol : gridCol + i;
                
                for (EditorPiece piece : placedPieces) {
                    if (piece.isVertical) {
                        for (int r = piece.row; r < piece.row + piece.length; r++) {
                            if (r == checkRow && piece.col == checkCol) {
                                statusLabel.setText("Space is occupied! Try a different position.");
                                return;
                            }
                        }
                    } else {
                        for (int c = piece.col; c < piece.col + piece.length; c++) {
                            if (piece.row == checkRow && c == checkCol) {
                                statusLabel.setText("Space is occupied! Try a different position.");
                                return;
                            }
                        }
                    }
                }
            }
            
            // Place the piece
            char pieceId = placingPrimaryPiece ? 'P' : nextPieceId;
            EditorPiece newPiece = new EditorPiece(pieceId, gridRow, gridCol, pieceLength, isVertical);
            placedPieces.add(newPiece);
            
            // Update state
            if (placingPrimaryPiece) {
                placingPrimaryPiece = false;
            } else {
                nextPieceId = (char) (nextPieceId + 1);
            }
            
            undoButton.setEnabled(true);
            updateButtonStates();
            repaint();
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                cols * CELL_SIZE + 2 * BORDER_SIZE,
                rows * CELL_SIZE + 2 * BORDER_SIZE
            );
        }
    }
    
    /**
     * Class to represent a piece in the editor
     */
    private static class EditorPiece {
        char id;
        int row;
        int col;
        int length;
        boolean isVertical;
        
        EditorPiece(char id, int row, int col, int length, boolean isVertical) {
            this.id = id;
            this.row = row;
            this.col = col;
            this.length = length;
            this.isVertical = isVertical;
        }
    }
}