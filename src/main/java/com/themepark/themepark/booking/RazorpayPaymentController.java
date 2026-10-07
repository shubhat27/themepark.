package com.themepark.themepark.booking;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.themepark.themepark.booking.BookingDtos.CheckoutOrder;
import com.themepark.themepark.booking.BookingDtos.PaymentConfiguration;
import com.themepark.themepark.booking.BookingDtos.PaymentConfirmation;
import com.themepark.themepark.booking.BookingDtos.PaymentRequest;
import com.themepark.themepark.booking.BookingDtos.PaymentVerificationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@Validated
public class RazorpayPaymentController {
    private final RazorpayPaymentService paymentService;

    public RazorpayPaymentController(RazorpayPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/configuration")
    public PaymentConfiguration configuration() {
        return new PaymentConfiguration(paymentService.isEnabled(), paymentService.isEnabled()
                ? publicKey() : "");
    }

    @PostMapping("/orders")
    public CheckoutOrder createOrder(@Valid @RequestBody PaymentRequest request) {
        return paymentService.createOrder(request);
    }

    @PostMapping("/verify")
    public PaymentConfirmation verify(@Valid @RequestBody PaymentVerificationRequest request) {
        return paymentService.verify(request);
    }

    private String publicKey() {
        return paymentService.publicKey();
    }
}