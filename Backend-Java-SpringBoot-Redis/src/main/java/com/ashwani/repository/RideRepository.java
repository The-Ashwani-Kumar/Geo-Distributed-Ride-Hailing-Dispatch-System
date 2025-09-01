package com.ashwani.repository;

import com.ashwani.config.RedisConfig;
import com.ashwani.entity.Driver;
import com.ashwani.entity.Ride;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.*;

@Repository
public class RideRepository {

    private final HashOperations<String, String, Ride> rideHashOps;
    private final HashOperations<String, String, Driver> driverHashOps;
    private final GeoOperations<String, String> geoOps;

    public RideRepository(RedisConnectionFactory factory) {

        RedisConfig redisConfig = new RedisConfig();

        // Typed RedisTemplates
        RedisTemplate<String, Ride> rideTemplate = redisConfig.createTemplate(factory, Ride.class);
        RedisTemplate<String, Driver> driverTemplate = redisConfig.createTemplate(factory, Driver.class);
        RedisTemplate<String, String> geoTemplate = redisConfig.createGeoTemplate(factory);

        this.rideHashOps = rideTemplate.opsForHash();
        this.driverHashOps = driverTemplate.opsForHash();
        this.geoOps = geoTemplate.opsForGeo();
    }

    // Book a new ride
    public void bookRide(Ride ride) {
        rideHashOps.put(RIDE_KEY, ride.getId(), ride);
    }

    // Update driver status
    public void updateDriverStatus(String driverId, String status) {
        Driver driver = driverHashOps.get(DRIVER_KEY, driverId);
        if (driver != null) {
            driver.setStatus(status);
            driverHashOps.put(DRIVER_KEY, driverId, driver);
        }
    }

    // Get all rides
    public List<Ride> findAllRides() {
        return rideHashOps.values(RIDE_KEY);
    }

    // Find ride by ID
    public Ride findRideById(String id) {
        return rideHashOps.get(RIDE_KEY, id);
    }

    // End a ride (update ride info)
    public void endRide(String rideId, Ride ride) {
        rideHashOps.put(RIDE_KEY, rideId, ride);
    }

    // Get nearby drivers within 5 km radius
    public GeoResults<GeoLocation<String>> getNearByDrivers(Double lat, Double lon) {
        return geoOps.radius(
                DRIVER_GEO_KEY,
                new Circle(new Point(lon, lat), new Distance(5, Metrics.KILOMETERS))
        );
    }

}
