package com.ashwani.entity;

import com.ashwani.enums.PassengerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Passenger {
    private String id;

    @NotNull(message = "Name cannot be null")
    private String name;

    private PassengerStatus status;

    @NotNull(message = "Latitude cannot be null")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    private Double longitude;
}
