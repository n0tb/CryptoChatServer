package CryptoChatServer;

import java.sql.*;
import java.util.List;

public class ChatDao {

    private Connection connection;
    private List<String> clients;

    public ChatDao(List<String> clients) {
        this.connection = getPSqlConnection();
        this.clients = clients;
    }

    public void registration(String login, String pass) {
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO clients (login, pass, online) " +
                    "VALUES ('" + login + "', '" + pass + "', '" + true + "')");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean login(String login, String pass) {
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.execute("select login from clients WHERE " +
                    "login='" + login + "'" + " and pass='" + pass + "'");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                if (resultSet.getString("login") != null) {
                    statement.executeUpdate("UPDATE clients SET online=TRUE " +
                            "WHERE login='" + login + "'");
                    return true;
                } else break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getUsersOnline() {
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT DISTINCT login from clients WHERE online=TRUE");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                if (!clients.contains(resultSet.getString("login")))
                    clients.add(resultSet.getString("login"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    public void setPubKey(String login, String pass, String pubKeyBase64Str) {
        try {
            PreparedStatement ps = connection
                    .prepareStatement("UPDATE clients SET pubkey=? WHERE login=? and pass=?");
            ps.setString(1, pubKeyBase64Str);
            ps.setString(2, login);
            ps.setString(3, pass);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPubKeyRecip(String recipient) {
        String pubKeyRecip = null;
        try {
            PreparedStatement ps =
                    connection.prepareStatement("SELECT pubkey FROM clients WHERE login=?");
            ps.setString(1, recipient);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    pubKeyRecip = rs.getString(1);
                }
                rs.close();
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubKeyRecip;
    }

    public void quit(String login, String pass) {
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("UPDATE clients SET online=FALSE WHERE " +
                    "login='" + login + "'" + " and pass='" + pass + "'");
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getPSqlConnection() {
        try {
            DriverManager.registerDriver
                    ((Driver) Class.forName("org.postgresql.Driver").newInstance());
            StringBuilder url = new StringBuilder();
            url.
                    append("jdbc:postgresql://").
                    append("192.168.0.110:").
                    append("5432/").
                    append("chatdb?").
                    append("useSSL=false&").
                    append("user=postgres");
//                    append("password=123");

//            System.out.println("Url: " + url + "\n");
            return DriverManager.getConnection(url.toString());
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
