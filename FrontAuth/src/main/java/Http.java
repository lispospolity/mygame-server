import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Http {
    public static void main(String[] args) throws IOException {
        DebugLog.Log("API starting...");
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/session", new LogoutHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/user", new DelaccountHandler());
        server.setExecutor(null); // uses default
        try {
            server.start();
        } catch (Exception e) {
            DebugLog.Log("API encountered an error during startup:");
            DebugLog.Log(e.toString());
        }
        DebugLog.Log("API started.");
        RenderWorld.LoadWorld();
        try {
            new Thread(() -> start.StartWS()).start();
        } catch (Exception e) {
            DebugLog.Log("WS Server encountered an error during startup:");
            DebugLog.Log(e.toString());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DebugLog.Log("Beginning shutdown...");
            DebugLog.Log("Saving player states...");
            //place for player states
            DB db = null;
            try {
                db = new DB();
            } catch (SQLException e) {
                DebugLog.Log(e.toString());
                throw new RuntimeException(e);
            }
            DebugLog.Log("Cleaning sessions...");
            db.CleanSessions();
            DebugLog.Log("Server shutting down...");
        }));
    }
}
//TODO finish frontend