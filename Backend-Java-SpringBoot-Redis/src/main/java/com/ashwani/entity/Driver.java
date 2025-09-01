package com.ashwani.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Driver {
    private String id;

    @NotNull(message = "Name cannot be null")
    private String name;
    private String status;     // "available" | "on_ride" | "offline"

    @NotNull(message = "Latitude cannot be null")
    private Double latitude;   // Current location (for Redis GEO)

    @NotNull(message = "Longitude cannot be null")
    private Double longitude;
}
