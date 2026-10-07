package com.themepark.themepark.booking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkBookingRepository extends JpaRepository<ParkBooking, Long> {
    Optional<ParkBooking> findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(
            String bookingReference, String customerEmail);

    List<ParkBooking> findAllByOrderByCreatedAtDesc();
}