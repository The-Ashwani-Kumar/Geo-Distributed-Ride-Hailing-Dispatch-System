package com.ashwani.service;

import com.ashwani.entity.Passenger;

import java.util.List;

public interface PassengerService {
    void addPassenger(Passenger passenger);

    List<Passenger> getAllPassengers();
}
