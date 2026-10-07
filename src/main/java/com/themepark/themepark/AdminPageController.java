package com.themepark.themepark;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.themepark.themepark.booking.BookingDtos.BookingResponse;
import com.themepark.themepark.booking.BookingService;

@Controller
public class AdminPageController {
    private final BookingService bookingService;

    public AdminPageController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        List<BookingResponse> bookings = bookingService.allBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("bookingCount", bookings.size());
        model.addAttribute("confirmedCount", bookings.stream()
                .filter(item -> item.status().name().equals("CONFIRMED")).count());
        model.addAttribute("cancelledCount", bookings.stream()
                .filter(item -> item.status().name().equals("CANCELLED")).count());
        return "admin";
    }
}