package com.themepark.themepark.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.themepark.themepark.booking.BookingDtos.BookingResponse;
import com.themepark.themepark.booking.BookingDtos.CreateRequest;
import com.themepark.themepark.booking.BookingDtos.Quote;

@Service
public class BookingService {
    private static final BigDecimal FAST_PASS_PRICE = BigDecimal.valueOf(399);
    private static final BigDecimal GROUP_DISCOUNT_RATE = new BigDecimal("0.10");

    private final ParkBookingRepository repository;

    public BookingService(ParkBookingRepository repository) {
        this.repository = repository;
    }

    public Quote quote(CreateRequest request) {
        PassType pass = request.passType();
        BigDecimal subtotal = pass.adultPrice().multiply(BigDecimal.valueOf(request.adults()))
                .add(pass.childPrice().multiply(BigDecimal.valueOf(request.children())))
                .add(FAST_PASS_PRICE.multiply(BigDecimal.valueOf(request.fastPasses())));
        BigDecimal discount = request.adults() + request.children() >= 4
                ? subtotal.multiply(GROUP_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        return new Quote(subtotal, discount, subtotal.subtract(discount), pass.adultPrice(),
                pass.childPrice(), FAST_PASS_PRICE, pass.label(),
                discount.signum() > 0 ? "10% group offer" : "Add 4 guests for 10% off");
    }

    @Transactional
    public BookingResponse create(CreateRequest request) {
        Quote quote = quote(request);
        String reference = "AW-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase(Locale.ROOT);
        ParkBooking booking = repository.save(new ParkBooking(reference,
                request.fullName().trim(), request.email().trim().toLowerCase(Locale.ROOT),
                normalizePhone(request.phone()), request.visitDate(), request.adults(),
                request.children(), request.fastPasses(), request.passType(), quote.subtotal(),
                quote.discount(), quote.total()));
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse findForGuest(String reference, String email) {
        return repository.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(
                        reference.trim(), email.trim())
                .map(BookingService::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No booking matches that reference and email."));
    }

    @Transactional
    public BookingResponse cancel(String reference, String email) {
        ParkBooking booking = repository.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(
                        reference.trim(), email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No booking matches that reference and email."));
        if (!booking.getVisitDate().isAfter(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bookings can only be cancelled before the visit date.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking is already cancelled.");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This booking is paid. Contact Adventure World to request a refund.");
        }
        booking.cancel();
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(BookingService::toResponse).toList();
    }

    private static String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }

    private static BookingResponse toResponse(ParkBooking booking) {
        return new BookingResponse(booking.getBookingReference(), booking.getCustomerName(),
                booking.getCustomerEmail(), booking.getCustomerPhone(), booking.getVisitDate(),
                booking.getAdultCount(), booking.getChildCount(), booking.getFastPassCount(),
                booking.getPassType(), booking.getPassType().label(), booking.getSubtotal(),
                booking.getDiscount(), booking.getTotalAmount(), booking.getStatus(),
                booking.getPaymentStatus(), booking.getCreatedAt());
    }
}