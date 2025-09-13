package com.ashwani.service;

import com.ashwani.entity.Ride;

import java.util.List;

public interface RideService {

    Ride bookRide(String passengerId);

    Ride endRide(String rideId);

    List<Ride> getAllRides();
}

