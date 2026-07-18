# 💬 PRO_CHAT — Java Socket Programming Chat Applications


A two-stage Java **TCP socket programming** project: a bare-bones single-client chat (`LEVEL_0_CUI`) that demonstrates raw socket I/O, followed by a production-leaning multi-client broadcast chat server (`LEVEL_1_MULTI_CLIENT`) with concurrency and timestamped logging. Built to demonstrate core `java.net`, `java.io`, and `java.util.concurrent` fundamentals without any external frameworks.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Repository Structure](#-repository-structure)
- [Architecture](#-architecture)
- [Level Comparison](#-level-comparison)
- [Prerequisites](#️-prerequisites)
- [Installation](#-installation)
- [Level 0 — Basic Chat](#1️⃣-level_0_cui--basic-chat)
- [Level 1 — Multi-Client Broadcast Chat](#2️⃣-level_1_multi_client--multi-client-broadcast-chat-with-logging)
- [Message Protocol](#-message-protocol)
- [Core Concepts Used](#-core-concepts-used)
- [Troubleshooting](#-troubleshooting)
- [Tech Stack](#️-tech-stack)
- [Roadmap](#-roadmap)
  
---

## 🔍 Overview

`PRO_CHAT` implements the same idea — a chat application over TCP sockets — at two levels of sophistication:

- **`LEVEL_0_CUI`**: the foundation. One client, one server, strict turn-based messaging. Good for understanding how `Socket`/`ServerSocket` and blocking streams work with zero extra moving parts.
- **`LEVEL_1_MULTI_CLIENT`**: the real thing. A multi-threaded server that accepts any number of clients, broadcasts messages in real time, lets the server operator chime in, and logs every event with a timestamp.

Each folder is self-contained and independently runnable — pick whichever matches what you're learning or building on.

## 📂 Repository Structure

```
PRO_CHAT/
├── README.md
│
├── LEVEL_0_CUI/
│   ├── ChatClient.java
│   └── ChatServer.java
│
└── LEVEL_1_MULTI_CLIENT/
    ├── ChatClient.java
    ├── ChatServer.java
    └── chat_log.txt          # auto-generated at runtime (gitignored)
```

> `chat_log.txt` is created automatically the first time `ChatServer` runs in `LEVEL_1_MULTI_CLIENT` — it isn't part of the source and doesn't need to be committed (see [`.gitignore`](.gitignore)).

## 🏗 Architecture

**Level 0 — one client, strict alternation:**

```
┌────────────┐   connect (port 2100)   ┌────────────┐
│ ChatClient │ ───────────────────────▶│ ChatServer │
│            │◀─────────────────────── │            │
└────────────┘   send → reply → send   └────────────┘
```

**Level 1 — many clients, real-time broadcast:**

```
┌──────────┐          ┌─────────────────────────────┐
│ Client A │◀───────▶│                             │
└──────────┘          │                             │       ┌───────────────┐
┌──────────┐          │   ChatServer (port 5000)    │────── │ chat_log.txt  │
│ Client B │◀───────▶│  1 thread per ClientHandler │       │ (timestamped) │
└──────────┘          │      + broadcast() fan-out  │       └───────────────┘
┌──────────┐          │                             │
│ Client C │◀───────▶│  Server console thread ───  ┼──▶ typed messages also
└──────────┘          └─────────────────────────────┘        broadcast to all
```



## 📊 Level Comparison

| | LEVEL_0_CUI | LEVEL_1_MULTI_CLIENT |
|---|---|---|
| **Clients supported** | 1 | Unlimited (concurrent) |
| **Concurrency model** | Single-threaded | Multi-threaded (1 thread per client + 1 console thread) |
| **Message flow** | Synchronous ping-pong (take turns) | Real-time async broadcast |
| **Logging** | ❌ None | ✅ Timestamped, written to `chat_log.txt` |
| **Server can send unprompted messages** | ❌ No (only replies) | ✅ Yes (dedicated console thread) |
| **Port** | 2100 | 5000 |
| **Package** | `LEVEL_0_CUI` | *(default package)* |
| **Best for learning** | Socket basics, blocking I/O | Threading, shared state, broadcast patterns |

## ⚙️ Prerequisites

- [Java Development Kit (JDK)](https://adoptium.net/) 8 or above — a full JDK, not just a JRE, since you need `javac`
- `javac` and `java` available on your system PATH
- Verify with:
  ```bash
  java -version
  javac -version
  ```
- Two or more terminal windows/tabs (one for the server, one per client)

## 📥 Installation

```bash
git clone https://github.com/<your-username>/PRO_CHAT.git
cd PRO_CHAT
```

No external dependencies or build tools required — pure JDK.

---

## 1️⃣ LEVEL_0_CUI — Basic Chat

A simple one-to-one client-server chat built directly with `Socket` and `ServerSocket`. Demonstrates the fundamentals of TCP communication: connecting, sending, and receiving messages line by line, in strict turn order.

**Features**
- Single client ↔ single server
- Synchronous ping-pong style messaging (client sends → server replies → repeat)
- Minimal, beginner-friendly code — no threads, no collections, just raw sockets and streams
- Graceful termination via the `end` keyword

**Design limitations** *(intentional — this is the "level 0" baseline)*
- Only one client can connect at a time; a second connection attempt will hang until the first disconnects
- Both sides must strictly alternate send/receive, or the app blocks on `readLine()`
- No message history or logging

### Run

```bash
javac LEVEL_0_CUI/ChatServer.java LEVEL_0_CUI/ChatClient.java
```

**Terminal 1 — server:**
```bash
java LEVEL_0_CUI.ChatServer
```
```
Server application is running...
Server is waiting at port 2100
```

**Terminal 2 — client:**
```bash
java LEVEL_0_CUI.ChatClient
```
```
Client application is running...
Connection is succesful with server
```

Type on the client, press Enter — the server displays it and can type a reply back. Type `end` on the client to close the session.

---

## 2️⃣ LEVEL_1_MULTI_CLIENT — Multi-Client Broadcast Chat with Logging

A real-time, multi-client chat application supporting many concurrent clients with timestamp-based logging — closer to how a real chat server (a simplified IRC/Slack channel) actually behaves.

**Features**
- **Multi-client support** — the server accepts and handles many clients at once, one dedicated thread per connection (`ClientHandler`)
- **Real-time broadcast** — any message from a client (or the server operator) is instantly relayed to everyone else connected; no waiting for turns
- **Server-side messaging** — the person running the server can type messages that get broadcast to all clients, labeled `SERVER:`
- **Timestamp-based logging** — every message and join/leave event is written with a `yyyy-MM-dd HH:mm:ss` timestamp to both the console and `chat_log.txt`
- **Thread-safe client tracking** — uses `ConcurrentHashMap.newKeySet()` so clients can join/leave safely while broadcasts are in flight
- **Graceful join/leave announcements** — `*** <username> joined/left the chat ***` broadcast automatically

### Run

```bash
javac LEVEL_1_MULTI_CLIENT/ChatServer.java LEVEL_1_MULTI_CLIENT/ChatClient.java
```

**Terminal 1 — server** (default port 5000):
```bash
cd LEVEL_1_MULTI_CLIENT
java ChatServer
```

**Terminal 2, 3, 4... — one per client:**
```bash
java ChatClient
# or specify host/port explicitly:
java ChatClient <host> <port>
```

You'll be prompted for a username; anything you type afterward is broadcast to everyone else connected. Type `/quit` to disconnect. Anything typed directly into the **server's** own terminal is broadcast to all clients as `SERVER: <message>`.

### Sample Session Log

```
[2026-07-18 19:12:20] SERVER: Server started
[2026-07-18 19:12:25] SERVER: Alice joined the chat
[2026-07-18 19:12:30] Alice: Hello everyone!
[2026-07-18 19:12:35] SERVER: Bob joined the chat
[2026-07-18 19:12:40] Bob: Hi Alice!
[2026-07-18 19:12:42] SERVER: Dinner in 5 mins!
[2026-07-18 19:12:45] SERVER: Alice left the chat
```

---

## 📡 Message Protocol

Both levels use plain newline-delimited text over the socket (no custom binary framing) — every `println()` on one side corresponds to one `readLine()` on the other.

**Level 0** — raw text passthrough; the server displays exactly what it receives, no formatting added.

**Level 1** — the server prefixes every broadcast so clients can tell who's talking:

| Wire format | Meaning |
|---|---|
| `<first line sent by client>` | Treated as the client's username (not broadcast) |
| `<username>: <message>` | A regular chat message from that client |
| `SERVER: <message>` | A message typed by the server operator |
| `*** <username> joined the chat ***` | Join announcement |
| `*** <username> left the chat ***` | Leave announcement |
| `/quit` (sent by client) | Client-initiated disconnect signal |

## 🧠 Core Concepts Used

| Concept | Where it's used |
|---|---|
| **`ServerSocket` / `Socket`** | Establishing TCP connections between client and server |
| **Blocking I/O (`BufferedReader.readLine()`)** | Reading incoming messages line by line |
| **Threads (`Thread`, `Runnable`)** | Handling multiple clients concurrently (Level 1); listening for incoming messages without blocking user input (both client apps) |
| **Daemon threads** | Background listener/console threads that shouldn't prevent the JVM from exiting |
| **`ConcurrentHashMap.newKeySet()`** | Thread-safe collection of connected clients (Level 1) |
| **`synchronized` methods** | Preventing garbled/interleaved log writes from multiple threads (Level 1) |
| **`try-with-resources`** | Automatically closing sockets and streams |
| **`PrintWriter(..., autoFlush=true)`** | Ensuring each message is actually sent immediately, not buffered |

## 🛠 Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Error: Could not find or load main class ChatServer` | Run `java ChatServer` without compiling first | Run `javac ChatServer.java` to produce a `.class` file, *then* `java ChatServer` |
| `Address already in use` / `BindException` | Another process already bound to port 2100/5000 | Close the other process, or wait a few seconds for the OS to release the port |
| Client connects but nothing happens (Level 0) | Level 0 is strictly turn-based | Expected — the server can't send until it's received one message, and vice versa |
| `Connection refused` | Server isn't running yet, or wrong host/port | Start the server first, confirm the port matches, then start the client |
| `javac` not recognized | Only a JRE is installed, not a full JDK | Install a JDK from [Adoptium](https://adoptium.net/) and add its `bin` folder to PATH |
| Log file not appearing | Working directory differs from where you expect | `chat_log.txt` is created in whatever directory you *ran* `java ChatServer` from |

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java (JDK 8+) | Core programming language |
| `java.net.Socket` / `ServerSocket` | TCP connection handling |
| `java.io` | Stream-based message I/O |
| `java.util.concurrent` | Thread-safe client management (Level 1) |
| `java.time` | Timestamp formatting for logs (Level 1) |

## 🔮 Roadmap

- [x] Basic single-client chat (`LEVEL_0_CUI`)
- [x] Multi-client broadcast chat with logging (`LEVEL_1_MULTI_CLIENT`)
    # coming soon......
- [ ] GUI-based chat interface (Swing/JavaFX)
- [ ] Private/direct messaging between clients
- [ ] Message encryption (TLS sockets)
- [ ] Persistent chat history reload on reconnect
- [ ] Configurable port via command-line args / config file
