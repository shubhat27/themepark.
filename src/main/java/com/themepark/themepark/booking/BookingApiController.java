package com.themepark.themepark.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.themepark.themepark.booking.BookingDtos.BookingResponse;
import com.themepark.themepark.booking.BookingDtos.CreateRequest;
import com.themepark.themepark.booking.BookingDtos.LookupRequest;
import com.themepark.themepark.booking.BookingDtos.TicketOption;
import com.themepark.themepark.booking.BookingDtos.Quote;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class BookingApiController {
    private final BookingService bookingService;

    public BookingApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking-quotes")
    public Quote quote(@Valid @RequestBody CreateRequest request) {
        return bookingService.quote(request);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateRequest request) {
        if (!request.termsAccepted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Accept the Terms & Conditions before reserving tickets.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @PostMapping("/bookings/lookup")
    public BookingResponse lookup(@Valid @RequestBody LookupRequest request) {
        return bookingService.findForGuest(request.reference(), request.email());
    }

    @PostMapping("/bookings/{reference}/cancel")
    public BookingResponse cancel(@PathVariable String reference, @Valid @RequestBody LookupRequest request) {
        return bookingService.cancel(reference, request.email());
    }

    @org.springframework.web.bind.annotation.GetMapping("/ticket-options")
    public TicketOption[] ticketOptions() {
        return java.util.Arrays.stream(PassType.values())
                .map(pass -> new TicketOption(pass, pass.label(), pass.description(),
                        pass.adultPrice(), pass.childPrice()))
                .toArray(TicketOption[]::new);
    }
}