package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import algorithm.*;
import model.Board;
import model.Move;
import model.Piece;
import util.FileParser;

public class Gui extends JFrame {
    private JPanel boardPanel;
    private JButton loadButton;
    private JButton solveButton;
    private JButton createBoardButton;
    private JButton saveButton;
    private JComboBox<String> algorithmSelector;
    private JComboBox<String> heuristicSelector;
    private JLabel statusLabel;
    private JSlider animationSpeedSlider;
    private JButton playPauseButton;
    private JButton stopButton;
    private JButton stepButton;
    
    private Board currentBoard;
    private List<Board> solutionSteps;
    private List<Move> solutionMoves;
    private int currentStep = 0;
    private Timer animationTimer;
    private AtomicBoolean animationRunning = new AtomicBoolean(false);
    
    private boolean showExitAnimation = false;
    private boolean showingFinalState = false;
    
    private int nodesVisited = 0;
    private double executionTime = 0;
    
    private Map<Character, Color> pieceColors = new HashMap<>();
    
    public Gui() {
        super("Rush Hour Puzzle Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        createTopPanel();
        createBoardPanel();
        createBottomPanel();
        
        animationTimer = new Timer(500, e -> {
            if (currentStep < solutionSteps.size() - 1) {
                currentStep++;
                
                if (currentStep == solutionSteps.size() - 1) {
                    showingFinalState = true;
                    showExitAnimation = false;
                    updateBoardDisplay();
                    
                    Timer exitAnimationTimer = new Timer(800, evt -> {
                        ((Timer)evt.getSource()).stop();
                        showingFinalState = false;
                        showExitAnimation = true;
                        updateBoardDisplay();
                        
                        Timer completionTimer = new Timer(1000, evt2 -> {
                            ((Timer)evt2.getSource()).stop();
                            animationTimer.stop();
                            animationRunning.set(false);
                            playPauseButton.setText("Play");
                            statusLabel.setText("Solution complete in " + (solutionSteps.size() - 1) + " steps!");
                        });
                        completionTimer.setRepeats(false);
                        completionTimer.start();
                    });
                    exitAnimationTimer.setRepeats(false);
                    exitAnimationTimer.start();
                } else {
                    showExitAnimation = false;
                    showingFinalState = false;
                    updateBoardDisplay();
                }
                
                statusLabel.setText("Step " + currentStep + " of " + (solutionSteps.size() - 1));
            }
        });
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel filePanel = new JPanel();
        loadButton = new JButton("Load Puzzle");
        loadButton.addActionListener(e -> loadPuzzleFromFile());
        
        createBoardButton = new JButton("Create Puzzle");
        createBoardButton.addActionListener(e -> openBoardEditor());
        
        saveButton = new JButton("Save Solution");
        saveButton.addActionListener(e -> saveSolutionToFile());
        saveButton.setEnabled(false);
        
        filePanel.add(loadButton);
        filePanel.add(createBoardButton);
        filePanel.add(saveButton);
        
        JPanel algoPanel = new JPanel();
        algorithmSelector = new JComboBox<>(new String[] {
            "Uniform Cost Search (UCS)",
            "Greedy Best-First Search (GBFS)",
            "A* Search",
            "IDA* Search"
        });
        
        heuristicSelector = new JComboBox<>(new String[] {
            "Blocking Pieces",
            "Manhattan Distance",
            "Combined"
        });
        heuristicSelector.setEnabled(false);
        
        algorithmSelector.addActionListener(e -> {
            int selectedIndex = algorithmSelector.getSelectedIndex();
            heuristicSelector.setEnabled(selectedIndex != 0);
        });
        
        solveButton = new JButton("Solve");
        solveButton.addActionListener(e -> solvePuzzle());
        solveButton.setEnabled(false);
        
        algoPanel.add(new JLabel("Algorithm:"));
        algoPanel.add(algorithmSelector);
        algoPanel.add(new JLabel("Heuristic:"));
        algoPanel.add(heuristicSelector);
        algoPanel.add(solveButton);
        
        topPanel.add(filePanel, BorderLayout.NORTH);
        topPanel.add(algoPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createBoardPanel() {
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(600, 600));
        boardPanel.setBackground(Color.WHITE);
        
        add(boardPanel, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        playPauseButton = new JButton("Play");
        playPauseButton.addActionListener(e -> togglePlayPause());
        playPauseButton.setEnabled(false);
        
        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopAnimation());
        stopButton.setEnabled(false);
        
        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> stepForward());
        stepButton.setEnabled(false);
        
        animationSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        animationSpeedSlider.setMajorTickSpacing(1);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setPaintLabels(true);
        animationSpeedSlider.setSnapToTicks(true);
        animationSpeedSlider.addChangeListener(e -> {
            if (!animationSpeedSlider.getValueIsAdjusting()) {
                updateAnimationSpeed();
            }
        });
        
        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(animationSpeedSlider);
        controlPanel.add(playPauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(stopButton);
        
        statusLabel = new JLabel("Ready to load or create a puzzle");
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // Load puzzle dari file
    private void loadPuzzleFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("test/input"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                FileParser parser = new FileParser();
                currentBoard = parser.parseFile(selectedFile.getAbsolutePath());
                
                solutionSteps = null;
                solutionMoves = null;
                currentStep = 0;
                showExitAnimation = false;
                showingFinalState = false;
                
                updateBoardDisplay();
                solveButton.setEnabled(true);
                playPauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                stepButton.setEnabled(false);
                saveButton.setEnabled(false);
                
                statusLabel.setText("Puzzle loaded from " + selectedFile.getName());
                
                assignColorsToNewPieces();
                
            } catch (IOException ex) {
                showErrorDialog("File Error", 
                    "Could not read the file: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showErrorDialog("File Format Error", ex.getMessage());
            } catch (Exception ex) {
                showErrorDialog("Unexpected Error", 
                    "An unexpected error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            title, 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void openBoardEditor() {
        BoardEditor editor = new BoardEditor(this);
        editor.setVisible(true);
    }
    
    // Save ke file txt
    private void saveSolutionToFile() {
        if (solutionSteps == null || solutionSteps.size() <= 1) {
            JOptionPane.showMessageDialog(this,
                "No solution to save.",
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("test/output"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(selectedFile))) {
                writer.println("Rush Hour Puzzle Solution");
                writer.println("========================");
                writer.println("Algorithm: " + algorithmSelector.getSelectedItem());
                if (algorithmSelector.getSelectedIndex() != 0) {
                    writer.println("Heuristic: " + heuristicSelector.getSelectedItem());
                }
                writer.println("Total steps: " + (solutionSteps.size() - 1));
                writer.println("Nodes visited: " + nodesVisited);
                writer.println("Execution time: " + executionTime + " seconds");
                writer.println();
                
                writer.println("Initial Board:");
                writer.println(solutionSteps.get(0).toString());
                writer.println();
                
                for (int i = 1; i < solutionSteps.size(); i++) {
                    Move move = solutionMoves.get(i-1);
                    Board board = solutionSteps.get(i);
                    Piece piece = board.getPieces().get(move.getPieceIndex());
                    char pieceId = piece.getId();
                    
                    String direction = move.getDirection(piece.isVertical());
                    int distance = move.getDistance(piece.isVertical());
                    
                    writer.println("Step " + i + ": Move piece " + pieceId + 
                                  " " + distance + " cell(s) " + direction);
                    writer.println(board.toString());
                    writer.println();
                }
                
                statusLabel.setText("Solution saved to " + selectedFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving file: " + ex.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void solvePuzzle() {
    if (currentBoard == null) {
        return;
    }
    
    int algorithmIndex = algorithmSelector.getSelectedIndex();
    int heuristicIndex = heuristicSelector.getSelectedIndex() + 1;
    
    solutionSteps = null;
    solutionMoves = null;
    currentStep = 0;
    nodesVisited = 0;
    executionTime = 0;
    showExitAnimation = false;
    showingFinalState = false;
    
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    solveButton.setEnabled(false);
    statusLabel.setText("Solving puzzle...");
    
    new SwingWorker<List<Board>, Void>() {
        @Override
        protected List<Board> doInBackground() throws Exception {
            SolutionCollector collector = new SolutionCollector();
            
            long startTime = System.currentTimeMillis();
            
            switch (algorithmIndex) {
                case 0: // UCS
                    UCS ucs = new UCS(collector);
                    ucs.solve(currentBoard);
                    nodesVisited = ucs.getNodesVisited();
                    break;
                case 1: // GBFS
                    GBFS gbfs = new GBFS(heuristicIndex, collector);
                    gbfs.solve(currentBoard);
                    nodesVisited = gbfs.getNodesVisited();
                    break;
                case 2: // A*
                    AStar aStar = new AStar(heuristicIndex, collector);
                    aStar.solve(currentBoard);
                    nodesVisited = aStar.getNodesVisited();
                    break;
                case 3: // IDA*
                    IDAStar idaStar = new IDAStar(heuristicIndex, collector);
                    idaStar.solve(currentBoard);
                    nodesVisited = idaStar.getNodesVisited();
                    break;
            }
            
            long endTime = System.currentTimeMillis();
            executionTime = (endTime - startTime) / 1000.0;
            
            return collector.getSolutionSteps();
        }
            
            @Override
            protected void done() {
                try {
                    solutionSteps = get();
                    
                    if (solutionSteps != null && solutionSteps.size() > 1) {
                        solutionMoves = extractMoves(solutionSteps);
                        currentStep = 0;
                        updateBoardDisplay();
                        
                        playPauseButton.setEnabled(true);
                        stopButton.setEnabled(true);
                        stepButton.setEnabled(true);
                        saveButton.setEnabled(true);
                        
                        String algorithmName = (String) algorithmSelector.getSelectedItem();
                        statusLabel.setText("Solution found in " + (solutionSteps.size() - 1) + 
                                           " steps using " + algorithmName + 
                                           " | Nodes visited: " + nodesVisited + 
                                           " | Time: " + executionTime + " sec");
                    } else {
                        statusLabel.setText("No solution found!");
                        saveButton.setEnabled(false);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Gui.this, 
                        "Error solving puzzle: " + ex.getMessage(), 
                        "Solver Error", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error solving puzzle");
                    ex.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    solveButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    // buat play pause
    private void togglePlayPause() {
        if (solutionSteps == null || solutionSteps.size() <= 1) {
            return;
        }
        
        if (currentStep >= solutionSteps.size() - 1 && showExitAnimation) {
            showExitAnimation = false;
            showingFinalState = false;
            currentStep = 0;
            updateBoardDisplay();
        }
        
        if (animationRunning.get()) {
            animationTimer.stop();
            animationRunning.set(false);
            playPauseButton.setText("Play");
        } else {
            updateAnimationSpeed();
            animationTimer.start();
            animationRunning.set(true);
            playPauseButton.setText("Pause");
        }
    }
    
    // Memberhentikan animasi
    private void stopAnimation() {
        if (animationRunning.get()) {
            animationTimer.stop();
            animationRunning.set(false);
            playPauseButton.setText("Play");
        }
        
        showExitAnimation = false;
        showingFinalState = false;
        currentStep = 0;
        updateBoardDisplay();
        statusLabel.setText("Animation stopped");
    }
    
    private void stepForward() {
        if (solutionSteps != null && currentStep < solutionSteps.size() - 1) {
            currentStep++;
            
            if (currentStep == solutionSteps.size() - 1) {
                showingFinalState = true;
                showExitAnimation = false;
                updateBoardDisplay();
                
                Timer exitAnimTimer = new Timer(800, e -> {
                    ((Timer)e.getSource()).stop();
                    showingFinalState = false;
                    showExitAnimation = true;
                    updateBoardDisplay();
                });
                exitAnimTimer.setRepeats(false);
                exitAnimTimer.start();
            } else {
                showExitAnimation = false;
                showingFinalState = false;
                updateBoardDisplay();
            }
            
            statusLabel.setText("Step " + currentStep + " of " + (solutionSteps.size() - 1));
        }
    }
    
    // Kecepatan animasi dgn slider
    private void updateAnimationSpeed() {
        int delay = 1100 - (animationSpeedSlider.getValue() * 100);
        animationTimer.setDelay(delay);
    }
    
    private void updateBoardDisplay() {
        if (currentBoard == null) {
            return;
        }
        
        boardPanel.repaint();
    }
    
    // Gambar papan
    private void drawBoard(Graphics g) {
        if (currentBoard == null) {
            return;
        }
        
        Board boardToDraw = (solutionSteps != null && currentStep < solutionSteps.size()) ? 
                            solutionSteps.get(currentStep) : currentBoard;
        
        int rows = boardToDraw.getRows();
        int cols = boardToDraw.getCols();
        
        int cellSize = Math.min(
            (boardPanel.getWidth() - 120) / cols,
            (boardPanel.getHeight() - 120) / rows
        );
        
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int startX = (boardPanel.getWidth() - boardWidth) / 2;
        int startY = (boardPanel.getHeight() - boardHeight) / 2;
        
        g.setColor(Color.LIGHT_GRAY);
        for (int r = 0; r <= rows; r++) {
            g.drawLine(startX, startY + r * cellSize, startX + cols * cellSize, startY + r * cellSize);
        }
        for (int c = 0; c <= cols; c++) {
            g.drawLine(startX + c * cellSize, startY, startX + c * cellSize, startY + rows * cellSize);
        }
        
        int exitRow = boardToDraw.getExitRow();
        int exitCol = boardToDraw.getExitCol();
        
        g.setColor(Color.GREEN);
        if (exitRow == -1) {
            g.fillRect(startX + exitCol * cellSize, startY - cellSize, cellSize, cellSize);
            int[] xPoints = {startX + exitCol * cellSize + cellSize/2, startX + exitCol * cellSize + cellSize/4, startX + exitCol * cellSize + 3*cellSize/4};
            int[] yPoints = {startY - cellSize - 10, startY - cellSize, startY - cellSize};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitRow == rows) {
            g.fillRect(startX + exitCol * cellSize, startY + rows * cellSize, cellSize, cellSize);
            int[] xPoints = {startX + exitCol * cellSize + cellSize/2, startX + exitCol * cellSize + cellSize/4, startX + exitCol * cellSize + 3*cellSize/4};
            int[] yPoints = {startY + rows * cellSize + cellSize + 10, startY + rows * cellSize + cellSize, startY + rows * cellSize + cellSize};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitCol == -1) {
            g.fillRect(startX - cellSize, startY + exitRow * cellSize, cellSize, cellSize);
            int[] xPoints = {startX - cellSize - 10, startX - cellSize, startX - cellSize};
            int[] yPoints = {startY + exitRow * cellSize + cellSize/2, startY + exitRow * cellSize + cellSize/4, startY + exitRow * cellSize + 3*cellSize/4};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitCol == cols) {
            g.fillRect(startX + cols * cellSize, startY + exitRow * cellSize, cellSize, cellSize);
            int[] xPoints = {startX + cols * cellSize + cellSize + 10, startX + cols * cellSize + cellSize, startX + cols * cellSize + cellSize};
            int[] yPoints = {startY + exitRow * cellSize + cellSize/2, startY + exitRow * cellSize + cellSize/4, startY + exitRow * cellSize + 3*cellSize/4};
            g.fillPolygon(xPoints, yPoints, 3);
        }
        
        for (Piece piece : boardToDraw.getPieces()) {
            int row = piece.getRow();
            int col = piece.getCol();
            int length = piece.getLength();
            boolean isVertical = piece.isVertical();
            char id = piece.getId();
            
            if (showExitAnimation && !showingFinalState && id == 'P' && 
                solutionSteps != null && currentStep == solutionSteps.size() - 1) {
                System.out.println("Skipping primary piece (P) for vanishing effect!");
                continue;
            }
            
            Color pieceColor = getPieceColor(id);
            
            boolean isPrimary = (id == 'P');
            boolean isLastStep = (solutionSteps != null && currentStep == solutionSteps.size() - 1);
            
            boolean showExiting = isPrimary && isLastStep && showingFinalState;
            int exitingOffset = cellSize / 3;
            
            g.setColor(pieceColor);
            if (isVertical) {
                if (showExiting) {
                    if (exitCol == -1) {
                        g.fillRect(startX + col * cellSize - exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    } else if (exitCol == cols) {
                        g.fillRect(startX + col * cellSize + exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    } else {
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    }
                } else {
                    g.fillRect(startX + col * cellSize + 2, 
                            startY + row * cellSize + 2, 
                            cellSize - 4, 
                            length * cellSize - 4);
                }
            } else {
                if (showExiting) {
                    if (exitRow == -1) {
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize - exitingOffset + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitRow == rows) {
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + exitingOffset + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitCol == -1) {
                        g.fillRect(startX + col * cellSize - exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitCol == cols) {
                        g.fillRect(startX + col * cellSize + exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else {
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    }
                } else {
                    g.fillRect(startX + col * cellSize + 2, 
                            startY + row * cellSize + 2, 
                            length * cellSize - 4, 
                            cellSize - 4);
                }
            }
            
            g.setColor(Color.BLACK);
            Font font = new Font("Arial", Font.BOLD, cellSize / 3);
            g.setFont(font);
            
            String text = String.valueOf(id);
            FontMetrics metrics = g.getFontMetrics(font);
            int textX, textY;
            
            if (isVertical) {
                if (showExiting) {
                    if (exitCol == -1) {
                        textX = startX + col * cellSize - exitingOffset + (cellSize - metrics.stringWidth(text)) / 2;
                    } else if (exitCol == cols) {
                        textX = startX + col * cellSize + exitingOffset + (cellSize - metrics.stringWidth(text)) / 2;
                    } else {
                        textX = startX + col * cellSize + (cellSize - metrics.stringWidth(text)) / 2;
                    }
                } else {
                    textX = startX + col * cellSize + (cellSize - metrics.stringWidth(text)) / 2;
                }
                textY = startY + row * cellSize + cellSize / 2 + metrics.getAscent() / 2;
            } else {
                textX = startX + col * cellSize + (cellSize - metrics.stringWidth(text)) / 2;
                if (showExiting) {
                    if (exitRow == -1) {
                        textY = startY + row * cellSize - exitingOffset + (cellSize + metrics.getAscent()) / 2;
                    } else if (exitRow == rows) {
                        textY = startY + row * cellSize + exitingOffset + (cellSize + metrics.getAscent()) / 2;
                    } else if (exitCol == -1) {
                        textY = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2;
                    } else if (exitCol == cols) {
                        textY = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2;
                    } else {
                        textY = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2;
                    }
                } else {
                    textY = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2;
                }
            }
            
            g.drawString(text, textX, textY);
        }
        
        if (solutionSteps != null && currentStep > 0 && currentStep < solutionMoves.size() + 1 && !showExitAnimation) {
            Move lastMove = solutionMoves.get(currentStep - 1);
            int pieceIndex = lastMove.getPieceIndex();
            Piece movedPiece = boardToDraw.getPieces().get(pieceIndex);
            
            int row = movedPiece.getRow();
            int col = movedPiece.getCol();
            int length = movedPiece.getLength();
            boolean isVertical = movedPiece.isVertical();
            
            g.setColor(Color.YELLOW);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));
            
            if (isVertical) {
                g2d.drawRect(startX + col * cellSize + 1, startY + row * cellSize + 1, 
                            cellSize - 2, length * cellSize - 2);
            } else {
                g2d.drawRect(startX + col * cellSize + 1, startY + row * cellSize + 1, 
                            length * cellSize - 2, cellSize - 2);
            }
        }
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
    
    private void assignColorsToNewPieces() {
        pieceColors.clear();
        
        for (Piece piece : currentBoard.getPieces()) {
            char id = piece.getId();
            
            if (id == 'P') {
                pieceColors.put(id, Color.RED);
            } else {
                pieceColors.put(id, generatePastelColor());
            }
        }
    }
    
    private List<Move> extractMoves(List<Board> steps) {
        List<Move> moves = new ArrayList<>();
        
        if (steps == null || steps.size() < 2) {
            return moves;
        }
        
        for (int i = 1; i < steps.size(); i++) {
            Board prev = steps.get(i-1);
            Board curr = steps.get(i);
            
            List<Piece> prevPieces = prev.getPieces();
            List<Piece> currPieces = curr.getPieces();
            
            for (int j = 0; j < prevPieces.size(); j++) {
                Piece prevPiece = prevPieces.get(j);
                Piece currPiece = currPieces.get(j);
                
                if (prevPiece.getRow() != currPiece.getRow() || 
                    prevPiece.getCol() != currPiece.getCol()) {
                    Move move = new Move(
                        j, 
                        prevPiece.getRow(), 
                        prevPiece.getCol(), 
                        currPiece.getRow(), 
                        currPiece.getCol()
                    );
                    moves.add(move);
                    break;
                }
            }
        }
        
        return moves;
    }
    
    public static class SolutionCollector {
        private List<Board> solutionSteps = new ArrayList<>();
        
        public void addStep(Board board) {
            solutionSteps.add(new Board(board));
        }
        
        public List<Board> getSolutionSteps() {
            return solutionSteps;
        }
    }
    
    // Gui starter
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new Gui());
    }
}