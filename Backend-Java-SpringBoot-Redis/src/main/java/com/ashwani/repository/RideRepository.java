package com.ashwani.repository;

import com.ashwani.entity.Driver;
import com.ashwani.entity.Ride;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.DriverStatus;
import com.ashwani.enums.Region;
import com.ashwani.sharding.ShardedRedisTemplateRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashwani.constant.ApplicationConstant.*;

@Repository
public class RideRepository {

    private static final Logger logger = LoggerFactory.getLogger(RideRepository.class);

    private final ShardedRedisTemplateRouter router;

    public RideRepository(ShardedRedisTemplateRouter router) {
        this.router = router;
    }

    public void save(Region region, Ride ride) {
        RedisTemplate<String, Ride> rideTemplate = router.getRideTemplate(region, ConsistencyLevel.STRONG);
        String rideKey = RIDE_KEY_PREFIX + region.name().toLowerCase();
        rideTemplate.opsForHash().put(rideKey, ride.getId(), ride);
    }

    public void updateDriverStatus(Region region, String driverId, DriverStatus status) {
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, ConsistencyLevel.STRONG);
        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        Driver driver = (Driver) driverTemplate.opsForHash().get(driverKey, driverId);

        if (driver != null) {
            driver.setStatus(status);
            driverTemplate.opsForHash().put(driverKey, driverId, driver);

            if (DriverStatus.AVAILABLE.equals(status)) {
                RedisTemplate<String, String> geoTemplate = router.getGeoTemplate(region, ConsistencyLevel.STRONG);
                String driverGeoKey = DRIVER_GEO_KEY_PREFIX + region.name().toLowerCase();
                geoTemplate.opsForGeo().add(driverGeoKey, new Point(driver.getLongitude(), driver.getLatitude()), driver.getId());
                logger.info("Driver {} re-added to GEO index with location ({}, {}) in region {}", driverId, driver.getLongitude(), driver.getLatitude(), region);
            }
        }
    }

    public List<Ride> findAll(Region region, ConsistencyLevel consistencyLevel) {
        logger.info("Fetching all rides with {} consistency in region {}", consistencyLevel, region);
        RedisTemplate<String, Ride> rideTemplate = router.getRideTemplate(region, consistencyLevel);
        String rideKey = RIDE_KEY_PREFIX + region.name().toLowerCase();
        return rideTemplate.opsForHash().values(rideKey).stream()
                .map(obj -> (Ride) obj)
                .collect(Collectors.toList());
    }

    public Ride findById(Region region, String id, ConsistencyLevel consistencyLevel) {
        logger.info("Fetching ride {} with {} consistency in region {}", id, consistencyLevel, region);
        RedisTemplate<String, Ride> rideTemplate = router.getRideTemplate(region, consistencyLevel);
        String rideKey = RIDE_KEY_PREFIX + region.name().toLowerCase();
        return (Ride) rideTemplate.opsForHash().get(rideKey, id);
    }

    public GeoResults<GeoLocation<String>> getNearByDrivers(Region region, Double lat, Double lon, ConsistencyLevel consistencyLevel) {
        logger.info("Searching for nearby drivers with {} consistency in region {}", consistencyLevel, region);
        RedisTemplate<String, String> geoTemplate = router.getGeoTemplate(region, consistencyLevel);
        String driverGeoKey = DRIVER_GEO_KEY_PREFIX + region.name().toLowerCase();
        return geoTemplate.opsForGeo().radius(
                driverGeoKey,
                new Circle(new Point(lon, lat), new Distance(50, Metrics.KILOMETERS))
        );
    }
}
