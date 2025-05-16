import java.io.BufferedReader;
import java.io.FileReader;
import model.Board;
import util.FileParser;

public class TestParserNew {
    public static void main(String[] args) {
        try {
            // First, read the file directly to see what's in it
            System.out.println("Reading file directly:");
            BufferedReader reader = new BufferedReader(new FileReader("test/test3.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            
            System.out.println("\nParsing with FileParser:");
            FileParser parser = new FileParser();
            Board board = parser.parseFile("test/test3.txt");
            
            System.out.println("Board dimensions: " + board.getRows() + "x" + board.getCols());
            System.out.println("Exit at: (" + board.getExitRow() + "," + board.getExitCol() + ")");
            System.out.println("Board state:");
            System.out.println(board);
            
            // Print pieces
            System.out.println("\nPieces:");
            for (int i = 0; i < board.getPieces().size(); i++) {
                System.out.println(board.getPieces().get(i));
            }
            
            // Check if the puzzle is solved
            System.out.println("\nIs solved: " + board.isSolved());
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
