package com.destaxa;

public class AuthorizerApplication {
    public static void main(String[] args) {
        SocketServer socketServer = new SocketServer(5050);
        socketServer.start();
    }
}
