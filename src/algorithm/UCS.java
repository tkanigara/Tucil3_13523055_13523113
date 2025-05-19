package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

public class UCS {
    private int nodesVisited = 0;
    private gui.Gui.SolutionCollector collector;

    public UCS(gui.Gui.SolutionCollector collector){
        this.collector = collector;
    }
    
    /* Fungsi solver buat UCS */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        
        Set<String> visited = new HashSet<>();
        
        queue.add(new Node(initialBoard, null, null, 0));
        
        BoardPrinter.printInitialBoard(initialBoard);
        
        boolean solved = false;
        Node solution = null;
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            nodesVisited++;
            
            String boardString = current.board.toString();
            if (visited.contains(boardString)) {
                continue;
            }
            
            visited.add(boardString);
            
            if (current.board.isSolved()) {
                solved = true;
                solution = current;
                break;
            }
            
            List<Board> nextStates = current.board.getNextStates();
            
            for (int i = 0; i < nextStates.size(); i++) {
                Board nextBoard = nextStates.get(i);
                
                if (visited.contains(nextBoard.toString())) {
                    continue;
                }
                
                Move move = findMove(current.board, nextBoard);
                
                queue.add(new Node(
                    nextBoard,
                    current,
                    move,
                    current.cost + 1
                ));
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solved) {
            printSolution(solution);
            
            System.out.println("Jumlah langkah: " + solution.cost);
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        } else {
            System.out.println("Tidak ada solusi yang ditemukan!");
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        }

        if (solved && collector != null) {
            List<Node> path = new ArrayList<>();
            Node current = solution;
            
            while (current != null) {
                path.add(current);
                current = current.parent;
            }
            
            Collections.reverse(path);
            
            for (Node node : path) {
                collector.addStep(node.board);
            }
        }
    }
    
    private Move findMove(Board from, Board to) {
        List<Piece> fromPieces = from.getPieces();
        List<Piece> toPieces = to.getPieces();
        
        for (int i = 0; i < fromPieces.size(); i++) {
            Piece fromPiece = fromPieces.get(i);
            Piece toPiece = toPieces.get(i);
            
            if (fromPiece.getRow() != toPiece.getRow() || 
                fromPiece.getCol() != toPiece.getCol()) {
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
    
    /* Fungsi utk print solusi */
    private void printSolution(Node solution) {
        List<Node> path = new ArrayList<>();
        Node current = solution;
        
        while (current.parent != null) {
            path.add(current);
            current = current.parent;
        }
        
        Collections.reverse(path);
        
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

    public int getNodesVisited(){
        return this.nodesVisited;
    }
}