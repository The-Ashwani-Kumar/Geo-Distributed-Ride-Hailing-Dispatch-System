package com.ashwani.repository;

import com.ashwani.config.RedisConfig;
import com.ashwani.entity.Passenger;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.PASSENGER_KEY;

@Repository
public class PassengerRepository {

    private final HashOperations<String, String, Passenger> hashOps;

    public PassengerRepository(RedisConnectionFactory factory) {

        // Create generic RedisTemplate for Passenger
        RedisConfig redisConfig = new RedisConfig();
        RedisTemplate<String, Passenger> passengerTemplate = redisConfig.createTemplate(factory, Passenger.class);
        this.hashOps = passengerTemplate.opsForHash();
    }

    // Save a passenger
    public void savePassenger(Passenger passenger) {
        hashOps.put(PASSENGER_KEY, passenger.getId(), passenger);
    }

    // Find a passenger by ID
    public Passenger findPassengerById(String id) {
        return hashOps.get(PASSENGER_KEY, id);
    }

    // Get all passengers
    public List<Passenger> getAllPassengers() {
        return List.copyOf(hashOps.values(PASSENGER_KEY));
    }

    // Optional: delete a passenger
    public void deletePassenger(String id) {
        hashOps.delete(PASSENGER_KEY, id);
    }
}
