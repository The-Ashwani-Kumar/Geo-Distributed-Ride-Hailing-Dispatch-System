package com.ashwani.service;

import com.ashwani.entity.Driver;
import com.ashwani.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService{

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public void addDriver(Driver driver) {
        driverRepository.saveDriver(driver);
    }

    @Override
    public void updateDriverLocation(String id, Double longitude, Double latitude) {
        driverRepository.updateDriverLocation(id, longitude, latitude);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAllDrivers();
    }
}
