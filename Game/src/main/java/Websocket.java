import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.google.gson.Gson;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Websocket extends WebSocketServer{
    private static Websocket instance;
    DB db;
    HashMap<WebSocket, String> users;
    public Websocket(InetSocketAddress address) {
        //helping values
        super(address); //address ws
        instance = this;
        users = new HashMap<WebSocket, String>();   //logged-in users map
        try {                                       //DB protocol
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            DebugLog.Log(error);
            throw new RuntimeException(e);
        }
        //every 5 sec check if player is still on just in case
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            users.forEach((conn, token) -> {
                if (token != null && !db.ValidSession(token)) conn.close();
            } );
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        users.put(conn, null);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String token = users.get(conn);
        String Name = db.GetName(token);
        Entity.state state = Entity.entmap.get(Name);
        try {
            Entity.DelEntity(Name, state);
        } catch (SQLException e) {
            String error = e.toString();
            DebugLog.Log(error);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Gson gson = new Gson();
        Map msg = gson.fromJson(message, Map.class);
        //beggining of auth message
        if (users.get(conn) == null) {
            if (!db.ValidSession(msg.get("a").toString())) {
                conn.close(1008, "Unauthorized");
                return;
            }
            Map<String, String> response = new HashMap<>();
            response.put("s", "ok");
            sendWS(conn, "255", response);
            String Name = db.GetName(msg.get("a").toString());
            try {
                new Entity(Name);
            } catch (SQLException e) {
                DebugLog.Log(e.toString());
            }
            users.put(conn, msg.get("a").toString());
            //get all entities
            HashMap<String, Entity.state> entities = Entity.entmap;
            //iterate on entities and send them all
            for (String entname:entities.keySet()) {
                String x = entities.get(entname).x()+"";
                String y = entities.get(entname).y()+"";
                Map<String, String> newent = new HashMap<>();
                newent.put("unm", entname);
                newent.put("x", x);
                newent.put("y", y);
                sendWS(conn, "200", newent);
            }
            return;
        }
        //end of auth message
        if (msg.get("t").equals("1")) {
            System.out.println(msg);
            //TODO walk
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        DebugLog.Log("WS error from "+conn+" caught: "+ex);
    }

    @Override
    public void onStart() {
        DebugLog.Log("Server WS started.");
    }
    public void sendWS(WebSocket conn, String type, Map<String, String> data) {
        //TODO for later make it compact with bytes arrays
        Gson gson = new Gson();
        data.put("t", type);
        conn.send(gson.toJson(data));
    }
    public void broadcastWS(String type, Map<String, String> data) {
        Gson gson = new Gson();
        data.put("t", type);
        for (WebSocket conn : users.keySet()) {
            if (users.get(conn)==null) continue;
            conn.send(gson.toJson(data));
        }
    }
    public static Websocket getInstance() {
        return instance;
    }
    public void LogOut(String token) {
        users.entrySet().stream()
                .filter(entry -> entry.getValue().equals(token))
                .findFirst()
                .ifPresent(entry -> {
                    entry.getKey().close();
                    String name = db.GetName(users.get(entry.getKey()));
                    try {
                        Entity.DelEntity(name, Entity.entmap.get(name));
                    } catch (SQLException e) {
                        String error = e.toString();
                        DebugLog.Log(error);
                        throw new RuntimeException(e);
                    }
                    users.remove(entry.getKey());
                });
    }
}
