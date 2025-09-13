package com.ashwani.service;

import com.ashwani.config.ConsistencyContext;
import com.ashwani.entity.Driver;
import com.ashwani.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public void addDriver(Driver driver) {
        driverRepository.saveDriver(driver);
        try {
            // Simulate replication lag
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void updateDriverLocation(String id, Double longitude, Double latitude) {
        driverRepository.updateDriverLocation(id, longitude, latitude);
    }

    @Override
    public List<Driver> getAllDrivers() {
        String consistency = ConsistencyContext.getConsistencyLevel();
        return driverRepository.findAllDrivers(consistency);
    }
}
