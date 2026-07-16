package LEVEL_1_BACKEND;

import java.io.*;
import java.net.*;

public class ChatClient
{
    public static void main(String args[])
    {
        try
        {
            Socket socket = new Socket("localhost", 2100);

            BufferedReader keyboard =
                    new BufferedReader(
                    new InputStreamReader(System.in));

            BufferedReader serverInput =
                    new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintStream serverOutput =
                    new PrintStream(socket.getOutputStream());

            System.out.println("===============================");
            System.out.println("      CHAT CLIENT");
            System.out.println("===============================");

            System.out.print("Enter Username : ");
            String username = keyboard.readLine();

            // Send username to server
            serverOutput.println(username);

            // Thread to receive messages
            Thread receive = new Thread()
            {
                public void run()
                {
                    try
                    {
                        String msg;

                        while((msg = serverInput.readLine()) != null)
                        {
                            System.out.println(msg);
                        }
                    }
                    catch(Exception e)
                    {
                        System.out.println("Disconnected from server.");
                    }
                }
            };

            receive.start();

            // Send messages
            while(true)
            {
                String message = keyboard.readLine();

                if(message.equalsIgnoreCase("exit"))
                {
                    socket.close();
                    System.exit(0);
                }

                serverOutput.println(message);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}