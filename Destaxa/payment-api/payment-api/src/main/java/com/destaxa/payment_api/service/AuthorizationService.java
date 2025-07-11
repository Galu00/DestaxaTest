package com.destaxa.payment_api.service;

import com.destaxa.payment_api.iso.ISOMessage;
import com.destaxa.payment_api.model.PaymentRequest;
import com.destaxa.payment_api.model.PaymentResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AuthorizationService {

    private static final String HOST = "localhost";
    private static final int PORT = 5050;

    public PaymentResponse authorizePayment(PaymentRequest request, String identifier) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            ISOMessage isoRequest = buildIsoRequest(request, identifier);
            out.println(isoRequest.toIsoString());

            String responseLine = in.readLine();
            if (responseLine == null) {
                throw new RuntimeException("Timeout: Nenhuma resposta do autorizador.");
            }

            ISOMessage isoResponse = ISOMessage.fromIsoString(responseLine);
            return buildPaymentResponse(isoResponse);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao conectar com autorizador: " + e.getMessage(), e);
        }
    }

    private ISOMessage buildIsoRequest(PaymentRequest req, String identifier) {
        LocalDateTime now = LocalDateTime.now();

        ISOMessage iso = new ISOMessage("0200");
        iso.setField(2, req.getCardNumber());
        iso.setField(3, req.getInstallments() > 1 ? "003001" : "003000");
        iso.setField(4, formatValue(req.getValue()));
        iso.setField(7, now.format(DateTimeFormatter.ofPattern("MMddHHmmss")));
        iso.setField(11, generateNsu());
        iso.setField(12, now.format(DateTimeFormatter.ofPattern("HHmmss")));
        iso.setField(13, now.format(DateTimeFormatter.ofPattern("MMdd")));
        iso.setField(14, String.format("%02d%02d", req.getExpYear(), req.getExpMonth()));
        iso.setField(42, identifier);
        iso.setField(67, String.format("%02d", req.getInstallments()));

        return iso;
    }

    private PaymentResponse buildPaymentResponse(ISOMessage iso) {
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(UUID.randomUUID().toString());
        resp.setValue(Double.parseDouble(iso.getField(4)) / 100);
        resp.setResponseCode(iso.getField(39));
        resp.setAuthorizationCode(iso.getField(38));

        LocalDateTime now = LocalDateTime.now();
        resp.setTransactionDate(now.format(DateTimeFormatter.ofPattern("yy-MM-dd")));
        resp.setTransactionHour(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        return resp;
    }

    private String generateNsu() {
        int nsu = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(nsu);
    }

    private String formatValue(double value) {
        int cents = (int) Math.round(value * 100);
        return String.format("%012d", cents);
    }
}
