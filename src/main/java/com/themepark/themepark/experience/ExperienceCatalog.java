package com.themepark.themepark.experience;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.themepark.themepark.booking.BookingDtos.ExperienceCard;

@Service
class ExperienceService {
    private final ParkExperienceRepository repository;

    ExperienceService(ParkExperienceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    List<ExperienceCard> cards() {
        return repository.findAllByOrderByFeaturedDescNameAsc().stream()
                .map(item -> new ExperienceCard(item.getSlug(), item.getName(), item.getCategory(),
                        item.getDescription(), item.getImageUrl(), item.getHeightGuide(),
                        item.getDuration(), item.isFeatured()))
                .toList();
    }
}

@Configuration
class ExperienceSeedConfiguration {
    @Bean
    ApplicationRunner seedExperiences(ParkExperienceRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.saveAll(List.of(
                    new ParkExperience("sky-rail", "Skyline Run", "THRILL",
                            "A high-speed launch above the park, with a skyline view at every turn.",
                            "https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?auto=format&fit=crop&w=1000&q=85",
                            "130 cm minimum", "3 min", true),
                    new ParkExperience("monsoon-bay", "Monsoon Bay", "WATER",
                            "A wide-open wave pool, cabanas, and splash areas for the whole family.",
                            "https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=1000&q=85",
                            "All ages", "Open session", true),
                    new ParkExperience("wild-trail", "Wild Trail", "ADVENTURE",
                            "Wind through a forest course of rope bridges, climbs, and zip lines.",
                            "https://images.unsplash.com/photo-1526976668912-1a811878dd37?auto=format&fit=crop&w=1000&q=85",
                            "120 cm minimum", "45 min", true),
                    new ParkExperience("little-explorers", "Little Explorers", "FAMILY",
                            "A colourful first-ride district designed for small adventurers and their grown-ups.",
                            "https://images.unsplash.com/photo-1504457047772-27faf1c00561?auto=format&fit=crop&w=1000&q=85",
                            "All ages", "2-8 min", true),
                    new ParkExperience("river-rapids", "River Rapids", "WATER",
                            "Take the winding river through cool spray, sudden drops, and tropical gardens.",
                            "https://images.unsplash.com/photo-1500375592092-40eb2168fd21?auto=format&fit=crop&w=1000&q=85",
                            "110 cm minimum", "12 min", false),
                    new ParkExperience("grand-wheel", "The Grand Wheel", "FAMILY",
                            "A gentle ride into the evening with a panoramic view over Adventure World.",
                            "https://images.unsplash.com/photo-1504457047772-27faf1c00561?auto=format&fit=crop&w=1000&q=85",
                            "All ages", "8 min", false)));
        };
    }
}