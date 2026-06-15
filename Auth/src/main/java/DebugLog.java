//debuglog
import java.io.PrintWriter;
import java.io.FileWriter;
//time
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
//exceptions
import java.io.IOException;
public class DebugLog {
    private static final PrintWriter logger;

    static {
        try {
            logger = new PrintWriter(new FileWriter("userslog.txt", true));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open logs", e);
        }
    }
    public static void Log(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        logger.println("["+timestamp+"] "+ message);
        logger.flush();
    }
}