package com.themepark.themepark.resort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.themepark.themepark.resort.StayDtos.CreateRequest;
import com.themepark.themepark.resort.StayDtos.StayResponse;

@Service
public class ResortStayService {
    private final ResortStayRepository repository;

    public ResortStayService(ResortStayRepository repository) { this.repository = repository; }

    public List<StayDtos.StayOption> options() {
        return Arrays.stream(RoomType.values())
                .map(room -> new StayDtos.StayOption(room, room.label(), room.description(),
                        room.nightlyRate(), room.maximumGuests()))
                .toList();
    }

    public BigDecimal quote(CreateRequest request) {
        return request.roomType().nightlyRate().multiply(BigDecimal.valueOf(request.nights()));
    }

    @Transactional
    public StayResponse create(CreateRequest request) {
        String reference = "ST-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase(Locale.ROOT);
        ResortStayReservation stay = repository.save(new ResortStayReservation(reference,
                request.fullName().trim(), request.email().trim().toLowerCase(Locale.ROOT),
                request.phone() == null || request.phone().isBlank() ? null : request.phone().trim(),
                request.checkIn(), request.nights(), request.guests(), request.roomType(), quote(request)));
        return toResponse(stay);
    }

    @Transactional(readOnly = true)
    public StayResponse findForGuest(String reference, String email) {
        return repository.findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase(reference.trim(), email.trim())
                .map(ResortStayService::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No reservation matches that reference and email."));
    }

    @Transactional
    public StayResponse cancel(String reference, String email) {
        ResortStayReservation stay = repository.findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase(
                        reference.trim(), email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No reservation matches that reference and email."));
        if (!stay.getCheckIn().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stays can only be cancelled before check-in.");
        }
        if (stay.getStatus().name().equals("CANCELLED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This reservation is already cancelled.");
        }
        stay.cancel();
        return toResponse(stay);
    }

    @Transactional(readOnly = true)
    public List<StayResponse> allReservations() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(ResortStayService::toResponse).toList();
    }

    private static StayResponse toResponse(ResortStayReservation stay) {
        return new StayResponse(stay.getBookingReference(), stay.getGuestName(), stay.getGuestEmail(),
                stay.getGuestPhone(), stay.getCheckIn(), stay.getNights(), stay.getGuests(),
                stay.getRoomType(), stay.getRoomType().label(), stay.getRoomType().nightlyRate(),
                stay.getTotalAmount(), stay.getStatus());
    }
}