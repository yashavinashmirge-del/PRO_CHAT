import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer
 * - Accepts multiple concurrent clients over TCP sockets.
 * - Broadcasts each incoming message to all connected clients in real time.
 * - Maintains a timestamp-based chat log (written to chat_log.txt and printed to console).
 */
public class ChatServer {

    private static final int PORT = 5000;
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "chat_log.txt";

    // Thread-safe set of all connected client handlers
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    // Single writer for the log file, shared across all client threads
    private static PrintWriter logWriter;

    public static void main(String[] args) {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Could not open log file: " + e.getMessage());
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            log("SERVER", "Server started");

            // Lets the person running the server type messages that get
            // broadcast to every connected client, same as any other sender.
            startServerConsoleSender();

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            logWriter.close();
        }
    }

    /** Writes a timestamped line to both console and the log file. */
    private static synchronized void log(String sender, String message) {
        String line = String.format("[%s] %s: %s",
                LocalDateTime.now().format(TS_FORMAT), sender, message);
        System.out.println(line);
        logWriter.println(line);
    }

    /**
     * Reads lines typed into the server's own console (System.in) on a
     * background thread and broadcasts each one to all connected clients,
     * labeled as coming from "SERVER". Runs for the life of the server.
     */
    private static void startServerConsoleSender() {
        Thread consoleThread = new Thread(() -> {
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                while ((line = consoleIn.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    log("SERVER", line);
                    broadcast("SERVER: " + line, null); // null = don't exclude anyone
                }
            } catch (IOException e) {
                System.err.println("Console reader error: " + e.getMessage());
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /** Sends a message to every connected client. */
    private static void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.send(message);
            }
        }
    }

    /** Handles a single client connection on its own thread. */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String username = "Anonymous";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = writer;

                // First line from the client is treated as their chosen username
                String firstLine = in.readLine();
                if (firstLine != null && !firstLine.isBlank()) {
                    username = firstLine.trim();
                }
                log("SERVER", username + " joined the chat");
                broadcast("*** " + username + " joined the chat ***", this);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    log(username, line);
                    broadcast(username + ": " + line, this);
                }
            } catch (IOException e) {
                System.err.println("Connection error for " + username + ": " + e.getMessage());
            } finally {
                clients.remove(this);
                log("SERVER", username + " left the chat");
                broadcast("*** " + username + " left the chat ***", this);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
