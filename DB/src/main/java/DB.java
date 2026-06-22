import java.sql.*;
public class DB {
    Connection conn;
    PlayerStates playerStates;
    env.logincredentials login = env.envread(0);
    public DB() throws SQLException {
        conn = DriverManager.getConnection(
                login.url(),
                login.user(),
                login.password()
        );
        playerStates = new PlayerStates();
    }

    public void AddUser(String Name, String Hash) throws SQLException {
        if (IsRegistered(Name)) return;
        try (PreparedStatement statement = conn.prepareStatement("""
        INSERT INTO userspw(username, pwd_hash)
        VALUES (?, ?)
      """)) {
            playerStates.SaveLoc(Name, "1", "1");
            statement.setString(1, Name);
            statement.setString(2, Hash);
            int rowsInserted = statement.executeUpdate();
        }
    }

    public String GetPassword(String Name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT pwd_hash
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, Name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("pwd_hash");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DelUser(String Name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            DELETE 
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, Name);
            int result = statement.executeUpdate();
            if (result>0) playerStates.DelState(Name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean IsRegistered(String Name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, Name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void LogIn(String Name, String token, long time) {
        try (PreparedStatement statement = conn.prepareStatement("""
        INSERT INTO sessions(username, token, time)
        VALUES (?, ?, ?)
      """)) {
            statement.setString(1, Name);
            statement.setString(2, token);
            statement.setLong(3, time);
            int rowsInserted = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void LogOut(String token) {
        try (PreparedStatement statement = conn.prepareStatement("""
            DELETE 
            FROM sessions
            WHERE token = ?
        """)) {
            statement.setString(1, token);
            int result = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean LoggedIn(String Name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM sessions
            WHERE username = ?
        """)) {
            statement.setString(1, Name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public String GetName(String token) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM sessions
            WHERE token = ?
        """)) {
            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean ValidSession(String token) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM sessions
            WHERE token = ?
            """)) {
            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void CleanSessions() {
        try (PreparedStatement statement = conn.prepareStatement("""
            DELETE 
            FROM sessions
        """)) {
            int result = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}