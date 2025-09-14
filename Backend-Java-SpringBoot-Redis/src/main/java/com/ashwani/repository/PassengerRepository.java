package com.ashwani.repository;

import com.ashwani.entity.Passenger;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.PassengerStatus;
import com.ashwani.enums.Region;
import com.ashwani.sharding.ShardedRedisTemplateRouter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashwani.constant.ApplicationConstant.PASSENGER_KEY_PREFIX;

@Repository
public class PassengerRepository {

    private final ShardedRedisTemplateRouter router;

    public PassengerRepository(ShardedRedisTemplateRouter router) {
        this.router = router;
    }

    public void save(Region region, Passenger passenger) {
        RedisTemplate<String, Passenger> template = router.getPassengerTemplate(region, ConsistencyLevel.STRONG);
        String passengerKey = PASSENGER_KEY_PREFIX + region.name().toLowerCase();
        template.opsForHash().put(passengerKey, passenger.getId(), passenger);
    }

    public Passenger findById(Region region, String id, ConsistencyLevel consistencyLevel) {
        RedisTemplate<String, Passenger> template = router.getPassengerTemplate(region, consistencyLevel);
        String passengerKey = PASSENGER_KEY_PREFIX + region.name().toLowerCase();
        return (Passenger) template.opsForHash().get(passengerKey, id);
    }

    public List<Passenger> findAll(Region region, ConsistencyLevel consistencyLevel) {
        RedisTemplate<String, Passenger> template = router.getPassengerTemplate(region, consistencyLevel);
        String passengerKey = PASSENGER_KEY_PREFIX + region.name().toLowerCase();
        return template.opsForHash().values(passengerKey).stream()
                .map(obj -> (Passenger) obj)
                .collect(Collectors.toList());
    }

    public void updatePassengerStatus(Region region, String passengerId, PassengerStatus status) {
        RedisTemplate<String, Passenger> template = router.getPassengerTemplate(region, ConsistencyLevel.STRONG);
        String passengerKey = PASSENGER_KEY_PREFIX + region.name().toLowerCase();
        Passenger passenger = (Passenger) template.opsForHash().get(passengerKey, passengerId);
        if (passenger != null) {
            passenger.setStatus(status);
            template.opsForHash().put(passengerKey, passengerId, passenger);
        }
    }

    public void delete(Region region, String id) {
        RedisTemplate<String, Passenger> template = router.getPassengerTemplate(region, ConsistencyLevel.STRONG);
        String passengerKey = PASSENGER_KEY_PREFIX + region.name().toLowerCase();
        template.opsForHash().delete(passengerKey, id);
    }
}
