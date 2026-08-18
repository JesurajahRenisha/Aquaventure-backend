package com.aquaventure.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/** Mock/stub payment gateway call -- no real payment processor is integrated. */
@Getter
@Setter
public class PaymentRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
