import algorithm.*;
import java.util.Scanner;
import model.Board;
import util.FileParser;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Rush Hour Puzzle Solver");
        System.out.println("======================");
        
        // Get the input file path
        System.out.print("Enter the path to the input file: ");
        String filePath = scanner.nextLine();
        
        try {
            // Parse the input file
            FileParser parser = new FileParser();
            Board initialBoard = parser.parseFile(filePath);
            
            // Choose the algorithm
            System.out.println("\nChoose the algorithm:");
            System.out.println("1. Uniform Cost Search (UCS)");
            System.out.println("2. Greedy Best-First Search (GBFS)");
            System.out.println("3. A* Search");
            System.out.println("4. IDA* Search");
            System.out.print("Enter your choice (1): ");
            int choice = 1;
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                choice = Integer.parseInt(input);
            }

            // If GBFS or A* is chosen, ask for the heuristic
            int heuristicChoice = 1; // Default to blocking pieces heuristic
            if (choice == 2 || choice == 3 || choice == 4) {
                System.out.println("\nChoose the heuristic:");
                System.out.println("1. Blocking Pieces (count blocking vehicles)");
                System.out.println("2. Manhattan Distance (distance to exit)");
                System.out.println("3. Combined (blocking pieces + weighted Manhattan)");
                System.out.print("Enter your choice (1): ");
                input = scanner.nextLine();
                if (!input.isEmpty()) {
                    heuristicChoice = Integer.parseInt(input);
                }
            }
            
            switch (choice) {
                case 1:
                    System.out.println("\nSolving with Uniform Cost Search (UCS)...");
                    UCS ucs = new UCS();
                    ucs.solve(initialBoard);
                    break;
                case 2:
                    System.out.println("\nSolving with Greedy Best-First Search (GBFS)...");
                    GBFS gbfs = new GBFS(heuristicChoice);
                    gbfs.solve(initialBoard);
                    break;
                case 3:
                    System.out.println("\nSolving with A* Search...");
                    AStar aStar = new AStar(heuristicChoice);
                    aStar.solve(initialBoard);
                    break;
                case 4:
                    System.out.println("Solving with Iterative Deepening A* (IDA*) Search...");
                    IDAStar idaStar = new IDAStar(heuristicChoice);
                    idaStar.solve(initialBoard);
                    break;
                default:
                    System.out.println("Invalid choice. Using UCS by default.");
                    UCS defaultUcs = new UCS();
                    defaultUcs.solve(initialBoard);
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}