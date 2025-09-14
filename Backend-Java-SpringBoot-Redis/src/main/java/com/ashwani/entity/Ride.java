package com.ashwani.entity;

import com.ashwani.enums.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Ride {
    private String id;          // Unique ID (UUID)

    @NotNull(message = "Passenger ID cannot be null")
    private String passengerId; // Linked passenger

    @NotNull(message = "Driver ID cannot be null")
    private String driverId;    // Linked driver

    private RideStatus status;  // "ongoing" | "completed" | "cancelled"
    private Long startTime;
    private Long endTime;       // Nullable if ride still ongoing
}
