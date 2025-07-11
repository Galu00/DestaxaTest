package com.destaxa.payment_api.model;

import lombok.Data;

@Data
public class PaymentRequest {
    private String externalId;
    private double value;
    private String cardNumber;
    private int installments;
    private String cvv;
    private int expMonth;
    private int expYear;
    private String holderName;
}
