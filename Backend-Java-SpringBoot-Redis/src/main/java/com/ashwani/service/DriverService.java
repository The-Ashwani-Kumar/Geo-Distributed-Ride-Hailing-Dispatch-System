package com.ashwani.service;

import com.ashwani.entity.Driver;

import java.util.List;

public interface DriverService {
    void addDriver(Driver driver);
    void updateDriverLocation(String id, Double longitude, Double latitude);
    List<Driver> getAllDrivers();
}
