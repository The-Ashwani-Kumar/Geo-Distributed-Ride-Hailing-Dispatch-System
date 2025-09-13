package com.ashwani.service;

import com.ashwani.config.ConsistencyContext;
import com.ashwani.entity.Passenger;
import com.ashwani.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public void addPassenger(Passenger passenger) {
        passengerRepository.save(passenger);
    }

    @Override
    public List<Passenger> getAllPassengers() {
        String consistency = ConsistencyContext.getConsistencyLevel();
        return passengerRepository.findAll(consistency);
    }
}
