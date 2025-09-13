package com.ashwani.repository;

import com.ashwani.entity.Driver;
import com.ashwani.entity.Ride;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.*;

@Repository
public class RideRepository {

    private static final Logger logger = LoggerFactory.getLogger(RideRepository.class);

    private final HashOperations<String, String, Ride> masterRideHashOps;
    private final HashOperations<String, String, Ride> replicaRideHashOps;
    private final HashOperations<String, String, Driver> masterDriverHashOps;
    private final GeoOperations<String, String> masterGeoOps;
    private final GeoOperations<String, String> replicaGeoOps;

    public RideRepository(@Qualifier("masterRideRedisTemplate") RedisTemplate<String, Ride> masterRideRedisTemplate,
                          @Qualifier("replicaRideRedisTemplate") RedisTemplate<String, Ride> replicaRideRedisTemplate,
                          @Qualifier("masterDriverRedisTemplate") RedisTemplate<String, Driver> masterDriverRedisTemplate,
                          @Qualifier("masterGeoRedisTemplate") RedisTemplate<String, String> masterGeoRedisTemplate,
                          @Qualifier("replicaGeoRedisTemplate") RedisTemplate<String, String> replicaGeoRedisTemplate) {
        this.masterRideHashOps = masterRideRedisTemplate.opsForHash();
        this.replicaRideHashOps = replicaRideRedisTemplate.opsForHash();
        this.masterDriverHashOps = masterDriverRedisTemplate.opsForHash();
        this.masterGeoOps = masterGeoRedisTemplate.opsForGeo();
        this.replicaGeoOps = replicaGeoRedisTemplate.opsForGeo();
    }

    public void save(Ride ride) {
        masterRideHashOps.put(RIDE_KEY, ride.getId(), ride);
    }

    public void updateDriverStatus(String driverId, String status) {
        Driver driver = masterDriverHashOps.get(DRIVER_KEY, driverId);
        if (driver != null) {
            driver.setStatus(status);
            masterDriverHashOps.put(DRIVER_KEY, driverId, driver);
            if ("available".equalsIgnoreCase(status)) {
                masterGeoOps.add(DRIVER_GEO_KEY, new Point(driver.getLongitude(), driver.getLatitude()), driver.getId());
                logger.info("Driver {} re-added to GEO index with location ({}, {})", driverId, driver.getLongitude(), driver.getLatitude());
            }
        }
    }

    public List<Ride> findAll(String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            logger.info("Fetching all rides with STRONG consistency (from master)");
            return masterRideHashOps.values(RIDE_KEY).stream().toList();
        } else {
            logger.info("Fetching all rides with EVENTUAL consistency (from replica)");
            return replicaRideHashOps.values(RIDE_KEY).stream().toList();
        }
    }

    public Ride findById(String id, String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            logger.info("Fetching ride {} with STRONG consistency (from master)", id);
            return masterRideHashOps.get(RIDE_KEY, id);
        } else {
            logger.info("Fetching ride {} with EVENTUAL consistency (from replica)", id);
            return replicaRideHashOps.get(RIDE_KEY, id);
        }
    }

    public GeoResults<GeoLocation<String>> getNearByDrivers(Double lat, Double lon, String consistency) {
        GeoOperations<String, String> geoOps;
        if ("strong".equalsIgnoreCase(consistency)) {
            logger.info("Searching for nearby drivers with STRONG consistency (from master)");
            geoOps = masterGeoOps;
        } else {
            logger.info("Searching for nearby drivers with EVENTUAL consistency (from replica)");
            geoOps = replicaGeoOps;
        }
        return geoOps.radius(
                DRIVER_GEO_KEY,
                new Circle(new Point(lon, lat), new Distance(50, Metrics.KILOMETERS))
        );
    }
}