import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Env {
    public static String read(String prompt) {
        Path filePath = Paths.get(".env");
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i<lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("=", 2);
                if (parts[0].equals(prompt)) return parts[1];
            }
            return "";
        } catch (IOException e) {
            Debug.log(e + "");
            return "";
        }
    }
}
