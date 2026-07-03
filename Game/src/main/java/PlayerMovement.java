
import java.util.HashMap;

public class PlayerMovement {
    public static HashMap<String, Long> walkAvailableTime = new HashMap<String, Long>();
    static int[][] world = World.grid;
    static Websocket instance = Websocket.getInstance();
    public static void moveUp(String name) {
        if (walkingDelay(name)) return;
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()][coords.y()-1] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x(), coords.y()-1, Entity.entmap.get(name).speedMultiplier()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+"");
        message.put("y", coords.y()-1+"");
        instance.broadcastWS("202", message);
    }
    public static void moveDown(String name) {
        if (walkingDelay(name)) return;
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()][coords.y()+1] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x(), coords.y()+1, Entity.entmap.get(name).speedMultiplier()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+"");
        message.put("y", coords.y()+1+"");
        instance.broadcastWS("202", message);
    }
    public static void moveLeft(String name) {
        if (walkingDelay(name)) return;
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()-1][coords.y()] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x()-1, coords.y(), Entity.entmap.get(name).speedMultiplier()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()-1+"");
        message.put("y", coords.y()+"");
        instance.broadcastWS("202", message);
    }
    public static void moveRight(String name) {
        if (walkingDelay(name)) return;
        Entity.state coords = Entity.entmap.get(name);
        if (world[coords.x()+1][coords.y()] == 1) return;
        Entity.entmap.replace(name, new Entity.state(coords.x()+1, coords.y(), Entity.entmap.get(name).speedMultiplier()));
        //message building and sending
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        message.put("x", coords.x()+1+"");
        message.put("y", coords.y()+"");
        instance.broadcastWS("202", message);
    }
    private static Boolean walkingDelay(String name) {

        if (!walkAvailableTime.containsKey(name)) {
            walkAvailableTime.put(name, System.currentTimeMillis());
        }
        if (walkAvailableTime.get(name)<System.currentTimeMillis()) {
            float speed = Entity.entmap.get(name).speedMultiplier();
            float fltDelay = 200/speed;
            int delay = (int)fltDelay;
            walkAvailableTime.put(name, System.currentTimeMillis()+delay);
            return false;
        }
        return true;
    }
}
