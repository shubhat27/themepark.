package com.themepark.themepark.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class BookingDtos {
    private BookingDtos() { }

    public record CreateRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @Pattern(regexp = "^$|^[+0-9() .-]{7,30}$", message = "Enter a valid phone number") String phone,
            @NotNull @FutureOrPresent LocalDate visitDate,
            @Min(1) @Max(12) int adults,
            @Min(0) @Max(12) int children,
            @NotNull PassType passType,
            @Min(0) @Max(12) int fastPasses,
            boolean termsAccepted) {
        @AssertTrue(message = "Choose between 1 and 12 guests")
        public boolean isGuestCountValid() {
            return adults + children >= 1 && adults + children <= 12 && fastPasses <= adults + children;
        }
    }

    public record LookupRequest(
            @NotBlank String reference,
            @NotBlank @Email String email) { }

    public record PaymentRequest(@NotBlank String reference, @NotBlank @Email String email) { }

    public record PaymentVerificationRequest(
            @NotBlank String reference,
            @NotBlank @Email String email,
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank @Pattern(regexp = "[a-fA-F0-9]{64}") String razorpaySignature) { }

    public record Quote(BigDecimal subtotal, BigDecimal discount, BigDecimal total,
            BigDecimal adultPrice, BigDecimal childPrice, BigDecimal fastPassPrice,
            String passLabel, String offerLabel) { }

    public record TicketOption(PassType id, String label, String description,
            BigDecimal adultPrice, BigDecimal childPrice) { }

    public record ReservationDetails(String reference, String fullName, String email,
            LocalDate visitDate, String reservationType, String description, int guests,
            String extras, BigDecimal total, String status) { }

    public record BookingResponse(String reference, String fullName, String email, String phone,
            LocalDate visitDate, int adults, int children, int fastPasses, PassType passType,
            String passLabel, BigDecimal subtotal, BigDecimal discount, BigDecimal total,
            BookingStatus status, PaymentStatus paymentStatus, Instant createdAt) { }

    public record PaymentConfiguration(boolean enabled, String keyId) { }

    public record CheckoutOrder(String keyId, String orderId, long amount, String currency,
            String reference, String fullName, String email) { }

    public record PaymentConfirmation(String reference, String paymentId, PaymentStatus status) { }

    public record ExperienceCard(String slug, String name, String category, String description,
            String imageUrl, String heightGuide, String duration, boolean featured) { }

    public record ApiError(String message) { }
}