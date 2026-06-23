import org.java_websocket.WebSocket;

import java.util.HashMap;

public class PlayerMovement {
    static int[][] world = World.grid;
    static Websocket instance = Websocket.getInstance();
    public static void moveUp(String name) {
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()][coords.y()-1] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x(), coords.y()-1));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+"");
        message.put("y", coords.y()-1+"");
        instance.broadcastWS("202", message);
    }
    public static void moveDown(String name) {
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()][coords.y()+1] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x(), coords.y()+1));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+"");
        message.put("y", coords.y()+1+"");
        instance.broadcastWS("202", message);
    }
    public static void moveLeft(String name) {
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()-1][coords.y()] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x()-1, coords.y()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()-1+"");
        message.put("y", coords.y()+"");
        instance.broadcastWS("202", message);
    }
    public static void moveRight(String name) {
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()+1][coords.y()] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x()+1, coords.y()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+1+"");
        message.put("y", coords.y()+"");
        instance.broadcastWS("202", message);
    }
}
