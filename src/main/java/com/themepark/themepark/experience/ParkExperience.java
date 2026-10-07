package com.themepark.themepark.experience;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "park_experiences")
public class ParkExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(nullable = false, length = 90)
    private String name;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "height_guide", nullable = false, length = 60)
    private String heightGuide;

    @Column(nullable = false, length = 40)
    private String duration;

    @Column(nullable = false)
    private boolean featured;

    protected ParkExperience() { }

    public ParkExperience(String slug, String name, String category, String description,
            String imageUrl, String heightGuide, String duration, boolean featured) {
        this.slug = slug;
        this.name = name;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.heightGuide = heightGuide;
        this.duration = duration;
        this.featured = featured;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getHeightGuide() { return heightGuide; }
    public String getDuration() { return duration; }
    public boolean isFeatured() { return featured; }
}