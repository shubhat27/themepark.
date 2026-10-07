package com.themepark.themepark.booking;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.themepark.themepark.booking.BookingDtos.CheckoutOrder;
import com.themepark.themepark.booking.BookingDtos.PaymentConfirmation;
import com.themepark.themepark.booking.BookingDtos.PaymentRequest;
import com.themepark.themepark.booking.BookingDtos.PaymentVerificationRequest;

@Service
public class RazorpayPaymentService {
    private final ParkBookingRepository repository;
    private final String keyId;
    private final String keySecret;

    public RazorpayPaymentService(ParkBookingRepository repository,
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret) {
        this.repository = repository;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public boolean isEnabled() {
        return !keyId.isBlank() && !keySecret.isBlank();
    }

    public String publicKey() {
        return isEnabled() ? keyId : "";
    }

    @Transactional
    public CheckoutOrder createOrder(PaymentRequest request) {
        requireConfigured();
        ParkBooking booking = findBooking(request.reference(), request.email());
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has been cancelled.");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking is already paid.");
        }

        long amount = booking.getTotalAmount().movePointRight(2).longValueExact();
        OrderResponse order;
        try {
            order = RestClient.create().post()
                    .uri("https://api.razorpay.com/v1/orders")
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .body(Map.of("amount", amount, "currency", "INR", "receipt", booking.getBookingReference(),
                            "notes", Map.of("booking_reference", booking.getBookingReference())))
                    .retrieve()
                    .body(OrderResponse.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Razorpay could not create the payment order. Your park booking is still saved.");
        }
        if (order == null || order.id() == null || order.id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Razorpay returned an invalid order.");
        }

        booking.beginPayment(order.id());
        return new CheckoutOrder(keyId, order.id(), amount, "INR", booking.getBookingReference(),
                booking.getCustomerName(), booking.getCustomerEmail());
    }

    @Transactional
    public PaymentConfirmation verify(PaymentVerificationRequest request) {
        requireConfigured();
        ParkBooking booking = findBooking(request.reference(), request.email());
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has been cancelled.");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking is already paid.");
        }
        if (booking.getRazorpayOrderId() == null ||
                !booking.getRazorpayOrderId().equals(request.razorpayOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The payment order does not match this booking.");
        }

        String signedPayload = request.razorpayOrderId() + "|" + request.razorpayPaymentId();
        String expected = signature(signedPayload, keySecret);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                request.razorpaySignature().toLowerCase().getBytes(StandardCharsets.US_ASCII))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay payment signature is invalid.");
        }

        booking.markPaid(request.razorpayPaymentId());
        return new PaymentConfirmation(booking.getBookingReference(), booking.getRazorpayPaymentId(),
                booking.getPaymentStatus());
    }

    private ParkBooking findBooking(String reference, String email) {
        return repository.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(reference.trim(), email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No booking matches that reference and email."));
    }

    private void requireConfigured() {
        if (!isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Online payments are not configured. Choose pay at the park instead.");
        }
    }

    private static String signature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Could not verify the payment signature.", exception);
        }
    }

    private record OrderResponse(String id) { }
}