import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Http {
    public static void main(String[] args) throws IOException {
        Debug.log("INIT: API starting...");
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/api/session", new SessionHandler());
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/auth/user", new AuthUserHandler());
        server.setExecutor(null); // uses default
        try {
            server.start();
        } catch (Exception e) {
            Debug.log("INIT: API encountered an error during startup:");
            Debug.log(e.toString());
        }
        Debug.log("INIT: API started.");
        try {
            new Thread(() -> new MailService()).start();
            Debug.log("INIT: SMTP started.");
        } catch (Exception e) {
            Debug.log("INIT: Mailservice found an issue during startup: "+e);
        }
        World.LoadWorld();
        try {
            new Thread(() -> start.startWS()).start();
            Debug.log("INIT: WebSocket server started.");
        } catch (Exception e) {
            Debug.log("INIT: WS Server encountered an error during startup: ");
            Debug.log(e.toString());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Debug.log("Beginning shutdown...");
            Debug.log("Saving player states...");
            //place for player states
            DB db;
            try {
                db = new DB();
            } catch (SQLException e) {
                Debug.log(e.toString());
                throw new RuntimeException(e);
            }
            Debug.log("Cleaning sessions...");
            db.cleanSessions();
            Debug.log("Server shutting down...");
        }));
    }
}
//TODO server bail if it has too many messages: 503 with no message
//also frontend compatibility with this error^