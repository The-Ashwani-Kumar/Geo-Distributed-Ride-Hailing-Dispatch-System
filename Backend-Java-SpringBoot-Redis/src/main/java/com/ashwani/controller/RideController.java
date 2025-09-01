package com.ashwani.controller;

import com.ashwani.entity.Ride;
import com.ashwani.service.RideService;
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
        return ResponseEntity.ok(rideService.bookRide(id));
    }

    @PostMapping("/end")
    public ResponseEntity<Ride> endRide(@RequestParam String id) {
        return ResponseEntity.ok(rideService.endRide(id));
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }
}
