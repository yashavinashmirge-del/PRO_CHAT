package LEVEL_1_BACKEND;
import java.sql.*;

public class UserDAO
{
    public static boolean register(String username,String password)
    {
        try
        {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps =
            con.prepareStatement(
            "INSERT INTO Users(Username,Password) VALUES(?,?)");

            ps.setString(1,username);
            ps.setString(2,password);

            ps.executeUpdate();

            con.close();

            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean login(String username,String password)
    {
        try
        {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps =
            con.prepareStatement(
            "SELECT * FROM Users WHERE Username=? AND Password=?");

            ps.setString(1,username);
            ps.setString(2,password);

            ResultSet rs = ps.executeQuery();

            boolean flag = rs.next();

            con.close();

            return flag;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}