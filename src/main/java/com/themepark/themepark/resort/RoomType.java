package com.themepark.themepark.resort;

import java.math.BigDecimal;

public enum RoomType {
    GARDEN_ROOM("Garden room", 8999, 4, "A calm room with a garden terrace and breakfast for two."),
    FOREST_VILLA("Forest villa", 12499, 6, "A private villa with a shaded deck and room for the whole family."),
    LAGOON_SUITE("Lagoon suite", 18999, 4, "A waterside suite with a private plunge pool and late checkout.");

    private final String label;
    private final BigDecimal nightlyRate;
    private final int maximumGuests;
    private final String description;

    RoomType(String label, int nightlyRate, int maximumGuests, String description) {
        this.label = label;
        this.nightlyRate = BigDecimal.valueOf(nightlyRate);
        this.maximumGuests = maximumGuests;
        this.description = description;
    }

    public String label() { return label; }
    public BigDecimal nightlyRate() { return nightlyRate; }
    public int maximumGuests() { return maximumGuests; }
    public String description() { return description; }
}