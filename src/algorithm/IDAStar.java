package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.SolutionWriter;

public class IDAStar {
    private int heuristicType;
    
    public IDAStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    /**
     * Solve the Rush Hour puzzle using Iterative Deepening A* Search.
     * 
     * @param initialBoard The initial board state
     * @param solutionWriter Writer for capturing the solution
     */
    public void solve(Board initialBoard, SolutionWriter solutionWriter) {
        long startTime = System.currentTimeMillis();
        int[] nodesVisited = new int[1]; // Use array to allow passing by reference
        
        // Display initial board
        solutionWriter.writeInitialBoard(initialBoard);
        
        // Initial bound is the heuristic value of the initial state
        int bound = calculateHeuristic(initialBoard);
        
        Node startNode = new Node(initialBoard, null, null, 0);
        startNode.h = bound;
        
        Node solution = null;
        int newBound;
        
        // Iteratively increase the bound until a solution is found
        while (true) {
            // Search with current bound
            SearchResult result = search(startNode, 0, bound, nodesVisited, new HashSet<>());
            
            if (result.found) {
                solution = result.node;
                break;
            }
            
            // No solution within current bound, update to new bound
            newBound = result.newBound;
            
            if (newBound == Integer.MAX_VALUE) {
                // No solution exists
                break;
            }
            
            // Continue with increased bound
            bound = newBound;
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solution != null) {
            // Reconstruct and print the solution path
            printSolution(solution, solutionWriter);
            
            // Print statistics
            solutionWriter.writeStatistics(solution.cost, nodesVisited[0], executionTime);
        } else {
            solutionWriter.writeNoSolution(nodesVisited[0], executionTime);
        }
    }
    
    /**
     * Recursive search function for IDA*.
     * 
     * @param node Current node being explored
     * @param g Current path cost
     * @param bound Current cost bound
     * @param nodesVisited Counter for nodes visited
     * @param visited Set of visited states
     * @return Search result containing found status and new bound
     */
    private SearchResult search(Node node, int g, int bound, int[] nodesVisited, Set<String> visited) {
        nodesVisited[0]++;
        
        int f = g + node.h;
        
        // If f exceeds bound, return the new minimum bound
        if (f > bound) {
            return new SearchResult(false, null, f);
        }
        
        // Check if we've reached the goal state
        if (node.board.isSolved()) {
            return new SearchResult(true, node, bound);
        }
        
        // Mark current state as visited
        String boardString = node.board.toString();
        visited.add(boardString);
        
        int minBound = Integer.MAX_VALUE;
        
        // Generate all possible next states
        List<Board> nextStates = node.board.getNextStates();
        
        // Sort nextStates by heuristic (optional optimization)
        List<NodeWithHeuristic> sortedStates = new ArrayList<>();
        for (Board nextBoard : nextStates) {
            if (!visited.contains(nextBoard.toString())) {
                int h = calculateHeuristic(nextBoard);
                sortedStates.add(new NodeWithHeuristic(nextBoard, h));
            }
        }
        
        // Sort by heuristic value
        sortedStates.sort(Comparator.comparingInt(n -> n.h));
        
        // Explore each successor
        for (NodeWithHeuristic nextState : sortedStates) {
            Board nextBoard = nextState.board;
            
            // Find which piece was moved and to where
            Move move = findMove(node.board, nextBoard);
            
            // Create new node
            Node nextNode = new Node(nextBoard, node, move, g + 1);
            nextNode.h = nextState.h;
            
            // Recursive search with incremented cost
            SearchResult result = search(nextNode, g + 1, bound, nodesVisited, new HashSet<>(visited));
            
            if (result.found) {
                return result;
            }
            
            // Update minimum bound
            if (result.newBound < minBound) {
                minBound = result.newBound;
            }
        }
        
        // No solution found within bound, return minimum new bound
        return new SearchResult(false, null, minBound);
    }
    
    /**
     * Calculate the heuristic value for a board state.
     * 
     * @param board The board state
     * @return The heuristic value
     */
    private int calculateHeuristic(Board board) {
        switch (heuristicType) {
            case 1: // Blocking pieces heuristic
                return calculateBlockingPieces(board);
            case 2: // Manhattan distance heuristic
                return calculateManhattanDistance(board);
            case 3: // Combined heuristic
                return calculateCombinedHeuristic(board);
            default:
                return calculateBlockingPieces(board);
        }
    }
    
