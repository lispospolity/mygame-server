import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class start {
    public static void startWS() {
        WebSocketServer server = new Websocket(new InetSocketAddress(9080));
        server.run();
    }
}
