package com.ashwani.service;

import com.ashwani.entity.Passenger;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.Region;
import com.ashwani.repository.PassengerRepository;
import com.ashwani.sharding.ConsistencyContext;
import com.ashwani.sharding.RegionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public void addPassenger(Region region, Passenger passenger) {
        passengerRepository.save(region, passenger);
    }

    @Override
    public List<Passenger> getAllPassengers() {
        ConsistencyLevel consistencyLevel = ConsistencyContext.getConsistencyLevel();
        return passengerRepository.findAll(RegionContext.getRegion(), consistencyLevel);
    }
}
