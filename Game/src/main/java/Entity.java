import java.sql.SQLException;
import java.util.HashMap;

public class Entity {
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

    public Entity(String Name) throws SQLException {
        HashMap<String, String> message = new HashMap<String, String>();
        //message declaration ^
        PlayerStates.state coords = playerstates.GetCoords(Name);
        message.put("unm", Name);
        message.put("x", ""+coords.x());
        message.put("y", ""+coords.y());
        //message building ^
        entmap.put(Name, new state(coords.x(),coords.y()));
        instance.broadcastWS("200", message);
    }
    public static void DelEntity(String Name, state state) throws SQLException {
        System.out.println(instance);
        playerstates.SaveLoc(Name, state.x()+"", state.y()+"");
        //broadcast deluser>
        HashMap<String, String> message = new HashMap<String, String>();
        message.put("unm", Name);
        instance.broadcastWS("201", message);
        //remove from here>
        entmap.remove(Name);
    }
    public record state(int x, int y) {}
}
