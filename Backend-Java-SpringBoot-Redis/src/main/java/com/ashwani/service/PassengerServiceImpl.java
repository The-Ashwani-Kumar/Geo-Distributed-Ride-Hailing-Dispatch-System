package com.ashwani.service;

import com.ashwani.entity.Passenger;
import com.ashwani.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerServiceImpl implements PassengerService{

    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public void addPassenger(Passenger passenger) {
        passengerRepository.savePassenger(passenger);
    }

    @Override
    public List<Passenger> getAllPassengers() {
        return passengerRepository.getAllPassengers();
    }
}
