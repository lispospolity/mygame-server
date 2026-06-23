import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Env {
    public static LoginCredentials envRead(int type) {
        Path filePath = Paths.get("DB/src/main/java/.env");
        try {
            List<String> lines = Files.readAllLines(filePath);
            if (type == 0) return new LoginCredentials(lines.get(0), lines.get(1), lines.get(2));
            return new LoginCredentials(lines.get(4), lines.get(1), lines.get(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public record LoginCredentials(
            String url,
            String user,
            String password
    ){}
}
