package com.destaxa;
import java.util.HashMap;
import java.util.Map;

public class ISOMessage {

    private String mti;
    private final Map<Integer, String> fields = new HashMap<>();

    public  ISOMessage(String mti) {
        this.mti = mti;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public void setField(int bit, String value) {
        fields.put(bit, value);
    }

    public String getField(int bit) {
        return fields.get(bit);
    }

    public Map<Integer, String> getAllFields() {
        return fields;
    }

    public String toIsoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mti);

        for (int i = 2; i <= 128; i++) {
            if (fields.containsKey(i)) {
                sb.append("|").append(i).append("=").append(fields.get(i));
            }
        }

        return sb.toString();
    }

    public static ISOMessage fromIsoString(String raw) {
        String[] parts = raw.split("\\|");
        ISOMessage message = new ISOMessage(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            String[] pair = parts[i].split("=");
            if (pair.length == 2) {
                int bit = Integer.parseInt(pair[0]);
                message.setField(bit, pair[1]);
            }
        }

        return message;
    }
}
