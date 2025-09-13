package com.ashwani.repository;

import com.ashwani.entity.Driver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ashwani.constant.ApplicationConstant.DRIVER_GEO_KEY;
import static com.ashwani.constant.ApplicationConstant.DRIVER_KEY;

@Repository
public class DriverRepository {

    private final HashOperations<String, String, Driver> masterHashOps;
    private final GeoOperations<String, String> masterGeoOps;
    private final HashOperations<String, String, Driver> replicaHashOps;
    private final GeoOperations<String, String> replicaGeoOps;

    public void saveDriver(Driver driver) {
        masterHashOps.put(DRIVER_KEY, driver.getId(), driver);
        masterGeoOps.add(DRIVER_GEO_KEY,
                new Point(driver.getLongitude(), driver.getLatitude()),
                driver.getId());
    }

    public DriverRepository(@Qualifier("masterDriverRedisTemplate") RedisTemplate<String, Driver> masterDriverRedisTemplate,
                            @Qualifier("replicaDriverRedisTemplate") RedisTemplate<String, Driver> replicaDriverRedisTemplate,
                            @Qualifier("masterGeoRedisTemplate") RedisTemplate<String, String> masterGeoRedisTemplate,
                            @Qualifier("replicaGeoRedisTemplate") RedisTemplate<String, String> replicaGeoRedisTemplate) {
        this.masterHashOps = masterDriverRedisTemplate.opsForHash();
        this.masterGeoOps = masterGeoRedisTemplate.opsForGeo();
        this.replicaHashOps = replicaDriverRedisTemplate.opsForHash();
        this.replicaGeoOps = replicaGeoRedisTemplate.opsForGeo();
    }

    public Driver findDriverById(String driverId, String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            return masterHashOps.get(DRIVER_KEY, driverId);
        } else {
            return replicaHashOps.get(DRIVER_KEY, driverId);
        }
    }


    public void updateDriverLocation(String driverId, Double longitude, Double latitude) {
        masterGeoOps.add(DRIVER_GEO_KEY, new Point(longitude, latitude), driverId);

        Driver driver = (Driver) masterHashOps.get(DRIVER_KEY, driverId);
        if (driver != null) {
            driver.setLongitude(longitude);
            driver.setLatitude(latitude);
            masterHashOps.put(DRIVER_KEY, driverId, driver);
        }
    }

    public List<Driver> findAllDrivers(String consistency) {
        if ("strong".equalsIgnoreCase(consistency)) {
            return masterHashOps.values(DRIVER_KEY).stream().toList();
        } else {
            return replicaHashOps.values(DRIVER_KEY).stream().toList();
        }
    }

    public void removeDriverFromGeoIndex(String driverId) {
        masterGeoOps.remove(DRIVER_GEO_KEY, driverId);
    }
}
