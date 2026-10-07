package com.themepark.themepark.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "park_bookings", indexes = {
        @Index(name = "idx_booking_reference_email", columnList = "booking_reference,customer_email")
})
public class ParkBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 12)
    private String bookingReference;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 254)
    private String customerEmail;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "adult_count", nullable = false)
    private int adultCount;

    @Column(name = "child_count", nullable = false)
    private int childCount;

    @Column(name = "fast_pass_count", nullable = false)
    private int fastPassCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type", nullable = false, length = 24)
    private PassType passType;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 16)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "razorpay_order_id", length = 64)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 64)
    private String razorpayPaymentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ParkBooking() { }

    public ParkBooking(String bookingReference, String customerName, String customerEmail,
            String customerPhone, LocalDate visitDate, int adultCount, int childCount,
            int fastPassCount, PassType passType, BigDecimal subtotal, BigDecimal discount,
            BigDecimal totalAmount) {
        this.bookingReference = bookingReference;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.visitDate = visitDate;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.fastPassCount = fastPassCount;
        this.passType = passType;
        this.subtotal = subtotal;
        this.discount = discount;
        this.totalAmount = totalAmount;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public LocalDate getVisitDate() { return visitDate; }
    public int getAdultCount() { return adultCount; }
    public int getChildCount() { return childCount; }
    public int getFastPassCount() { return fastPassCount; }
    public PassType getPassType() { return passType; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public Instant getCreatedAt() { return createdAt; }

    public void beginPayment(String orderId) {
        razorpayOrderId = orderId;
        paymentStatus = PaymentStatus.PENDING;
    }

    public void markPaid(String paymentId) {
        razorpayPaymentId = paymentId;
        paymentStatus = PaymentStatus.PAID;
    }

    public void cancel() {
        status = BookingStatus.CANCELLED;
    }
}