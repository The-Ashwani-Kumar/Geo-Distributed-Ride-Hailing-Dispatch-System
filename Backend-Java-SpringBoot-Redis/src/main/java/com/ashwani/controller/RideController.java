package com.ashwani.controller;

import com.ashwani.entity.Ride;
import com.ashwani.service.RideService;
import com.ashwani.sharding.RegionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/book")
    public ResponseEntity<Ride> bookRide(@RequestParam String id) {
        return ResponseEntity.ok(rideService.bookRide(RegionContext.getRegion(), id));
    }

    @PostMapping("/end")
    public ResponseEntity<Ride> endRide(@RequestParam String id) {
        return ResponseEntity.ok(rideService.endRide(RegionContext.getRegion(), id));
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }
}
