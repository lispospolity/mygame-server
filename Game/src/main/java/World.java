import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class World {
    public static int[][] grid;
    private static int[][] readWorld() {
        Debug.log("INIT: Reading game.world");
        int[][] grid = new int[10][10];
        Path filePath = Paths.get("game.world");
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int y = 0; y<lines.size(); y++) {
                for (int x = 0; x<lines.get(y).length();x++) {
                    String line = lines.get(y);
                    grid[x][y] = line.charAt(x)-'0';
                }
            }
        } catch (IOException e) {
            String error = e.toString();
            Debug.log(error);
            e.printStackTrace();
        }
        return grid;
    }
    public static void LoadWorld() {
        grid = readWorld();
        Debug.log("INIT: World loaded succesfully.");
        //create entities (when I add NPCs)
    }
}
