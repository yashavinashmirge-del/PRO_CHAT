# Chat Messenger (Java , Socket Programming)

A real-time multi-client chat application built with raw TCP sockets, with
timestamp-based chat logging.

## Features

- **GUI** - `coming soon...`
- **Real-time messaging** — `ChatServer` accepts multiple clients concurrently
  (one thread per client) and instantly broadcasts each message to everyone
  else connected.
- **Timestamp-based chat logging** — every message and join/leave event is
  written with a `yyyy-MM-dd HH:mm:ss` timestamp to both the console and
  `chat_log.txt`.

## Files
- `ChatServer.java` — listens on a port, manages client connections, broadcasts
  messages, writes the log.
- `ChatClient.java` — connects to the server; a background thread receives
  messages while the main thread sends what you type.

## How to run

1. **Compile** (requires a full JDK, i.e. `javac` on your PATH):
   ```bash
   javac ChatServer.java ChatClient.java

   ```

2. **Start the server** (default port 5000):
   ```bash
   java ChatServer
   ```

3. **Start one or more clients**, each in its own terminal:
   ```bash
   java ChatClient
   # or specify host/port:
   java ChatClient <host> <port>
   ```
   You'll be prompted for a username, then anything you type is broadcast to
   everyone else connected. Type `/quit` to disconnect.

> Note: on Java 11+ you can also skip the compile step and run each file
> directly with `java ChatServer.java` / `java ChatClient.java` using the
> single-file source-launcher.

## How it works
- `ChatServer` opens a `ServerSocket` and spins up a new `ClientHandler`
  thread per accepted connection, so clients don't block one another.
- Each `ClientHandler` reads lines from its client's socket and calls
  `broadcast()`, which pushes the message to every other connected
  `ClientHandler`.
- A single synchronized `log()` method timestamps every event and writes it to
  both stdout and `chat_log.txt`, so the log stays consistent even with
  multiple client threads writing concurrently.
- `ChatClient` uses a daemon background thread purely for reading incoming
  broadcasts, so the UI never blocks — you can type a new message while
  others arrive.

## Tested behavior
This was compiled and run end-to-end (multiple simulated clients connecting,
chatting, and disconnecting) to confirm real-time broadcast delivery and
correct timestamped logging.
