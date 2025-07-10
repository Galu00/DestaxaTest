package com.destaxa;

public class AuthorizationRules {

    public static ISOMessage process(ISOMessage request) {
        String valorStr = request.getField(4);
        if (valorStr == null || valorStr.isEmpty()) return null;

        long valor = Long.parseLong(valorStr);

        if (valor > 100000) {
            return null;
        }

        ISOMessage response = new ISOMessage("0210");

        response.setField(4, valorStr);
        response.setField(7, request.getField(7));
        response.setField(11, request.getField(11));
        response.setField(12, request.getField(12));
        response.setField(13, request.getField(13));
        response.setField(42, request.getField(42));

        if (valor % 2 == 0) {
            response.setField(39, "000");
            response.setField(38, createAuthCod());
        } else {
            response.setField(39, "051");
        }

        response.setField(127, createNsuHost());

        return response;
    }

    private static String createAuthCod() {
        return String.valueOf((int) (Math.random() * 900000 + 100000));
    }

    private static String createNsuHost() {
        return "NSU" + System.currentTimeMillis();
    }
}
