package com.themepark.themepark.resort;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResortStayRepository extends JpaRepository<ResortStayReservation, Long> {
    Optional<ResortStayReservation> findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase(
            String reference, String email);

    List<ResortStayReservation> findAllByOrderByCreatedAtDesc();
}