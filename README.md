# 🚨 Emergency Response STOMP Platform

![Java](https://img.shields.io/badge/Java-17+-orange)
![C++](https://img.shields.io/badge/C++-17+-blue)
![Architecture](https://img.shields.io/badge/Architecture-Client%2FServer-success)
![Protocol](https://img.shields.io/badge/Protocol-STOMP-yellow)

## 📖 Overview
The **Emergency Response Platform** is a real-time, event-driven communication system designed to handle emergency reporting and broadcasting. It enables users to subscribe to specific emergency channels (e.g., fire, medical, police, natural disasters), report incidents, and receive live updates.

The system is built on a custom implementation of the **STOMP (Simple Text Oriented Messaging Protocol)** over TCP, featuring a high-performance **Java Backend** and a multi-threaded **C++ Client**.

## ✨ Technical Highlights
* **Robust Concurrency Models:** The server supports both **Thread-Per-Client (TPC)** for straightforward synchronous handling, and the **Reactor Pattern (Non-blocking I/O)** for high scalability and efficient resource management under heavy loads.
* **Generic Network Architecture:** Built using strict OOP principles. The network layer is completely decoupled from the application logic using Generic Interfaces (`MessageEncoderDecoder<T>` and `MessagingProtocol<T>`).
* **Network Fragmentation Handling:** Custom implementation of a dynamic byte-buffer to seamlessly handle TCP data fragmentation and reconstruct STOMP frames accurately.
* **Multi-threaded Client:** The C++ client utilizes standard library threads, mutexes, and condition variables to separate user keyboard input from asynchronous network message receiving without race conditions.

## 🏗️ Architecture

### Server (Java)
* Implements a centralized STOMP message broker.
* Maintains client state, manages subscriptions via concurrent data structures, and routes messages to relevant subscribers.
* Processes raw TCP streams into structured `StompFrame` objects.

### Client (C++)
* Console-based user interface.
* Asynchronously handles server communication while accepting concurrent user input.
* Parses emergency JSON files and serializes them into STOMP SEND frames.
* Generates localized summary reports based on received channel data.

## 🚀 Installation & Setup

### Prerequisites
* Java 17+ and Maven (for the Server)
* C++17+ and Make (for the Client)
* Linux environment (or WSL/Docker on Windows)

### 1. Server Setup
Navigate to the server directory, compile, and run:
```bash
cd server
mvn compile
# To run in Thread-Per-Client mode:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
# OR to run in Reactor mode:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 reactor"

2. Client Setup
Navigate to the client directory, compile, and run:
cd client
make
./bin/StompESClient
💻 Client Commands
Once the client is running, you can use the following commands:
Login: login {host:port} {username} {password}
Subscribe: join {channel_name}
Unsubscribe: exit {channel_name}
Report Incident: report {file_path.json}
Generate Summary: summary {channel_name} {user} {destination_file}
Logout: logout

📂 Project Structure
Emergency-Response-STOMP-Server/
├── server/                 # Java Backend
│   ├── src/main/java/...   # Server logic, Reactor, Protocols
│   └── pom.xml             # Maven configuration
├── client/                 # C++ Frontend
│   ├── src/                # Client logic, Threads, Sockets
│   ├── include/            # Header files
│   └── Makefile            # Build configuration
└── README.md
