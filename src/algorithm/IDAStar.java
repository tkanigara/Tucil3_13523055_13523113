package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

/**
 * Iterative Deepening A* (IDA*) algorithm implementation for Rush Hour puzzles.
 * Combines the memory efficiency of iterative deepening with the performance of A*.
 */
public class IDAStar {
    private int nodesVisited = 0;
    private int heuristicType;
    private List<Node> solutionPath;
    private gui.Gui.SolutionCollector collector;
    
    // Heuristic types - same as GBFS and A* for consistency
    public static final int BLOCKING_PIECES = 1;
    public static final int MANHATTAN_DISTANCE = 2;
    public static final int COMBINED = 3;
    
    /**
     * Constructor with heuristic selection
     * 
     * @param heuristicType The type of heuristic to use
     */
    public IDAStar(int heuristicType, gui.Gui.SolutionCollector collector) {
        this.heuristicType = heuristicType;
        this.collector = collector;
    }
    
    /**
     * Default constructor - uses blocking pieces heuristic
     */
    public IDAStar() {
        this(BLOCKING_PIECES, null);
    }
    
    /**
     * Solve the Rush Hour puzzle using IDA* Search.
     * 
     * @param initialBoard The initial board state
     */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        // Print which heuristic is being used
        System.out.println("Using heuristic: " + getHeuristicName());
        
        // Print initial board
        BoardPrinter.printInitialBoard(initialBoard);
        
        // Calculate initial heuristic
        int initialHeuristic = calculateHeuristic(initialBoard);
        Node root = new Node(initialBoard, null, null, 0, initialHeuristic);
        
        // Start with threshold = initial heuristic
        int threshold = initialHeuristic;
        
        boolean solved = false;
        int solutionCost = 0;
        
        // Keep track of all visited states
        Set<String> globalVisited = new HashSet<>();
        
        while (!solved) {
            // For each iteration, we need a fresh set of visited states
            Set<String> visited = new HashSet<>();
            visited.add(initialBoard.toString());
            globalVisited.add(initialBoard.toString());
            nodesVisited++;
            
            // Track next threshold
            int nextThreshold = Integer.MAX_VALUE;
            
            // Store solution path when found
            solutionPath = new ArrayList<>();
            
            // Perform depth-first search with current threshold
            DFSResult result = depthFirstSearch(root, 0, threshold, visited, globalVisited);
            
            if (result.solved) {
                solved = true;
                solutionCost = result.cost;
            } else {
                // If not solved, update threshold to the next minimum f-value
                threshold = result.nextThreshold;
                
                // If threshold is too large, no solution exists
                if (threshold == Integer.MAX_VALUE) {
                    break;
                }
                
                System.out.println("Increasing threshold to: " + threshold);
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solved && collector != null) {
            // Add initial board
            collector.addStep(initialBoard);
            
            // Add each board state in the solution path
            for (Node node : solutionPath) {
                collector.addStep(node.board);
            }
        }

        if (solved) {
            // Print the solution path
            Collections.reverse(solutionPath);
            printSolution(solutionPath);
            
            // Print statistics
            System.out.println("Jumlah langkah: " + solutionCost);
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        } else {
            System.out.println("Tidak ada solusi yang ditemukan!");
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        }
    }
    
    /**
     * Recursive depth-first search with a threshold limit.
     * 
     * @param node Current search node
     * @param cost Current path cost (g)
     * @param threshold Current f-cost threshold
     * @param visited Set of visited states in this iteration
     * @param globalVisited Set of all visited states across all iterations
     * @return Result of the search
     */
    private DFSResult depthFirstSearch(Node node, int cost, int threshold, 
                                      Set<String> visited, Set<String> globalVisited) {
        // Calculate f = g + h
        int f = cost + node.heuristic;
        
        // If f exceeds threshold, return the f value
        if (f > threshold) {
            return new DFSResult(false, 0, f);
        }
        
        // Check if we've reached the goal
        if (node.board.isSolved()) {
            // Save the solution path
            Node current = node;
            while (current.parent != null) {
                solutionPath.add(current);
                current = current.parent;
            }
            
            return new DFSResult(true, cost, f);
        }
        
        // Track the minimum f-value of all children
        int min = Integer.MAX_VALUE;
        
        // Generate all possible next states
        List<Board> nextStates = node.board.getNextStates();
        
        for (Board nextBoard : nextStates) {
            String boardString = nextBoard.toString();
            
            // Skip if we've already visited this state in this iteration
            if (visited.contains(boardString)) {
                continue;
            }
            
            // Track if this is a state we've seen before in any iteration
            boolean isNewGlobal = !globalVisited.contains(boardString);
            
            // Mark as visited
            visited.add(boardString);
            globalVisited.add(boardString);
            nodesVisited++;
            
            // Find which piece was moved
            Move move = findMove(node.board, nextBoard);
            
            // Calculate heuristic
            int heuristic = calculateHeuristic(nextBoard);
            
            // Create new node
            Node childNode = new Node(nextBoard, node, move, cost + 1, heuristic);
            
            // Recursively search from this node
            DFSResult result = depthFirstSearch(childNode, cost + 1, threshold, visited, globalVisited);
            
            // If solution found, propagate it up
            if (result.solved) {
                return result;
            }
            
            // Otherwise, update minimum f-value
            min = Math.min(min, result.nextThreshold);
            
            // Remove from visited to allow other paths to explore this state
            visited.remove(boardString);
            
            // If this was a new state globally, but not the solution,
            // we can remove it from the global visited set to allow future iterations
            // to potentially explore it with a different path
            if (isNewGlobal && !result.solved) {
                globalVisited.remove(boardString);
            }
        }
        
        // Return the minimum f-value for the next threshold
        return new DFSResult(false, 0, min);
    }
    
