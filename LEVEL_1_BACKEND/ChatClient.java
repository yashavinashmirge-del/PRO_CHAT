import java.io.*;
import java.net.*;

/**
 * ChatClient
 * - Connects to ChatServer over a TCP socket.
 * - Runs a background thread to receive messages in real time while the
 *   main thread reads user input and sends it.
 */
public class ChatClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.print("Enter your username: ");
            String username = stdIn.readLine();
            out.println(username);

            // Background thread: continuously listens for incoming messages
            Thread listener = new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            listener.setDaemon(true);
            listener.start();

            System.out.println("Connected to chat server. Type '/quit' to exit.");
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("/quit")) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("Could not connect to " + host + ":" + port + " - " + e.getMessage());
        }
    }
}
