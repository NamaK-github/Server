package server;

import java.sql.*;

/**
 * Created by NamaK on 28.02.17.
 */
public class SQLHandler {
    private static Connection connection;
    private static final String DB_LOGIN = "root";
    private static final String DB_PASSWORD = "12321";

    public static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat?useSSL=false", DB_LOGIN, DB_PASSWORD);

    }

    public static String getNick(String login, String password) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "select nick from `users` where login = '" + login
                        + "' and password = '" + password + "'");
        String nick = null;
        while (rs.next()) {
            nick = rs.getString(1);
        }
        return nick;
    }

    public static void closeConnection(){
        if (connection!=null){
            try {
                connection.close();
                System.out.println("Соединение с базой данных разорвано.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
