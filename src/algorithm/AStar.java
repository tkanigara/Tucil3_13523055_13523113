package algorithm;

import java.util.*;
import model.Board;
import model.Move;
import model.Piece;
import util.BoardPrinter;

public class AStar {
    private int nodesVisited = 0;
    private int heuristicType;
    private gui.Gui.SolutionCollector collector;
    
    public static final int BLOCKING_PIECES = 1;
    public static final int MANHATTAN_DISTANCE = 2;
    public static final int COMBINED = 3;
    
    public AStar(int heuristicType, gui.Gui.SolutionCollector collector) {
        this.heuristicType = heuristicType;
        this.collector = collector;
    }

    public AStar() {
        this(BLOCKING_PIECES, null);
    }
    
    /* Fungsi solver AStar */
    public void solve(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        
        System.out.println("Using heuristic: " + getHeuristicName());
        
        PriorityQueue<Node> queue = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.cost + node.heuristic)
        );
        
        Set<String> visited = new HashSet<>();
        
        int initialHeuristic = calculateHeuristic(initialBoard);
        queue.add(new Node(initialBoard, null, null, 0, initialHeuristic));
        
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
                
                int heuristic = calculateHeuristic(nextBoard);
                
                queue.add(new Node(
                    nextBoard,
                    current,
                    move,
                    current.cost + 1,  
                    heuristic          
                ));
            }
        }
        
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        
        if (solved && collector != null) {
            List<Node> path = new ArrayList<>();
            Node current = solution;
            
            collector.addStep(initialBoard);
            
            while (current != null) {
                path.add(current);
                current = current.parent;
            }
            
            Collections.reverse(path);
            
            for (int i = 1; i < path.size(); i++) {
                collector.addStep(path.get(i).board);
            }
        }

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
    
    /* Heuristik mobil yg ngeblok */
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
    
    /* Heuristik Manhattan */
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
    
    /* Kombinasi heursitik */
    private int calculateCombinedHeuristic(Board board) {
        int blockingPieces = calculateBlockingPiecesHeuristic(board);
        int manhattanDistance = calculateManhattanHeuristic(board);
        
        return blockingPieces * 2 + manhattanDistance;
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

    public int getNodesVisited(){
        return this.nodesVisited;
    }
}