package com.ashwani.service;

import com.ashwani.entity.Driver;
import com.ashwani.enums.Region;

import java.util.List;

public interface DriverService {
    void addDriver(Region region, Driver driver);
    void updateDriverLocation(Region region, String id, Double longitude, Double latitude);
    List<Driver> getAllDrivers();
}
