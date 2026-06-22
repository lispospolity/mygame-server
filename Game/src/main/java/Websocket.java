import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Websocket extends WebSocketServer{
    private static final Logger log = LoggerFactory.getLogger(Websocket.class);
    private static Websocket instance;
    DB db;
    HashMap<WebSocket, String> users;
    HashMap<WebSocket, Boolean> pingstate;
    public Websocket(InetSocketAddress address) {
        //helping values
        super(address); //address ws
        instance = this;
        users = new HashMap<WebSocket, String>(); //logged-in users map
        pingstate = new HashMap<WebSocket, Boolean>();
        try {                                       //DB protocol
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            Debug.Log(error);
            throw new RuntimeException(e);
        }
        //every 5 sec check if player is still on just in case
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            users.entrySet().removeIf(entry -> entry.getKey().isClosed());
            pingstate.entrySet().removeIf(entry -> !users.containsKey(entry.getKey()));//removing closed thingies
            if (users.isEmpty()) return;
            users.forEach((conn, token) -> {
                if (users.get(conn) == (null)) return;
                if (!pingstate.containsKey(conn)) return;
                if (!pingstate.get(conn)) {
                    new Thread (() -> LogOut(token)).start();
                } else {
                    pingstate.replace(conn, false);
                }
            } );
        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        users.put(conn, null);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        pingstate.replace(conn, false);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Gson gson = new Gson();
        Map msg = gson.fromJson(message, Map.class);
        //beggining of auth message
        if (msg.get("t").equals("0")) {
            if (!db.ValidSession(msg.get("a").toString())) {
                conn.close(1008, "Unauthorized");
                return;
            }
            //get all entities
            HashMap<String, Entity.state> entities = Entity.entmap;
            //get your name
            String Name = db.GetName(msg.get("a").toString());
            if (users.get(conn) == null) {
                if (!entities.containsKey(Name)) {
                    try {
                        new Entity(Name);
                    } catch (SQLException e) {
                        Debug.Log(e.toString());
                    }
                }
                users.put(conn, msg.get("a").toString());
            }
            Map<String, String> response = new HashMap<>();
            response.put("s", "ok");
            sendWS(conn, "255", response);
            pingstate.put(conn, true);
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
        if (users.get(conn) == null) return;       //last check if user is authorized
        if (msg.get("t").equals("1")) {
            System.out.println(msg);
            //TODO keypress
        }
        if (msg.get("t").equals("5")) {
            pingstate.replace(conn, true);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Debug.Log("WS error from "+conn+" caught: "+ex);
    }

    @Override
    public void onStart() {
        Debug.Log("INIT: Server WS started.");
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
            if (conn.isClosed()) continue;
            if (users.get(conn)==null) continue;
            conn.send(gson.toJson(data));
        }
    }
    public static Websocket getInstance() {
        return instance;
    }
    public void LogOut(String token) {
        users.entrySet().stream()
                .filter(entry -> token.equals(entry.getValue()))
                .findFirst()
                .ifPresent(entry -> {
                    entry.getKey().close();
                    String name = db.GetName(users.get(entry.getKey()));
                    users.remove(entry.getKey());
                    try {
                        Entity.DelEntity(name, Entity.entmap.get(name));
                    } catch (SQLException e) {
                        String error = e.toString();
                        Debug.Log(error);
                    }
                    db.LogOut(token);
                });
    }
}
