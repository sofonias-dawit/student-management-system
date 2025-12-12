// DB.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    // IMPORTANT: Replace with your actual database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/amit?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the MySQL JDBC driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}