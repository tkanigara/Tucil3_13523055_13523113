package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

public class IDAStar {
    private int nodesVisited = 0;
    private int heuristicType;
    private List<Node> solutionPath;
    private gui.Gui.SolutionCollector collector;
    
    public static final int BLOCKING_PIECES = 1;
    public static final int MANHATTAN_DISTANCE = 2;
    public static final int COMBINED = 3;
    
    public IDAStar(int heuristicType, gui.Gui.SolutionCollector collector) {
        this.heuristicType = heuristicType;
        this.collector = collector;
    }
    
    public IDAStar() {
        this(BLOCKING_PIECES, null);
    }

    /* Solver IDA* */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        System.out.println("Using heuristic: " + getHeuristicName());
        
        BoardPrinter.printInitialBoard(initialBoard);
        
        int initialHeuristic = calculateHeuristic(initialBoard);
        Node root = new Node(initialBoard, null, null, 0, initialHeuristic);
        
        int threshold = initialHeuristic;
        
        boolean solved = false;
        int solutionCost = 0;
        
        Set<String> globalVisited = new HashSet<>();
        
        while (!solved) {
            Set<String> visited = new HashSet<>();
            visited.add(initialBoard.toString());
            globalVisited.add(initialBoard.toString());
            nodesVisited++;
            
            solutionPath = new ArrayList<>();
            
            DFSResult result = depthFirstSearch(root, 0, threshold, visited, globalVisited);
            
            if (result.solved) {
                solved = true;
                solutionCost = result.cost;
            } else {
                threshold = result.nextThreshold;
                
                if (threshold == Integer.MAX_VALUE) {
                    break;
                }
                
                System.out.println("Increasing threshold to: " + threshold);
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
          if (solved) {
            Collections.reverse(solutionPath);
            
            if (collector != null) {
                collector.addStep(initialBoard);
                
                for (Node node : solutionPath) {
                    collector.addStep(node.board);
                }
            }
            
            printSolution(solutionPath);
            
            System.out.println("Jumlah langkah: " + solutionCost);
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        } else {
            System.out.println("Tidak ada solusi yang ditemukan!");
            System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
            System.out.println("Waktu eksekusi: " + executionTime + " detik");
        }
    }
    
    private DFSResult depthFirstSearch(Node node, int cost, int threshold, 
                                      Set<String> visited, Set<String> globalVisited) {
        int f = cost + node.heuristic;
        
        if (f > threshold) {
            return new DFSResult(false, 0, f);
        }
        
        if (node.board.isSolved()) {
            Node current = node;
            while (current.parent != null) {
                solutionPath.add(current);
                current = current.parent;
            }
            
            return new DFSResult(true, cost, f);
        }
        
        int min = Integer.MAX_VALUE;
        
        List<Board> nextStates = node.board.getNextStates();
        
        for (Board nextBoard : nextStates) {
            String boardString = nextBoard.toString();
            
            if (visited.contains(boardString)) {
                continue;
            }
            
            boolean isNewGlobal = !globalVisited.contains(boardString);
            
            visited.add(boardString);
            globalVisited.add(boardString);
            nodesVisited++;
            
            Move move = findMove(node.board, nextBoard);
            
            int heuristic = calculateHeuristic(nextBoard);
            
            Node childNode = new Node(nextBoard, node, move, cost + 1, heuristic);
            
            DFSResult result = depthFirstSearch(childNode, cost + 1, threshold, visited, globalVisited);
            
            if (result.solved) {
                return result;
            }
            
            min = Math.min(min, result.nextThreshold);
            
            visited.remove(boardString);
            
            if (isNewGlobal && !result.solved) {
                globalVisited.remove(boardString);
            }
        }
        
        return new DFSResult(false, 0, min);
    }
    
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
    
    // Heuristik berdasarkan jumlah mobil yang menghalangi
    private int calculateBlockingPiecesHeuristic(Board board) {
        Piece primaryPiece = board.getPrimaryPiece();
        if (primaryPiece == null) {
            return Integer.MAX_VALUE;
        }
        
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        char[][] grid = board.getGrid();
        int rows = board.getRows();
        int cols = board.getCols();
        
        int blockingPieces = 0;
        
        if (primaryPiece.isHorizontal()) {
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
    
    // Heuristik berdasarkan jarak Manhattan ke pintu keluar
    private int calculateManhattanHeuristic(Board board) {
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
    
    // menghitung heuristik gabungan dari blocking pieces dan Manhattan distance
    private int calculateCombinedHeuristic(Board board) {
        int blockingPieces = calculateBlockingPiecesHeuristic(board);
        int manhattanDistance = calculateManhattanHeuristic(board);
        
        return blockingPieces + manhattanDistance;
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
    
    // Mencetak solusi utk setiap langkah
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
    
    private static class Node {
        Board board;
        Node parent;
        Move move;
        int cost;
        int heuristic;
        
        Node(Board board, Node parent, Move move, int cost, int heuristic) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.cost = cost;
            this.heuristic = heuristic;
        }
    }
    
    private static class DFSResult {
        boolean solved;
        int cost;
        int nextThreshold;
        
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