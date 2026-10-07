package com.themepark.themepark.resort;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.themepark.themepark.booking.BookingStatus;

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
@Table(name = "resort_stay_reservations", indexes = {
        @Index(name = "idx_stay_reference_email", columnList = "booking_reference,guest_email")
})
public class ResortStayReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 12)
    private String bookingReference;

    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;

    @Column(name = "guest_email", nullable = false, length = 254)
    private String guestEmail;

    @Column(name = "guest_phone", length = 30)
    private String guestPhone;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private int nights;

    @Column(nullable = false)
    private int guests;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 24)
    private RoomType roomType;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResortStayReservation() { }

    public ResortStayReservation(String bookingReference, String guestName, String guestEmail,
            String guestPhone, LocalDate checkIn, int nights, int guests, RoomType roomType,
            BigDecimal totalAmount) {
        this.bookingReference = bookingReference;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.checkIn = checkIn;
        this.nights = nights;
        this.guests = guests;
        this.roomType = roomType;
        this.totalAmount = totalAmount;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public String getBookingReference() { return bookingReference; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public String getGuestPhone() { return guestPhone; }
    public LocalDate getCheckIn() { return checkIn; }
    public int getNights() { return nights; }
    public int getGuests() { return guests; }
    public RoomType getRoomType() { return roomType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void cancel() { status = BookingStatus.CANCELLED; }
}