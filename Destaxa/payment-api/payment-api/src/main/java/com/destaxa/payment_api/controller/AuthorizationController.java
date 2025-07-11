package com.destaxa.payment_api.controller;

import com.destaxa.payment_api.model.PaymentRequest;
import com.destaxa.payment_api.model.PaymentResponse;
import com.destaxa.payment_api.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> authorize(@RequestBody PaymentRequest request,
                                                     @RequestHeader("x-identifier") String identifier) {
        PaymentResponse response = authorizationService.authorizePayment(request, identifier);
        return ResponseEntity.ok(response);
    }
}
