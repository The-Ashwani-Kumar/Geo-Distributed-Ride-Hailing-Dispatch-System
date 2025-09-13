package com.ashwani.repository;

import com.ashwani.entity.Passenger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.PASSENGER_KEY;

@Repository
public class PassengerRepository {

    private final HashOperations<String, String, Passenger> masterHashOps;
    private final HashOperations<String, String, Passenger> replicaHashOps;

    public PassengerRepository(@Qualifier("masterPassengerRedisTemplate") RedisTemplate<String, Passenger> masterPassengerRedisTemplate,
                               @Qualifier("replicaPassengerRedisTemplate") RedisTemplate<String, Passenger> replicaPassengerRedisTemplate) {
        this.masterHashOps = masterPassengerRedisTemplate.opsForHash();
        this.replicaHashOps = replicaPassengerRedisTemplate.opsForHash();
    }

    public void save(Passenger passenger) {
        masterHashOps.put(PASSENGER_KEY, passenger.getId(), passenger);
    }


    public Passenger findById(String id, String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            return masterHashOps.get(PASSENGER_KEY, id);
        } else {
            return replicaHashOps.get(PASSENGER_KEY, id);
        }
    }

    public List<Passenger> findAll(String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            return masterHashOps.values(PASSENGER_KEY).stream().toList();
        } else {
            return replicaHashOps.values(PASSENGER_KEY).stream().toList();
        }
    }


    public void delete(String id) {
        masterHashOps.delete(PASSENGER_KEY, id);
    }
}