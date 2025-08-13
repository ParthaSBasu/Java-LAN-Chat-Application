# Java LAN Chat Application

A simple **LAN-based Chat Application** built with **Java NIO** and **Swing**, designed to allow multiple clients in the same network to communicate with each other in real time.  
Perfect for learning **socket programming**, **multi-threading**, and **Java networking fundamentals**.

---

## Features

- **Multi-Client Support** – Multiple clients can connect to the same server.
- **Real-Time Messaging** – Messages are sent and received instantly.
- **Java NIO Networking** – Uses `SocketChannel` and `ServerSocketChannel` for efficient communication.
- **Graphical User Interface (GUI)** – Built with **Java Swing**.
- **Auto IP Handling** – Only server port needs to be configured.
- **Threaded Server** – Handles clients concurrently using `ExecutorService`.

---

## Technologies Used

- **Java SE 8+**
- **Java NIO** (`SocketChannel`, `ServerSocketChannel`, `Channels`)
- **Swing** for GUI
- **ExecutorService** for concurrency

---

## How It Works

1. **Server**  
   - Listens on a given port (default: `5000`) for incoming connections.
   - Handles multiple clients simultaneously.
   - Broadcasts messages to all connected clients.

2. **Client**  
   - Connects to the server using the server’s IP and port.
   - Displays chat messages in a scrollable text area.
   - Sends typed messages to the server.

---


**Learning Goals**

This project demonstrates:
Basics of TCP socket programming in Java.
Use of Java NIO for non-blocking I/O.
Handling multiple clients using threads.
Building a simple GUI-based application with Swing.



