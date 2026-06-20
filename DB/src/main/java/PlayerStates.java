import java.sql.*;
public class PlayerStates {
    Connection conn;

    public PlayerStates() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/userstates",
                "root",
                "ZAQ!2wsx"
        );
    }

    public void SaveLoc(String Name, String x, String y) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("""
        INSERT INTO userloc(username, x, y)
        VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y)
      """)) {
            statement.setString(1, Name);
            statement.setString(2, x);
            statement.setString(3, y);
            int rowsInserted = statement.executeUpdate();
        }
    }
    public void DelState(String Name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            DELETE 
            FROM userloc
            WHERE username = ?
        """)) {
            statement.setString(1, Name);
            int result = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public state GetCoords(String Name) {
        try (PreparedStatement statement1 = conn.prepareStatement("""
            SELECT x, y
            FROM userloc
            WHERE username = ?
        """)) {
            statement1.setString(1, Name);
            ResultSet resultSet = statement1.executeQuery();
            if (resultSet.next()) {
                return new state(resultSet.getInt("x"), resultSet.getInt("y"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    record state(int x, int y) {}
}
//TODO hide the DB login parameters