import java.sql.SQLException;
import java.util.HashMap;

public class Entity {
    static int defaultSpeed = 1;
    static HashMap<String, state> entmap = new HashMap<String, state>();
    static Websocket instance = Websocket.getInstance();
    static PlayerStates playerstates;
    static {
        try {
            playerstates = new PlayerStates();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Entity(String name) throws SQLException {
        HashMap<String, String> message = new HashMap<String, String>();
        //message declaration ^
        PlayerStates.state coords = playerstates.getCoords(name);
        message.put("unm", name);
        message.put("x", ""+coords.x());
        message.put("y", ""+coords.y());
        //message building ^
        entmap.put(name, new state(coords.x(),coords.y(), defaultSpeed));
        instance.broadcastWS("200", message);
    }
    public static void delEntity(String name, state state) throws SQLException {
        playerstates.saveLoc(name, state.x()+"", state.y()+"");
        //broadcast deluser>
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", name);
        instance.broadcastWS("201", message);
        //remove from here>
        entmap.remove(name);
        //remove from walking delay hashmap
        PlayerMovement.walkAvailableTime.remove(name);
    }
    public record state(int x, int y, int speedMultiplier) {}
}
//TODO add hotbar and events for swapping weapons in it (add it to state too)
//TODO add health to state and class health regen with a scheduler
//TODO change name to Player and make an Enemy or Entity class (Entity better)