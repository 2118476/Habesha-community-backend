package com.habesha.community.dto;

import lombok.Data;

@Data
public class HomeSwapRequest {
    private String title;
    private String location;
    private String description;

    // new fields to match UI
    private String availableFrom; // ISO date from FE; parse in service if you want LocalDate
    private String availableTo;
    private String homeType;      // "entire" | "room"
    private Integer bedrooms;
}
