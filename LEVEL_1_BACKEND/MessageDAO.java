package LEVEL_1_BACKEND;
import java.sql.*;

public class MessageDAO
{
    public static void saveMessage(String sender,String message)
    {
        try
        {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps =
            con.prepareStatement(
            "INSERT INTO Messages(Sender,Message) VALUES(?,?)");

            ps.setString(1,sender);
            ps.setString(2,message);

            ps.executeUpdate();

            con.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void displayHistory()
    {
        try
        {
            Connection con = DBConnection.getConnection();

            Statement st = con.createStatement();

            ResultSet rs =
            st.executeQuery("SELECT * FROM Messages");

            System.out.println("\n------ Chat History ------");

            while(rs.next())
            {
                System.out.println(
                rs.getString("Sender")
                +" : "
                +rs.getString("Message"));
            }

            System.out.println("--------------------------");

            con.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}