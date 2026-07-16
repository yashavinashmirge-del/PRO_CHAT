package LEVEL_1_BACKEND;



import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread
{
    private Socket socket;
    private BufferedReader input;
    private PrintStream output;
    private String username;

    // List of all connected clients
    public static Vector<ClientHandler> clients = new Vector<>();

    public ClientHandler(Socket socket)
    {
        this.socket = socket;

        try
        {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output = new PrintStream(socket.getOutputStream());

            // First message from client is username
            username = input.readLine();

            clients.add(this);

            System.out.println(username + " joined the chat.");

            broadcast("*** " + username + " joined the chat ***");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Send message to this client
    public void sendMessage(String msg)
    {
        output.println(msg);
    }

    // Broadcast to all clients
    public void broadcast(String msg)
    {
        for(ClientHandler client : clients)
        {
            client.sendMessage(msg);
        }
    }

    @Override
    public void run()
    {
        String message;

        try
        {
            while((message = input.readLine()) != null)
            {
                System.out.println(username + " : " + message);

                // Save into database
                MessageDAO.saveMessage(username, message);

                // Send to everyone
                broadcast(username + " : " + message);
            }
        }
        catch(Exception e)
        {
            System.out.println(username + " disconnected.");
        }
        finally
        {
            try
            {
                clients.remove(this);

                broadcast("*** " + username + " left the chat ***");

                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}