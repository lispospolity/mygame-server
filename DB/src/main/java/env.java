import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class env {
    public static logincredentials envread() {
        Path filePath = Paths.get("DB/src/main/java/.env");
        try {
            List<String> lines = Files.readAllLines(filePath);
            return new logincredentials(lines.get(0), lines.get(1), lines.get(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public record logincredentials(
            String url,
            String user,
            String password
    ){}
}
