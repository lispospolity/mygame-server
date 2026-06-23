import org.java_websocket.WebSocket;
import java.util.Map;

public class EntityEvent {
    static Websocket instance = Websocket.getInstance();
    public static void handle(WebSocket conn, Map msg) {
        if (msg.get("t").equals("1")) {
            keyPress(conn, msg);
            return;
        }
    }
    private static void keyPress(WebSocket conn, Map msg) {
        int key = ((Double) msg.get("k")).intValue();
        String name = instance.connToName.get(conn);
        if (key == 1||key == 5) {
            PlayerMovement.moveUp(name);
            return;
        }
        if (key == 2||key == 6) {
            PlayerMovement.moveDown(name);
            return;
        }
        if (key == 3||key == 7) {
            PlayerMovement.moveLeft(name);
            return;
        }
        if (key == 4||key == 8) {
            PlayerMovement.moveRight(name);
            return;
        }
    }
}
