import algorithm.*;
import gui.Gui;
import java.util.Scanner;
import model.Board;
import util.FileParser;

public class Main {
    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            // buat launch GUI
            javax.swing.SwingUtilities.invokeLater(() -> new Gui());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Rush Hour Puzzle Solver");
        System.out.println("======================");
        
        // Input path kalau CLI
        System.out.print("Enter the path to the input file: ");
        String filePath = scanner.nextLine();
        
        try {
            // Parse input
            FileParser parser = new FileParser();
            Board initialBoard = parser.parseFile(filePath);
            
            // Pilih algoritma
            System.out.println("\nChoose the algorithm:");
            System.out.println("1. Uniform Cost Search (UCS)");
            System.out.println("2. Greedy Best-First Search (GBFS)");
            System.out.println("3. A* Search");
            System.out.println("4. IDA* Search");
            System.out.print("Enter your choice: ");
            int choice = 1;
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                choice = Integer.parseInt(input);
            }

            int heuristicChoice = 1; // default
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
                    UCS ucs = new UCS(null);
                    ucs.solve(initialBoard);
                    break;
                case 2:
                    System.out.println("\nSolving with Greedy Best-First Search (GBFS)...");
                    GBFS gbfs = new GBFS(heuristicChoice,null);
                    gbfs.solve(initialBoard);
                    break;
                case 3:
                    System.out.println("\nSolving with A* Search...");
                    AStar aStar = new AStar(heuristicChoice, null);
                    aStar.solve(initialBoard);
                    break;
                case 4:
                    System.out.println("Solving with Iterative Deepening A* (IDA*) Search...");
                    IDAStar idaStar = new IDAStar(heuristicChoice, null);
                    idaStar.solve(initialBoard);
                    break;
                default:
                    System.out.println("Invalid choice. Using UCS by default.");
                    UCS defaultUcs = new UCS(null);
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