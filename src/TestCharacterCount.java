import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestCharacterCount {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("test/test3.txt"));
            // Skip first two lines
            reader.readLine(); // dimensions
            reader.readLine(); // number of pieces
            
            // Read the first 3 lines of the board (0-indexed, so line 2 is the third line)
            String line0 = reader.readLine();
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            
            System.out.println("Line 0 length: " + line0.length() + ", content: \"" + line0 + "\"");
            System.out.println("Line 1 length: " + line1.length() + ", content: \"" + line1 + "\"");
            System.out.println("Line 2 length: " + line2.length() + ", content: \"" + line2 + "\"");
            
            // Print each character in line2 with its index
            System.out.println("\nCharacters in line 2:");
            for (int i = 0; i < line2.length(); i++) {
                System.out.println("Index " + i + ": '" + line2.charAt(i) + "'");
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
