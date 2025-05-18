package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

/**
 * Uniform Cost Search (UCS) algorithm implementation for solving Rush Hour puzzles.
 */
public class UCS {
    private int nodesVisited = 0;
    
    /**
     * Solve the Rush Hour puzzle using Uniform Cost Search.
     * 
     * @param initialBoard The initial board state
     */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        // Priority queue ordered by path cost (number of moves)
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        
        // To keep track of visited states
        Set<String> visited = new HashSet<>();
        
        // Add initial state to queue
        queue.add(new Node(initialBoard, null, null, 0));
        
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
                
                // Add to queue with increased cost
                queue.add(new Node(
                    nextBoard,
                    current,
                    move,
                    current.cost + 1  // Each board transition counts as 1 move
                ));
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
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
        Board board;  // Current board state
        Node parent;  // Parent node
        Move move;    // Move that was applied to reach this state
        int cost;     // Path cost (number of moves from initial state)
        
        Node(Board board, Node parent, Move move, int cost) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
        }
    }
}