    // Implement your heuristic functions here
    private int calculateBlockingPieces(Board board) {
        // Count the number of pieces blocking the primary piece's path to the exit
        // Implementation details would depend on your Board class
        
        // Find the primary piece
        Piece primaryPiece = null;
        for (Piece piece : board.getPieces()) {
            if (piece.isPrimary()) {
                primaryPiece = piece;
                break;
            }
        }
        
        if (primaryPiece == null) {
            return 0;
        }
        
        int count = 0;
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        // Count blocking pieces based on exit location
        if (primaryPiece.isHorizontal()) {
            // Horizontal primary piece, check path to left or right exit
            if (exitCol == -1) {
                // Left exit
                for (int c = 0; c < primaryPiece.getCol(); c++) {
                    if (board.getGrid()[primaryPiece.getRow()][c] != '.') {
                        count++;
                    }
                }
            } else if (exitCol == board.getCols()) {
                // Right exit
                for (int c = primaryPiece.getCol() + primaryPiece.getLength(); c < board.getCols(); c++) {
                    if (board.getGrid()[primaryPiece.getRow()][c] != '.') {
                        count++;
                    }
                }
            }
        } else {
            // Vertical primary piece, check path to top or bottom exit
            if (exitRow == -1) {
                // Top exit
                for (int r = 0; r < primaryPiece.getRow(); r++) {
                    if (board.getGrid()[r][primaryPiece.getCol()] != '.') {
                        count++;
                    }
                }
            } else if (exitRow == board.getRows()) {
                // Bottom exit
                for (int r = primaryPiece.getRow() + primaryPiece.getLength(); r < board.getRows(); r++) {
                    if (board.getGrid()[r][primaryPiece.getCol()] != '.') {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    private int calculateManhattanDistance(Board board) {
        // Calculate Manhattan distance from primary piece to exit
        
        // Find the primary piece
        Piece primaryPiece = null;
        for (Piece piece : board.getPieces()) {
            if (piece.isPrimary()) {
                primaryPiece = piece;
                break;
            }
        }
        
        if (primaryPiece == null) {
            return 0;
        }
        
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        if (primaryPiece.isHorizontal()) {
            // For horizontal primary piece
            if (exitCol == -1) {
                // Left exit
                return primaryPiece.getCol();
            } else if (exitCol == board.getCols()) {
                // Right exit
                return board.getCols() - (primaryPiece.getCol() + primaryPiece.getLength());
            }
        } else {
            // For vertical primary piece
            if (exitRow == -1) {
                // Top exit
                return primaryPiece.getRow();
            } else if (exitRow == board.getRows()) {
                // Bottom exit
                return board.getRows() - (primaryPiece.getRow() + primaryPiece.getLength());
            }
        }
        
        return 0;
    }
    
    private int calculateCombinedHeuristic(Board board) {
        // Combined heuristic: blocking pieces + weighted manhattan distance
        return calculateBlockingPieces(board) + 2 * calculateManhattanDistance(board);
    }
    
    /**
     * Find the move that transforms one board state to another.
     * 
     * @param fromBoard The starting board state
     * @param toBoard The resulting board state
     * @return The move that was applied
     */
    private Move findMove(Board fromBoard, Board toBoard) {
        List<Piece> fromPieces = fromBoard.getPieces();
        List<Piece> toPieces = toBoard.getPieces();
        
        for (int i = 0; i < fromPieces.size(); i++) {
            Piece fromPiece = fromPieces.get(i);
            Piece toPiece = toPieces.get(i);
            
            if (fromPiece.getRow() != toPiece.getRow() || fromPiece.getCol() != toPiece.getCol()) {
                // This piece moved
                // int rowDiff = toPiece.getRow() - fromPiece.getRow();
                // int colDiff = toPiece.getCol() - fromPiece.getCol();
                
                return new Move(
                    i, 
                    fromPiece.getRow(), 
                    fromPiece.getCol(), 
                    toPiece.getRow(), 
                    toPiece.getCol()
                );
            }
        }
        
        return null; // Should not happen if boards are different
    }
    
    /**
     * Print the solution path
     * 
     * @param solution The solution node
     * @param solutionWriter Writer for capturing the solution
     */
    private void printSolution(Node solution, SolutionWriter solutionWriter) {
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
            
            solutionWriter.writeBoardAfterMove(node.board, i + 1, pieceId, direction, distance);
        }
    }
    
    /**
     * Helper class for storing search results
     */
    private static class SearchResult {
        boolean found;
        Node node;
        int newBound;
        
        public SearchResult(boolean found, Node node, int newBound) {
            this.found = found;
            this.node = node;
            this.newBound = newBound;
        }
    }
    
    /**
     * Helper class for sorting by heuristic
     */
    private static class NodeWithHeuristic {
        Board board;
        int h;
        
        public NodeWithHeuristic(Board board, int h) {
            this.board = board;
            this.h = h;
        }
    }
    
    /**
     * Node class for IDA* search
     */
    private static class Node {
        Board board;
        Node parent;
        Move move;
        int cost; // g(n) - cost from start to this node
        int h;    // h(n) - estimated cost to goal
        
        public Node(Board board, Node parent, Move move, int cost) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
            this.h = 0;
        }
    }
}