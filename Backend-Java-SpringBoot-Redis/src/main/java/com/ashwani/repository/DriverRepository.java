package com.ashwani.repository;

import com.ashwani.config.RedisConfig;
import com.ashwani.entity.Driver;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.DRIVER_GEO_KEY;
import static com.ashwani.constant.ApplicationConstant.DRIVER_KEY;

@Repository
public class DriverRepository {

    private final HashOperations<String, String, Driver> hashOps;
    private final GeoOperations<String, String> geoOps;

    public DriverRepository(RedisConnectionFactory factory) {

        // Create generic templates using RedisConfig
        RedisConfig redisConfig = new RedisConfig();
        RedisTemplate<String, Driver> driverTemplate = redisConfig.createTemplate(factory, Driver.class);
        RedisTemplate<String, String> geoTemplate = redisConfig.createGeoTemplate(factory);

        this.hashOps = driverTemplate.opsForHash();
        this.geoOps = geoTemplate.opsForGeo();
    }

    public void saveDriver(Driver driver) {
        hashOps.put(DRIVER_KEY, driver.getId(), driver);
        geoOps.add(DRIVER_GEO_KEY,
                new Point(driver.getLongitude(), driver.getLatitude()),
                driver.getId());
    }

    public Driver findDriverById(String driverId) {
        return hashOps.get(DRIVER_KEY, driverId);
    }

    public void updateDriverLocation(String driverId, Double longitude, Double latitude) {
        geoOps.add(DRIVER_GEO_KEY, new Point(longitude, latitude), driverId);

        Driver driver = hashOps.get(DRIVER_KEY, driverId);
        if (driver != null) {
            driver.setLongitude(longitude);
            driver.setLatitude(latitude);
            hashOps.put(DRIVER_KEY, driverId, driver);
        }
    }

    public List<Driver> findAllDrivers() {
        return List.copyOf(hashOps.values(DRIVER_KEY));
    }
}
