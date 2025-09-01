package com.ashwani.controller;

import com.ashwani.entity.Driver;
import com.ashwani.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @PostMapping
    public ResponseEntity<Driver> addDriver(@Valid @RequestBody Driver driver) {
        if (driver.getId() == null || driver.getId().isEmpty()) {
            driver.setId(UUID.randomUUID().toString());
        }

        driver.setStatus("available");
        driverService.addDriver(driver);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/updateLocation")
    public void updateDriverLocation(@RequestParam String id, @RequestParam Double longitude, @RequestParam Double latitude) {
        driverService.updateDriverLocation(id, longitude, latitude);
    }

    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }
}

