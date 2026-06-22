import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Http {
    public static void main(String[] args) throws IOException {
        Debug.Log("INIT: API starting...");
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/session", new LogoutHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/user", new DelaccountHandler());
        server.setExecutor(null); // uses default
        try {
            server.start();
        } catch (Exception e) {
            Debug.Log("INIT: API encountered an error during startup:");
            Debug.Log(e.toString());
        }
        Debug.Log("INIT: API started.");
        RenderWorld.LoadWorld();
        try {
            new Thread(() -> start.StartWS()).start();
        } catch (Exception e) {
            Debug.Log("INIT: WS Server encountered an error during startup:");
            Debug.Log(e.toString());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Debug.Log("Beginning shutdown...");
            Debug.Log("Saving player states...");
            //place for player states
            DB db;
            try {
                db = new DB();
            } catch (SQLException e) {
                Debug.Log(e.toString());
                throw new RuntimeException(e);
            }
            Debug.Log("Cleaning sessions...");
            db.CleanSessions();
            Debug.Log("Server shutting down...");
        }));
    }
}
//TODO finish frontend