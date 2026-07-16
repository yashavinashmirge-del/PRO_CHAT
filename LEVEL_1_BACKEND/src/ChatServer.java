package LEVEL_1_BACKEND;

import java.net.*;

public class ChatServer
{
    public static void main(String args[])
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(2100);

            System.out.println("=================================");
            System.out.println("      CHAT SERVER STARTED");
            System.out.println("=================================");
            System.out.println("Listening on Port : 2100");

            while(true)
            {
                Socket socket = serverSocket.accept();

                System.out.println("New Client Connected : "
                        + socket.getInetAddress());

                ClientHandler client = new ClientHandler(socket);

                client.start();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}