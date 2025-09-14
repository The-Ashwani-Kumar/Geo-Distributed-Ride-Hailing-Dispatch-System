package com.ashwani.service;

import com.ashwani.entity.Passenger;
import com.ashwani.enums.Region;

import java.util.List;

public interface PassengerService {

    void addPassenger(Region region, Passenger passenger);

    List<Passenger> getAllPassengers();
}