    /**
     * Get the name of the current heuristic for display
     */
    private String getHeuristicName() {
        switch (heuristicType) {
            case BLOCKING_PIECES:
                return "Blocking Pieces";
            case MANHATTAN_DISTANCE:
                return "Manhattan Distance";
            case COMBINED:
                return "Combined (Blocking + Manhattan)";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Calculate a heuristic value for a given board state.
     */
    private int calculateHeuristic(Board board) {
        switch (heuristicType) {
            case MANHATTAN_DISTANCE:
                return calculateManhattanHeuristic(board);
            case COMBINED:
                return calculateCombinedHeuristic(board);
            case BLOCKING_PIECES:
            default:
                return calculateBlockingPiecesHeuristic(board);
        }
    }
    
    // Heuristic methods copied from A*
    private int calculateBlockingPiecesHeuristic(Board board) {
        // Same implementation as in AStar
        // Find the primary piece
        Piece primaryPiece = board.getPrimaryPiece();
        if (primaryPiece == null) {
            return Integer.MAX_VALUE;
        }
        
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        char[][] grid = board.getGrid();
        int rows = board.getRows();
        int cols = board.getCols();
        
        // Count blocking pieces between primary piece and exit
        int blockingPieces = 0;
        
        if (primaryPiece.isHorizontal()) {
            // Count horizontal blocking pieces
            if (exitCol == cols) {
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() + primaryPiece.getLength(); col < cols; col++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            } else if (exitCol == -1) {
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() - 1; col >= 0; col--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            }
        } else {
            // Count vertical blocking pieces
            if (exitRow == rows) {
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() + primaryPiece.getLength(); row < rows; row++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            } else if (exitRow == -1) {
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() - 1; row >= 0; row--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            }
        }
        
        return blockingPieces;
    }
    
    private int calculateManhattanHeuristic(Board board) {
        // Same implementation as in AStar
        Piece primaryPiece = board.getPrimaryPiece();
        if (primaryPiece == null) {
            return Integer.MAX_VALUE;
        }
        
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        int rows = board.getRows();
        int cols = board.getCols();
        
        int manhattanDistance = 0;
        
        if (primaryPiece.isHorizontal()) {
            if (exitCol == cols) {
                manhattanDistance = cols - (primaryPiece.getCol() + primaryPiece.getLength());
            } else if (exitCol == -1) {
                manhattanDistance = primaryPiece.getCol();
            }
        } else {
            if (exitRow == rows) {
                manhattanDistance = rows - (primaryPiece.getRow() + primaryPiece.getLength());
            } else if (exitRow == -1) {
                manhattanDistance = primaryPiece.getRow();
            }
        }
        
        return manhattanDistance;
    }
    
    private int calculateCombinedHeuristic(Board board) {
        int blockingPieces = calculateBlockingPiecesHeuristic(board);
        int manhattanDistance = calculateManhattanHeuristic(board);
        
        return blockingPieces + manhattanDistance;
    }
    
    /**
     * Find the move that transforms one board state to another
     */
    private Move findMove(Board from, Board to) {
        List<Piece> fromPieces = from.getPieces();
        List<Piece> toPieces = to.getPieces();
        
        for (int i = 0; i < fromPieces.size(); i++) {
            Piece fromPiece = fromPieces.get(i);
            Piece toPiece = toPieces.get(i);
            
            if (fromPiece.getRow() != toPiece.getRow() || 
                fromPiece.getCol() != toPiece.getCol()) {
                // Found the moved piece
                return new Move(
                    i, 
                    fromPiece.getRow(), 
                    fromPiece.getCol(), 
                    toPiece.getRow(), 
                    toPiece.getCol()
                );
            }
        }
        
        throw new IllegalStateException("Could not find the move between board states");
    }
    
    /**
     * Print the solution path
     */
    private void printSolution(List<Node> path) {
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            Move move = node.move;
            int pieceIndex = move.getPieceIndex();
            Piece piece = node.board.getPieces().get(pieceIndex);
            char pieceId = piece.getId();
            
            String direction = move.getDirection(piece.isVertical());
            int distance = move.getDistance(piece.isVertical());
            
            BoardPrinter.printBoardAfterMove(node.board, i + 1, pieceId, direction, distance);
        }
    }
    
    /**
     * Inner class representing a node in the search tree.
     */
    private static class Node {
        Board board;       // Current board state
        Node parent;       // Parent node
        Move move;         // Move that was applied to reach this state
        int cost;          // g = Path cost
        int heuristic;     // h = Heuristic value
        
        Node(Board board, Node parent, Move move, int cost, int heuristic) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
            this.heuristic = heuristic;
        }
    }
    
    /**
     * Class to hold the result of a depth-first search iteration.
     */
    private static class DFSResult {
        boolean solved;        // Whether a solution was found
        int cost;              // Solution cost (if found)
        int nextThreshold;     // Next threshold to use (if not solved)
        
        DFSResult(boolean solved, int cost, int nextThreshold) {
            this.solved = solved;
            this.cost = cost;
            this.nextThreshold = nextThreshold;
        }
    }

    public int getNodesVisited(){
        return this.nodesVisited;
    }
}