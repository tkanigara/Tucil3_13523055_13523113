package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.SolutionWriter;

public class GBFS {
    private int heuristicType;
    
    public GBFS(int heuristicType) {
        this.heuristicType = heuristicType;
    }
    
    /**
     * Solve the Rush Hour puzzle using Greedy Best-First Search.
     * 
     * @param initialBoard The initial board state
     * @param solutionWriter Writer for capturing the solution
     */
    public void solve(Board initialBoard, SolutionWriter solutionWriter) {
        long startTime = System.currentTimeMillis();
        int nodesVisited = 0;
        
        // Create the priority queue ordered by heuristic value h(n)
        PriorityQueue<Node> queue = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.h)
        );
        
        // Keep track of visited states
        Set<String> visited = new HashSet<>();
        
        // Start with the initial state
        Node startNode = new Node(initialBoard, null, null, 0);
        startNode.h = calculateHeuristic(initialBoard);
        queue.add(startNode);
        
        // Display initial board
        solutionWriter.writeInitialBoard(initialBoard);
        
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
            for (Board nextBoard : nextStates) {
                // Skip if we've already visited this state
                if (visited.contains(nextBoard.toString())) {
                    continue;
                }
                
                // Find which piece was moved and to where
                Move move = findMove(current.board, nextBoard);
                
                // Create new node with updated heuristic value
                Node nextNode = new Node(
                    nextBoard,
                    current,
                    move,
                    current.cost + 1  // Track cost for statistics, not used for ordering
                );
                
                nextNode.h = calculateHeuristic(nextBoard);
                
                queue.add(nextNode);
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solved) {
            // Reconstruct and print the solution path
            printSolution(solution, solutionWriter);
            
            // Print statistics
            solutionWriter.writeStatistics(solution.cost, nodesVisited, executionTime);
        } else {
            solutionWriter.writeNoSolution(nodesVisited, executionTime);
        }
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
     * Node class for Greedy Best-First Search
     */
    private static class Node {
        Board board;
        Node parent;
        Move move;
        int cost; // Used for statistics only, not for ordering
        int h;    // h(n) - heuristic value
        
        public Node(Board board, Node parent, Move move, int cost) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
            this.h = 0;
        }
    }
}