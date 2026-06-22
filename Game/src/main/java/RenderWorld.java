import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RenderWorld {
    private static int[][] ReadWorld() {
        Debug.Log("INIT: Reading game.world");
        int[][] grid = new int[100][100];
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
            Debug.Log(error);
            e.printStackTrace();
        }
        return grid;
    }
    public static void LoadWorld() {
        int [][] grid = ReadWorld();
        Debug.Log("INIT: World loaded succesfully.");
        //create entities
    }
//TODO finish that
}
