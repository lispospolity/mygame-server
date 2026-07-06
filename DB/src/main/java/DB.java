import java.sql.*;
public class DB {
    Connection conn;
    PlayerStates playerStates;
    Env.LoginCredentials login = Env.envRead(0);
    public DB() throws SQLException {
        conn = DriverManager.getConnection(
                login.url(),
                login.user(),
                login.password()
        );
        playerStates = new PlayerStates();
    }

    public void addUser(String name, String hash) {
        if (isRegistered(name)) return;
        try (PreparedStatement statement = conn.prepareStatement("""
        INSERT INTO userspw(username, pwd_hash)
        VALUES (?, ?)
      """)) {
            playerStates.saveLoc(name, "1", "1");
            statement.setString(1, name);
            statement.setString(2, hash);
            int rowsInserted = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPassword(String name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT pwd_hash
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("pwd_hash");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delUser(String name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            DELETE 
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, name);
            int result = statement.executeUpdate();
            if (result>0) playerStates.delState(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean isRegistered(String name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM userspw
            WHERE username = ?
        """)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void logIn(String name, String token, long time) {
        try (PreparedStatement statement = conn.prepareStatement("""
        INSERT INTO sessions(username, token, time)
        VALUES (?, ?, ?)
      """)) {
            statement.setString(1, name);
            statement.setString(2, token);
            statement.setLong(3, time);
            int rowsInserted = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void logOut(String token) {
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

    public Boolean loggedIn(String name) {
        try (PreparedStatement statement = conn.prepareStatement("""
            SELECT username
            FROM sessions
            WHERE username = ?
        """)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public String getName(String token) {
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
    public boolean validSession(String token) {
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
    public void cleanSessions() {
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