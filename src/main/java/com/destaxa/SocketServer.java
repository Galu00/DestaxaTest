package com.destaxa;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Authorizer listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            System.err.println("Error on socket server: " + e.getMessage());
        }
    }
}
