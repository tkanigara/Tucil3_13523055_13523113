import algorithm.UCS;
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
            // We'll add these later
            // System.out.println("2. A* Search");
            // System.out.println("3. Greedy Best-First Search");
            System.out.print("Enter your choice (1): ");
            int choice = 1;
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                choice = Integer.parseInt(input);
            }
            
            switch (choice) {
                case 1:
                    System.out.println("\nSolving with Uniform Cost Search (UCS)...");
                    UCS ucs = new UCS();
                    ucs.solve(initialBoard);
                    break;
                // We'll add these later
                // case 2:
                //     System.out.println("\nSolving with A* Search...");
                //     AStar aStar = new AStar();
                //     aStar.solve(initialBoard);
                //     break;
                // case 3:
                //     System.out.println("\nSolving with Greedy Best-First Search...");
                //     GreedyBFS greedy = new GreedyBFS();
                //     greedy.solve(initialBoard);
                //     break;
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
