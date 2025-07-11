package com.destaxa;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

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

            GenericPackager packager = new GenericPackager("iso87ascii.xml");

            ISOMsg isoRequest = new ISOMsg();
            isoRequest.setPackager(packager);
            isoRequest.unpack(rawRequest.getBytes());

            ISOMsg isoResponse = AuthorizationRules.process(isoRequest);

            if (isoResponse != null) {
                byte[] packed = isoResponse.pack();
                String responseText = new String(packed);
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
