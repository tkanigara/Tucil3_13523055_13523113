package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BoardEditor extends JFrame {
    private int rows = 6;
    private int cols = 6;
    private EditorPanel boardPanel;
    
    private char nextPieceId = 'A';
    private boolean placingPrimaryPiece = false;
    private boolean placingExit = false;
    private boolean isVertical = false;
    private int pieceLength = 2;
    private int exitRow = -1;
    private int exitCol = -1;
    
    private JButton createButton;
    private JButton cancelButton;
    private JButton primaryPieceButton;
    private JButton exitButton;
    private JComboBox<String> orientationSelector;
    private JSpinner lengthSpinner;
    private JButton clearButton;
    private JButton undoButton;
    private JLabel statusLabel;
    
    private List<EditorPiece> placedPieces = new ArrayList<>();
    private Map<Character, Color> pieceColors = new HashMap<>();
    
    public BoardEditor(JFrame parent) {
        super("Rush Hour Board Editor");
        setModal(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLayout(new BorderLayout());
        
        createControlPanel();
        createBoardPanel();
        createStatusBar();
        
        setLocationRelativeTo(parent);
    }
    
    private void setModal(JFrame parent) {
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
    
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        
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
        
        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Create Puzzle");
        createButton.addActionListener(e -> createPuzzle());
        createButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        controlPanel.add(sizePanel, BorderLayout.NORTH);
        controlPanel.add(piecePanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    private void createBoardPanel() {
        boardPanel = new EditorPanel();
        boardPanel.setPreferredSize(new Dimension(600, 600));
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.add(boardPanel);
        add(new JScrollPane(centeringPanel), BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Click on the board to place pieces");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void togglePrimaryPiecePlacement() {
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
    
    private void toggleExitPlacement() {
        placingExit = !placingExit;
        placingPrimaryPiece = false;
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        primaryPieceButton.setBackground(placingPrimaryPiece ? Color.YELLOW : null);
        exitButton.setBackground(placingExit ? Color.YELLOW : null);
        
        int maxLength = isVertical ? rows : cols;
        SpinnerNumberModel model = (SpinnerNumberModel) lengthSpinner.getModel();
        model.setMaximum(maxLength);
        
        if (pieceLength > maxLength) {
            lengthSpinner.setValue(maxLength);
            pieceLength = maxLength;
        }
        
        if (placingPrimaryPiece) {
            statusLabel.setText("Click on the board to place the primary piece (P)");
        } else if (placingExit) {
            statusLabel.setText("Click on a board edge to place the exit");
        } else {
            statusLabel.setText("Click on the board to place piece '" + nextPieceId + "'");
        }
        
        createButton.setEnabled(hasPrimaryPiece() && exitRow != -1 && exitCol != -1);
    }
    
    private boolean hasPrimaryPiece() {
        for (EditorPiece piece : placedPieces) {
            if (piece.id == 'P') {
                return true;
            }
        }
        return false;
    }
    
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
    
    // Reset board
    private void resetBoard() {
        clearBoard();
        updateButtonStates();
        boardPanel.repaint();
    }
    
    // buat undo
    private void undoLastPlacement() {
        if (!placedPieces.isEmpty()) {
            EditorPiece removed = placedPieces.remove(placedPieces.size() - 1);
            
            if (removed.id == 'P') {
                createButton.setEnabled(false);
            }
            
            if (removed.id != 'P' && removed.id > 'A') {
                nextPieceId = (char) (removed.id - 1);
            }
            
            undoButton.setEnabled(!placedPieces.isEmpty());
            updateButtonStates();
            boardPanel.repaint();
        }
    }
    
    // Buat dan simpan puzzle
    private void createPuzzle() {
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
        
        JFileChooser fileChooser = new JFileChooser(new File("test/input"));
        fileChooser.setDialogTitle("Save Puzzle");
        fileChooser.setSelectedFile(new File("custom_puzzle.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
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
    
    // Simpan puzzle ke file
    private void savePuzzleToFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        
        writer.write(rows + " " + cols + "\n");
        
        int regularPieceCount = 0;
        for (EditorPiece piece : placedPieces) {
            if (piece.id != 'P') {
                regularPieceCount++;
            }
        }
        
        writer.write(regularPieceCount + "\n");
        
        char[][] grid = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = '.';
            }
        }
        
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
        
        for (int r = 0; r < rows; r++) {
            if (exitRow == r && exitCol == -1) {
                writer.write('K');
            }
            
            for (int c = 0; c < cols; c++) {
                writer.write(grid[r][c]);
            }
            
            if (exitRow == r && exitCol == cols) {
                writer.write('K');
            }
            
            writer.write("\n");
        }
        
        if (exitRow == -1) {
            for (int c = 0; c < cols; c++) {
                if (c == exitCol) {
                    writer.write('K');
                } else {
                    writer.write('.');
                }
            }
            writer.write("\n");
        } else if (exitRow == rows) {
            for (int c = 0; c < cols; c++) {
                if (c == exitCol) {
                    writer.write('K');
                } else {
                    writer.write('.');
                }
            }
            writer.write("\n");
        }
        
        writer.close();
    }
    
    private Color getPieceColor(char id) {
        if (id == 'P') {
            return Color.RED;
        }
        
        if (!pieceColors.containsKey(id)) {
            pieceColors.put(id, generatePastelColor());
        }
        
        return pieceColors.get(id);
    }
    
    private Color generatePastelColor() {
        float hue = (float) Math.random();
        float saturation = 0.5f;
        float brightness = 0.9f;
        
        return Color.getHSBColor(hue, saturation, brightness);
    }
    
    // menampilkan papan dan menangani input mouse
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
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverPoint = e.getPoint();
                    repaint();
                }
            });
            
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
        
        // Menggambar papan dan semua elemennya
        private void drawBoard(Graphics g) {
            int startX = (getWidth() - cols * CELL_SIZE) / 2;
            int startY = (getHeight() - rows * CELL_SIZE) / 2;
            int boardWidth = cols * CELL_SIZE;
            int boardHeight = rows * CELL_SIZE;
            
            g.setColor(Color.WHITE);
            g.fillRect(startX, startY, boardWidth, boardHeight);
            
            g.setColor(Color.LIGHT_GRAY);
            for (int r = 0; r <= rows; r++) {
                g.drawLine(startX, startY + r * CELL_SIZE, startX + boardWidth, startY + r * CELL_SIZE);
            }
            for (int c = 0; c <= cols; c++) {
                g.drawLine(startX + c * CELL_SIZE, startY, startX + c * CELL_SIZE, startY + boardHeight);
            }
            
            g.setColor(Color.GREEN);
            if (exitRow != -1 || exitCol != -1) {
                if (exitRow == -1) {
                    g.fillRect(startX + exitCol * CELL_SIZE, startY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE/2);
                } else if (exitRow == rows) {
                    g.fillRect(startX + exitCol * CELL_SIZE, startY + rows * CELL_SIZE, CELL_SIZE, CELL_SIZE/2);
                } else if (exitCol == -1) {
                    g.fillRect(startX - CELL_SIZE/2, startY + exitRow * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                } else if (exitCol == cols) {
                    g.fillRect(startX + cols * CELL_SIZE, startY + exitRow * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                }
            }
            
            for (EditorPiece piece : placedPieces) {
                Color pieceColor = getPieceColor(piece.id);
                
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
            
            if ((placingPrimaryPiece || !placingExit) && hoverPoint != null) {
                int mouseX = hoverPoint.x;
                int mouseY = hoverPoint.y;
                
                int gridCol = (mouseX - startX) / CELL_SIZE;
                int gridRow = (mouseY - startY) / CELL_SIZE;
                
                if (gridCol >= 0 && gridCol < cols && gridRow >= 0 && gridRow < rows) {
                    boolean fits = true;
                    if (isVertical) {
                        fits = gridRow + pieceLength <= rows;
                    } else {
                        fits = gridCol + pieceLength <= cols;
                    }
                    
                    if (fits) {
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
                        
                        Color hoverColor;
                        if (placingPrimaryPiece) {
                            hoverColor = overlaps ? 
                                new Color(255, 0, 0, 128) :
                                new Color(255, 0, 0, 64);
                        } else {
                            hoverColor = overlaps ? 
                                new Color(255, 0, 0, 128) :
                                new Color(0, 150, 0, 64);
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
            
            if (placingExit && hoverPoint != null) {
                int mouseX = hoverPoint.x;
                int mouseY = hoverPoint.y;
                
                boolean validHover = false;
                
                if (mouseY < startY && mouseY > startY - CELL_SIZE/2) {
                    int col = (mouseX - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        g.setColor(new Color(0, 200, 0, 128));
                        g.fillRect(startX + col * CELL_SIZE, startY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE/2);
                        validHover = true;
                    }
                }
                
                if (!validHover && mouseY > startY + boardHeight && mouseY < startY + boardHeight + CELL_SIZE/2) {
                    int col = (mouseX - startX) / CELL_SIZE;
                    if (col >= 0 && col < cols) {
                        g.setColor(new Color(0, 200, 0, 128));
                        g.fillRect(startX + col * CELL_SIZE, startY + rows * CELL_SIZE, CELL_SIZE, CELL_SIZE/2);
                        validHover = true;
                    }
                }
                
                if (!validHover && mouseX < startX && mouseX > startX - CELL_SIZE/2) {
                    int row = (mouseY - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        g.setColor(new Color(0, 200, 0, 128));
                        g.fillRect(startX - CELL_SIZE/2, startY + row * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                        validHover = true;
                    }
                }
                
                if (!validHover && mouseX > startX + boardWidth && mouseX < startX + boardWidth + CELL_SIZE/2) {
                    int row = (mouseY - startY) / CELL_SIZE;
                    if (row >= 0 && row < rows) {
                        g.setColor(new Color(0, 200, 0, 128));
                        g.fillRect(startX + cols * CELL_SIZE, startY + row * CELL_SIZE, CELL_SIZE/2, CELL_SIZE);
                    }
                }
            }
        }
        
        private void handleMouseClick(int x, int y) {
            int startX = (getWidth() - cols * CELL_SIZE) / 2;
            int startY = (getHeight() - rows * CELL_SIZE) / 2;
            int boardWidth = cols * CELL_SIZE;
            int boardHeight = rows * CELL_SIZE;
            
            if (placingExit) {
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
                
                return;
            }
            
            int gridCol = (x - startX) / CELL_SIZE;
            int gridRow = (y - startY) / CELL_SIZE;
            
            if (gridCol < 0 || gridCol >= cols || gridRow < 0 || gridRow >= rows) {
                return;
            }
            
            if (isVertical && gridRow + pieceLength > rows) {
                statusLabel.setText("Piece doesn't fit! Try a different position or orientation.");
                return;
            }
            if (!isVertical && gridCol + pieceLength > cols) {
                statusLabel.setText("Piece doesn't fit! Try a different position or orientation.");
                return;
            }
            
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
            
            char pieceId = placingPrimaryPiece ? 'P' : nextPieceId;
            EditorPiece newPiece = new EditorPiece(pieceId, gridRow, gridCol, pieceLength, isVertical);
            placedPieces.add(newPiece);
            
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