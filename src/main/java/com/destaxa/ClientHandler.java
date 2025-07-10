package com.destaxa;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true
                )
        ) {

            String rawRequest = reader.readLine();
            if (rawRequest == null || rawRequest.isEmpty()) {
                System.out.println("Empty message received, ignored.");
                return;
            }

            System.out.println("Message received:\n" + rawRequest);

            ISOMessage isoRequest = ISOMessage.fromIsoString(rawRequest);

            ISOMessage isoResponse = AuthorizationRules.process(isoRequest);

            if (isoResponse != null) {
                String responseText = isoResponse.toIsoString();
                System.out.println("Sending response:\n" + responseText);
                writer.println(responseText);
            } else {
                System.out.println("Timeout exception...");
            }

        } catch (Exception e) {
            System.err.println("Error processing client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
