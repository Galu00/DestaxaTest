package com.destaxa;

import org.jpos.iso.ISOMsg;

public class AuthorizationRules {

    private static final String XML_LAYOUT = "iso87ascii.xml";

    public static ISOMsg process(ISOMsg request) throws Exception {
        String valorStr = request.getString(4);
        if (valorStr == null || valorStr.isEmpty()) return null;

        long valor = Long.parseLong(valorStr);

        if (valor > 100000) return null;

        ISOMsg response = new ISOMsg();
        response.setMTI("0210");

        response.set(4, valorStr);
        response.set(7, request.getString(7));
        response.set(11, request.getString(11));
        response.set(12, request.getString(12));
        response.set(13, request.getString(13));
        response.set(42, request.getString(42));

        if (valor % 2 == 0) {
            response.set(39, "000");
            response.set(38, generateAuthCode());
        } else {
            response.set(39, "051");
        }

        response.set(127, generateNSU());

        return response;
    }

    private static String generateAuthCode() {
        return String.valueOf((int) (Math.random() * 900000 + 100000));
    }

    private static String generateNSU() {
        return "NSU" + System.currentTimeMillis();
    }
}

