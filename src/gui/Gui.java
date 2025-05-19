package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
    private boolean showExitAnimation = false;
    private boolean showingFinalState = false;
    
    // Colors for different pieces
    private Map<Character, Color> pieceColors = new HashMap<>();

    // Performance statistic
    private JLabel performanceLabel;
    private int nodesVisited = 0;
    private long executionTime = 0;
    
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
                    System.out.println("REACHED FINAL STEP - SHOWING CAR AT EXIT");
                    showingFinalState = true;
                    showExitAnimation = false;
                    updateBoardDisplay();
                    
                    // After a delay, show the exit animation where car vanishes
                    Timer exitAnimationTimer = new Timer(800, evt -> {
                        ((Timer)evt.getSource()).stop();
                        System.out.println("NOW ACTIVATING EXIT ANIMATION");
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
        
        filePanel.add(loadButton);
        filePanel.add(createBoardButton);
        
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
        boardPanel.setPreferredSize(new Dimension(500, 500));
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
        JLabel speedLabel = new JLabel("Animation Speed:");
        animationSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setMajorTickSpacing(1);
        animationSpeedSlider.addChangeListener(e -> {
            if (!animationSpeedSlider.getValueIsAdjusting()) {
                updateAnimationSpeed();
            }
        });
        
        playPauseButton = new JButton("Play");
        playPauseButton.addActionListener(e -> togglePlayPause());
        playPauseButton.setEnabled(false);
        
        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> stepForward());
        stepButton.setEnabled(false);
        
        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopAnimation());
        stopButton.setEnabled(false);
        
        controlPanel.add(speedLabel);
        controlPanel.add(animationSpeedSlider);
        controlPanel.add(playPauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(stopButton);
        
        // Performance metrics panel
        JPanel performancePanel = new JPanel();
        performanceLabel = new JLabel("Nodes visited: - | Execution time: - ms");
        performancePanel.add(performanceLabel);
        
        // Status label
        statusLabel = new JLabel("Ready to load or create a puzzle");
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        
        // Add both panels to the bottom
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(performancePanel);
        infoPanel.add(statusPanel);
        
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load a puzzle from a file
     */
    private void loadPuzzleFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("test")); // Set initial directory to test folder
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
                
                // Reset performance metrics
                performanceLabel.setText("Nodes visited: - | Execution time: - ms");
                
                statusLabel.setText("Puzzle loaded from " + selectedFile.getName());
                
                // Assign colors to pieces
                assignColorsToNewPieces();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading file: " + ex.getMessage(), 
                    "Load Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Open a board editor to create a puzzle
     */
    private void openBoardEditor() {
        BoardEditor editor = new BoardEditor(this);
        editor.setVisible(true);
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
        
        // Set cursor to wait
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        solveButton.setEnabled(false);
        statusLabel.setText("Solving puzzle...");
        
        // Run solver in background thread
        new SwingWorker<SolverResult, Void>() {
            @Override
            protected SolverResult doInBackground() throws Exception {
                SolutionCollector collector = new SolutionCollector();
                SolverResult result = new SolverResult();
                
                long startTime = System.currentTimeMillis();
                
                switch (algorithmIndex) {
                    case 0: // UCS
                        UCS ucs = new UCSForGUI(collector);
                        ucs.solve(currentBoard);
                        result.nodesVisited = ucs.getNodesVisited();
                        break;
                    case 1: // GBFS
                        GBFS gbfs = new GBFSForGUI(heuristicIndex, collector);
                        gbfs.solve(currentBoard);
                        result.nodesVisited = gbfs.getNodesVisited();
                        break;
                    case 2: // A*
                        AStar aStar = new AStarForGUI(heuristicIndex, collector);
                        aStar.solve(currentBoard);
                        result.nodesVisited = aStar.getNodesVisited();
                        break;
                    case 3: // IDA*
                        IDAStar idaStar = new IDAStarForGUI(heuristicIndex, collector);
                        idaStar.solve(currentBoard);
                        result.nodesVisited = idaStar.getNodesVisited();
                        break;
                }
                
                result.executionTime = System.currentTimeMillis() - startTime;
                result.solutionSteps = collector.getSolutionSteps();
                
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    SolverResult result = get();
                    solutionSteps = result.solutionSteps;
                    solutionMoves = extractMoves(solutionSteps);
                    
                    // Store and display performance metrics
                    nodesVisited = result.nodesVisited;
                    executionTime = result.executionTime;
                    performanceLabel.setText(String.format(
                        "Nodes visited: %d | Execution time: %d ms", 
                        nodesVisited, 
                        executionTime
                    ));
                    
                    if (solutionSteps != null && !solutionSteps.isEmpty()) {
                        currentStep = 0;
                        showExitAnimation = false;
                        showingFinalState = false;
                        updateBoardDisplay();
                        
                        // Enable animation controls
                        playPauseButton.setEnabled(true);
                        stopButton.setEnabled(true);
                        stepButton.setEnabled(true);
                        
                        statusLabel.setText("Solution found in " + (solutionSteps.size() - 1) + " steps!");
                    } else {
                        statusLabel.setText("No solution found!");
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
        if (solutionSteps == null) {
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
        
        // If we have a solution, show the current step
        if (solutionSteps != null && currentStep < solutionSteps.size()) {
            boardPanel.repaint();
        } else {
            // Otherwise show the current board
            boardPanel.repaint();
        }
    }
    
    /**
     * Draw the board on the graphics context
     */
    private void drawBoard(Graphics g) {
        if (showExitAnimation) {
            System.out.println("Exit animation is active!");
        }
        if (showingFinalState) {
            System.out.println("Showing final state with car at exit!");
        }

        if (currentBoard == null) {
            return;
        }
        
        // Determine which board to draw
        Board boardToDraw = (solutionSteps != null && currentStep < solutionSteps.size()) ? 
                            solutionSteps.get(currentStep) : currentBoard;
        
        // Get board dimensions
        int rows = boardToDraw.getRows();
        int cols = boardToDraw.getCols();
        
        // Calculate cell size
        int cellSize = Math.min(boardPanel.getWidth() / cols, boardPanel.getHeight() / rows);
        
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
        
        // Draw exit marker
        int exitRow = boardToDraw.getExitRow();
        int exitCol = boardToDraw.getExitCol();
        
        g.setColor(Color.GREEN);
        if (exitRow == -1) { // Top exit
            g.fillRect(startX + exitCol * cellSize, startY - cellSize/2, cellSize, cellSize/2);
        } else if (exitRow == rows) { // Bottom exit
            g.fillRect(startX + exitCol * cellSize, startY + rows * cellSize, cellSize, cellSize/2);
        } else if (exitCol == -1) { // Left exit
            g.fillRect(startX - cellSize/2, startY + exitRow * cellSize, cellSize/2, cellSize);
        } else if (exitCol == cols) { // Right exit
            g.fillRect(startX + cols * cellSize, startY + exitRow * cellSize, cellSize/2, cellSize);
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
            
            // Draw the piece
            g.setColor(pieceColor);
            if (isVertical) {
                g.fillRect(startX + col * cellSize + 2, startY + row * cellSize + 2, 
                        cellSize - 4, length * cellSize - 4);
            } else {
                g.fillRect(startX + col * cellSize + 2, startY + row * cellSize + 2, 
                        length * cellSize - 4, cellSize - 4);
            }
            
            // Draw piece ID
            g.setColor(Color.BLACK);
            Font font = new Font("Arial", Font.BOLD, cellSize / 3);
            g.setFont(font);
            
            String text = String.valueOf(id);
            FontMetrics metrics = g.getFontMetrics(font);
            int textX, textY;
            
            if (isVertical) {
                textX = startX + col * cellSize + (cellSize - metrics.stringWidth(text)) / 2;
                textY = startY + row * cellSize + cellSize / 2;
            } else {
                textX = startX + col * cellSize + (cellSize - metrics.stringWidth(text)) / 2;
                textY = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2;
            }
            
            g.drawString(text, textX, textY);
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
     * Internal class to hold solver results
     */
    private static class SolverResult {
        List<Board> solutionSteps;
        int nodesVisited = 0;
        long executionTime = 0;
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
        public UCSForGUI(SolutionCollector collector) {
            super(collector);
        }
    }
    
    /**
     * GBFS implementation that collects solution steps for GUI
     */
    private static class GBFSForGUI extends GBFS {
        public GBFSForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
        }
    }
    
    /**
     * A* implementation that collects solution steps for GUI
     */
    private static class AStarForGUI extends AStar {
        public AStarForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
        }
    }
    
    /**
     * IDA* implementation that collects solution steps for GUI
     */
    private static class IDAStarForGUI extends IDAStar {
        public IDAStarForGUI(int heuristicType, SolutionCollector collector) {
            super(heuristicType, collector);
        }
    }
}