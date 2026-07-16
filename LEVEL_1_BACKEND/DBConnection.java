package LEVEL_1_BACKEND;
import java.sql.*;

public class DBConnection
{
    private static final String URL =
            "jdbc:mysql://localhost:3306/ChatDB";

    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL,USER,PASSWORD);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}