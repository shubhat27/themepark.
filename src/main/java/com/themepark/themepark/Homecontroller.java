package com.themepark.themepark;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Homecontroller {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about-experience";
    }

    @GetMapping("/rides")
    public String rides() {
        return "rides-experiences";
    }

    @GetMapping("/tickets")
    public String tickets() {
        return "ticket-booking";
    }

    @GetMapping("/stay")
    public String stay() {
        return "stay-booking";
    }

    @GetMapping("/dining")
    public String dining() {
        return "dining-experience";
    }

    @GetMapping("/food")
    public String food() {
        return "food-experience";
    }

    @GetMapping("/offers")
    public String offers() {
        return "offers-experience";
    }

    @GetMapping("/my-bookings")
    public String bookings() {
        return "booking-lookup";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact-experience";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/plan-your-visit")
    public String planYourVisit() {
        return "plan-your-visit";
    }

    @GetMapping("/account")
    public String account() {
        return "account";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/safety")
    public String safety() {
        return "safety";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-experience";
    }

    @GetMapping("/terms-and-conditions")
    public String termsAndConditions() {
        return "terms-experience";
    }

    @GetMapping("/cancellation-policy")
    public String cancellationPolicy() {
        return "cancellation-policy";
    }

}