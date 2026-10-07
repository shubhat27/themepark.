package com.themepark.themepark.resort;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.themepark.themepark.booking.BookingStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class StayDtos {
    private StayDtos() { }

    public record CreateRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @Pattern(regexp = "^$|^[+0-9() .-]{7,30}$", message = "Enter a valid phone number") String phone,
            @NotNull @FutureOrPresent LocalDate checkIn,
            @Min(1) @Max(14) int nights,
            @Min(1) @Max(6) int guests,
            @NotNull RoomType roomType,
            boolean termsAccepted) {
        @AssertTrue(message = "The selected room does not fit this group")
        public boolean isRoomOccupancyValid() {
            return roomType == null || guests <= roomType.maximumGuests();
        }
    }

    public record StayOption(RoomType id, String label, String description,
            BigDecimal nightlyRate, int maximumGuests) { }

    public record StayResponse(String reference, String fullName, String email, String phone,
            LocalDate checkIn, int nights, int guests, RoomType roomType, String roomLabel,
            BigDecimal nightlyRate, BigDecimal total, BookingStatus status) { }
}