package com.ashwani.service;

import com.ashwani.entity.Driver;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.Region;
import com.ashwani.repository.DriverRepository;
import com.ashwani.sharding.ConsistencyContext;
import com.ashwani.sharding.RegionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public void addDriver(Region region, Driver driver) {
        driverRepository.saveDriver(region, driver);
        try {
            // Simulate replication lag
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void updateDriverLocation(Region region, String id, Double longitude, Double latitude) {
        driverRepository.updateDriverLocation(region, id, longitude, latitude);
    }

    @Override
    public List<Driver> getAllDrivers() {
        ConsistencyLevel consistencyLevel = ConsistencyContext.getConsistencyLevel();
        return driverRepository.findAllDrivers(RegionContext.getRegion(), consistencyLevel);
    }
}
