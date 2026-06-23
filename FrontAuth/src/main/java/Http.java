import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Http {
    public static void main(String[] args) throws IOException {
        Debug.log("INIT: API starting...");
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/session", new LogoutHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/user", new DelaccountHandler());
        server.setExecutor(null); // uses default
        try {
            server.start();
        } catch (Exception e) {
            Debug.log("INIT: API encountered an error during startup:");
            Debug.log(e.toString());
        }
        Debug.log("INIT: API started.");
        World.LoadWorld();
        try {
            new Thread(() -> start.startWS()).start();
        } catch (Exception e) {
            Debug.log("INIT: WS Server encountered an error during startup:");
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
//TODO rate limiting for later