package com.ashwani.service;

import com.ashwani.entity.Ride;

import java.util.List;

public interface RideService {
    Ride bookRide(String id);
    Ride endRide(String id);
    List<Ride> getAllRides();
}
