import org.java_websocket.WebSocket;
import java.util.Map;

public class EntityEvent {
    public static void handle(WebSocket conn, Map msg) {
        if (msg.get("t").equals("1")) {
            keyPress(conn, msg);
            return;
        }
    }
    private static void keyPress(WebSocket conn, Map msg) {
        System.out.println(msg);
    }
}
