package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
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
    // GUI components
    private JPanel boardPanel;
    private JButton loadButton;
    private JButton solveButton;
    private JButton createBoardButton;
    private JButton saveButton; // New save button
    private JComboBox<String> algorithmSelector;
    private JComboBox<String> heuristicSelector;
    private JLabel statusLabel;
    private JSlider animationSpeedSlider;
    private JButton playPauseButton;
    private JButton stopButton;
    private JButton stepButton;
    
    // Game state
    private Board currentBoard;
    private List<Board> solutionSteps;
    private List<Move> solutionMoves;
    private int currentStep = 0;
    private Timer animationTimer;
    private AtomicBoolean animationRunning = new AtomicBoolean(false);
    
    // Animation states for vanishing effect
    private boolean showExitAnimation = false;
    private boolean showingFinalState = false;
    
    // Performance metrics
    private int nodesVisited = 0;
    private double executionTime = 0;
    
    // Colors for different pieces
    private Map<Character, Color> pieceColors = new HashMap<>();
    
    /**
     * Constructor - initialize the GUI
     */
    public Gui() {
        // Set up the JFrame
        super("Rush Hour Puzzle Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        // Create components
        createTopPanel();
        createBoardPanel();
        createBottomPanel();
        
        // Initialize animation timer
        animationTimer = new Timer(500, e -> {
            if (currentStep < solutionSteps.size() - 1) {
                currentStep++;
                
                // Check if we've reached the final step
                if (currentStep == solutionSteps.size() - 1) {
                    showingFinalState = true;
                    showExitAnimation = false;
                    updateBoardDisplay();
                    
                    // After a delay, show the exit animation where car vanishes
                    Timer exitAnimationTimer = new Timer(800, evt -> {
                        ((Timer)evt.getSource()).stop();
                        showingFinalState = false;
                        showExitAnimation = true;
                        updateBoardDisplay();
                        
                        // After another delay, show completion message
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
        
        // Display the window
        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }
    
    /**
     * Create the top control panel with file operations and algorithm selection
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // File operations
        JPanel filePanel = new JPanel();
        loadButton = new JButton("Load Puzzle");
        loadButton.addActionListener(e -> loadPuzzleFromFile());
        
        createBoardButton = new JButton("Create Puzzle");
        createBoardButton.addActionListener(e -> openBoardEditor());
        
        saveButton = new JButton("Save Solution");
        saveButton.addActionListener(e -> saveSolutionToFile());
        saveButton.setEnabled(false); // Initially disabled until we have a solution
        
        filePanel.add(loadButton);
        filePanel.add(createBoardButton);
        filePanel.add(saveButton);
        
        // Algorithm selection
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
            // Enable heuristic selection only for algorithms that use it
            int selectedIndex = algorithmSelector.getSelectedIndex();
            heuristicSelector.setEnabled(selectedIndex != 0); // UCS doesn't use heuristics
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
    
    /**
     * Create the central board panel for displaying the puzzle
     */
    private void createBoardPanel() {
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        // Increase the preferred size to ensure there's room for the exits
        boardPanel.setPreferredSize(new Dimension(600, 600));
        boardPanel.setBackground(Color.WHITE);
        
        add(boardPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create the bottom panel with animation controls and status info
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Animation controls
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
        
        // Status label
        statusLabel = new JLabel("Ready to load or create a puzzle");
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load a puzzle from a file
     */
    private void loadPuzzleFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("test/input")); // Set initial directory to test folder
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                FileParser parser = new FileParser();
                currentBoard = parser.parseFile(selectedFile.getAbsolutePath());
                
                // Reset animation state
                solutionSteps = null;
                solutionMoves = null;
                currentStep = 0;
                showExitAnimation = false;
                showingFinalState = false;
                
                // Update UI
                updateBoardDisplay();
                solveButton.setEnabled(true);
                playPauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                stepButton.setEnabled(false);
                saveButton.setEnabled(false);
                
                statusLabel.setText("Puzzle loaded from " + selectedFile.getName());
                
                // Assign colors to pieces
                assignColorsToNewPieces();
                
            } catch (IOException ex) {
                // File read error
                showErrorDialog("File Error", 
                    "Could not read the file: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                // File format error
                showErrorDialog("File Format Error", ex.getMessage());
            } catch (Exception ex) {
                // Unexpected error
                showErrorDialog("Unexpected Error", 
                    "An unexpected error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Display an error dialog with a detailed message
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            title, 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Open a board editor to create a puzzle
     */
    private void openBoardEditor() {
        BoardEditor editor = new BoardEditor(this);
        editor.setVisible(true);
    }
    
    /**
     * Save the solution steps to a text file
     */
    private void saveSolutionToFile() {
        if (solutionSteps == null || solutionSteps.size() <= 1) {
            JOptionPane.showMessageDialog(this,
                "No solution to save.",
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("test/output")); // Set initial directory to output folder
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Add .txt extension if not present
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(selectedFile))) {
                // Write header
                writer.println("Rush Hour Puzzle Solution");
                writer.println("========================");
                writer.println("Algorithm: " + algorithmSelector.getSelectedItem());
                if (algorithmSelector.getSelectedIndex() != 0) { // Not UCS
                    writer.println("Heuristic: " + heuristicSelector.getSelectedItem());
                }
                writer.println("Total steps: " + (solutionSteps.size() - 1));
                writer.println("Nodes visited: " + nodesVisited);
                writer.println("Execution time: " + executionTime + " seconds");
                writer.println();
                
                // Write initial state
                writer.println("Initial Board:");
                writer.println(solutionSteps.get(0).toString());
                writer.println();
                
                // Write each step
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
    
    /**
     * Solve the puzzle using the selected algorithm
     */
    private void solvePuzzle() {
    if (currentBoard == null) {
        return;
    }
    
    // Get selected algorithm and heuristic
    int algorithmIndex = algorithmSelector.getSelectedIndex();
    int heuristicIndex = heuristicSelector.getSelectedIndex() + 1; // 1-based for the algorithm classes
    
    // Reset solution data
    solutionSteps = null;
    solutionMoves = null;
    currentStep = 0;
    nodesVisited = 0;
    executionTime = 0;
    showExitAnimation = false;
    showingFinalState = false;
    
    // Set cursor to wait
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    solveButton.setEnabled(false);
    statusLabel.setText("Solving puzzle...");
    
    // Run solver in background thread
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
                        
                        // Enable animation controls
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
                    // Reset cursor
                    setCursor(Cursor.getDefaultCursor());
                    solveButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    /**
     * Toggle between play and pause for animation
     */
    private void togglePlayPause() {
        if (solutionSteps == null || solutionSteps.size() <= 1) {
            return;
        }
        
        if (currentStep >= solutionSteps.size() - 1 && showExitAnimation) {
            // If we're at the end with exit animation, restart from beginning
            showExitAnimation = false;
            showingFinalState = false;
            currentStep = 0;
            updateBoardDisplay();
        }
        
        if (animationRunning.get()) {
            // Pause
            animationTimer.stop();
            animationRunning.set(false);
            playPauseButton.setText("Play");
        } else {
            // Play
            updateAnimationSpeed();
            animationTimer.start();
            animationRunning.set(true);
            playPauseButton.setText("Pause");
        }
    }
    
    /**
     * Stop the animation and reset to the beginning
     */
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
    
    /**
     * Step forward one move in the solution
     */
    private void stepForward() {
        if (solutionSteps != null && currentStep < solutionSteps.size() - 1) {
            currentStep++;
            
            // Check if we've reached the last step
            if (currentStep == solutionSteps.size() - 1) {
                // First show the car at the exit
                showingFinalState = true;
                showExitAnimation = false;
                updateBoardDisplay();
                
                // After a delay, show the exit animation
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
    
    /**
     * Update the animation speed based on the slider value
     */
    private void updateAnimationSpeed() {
        // Convert slider value (1-10) to delay in milliseconds (1000ms to 100ms)
        int delay = 1100 - (animationSpeedSlider.getValue() * 100);
        animationTimer.setDelay(delay);
    }
    
    /**
     * Update the board display with the current board state
     */
    private void updateBoardDisplay() {
        if (currentBoard == null) {
            return;
        }
        
        boardPanel.repaint();
    }
    
    /**
     * Draw the board on the graphics context
     */
    private void drawBoard(Graphics g) {
        if (currentBoard == null) {
            return;
        }
        
        // Determine which board to draw
        Board boardToDraw = (solutionSteps != null && currentStep < solutionSteps.size()) ? 
                            solutionSteps.get(currentStep) : currentBoard;
        
        // Get board dimensions
        int rows = boardToDraw.getRows();
        int cols = boardToDraw.getCols();
        
        // Calculate cell size with extra padding to ensure exits are visible
        int cellSize = Math.min(
            (boardPanel.getWidth() - 120) / cols,  // More horizontal padding (120px)
            (boardPanel.getHeight() - 120) / rows  // More vertical padding (120px)
        );
        
        // Calculate board position (centered)
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int startX = (boardPanel.getWidth() - boardWidth) / 2;
        int startY = (boardPanel.getHeight() - boardHeight) / 2;
        
        // Draw grid
        g.setColor(Color.LIGHT_GRAY);
        for (int r = 0; r <= rows; r++) {
            g.drawLine(startX, startY + r * cellSize, startX + cols * cellSize, startY + r * cellSize);
        }
        for (int c = 0; c <= cols; c++) {
            g.drawLine(startX + c * cellSize, startY, startX + c * cellSize, startY + rows * cellSize);
        }
        
        // Draw exit marker with enhanced visibility
        int exitRow = boardToDraw.getExitRow();
        int exitCol = boardToDraw.getExitCol();
        
        g.setColor(Color.GREEN);
        if (exitRow == -1) { // Top exit
            // Make top exit more visible by making it larger
            g.fillRect(startX + exitCol * cellSize, startY - cellSize, cellSize, cellSize);
            // Add a small triangle pointing up
            int[] xPoints = {startX + exitCol * cellSize + cellSize/2, startX + exitCol * cellSize + cellSize/4, startX + exitCol * cellSize + 3*cellSize/4};
            int[] yPoints = {startY - cellSize - 10, startY - cellSize, startY - cellSize};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitRow == rows) { // Bottom exit
            // Make bottom exit more visible by making it larger
            g.fillRect(startX + exitCol * cellSize, startY + rows * cellSize, cellSize, cellSize);
            // Add a small triangle pointing down
            int[] xPoints = {startX + exitCol * cellSize + cellSize/2, startX + exitCol * cellSize + cellSize/4, startX + exitCol * cellSize + 3*cellSize/4};
            int[] yPoints = {startY + rows * cellSize + cellSize + 10, startY + rows * cellSize + cellSize, startY + rows * cellSize + cellSize};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitCol == -1) { // Left exit
            // Make left exit more visible by making it larger
            g.fillRect(startX - cellSize, startY + exitRow * cellSize, cellSize, cellSize);
            // Add a small triangle pointing left
            int[] xPoints = {startX - cellSize - 10, startX - cellSize, startX - cellSize};
            int[] yPoints = {startY + exitRow * cellSize + cellSize/2, startY + exitRow * cellSize + cellSize/4, startY + exitRow * cellSize + 3*cellSize/4};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (exitCol == cols) { // Right exit
            // Make right exit more visible by making it larger
            g.fillRect(startX + cols * cellSize, startY + exitRow * cellSize, cellSize, cellSize);
            // Add a small triangle pointing right
            int[] xPoints = {startX + cols * cellSize + cellSize + 10, startX + cols * cellSize + cellSize, startX + cols * cellSize + cellSize};
            int[] yPoints = {startY + exitRow * cellSize + cellSize/2, startY + exitRow * cellSize + cellSize/4, startY + exitRow * cellSize + 3*cellSize/4};
            g.fillPolygon(xPoints, yPoints, 3);
        }
        
        // Draw pieces
        for (Piece piece : boardToDraw.getPieces()) {
            int row = piece.getRow();
            int col = piece.getCol();
            int length = piece.getLength();
            boolean isVertical = piece.isVertical();
            char id = piece.getId();
            
            // Skip drawing the primary piece only if showing exit animation and not showing final state
            if (showExitAnimation && !showingFinalState && id == 'P' && 
                solutionSteps != null && currentStep == solutionSteps.size() - 1) {
                System.out.println("Skipping primary piece (P) for vanishing effect!");
                continue; // Skip the primary piece for vanishing effect
            }
            
            // Get color for the piece
            Color pieceColor = getPieceColor(id);
            
            // Check if this is the primary piece and if we're at the last step of the solution
            boolean isPrimary = (id == 'P');
            boolean isLastStep = (solutionSteps != null && currentStep == solutionSteps.size() - 1);
            
            // Determine if the primary piece should appear to be exiting (only during final state, not during vanish)
            boolean showExiting = isPrimary && isLastStep && showingFinalState;
            int exitingOffset = cellSize / 3;  // Distance to move the piece toward the exit
            
            // Draw the piece, possibly with exiting effect
            g.setColor(pieceColor);
            if (isVertical) {
                if (showExiting) {
                    // For vertical pieces, adjust based on exit direction
                    if (exitCol == -1) {
                        // Left exit - shift piece left and reduce visible length
                        g.fillRect(startX + col * cellSize - exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    } else if (exitCol == cols) {
                        // Right exit - shift piece right and reduce visible length
                        g.fillRect(startX + col * cellSize + exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    } else {
                        // Normal drawing
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + 2, 
                                cellSize - 4, 
                                length * cellSize - 4);
                    }
                } else {
                    // Normal drawing
                    g.fillRect(startX + col * cellSize + 2, 
                            startY + row * cellSize + 2, 
                            cellSize - 4, 
                            length * cellSize - 4);
                }
            } else {
                if (showExiting) {
                    // For horizontal pieces, adjust based on exit direction
                    if (exitRow == -1) {
                        // Top exit - shift piece up and reduce visible length
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize - exitingOffset + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitRow == rows) {
                        // Bottom exit - shift piece down and reduce visible length
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + exitingOffset + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitCol == -1) {
                        // Left exit - shift piece left
                        g.fillRect(startX + col * cellSize - exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else if (exitCol == cols) {
                        // Right exit - shift piece right
                        g.fillRect(startX + col * cellSize + exitingOffset + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    } else {
                        // Normal drawing
                        g.fillRect(startX + col * cellSize + 2, 
                                startY + row * cellSize + 2, 
                                length * cellSize - 4, 
                                cellSize - 4);
                    }
                } else {
                    // Normal drawing
                    g.fillRect(startX + col * cellSize + 2, 
                            startY + row * cellSize + 2, 
                            length * cellSize - 4, 
                            cellSize - 4);
                }
            }
            
            // Draw piece ID
            g.setColor(Color.BLACK);
            Font font = new Font("Arial", Font.BOLD, cellSize / 3);
            g.setFont(font);
            
            String text = String.valueOf(id);
            FontMetrics metrics = g.getFontMetrics(font);
            int textX, textY;
            
            // Position the text, accounting for exit animation
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
        
        // Highlight moved piece if we're showing a solution
        if (solutionSteps != null && currentStep > 0 && currentStep < solutionMoves.size() + 1 && !showExitAnimation) {
            Move lastMove = solutionMoves.get(currentStep - 1);
            int pieceIndex = lastMove.getPieceIndex();
            Piece movedPiece = boardToDraw.getPieces().get(pieceIndex);
            
            int row = movedPiece.getRow();
            int col = movedPiece.getCol();
            int length = movedPiece.getLength();
            boolean isVertical = movedPiece.isVertical();
            
            // Draw highlight border around the moved piece
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
    
    /**
     * Get the color for a piece
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
     * Assign colors to all pieces in the board
     */
    private void assignColorsToNewPieces() {
        pieceColors.clear(); // Clear previous colors
        
        // Assign a color to each piece
        for (Piece piece : currentBoard.getPieces()) {
            char id = piece.getId();
            
            // Primary piece is always red
            if (id == 'P') {
                pieceColors.put(id, Color.RED);
            } else {
                pieceColors.put(id, generatePastelColor());
            }
        }
    }
    
    /**
     * Extract moves from a list of boards
     */
    private List<Move> extractMoves(List<Board> steps) {
        List<Move> moves = new ArrayList<>();
        
        if (steps == null || steps.size() < 2) {
            return moves;
        }
        
        for (int i = 1; i < steps.size(); i++) {
            Board prev = steps.get(i-1);
            Board curr = steps.get(i);
            
            // Find the moved piece
            List<Piece> prevPieces = prev.getPieces();
            List<Piece> currPieces = curr.getPieces();
            
            for (int j = 0; j < prevPieces.size(); j++) {
                Piece prevPiece = prevPieces.get(j);
                Piece currPiece = currPieces.get(j);
                
                if (prevPiece.getRow() != currPiece.getRow() || 
                    prevPiece.getCol() != currPiece.getCol()) {
                    // Found the moved piece
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
    
    /**
     * Inner class to collect the solution steps from the algorithms
     */
    public static class SolutionCollector {
        private List<Board> solutionSteps = new ArrayList<>();
        
        public void addStep(Board board) {
            solutionSteps.add(new Board(board)); // Make a copy
        }
        
        public List<Board> getSolutionSteps() {
            return solutionSteps;
        }
    }
    
    /**
     * UCS implementation that collects solution steps for GUI
     */
    private static class UCSForGUI extends UCS {
        private SolutionCollector collector;
        private int nodesVisited;
        
        public UCSForGUI(SolutionCollector collector) {
            super(collector);
            this.collector = collector;
        }
        
        public int getNodesVisited() {
            return nodesVisited;
        }
        
        // You'll need to modify UCS to track nodes visited and pass the collector
    }
    
    /**
     * GBFS implementation that collects solution steps for GUI
     */
    private static class GBFSForGUI extends GBFS {
        private SolutionCollector collector;
        private int nodesVisited;
        
        public GBFSForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
            this.collector = collector;
        }
        
        public int getNodesVisited() {
            return nodesVisited;
        }
        
        // You'll need to modify GBFS to track nodes visited and pass the collector
    }
    
    /**
     * A* implementation that collects solution steps for GUI
     */
    private static class AStarForGUI extends AStar {
        private SolutionCollector collector;
        private int nodesVisited;
        
        public AStarForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
            this.collector = collector;
        }
        
        public int getNodesVisited() {
            return nodesVisited;
        }
        
        // You'll need to modify AStar to track nodes visited and pass the collector
    }
    
    /**
     * IDA* implementation that collects solution steps for GUI
     */
    private static class IDAStarForGUI extends IDAStar {
        private SolutionCollector collector;
        private int nodesVisited;
        
        public IDAStarForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
            this.collector = collector;
        }
        
        public int getNodesVisited() {
            return nodesVisited;
        }
        
        // You'll need to modify IDAStar to track nodes visited and pass the collector
    }
    
    /**
     * Main method to start the GUI
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and display the GUI
        SwingUtilities.invokeLater(() -> new Gui());
    }
}