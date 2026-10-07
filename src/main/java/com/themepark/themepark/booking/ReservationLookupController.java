package com.themepark.themepark.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.themepark.themepark.booking.BookingDtos.BookingResponse;
import com.themepark.themepark.booking.BookingDtos.LookupRequest;
import com.themepark.themepark.booking.BookingDtos.ReservationDetails;
import com.themepark.themepark.resort.ResortStayService;
import com.themepark.themepark.resort.StayDtos.StayResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationLookupController {
    private final BookingService parkService;
    private final ResortStayService stayService;

    public ReservationLookupController(BookingService parkService, ResortStayService stayService) {
        this.parkService = parkService;
        this.stayService = stayService;
    }

    @PostMapping("/lookup")
    public ReservationDetails lookup(@Valid @RequestBody LookupRequest request) {
        try {
            return fromPark(parkService.findForGuest(request.reference(), request.email()));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() != HttpStatus.NOT_FOUND) throw exception;
            return fromStay(stayService.findForGuest(request.reference(), request.email()));
        }
    }

    @PostMapping("/{reference}/cancel")
    public ReservationDetails cancel(@PathVariable String reference, @Valid @RequestBody LookupRequest request) {
        try {
            return fromPark(parkService.cancel(reference, request.email()));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() != HttpStatus.NOT_FOUND) throw exception;
            return fromStay(stayService.cancel(reference, request.email()));
        }
    }

    private static ReservationDetails fromPark(BookingResponse booking) {
        String extras = booking.fastPasses() == 0 ? "No fast passes" : booking.fastPasses() + " fast passes";
        return new ReservationDetails(booking.reference(), booking.fullName(), booking.email(),
                booking.visitDate(), "PARK", booking.passLabel(), booking.adults() + booking.children(),
                extras, booking.total(), booking.status().name());
    }

    private static ReservationDetails fromStay(StayResponse stay) {
        return new ReservationDetails(stay.reference(), stay.fullName(), stay.email(), stay.checkIn(),
                "RESORT", stay.roomLabel() + " / " + stay.nights() + (stay.nights() == 1 ? " night" : " nights"),
                stay.guests(), "Check-out " + stay.checkIn().plusDays(stay.nights()), stay.total(), stay.status().name());
    }
}