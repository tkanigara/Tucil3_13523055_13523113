package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

/**
 * Greedy Best-First Search (GBFS) algorithm implementation for solving Rush Hour puzzles.
 * Uses a heuristic to prioritize states that seem closer to the goal.
 */
public class GBFS {
    private int nodesVisited = 0;
    private int heuristicType;
    private gui.Gui.SolutionCollector collector;
    
    // Heuristic types
    public static final int BLOCKING_PIECES = 1;
    public static final int MANHATTAN_DISTANCE = 2;
    public static final int COMBINED = 3;
    
    /**
     * Constructor with heuristic selection
     * 
     * @param heuristicType The type of heuristic to use
     */
    public GBFS(int heuristicType, gui.Gui.SolutionCollector collector) {
        this.heuristicType = heuristicType;
        this.collector = collector;
    }
    
    /**
     * Default constructor - uses blocking pieces heuristic
     */
    public GBFS() {
        this(BLOCKING_PIECES,null);
    }
    
    /**
     * Solve the Rush Hour puzzle using Greedy Best-First Search.
     * 
     * @param initialBoard The initial board state
     */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        // Print which heuristic is being used
        System.out.println("Using heuristic: " + getHeuristicName());
        
        // Priority queue ordered by heuristic value (estimated cost to goal)
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.heuristic));
        
        // To keep track of visited states
        Set<String> visited = new HashSet<>();
        
        // Calculate initial heuristic and add initial state to queue
        int initialHeuristic = calculateHeuristic(initialBoard);
        queue.add(new Node(initialBoard, null, null, 0, initialHeuristic));
        
        // Print initial board
        BoardPrinter.printInitialBoard(initialBoard);
        
        boolean solved = false;
        Node solution = null;
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            nodesVisited++;
            
            // Skip if we've already visited this state
            String boardString = current.board.toString();
            if (visited.contains(boardString)) {
                continue;
            }
            
            // Mark as visited
            visited.add(boardString);
            
            // Check if we've reached the goal state
            if (current.board.isSolved()) {
                solved = true;
                solution = current;
                break;
            }
            
            // Generate all possible next states
            List<Board> nextStates = current.board.getNextStates();
            
            // Add each next state to the queue
            for (int i = 0; i < nextStates.size(); i++) {
                Board nextBoard = nextStates.get(i);
                
                // Skip if we've already visited this state
                if (visited.contains(nextBoard.toString())) {
                    continue;
                }
                
                // Find which piece was moved and to where
                Move move = findMove(current.board, nextBoard);
                
                // Calculate heuristic for this state
                int heuristic = calculateHeuristic(nextBoard);
                
                // Add to queue with calculated heuristic
                queue.add(new Node(
                    nextBoard,
                    current,
                    move,
                    current.cost + 1,  // Each board transition counts as 1 move
                    heuristic
                ));
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solved && collector != null) {
            // Collect the solution steps
            List<Node> path = new ArrayList<>();
            Node current = solution;
            
            // Collect from goal to start
            while (current != null) {
                path.add(current);
                current = current.parent;
            }
            
            // Reverse to get from start to goal
            Collections.reverse(path);
            
            // Add each board state to the collector
            for (Node node : path) {
                collector.addStep(node.board);
            }
        }

        if (solved) {
            // Reconstruct and print the solution path
            printSolution(solution);
            
            // Print statistics
            System.out.println("Jumlah langkah: " + solution.cost);
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        } else {
            System.out.println("Tidak ada solusi yang ditemukan!");
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        }
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
     * Lower values are better (closer to goal).
     * 
     * @param board The board state to evaluate
     * @return A heuristic value
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
    
    /**
     * Calculate the blocking pieces heuristic.
     * Counts how many vehicles block the primary piece's path to the exit.
     */
    private int calculateBlockingPiecesHeuristic(Board board) {
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
            // Primary piece is horizontal - check path to exit
            if (exitCol == cols) {
                // Exit is on the right edge
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() + primaryPiece.getLength(); col < cols; col++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            } else if (exitCol == -1) {
                // Exit is on the left edge
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() - 1; col >= 0; col--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            }
        } else {
            // Primary piece is vertical - check path to exit
            if (exitRow == rows) {
                // Exit is on the bottom edge
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() + primaryPiece.getLength(); row < rows; row++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            } else if (exitRow == -1) {
                // Exit is on the top edge
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() - 1; row >= 0; row--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        blockingPieces++;
                    }
                }
            }
        }
        
        // Prioritize moving the primary piece to the exit when path is clear
        if (blockingPieces == 0) {
            if (primaryPiece.isHorizontal()) {
                if (exitCol == cols) {
                    // Right exit - add distance to right edge
                    int distanceToExit = cols - (primaryPiece.getCol() + primaryPiece.getLength());
                    return distanceToExit > 0 ? 1 : 0; // Small non-zero value to prioritize moving to exit
                } else if (exitCol == -1) {
                    // Left exit - add distance to left edge
                    int distanceToExit = primaryPiece.getCol();
                    return distanceToExit > 0 ? 1 : 0;
                }
            } else {
                if (exitRow == rows) {
                    // Bottom exit - add distance to bottom edge
                    int distanceToExit = rows - (primaryPiece.getRow() + primaryPiece.getLength());
                    return distanceToExit > 0 ? 1 : 0;
                } else if (exitRow == -1) {
                    // Top exit - add distance to top edge
                    int distanceToExit = primaryPiece.getRow();
                    return distanceToExit > 0 ? 1 : 0;
                }
            }
        }
        
        return blockingPieces;
    }
    
    // Other heuristic methods remain largely the same
    
    /**
     * Calculate the Manhattan distance heuristic.
     * Measures the direct distance from the primary piece to the exit.
     */
    private int calculateManhattanHeuristic(Board board) {
        // Find the primary piece
        Piece primaryPiece = board.getPrimaryPiece();
        if (primaryPiece == null) {
            return Integer.MAX_VALUE;
        }
        
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        int rows = board.getRows();
        int cols = board.getCols();
        
        // Calculate Manhattan distance from primary piece to exit
        int manhattanDistance = 0;
        
        if (primaryPiece.isHorizontal()) {
            // For horizontal primary piece, we care about the distance to the right or left edge
            if (exitCol == cols) {
                // Exit is on the right edge
                // Distance from the right end of the piece to the right edge
                manhattanDistance = cols - (primaryPiece.getCol() + primaryPiece.getLength());
            } else if (exitCol == -1) {
                // Exit is on the left edge
                // Distance from the left end of the piece to the left edge
                manhattanDistance = primaryPiece.getCol();
            }
        } else {
            // For vertical primary piece, we care about the distance to the top or bottom edge
            if (exitRow == rows) {
                // Exit is on the bottom edge
                // Distance from the bottom end of the piece to the bottom edge
                manhattanDistance = rows - (primaryPiece.getRow() + primaryPiece.getLength());
            } else if (exitRow == -1) {
                // Exit is on the top edge
                // Distance from the top end of the piece to the top edge
                manhattanDistance = primaryPiece.getRow();
            }
        }
        
        // Also check if there are obstacles in the path
        // This makes the heuristic more informative
        char[][] grid = board.getGrid();
        boolean hasObstacles = false;
        
        if (primaryPiece.isHorizontal()) {
            if (exitCol == cols) {
                // Check right path
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() + primaryPiece.getLength(); col < cols; col++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        hasObstacles = true;
                        break;
                    }
                }
            } else if (exitCol == -1) {
                // Check left path
                int row = primaryPiece.getRow();
                for (int col = primaryPiece.getCol() - 1; col >= 0; col--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        hasObstacles = true;
                        break;
                    }
                }
            }
        } else {
            if (exitRow == rows) {
                // Check bottom path
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() + primaryPiece.getLength(); row < rows; row++) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        hasObstacles = true;
                        break;
                    }
                }
            } else if (exitRow == -1) {
                // Check top path
                int col = primaryPiece.getCol();
                for (int row = primaryPiece.getRow() - 1; row >= 0; row--) {
                    if (grid[row][col] != '.' && grid[row][col] != 'K') {
                        hasObstacles = true;
                        break;
                    }
                }
            }
        }
        
        // If there are obstacles, add a significant penalty
        return manhattanDistance + (hasObstacles ? 10 : 0);
    }
    
    /**
     * Calculate a combined heuristic.
     * Uses both blocking pieces and Manhattan distance.
     */
    private int calculateCombinedHeuristic(Board board) {
        int blockingPieces = calculateBlockingPiecesHeuristic(board);
        int manhattanDistance = calculateManhattanHeuristic(board);
        
        // If there are no blocking pieces, prioritize Manhattan distance
        if (blockingPieces == 0) {
            return manhattanDistance;
        }
        
        // Otherwise, give more weight to blocking pieces
        return blockingPieces * 10 + manhattanDistance;
    }
    
    /**
     * Find the move that transforms one board state to another
     * 
     * @param from Initial board state
     * @param to   Resulting board state
     * @return The move that was applied
     */
    private Move findMove(Board from, Board to) {
        // Compare the positions of all pieces to find which one moved
        List<Piece> fromPieces = from.getPieces();
        List<Piece> toPieces = to.getPieces();
        
        for (int i = 0; i < fromPieces.size(); i++) {
            Piece fromPiece = fromPieces.get(i);
            Piece toPiece = toPieces.get(i);
            
            if (fromPiece.getRow() != toPiece.getRow() || 
                fromPiece.getCol() != toPiece.getCol()) {
                // Found the moved piece - create a move with from and to positions
                return new Move(
                    i, 
                    fromPiece.getRow(), 
                    fromPiece.getCol(), 
                    toPiece.getRow(), 
                    toPiece.getCol()
                );
            }
        }
        
        // Should never reach here if the boards are different
        throw new IllegalStateException("Could not find the move between board states");
    }
    
    /**
     * Print the solution path
     * 
     * @param solution The solution node
     */
    private void printSolution(Node solution) {
        // Reconstruct the path from the goal to the initial state
        List<Node> path = new ArrayList<>();
        Node current = solution;
        
        while (current.parent != null) {
            path.add(current);
            current = current.parent;
        }
        
        // Reverse the path to print from initial to goal
        Collections.reverse(path);
        
        // Print each step
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
        int cost;          // Path cost (number of moves from initial state)
        int heuristic;     // Heuristic value (estimated cost to goal)
        
        Node(Board board, Node parent, Move move, int cost, int heuristic) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
            this.heuristic = heuristic;
        }
    }

    public int getNodesVisited(){
        return this.nodesVisited;
    }
}