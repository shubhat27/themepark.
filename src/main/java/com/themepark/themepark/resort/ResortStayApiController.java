package com.themepark.themepark.resort;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.themepark.themepark.booking.BookingDtos.LookupRequest;
import com.themepark.themepark.resort.StayDtos.CreateRequest;
import com.themepark.themepark.resort.StayDtos.StayOption;
import com.themepark.themepark.resort.StayDtos.StayResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stay-reservations")
@Validated
public class ResortStayApiController {
    private final ResortStayService stayService;

    public ResortStayApiController(ResortStayService stayService) { this.stayService = stayService; }

    @GetMapping("/options")
    public List<StayOption> options() { return stayService.options(); }

    @PostMapping("/quote")
    public java.math.BigDecimal quote(@Valid @RequestBody CreateRequest request) {
        return stayService.quote(request);
    }

    @PostMapping
    public ResponseEntity<StayResponse> create(@Valid @RequestBody CreateRequest request) {
        if (!request.termsAccepted()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Accept the Terms & Conditions before reserving a resort stay.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(stayService.create(request));
    }

    @PostMapping("/lookup")
    public StayResponse lookup(@Valid @RequestBody LookupRequest request) {
        return stayService.findForGuest(request.reference(), request.email());
    }

    @PostMapping("/{reference}/cancel")
    public StayResponse cancel(@PathVariable String reference, @Valid @RequestBody LookupRequest request) {
        return stayService.cancel(reference, request.email());
    }
}