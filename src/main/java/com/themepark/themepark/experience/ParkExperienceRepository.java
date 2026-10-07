package com.themepark.themepark.experience;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkExperienceRepository extends JpaRepository<ParkExperience, Long> {
    List<ParkExperience> findAllByOrderByFeaturedDescNameAsc();
}