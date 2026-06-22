import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.*;

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
        //ping mechanism
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            List<WebSocket> closedConns = users.keySet().stream()
                    .filter(WebSocket::isClosed)
                    .toList();
            Set<String> toLogOut = new HashSet<>();
            for (WebSocket closed : closedConns) {
                String token = users.get(closed);
                if (token == null) {
                    users.remove(closed);
                    pingstate.remove(closed);
                    continue;
                }
                boolean hasOpenConn = users.keySet().stream()
                        .anyMatch(c -> c.isOpen() && token.equals(users.get(c)));
                if (hasOpenConn) {
                    users.remove(closed);
                    pingstate.remove(closed);
                } else {
                    toLogOut.add(token);
                }
            }

            users.forEach((conn, token) -> {
                if (token == null) return;
                if (!pingstate.containsKey(conn)) return;
                if (!pingstate.get(conn)) {
                    toLogOut.add(token);   // put away to log out
                } else {
                    pingstate.replace(conn, false);
                }
            });


            for (String token : toLogOut) {
                new Thread(() -> LogOut(token)).start();
                toLogOut.remove(token);
            }
        }, 0, 30, TimeUnit.SECONDS);
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
        //TODO client->server optimize with bytes arrays
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
        if (msg.get("t").equals("5")) {
            pingstate.replace(conn, true);
        }
        EntityEvent.handle(conn, msg);
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
        //TODO server->client optimize with bytes arrays
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
                    if (!Entity.entmap.containsKey(name)) return;
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
