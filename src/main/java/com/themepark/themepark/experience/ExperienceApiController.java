package com.themepark.themepark.experience;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.themepark.themepark.booking.BookingDtos.ExperienceCard;

@RestController
@RequestMapping("/api/experiences")
public class ExperienceApiController {
    private final ExperienceService experienceService;

    public ExperienceApiController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping
    public List<ExperienceCard> experiences() {
        return experienceService.cards();
    }
}