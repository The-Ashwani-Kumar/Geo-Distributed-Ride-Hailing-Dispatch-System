package com.ashwani.service;

import com.ashwani.entity.Ride;
import com.ashwani.enums.Region;

import java.util.List;

public interface RideService {

    Ride bookRide(Region region, String passengerId);

    Ride endRide(Region region, String rideId);

    List<Ride> getAllRides();

    Ride getRideById(String rideId);
}
