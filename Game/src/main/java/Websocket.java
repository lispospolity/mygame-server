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
    HashMap<WebSocket, String> connToName;
    public Websocket(InetSocketAddress address) {
        //helping values
        super(address); //address ws
        instance = this;
        users = new HashMap<WebSocket, String>(); //logged-in users map
        connToName = new HashMap<WebSocket, String>(); //helpful map for getting entity states
        pingstate = new HashMap<WebSocket, Boolean>();
        try {                                       //DB protocol
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            Debug.log(error);
            throw new RuntimeException(e);
        }
        //ping mechanism
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            List<WebSocket> closedConns = users.keySet().stream()
                    .filter(WebSocket::isClosed)
                    .toList();
            Set<String> toLogOut = new HashSet<>();
            //System.out.println("scheduler opened with pingstate: "+pingstate);
            //System.out.println("to logout: "+toLogOut);
            //System.out.println("users: "+users);
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
            if (!db.validSession(msg.get("a").toString())) {
                conn.close(1008, "Unauthorized");
                return;
            }
            //get all entities
            HashMap<String, Entity.state> entities = Entity.entmap;
            //get your name
            String name = db.getName(msg.get("a").toString());
            if (users.get(conn) == null) {
                if (!entities.containsKey(name)) {
                    try {
                        new Entity(name);
                    } catch (SQLException e) {
                        Debug.log(e.toString());
                    }
                }
                users.put(conn, msg.get("a").toString());
                connToName.put(conn, name);
            }
            Map<String, String> response = new HashMap<>();
            response.put("s", "ok");
            sendWS(conn, "255", response);
            pingstate.put(conn, true);
            //iterate on entities and send them all
            for (String entName:entities.keySet()) {
                String x = entities.get(entName).x()+"";
                String y = entities.get(entName).y()+"";
                Map<String, String> newEnt = new HashMap<>();
                newEnt.put("unm", entName);
                newEnt.put("x", x);
                newEnt.put("y", y);
                sendWS(conn, "200", newEnt);
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
        Debug.log("WS error from "+conn+" caught: "+ex);
    }

    @Override
    public void onStart() {
        Debug.log("INIT: Server WS started.");
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
        //System.out.println("logout started with data: "+token);
        users.entrySet().stream()
                .filter(entry -> token.equals(entry.getValue()))
                .findFirst()
                .ifPresent(entry -> {
                    String name = db.getName(token);
                    entry.getKey().close();
                    users.remove(entry.getKey());
                    connToName.remove(entry.getKey());
                    db.logOut(token);                       // sesja znika ZAWSZE
                    Entity.state s = Entity.entmap.get(name);
                    if (s == null) return;                  // guard tylko dla delEntity
                    try {
                        Entity.delEntity(name, s);
                    } catch (SQLException e) {
                        Debug.log(e.toString());
                    }
                });
    }
}
