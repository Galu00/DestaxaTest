package com.destaxa.payment_api.service;

import com.destaxa.payment_api.model.PaymentRequest;
import com.destaxa.payment_api.model.PaymentResponse;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
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
    InputStream is = getClass().getClassLoader().getResourceAsStream("iso87ascii.xml");

    public PaymentResponse authorizePayment(PaymentRequest request, String identifier) {
        try (
                Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            ISOMsg isoRequest = buildIsoRequest(request, identifier);
            String packedRequest = new String(isoRequest.pack());
            out.println(packedRequest);

            String responseLine = in.readLine();
            if (responseLine == null) {
                throw new RuntimeException("Timeout: Nenhuma resposta do autorizador.");
            }

            ISOMsg isoResponse = unpackIsoResponse(responseLine);
            return buildPaymentResponse(isoResponse);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar autorização: " + e.getMessage(), e);
        }
    }

    private ISOMsg buildIsoRequest(PaymentRequest req, String identifier) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        GenericPackager packager = new GenericPackager(is);

        ISOMsg iso = new ISOMsg();
        iso.setPackager(packager);
        iso.setMTI("0200");

        iso.set(2, req.getCardNumber());
        iso.set(3, req.getInstallments() > 1 ? "003001" : "003000");
        iso.set(4, formatValue(req.getValue()));
        iso.set(7, now.format(DateTimeFormatter.ofPattern("MMddHHmmss")));
        iso.set(11, generateNsu());
        iso.set(12, now.format(DateTimeFormatter.ofPattern("HHmmss")));
        iso.set(13, now.format(DateTimeFormatter.ofPattern("MMdd")));
        iso.set(14, String.format("%02d%02d", req.getExpYear(), req.getExpMonth()));
        iso.set(42, identifier);
        iso.set(67, String.format("%02d", req.getInstallments()));

        return iso;
    }


    private ISOMsg unpackIsoResponse(String responseLine) throws Exception {
        GenericPackager packager = new GenericPackager(is);
        ISOMsg response = new ISOMsg();
        response.setPackager(packager);
        response.unpack(responseLine.getBytes());
        return response;
    }

    private PaymentResponse buildPaymentResponse(ISOMsg iso) throws Exception {
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(UUID.randomUUID().toString());
        resp.setValue(Double.parseDouble(iso.getString(4)) / 100);
        resp.setResponseCode(iso.getString(39));
        resp.setAuthorizationCode(iso.getString(38));

        LocalDateTime now = LocalDateTime.now();
        resp.setTransactionDate(now.format(DateTimeFormatter.ofPattern("yy-MM-dd")));
        resp.setTransactionHour(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        return resp;
    }

    private String generateNsu() {
        return String.valueOf((int) (Math.random() * 900000 + 100000));
    }

    private String formatValue(double value) {
        int cents = (int) Math.round(value * 100);
        return String.format("%012d", cents);
    }
}
