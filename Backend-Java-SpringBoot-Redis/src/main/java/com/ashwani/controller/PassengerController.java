package com.ashwani.controller;

import com.ashwani.entity.Passenger;
import com.ashwani.repository.PassengerRepository;
import com.ashwani.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/passengers")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping
    public ResponseEntity<Passenger> addPassenger(@Valid @RequestBody Passenger passenger) {
        // Generate ID if not provided
        if (passenger.getId() == null || passenger.getId().isEmpty()) {
            passenger.setId(UUID.randomUUID().toString());
        }
        passenger.setStatus("online");
        passengerService.addPassenger(passenger);
        return ResponseEntity.ok(passenger);
    }

    @GetMapping
    public ResponseEntity<List<Passenger>> getAllPassengers() {
        return ResponseEntity.ok(passengerService.getAllPassengers());
    }
}

