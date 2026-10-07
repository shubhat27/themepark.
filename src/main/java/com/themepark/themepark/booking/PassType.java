package com.themepark.themepark.booking;

import java.math.BigDecimal;

public enum PassType {
    DAY_PASS("Park day", 1499, 1099, "All-day access to Adventure World"),
    ALL_PARKS("Park + water park", 2199, 1699, "All-day access to every park zone"),
    EVENING("Golden hour", 999, 699, "Entry from 4:00 PM until close");

    private final String label;
    private final BigDecimal adultPrice;
    private final BigDecimal childPrice;
    private final String description;

    PassType(String label, int adultPrice, int childPrice, String description) {
        this.label = label;
        this.adultPrice = BigDecimal.valueOf(adultPrice);
        this.childPrice = BigDecimal.valueOf(childPrice);
        this.description = description;
    }

    public String label() { return label; }
    public BigDecimal adultPrice() { return adultPrice; }
    public BigDecimal childPrice() { return childPrice; }
    public String description() { return description; }
}