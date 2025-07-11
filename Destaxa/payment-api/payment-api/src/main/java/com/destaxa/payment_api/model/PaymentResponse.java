package com.destaxa.payment_api.model;

import lombok.Data;

@Data
public class PaymentResponse {
    private String paymentId;
    private double value;
    private String responseCode;
    private String authorizationCode;
    private String transactionDate;
    private String transactionHour;
}
