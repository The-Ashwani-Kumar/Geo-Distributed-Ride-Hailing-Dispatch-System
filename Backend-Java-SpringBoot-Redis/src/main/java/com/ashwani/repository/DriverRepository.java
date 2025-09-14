package com.ashwani.repository;

import com.ashwani.entity.Driver;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.DriverStatus;
import com.ashwani.enums.Region;
import com.ashwani.sharding.ShardedRedisTemplateRouter;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashwani.constant.ApplicationConstant.DRIVER_GEO_KEY_PREFIX;
import static com.ashwani.constant.ApplicationConstant.DRIVER_KEY_PREFIX;

@Repository
public class DriverRepository {

    private final ShardedRedisTemplateRouter router;

    public DriverRepository(ShardedRedisTemplateRouter router) {
        this.router = router;
    }

    public void saveDriver(Region region, Driver driver) {
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, ConsistencyLevel.STRONG);
        RedisTemplate<String, String> geoTemplate = router.getGeoTemplate(region, ConsistencyLevel.STRONG);

        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        String driverGeoKey = DRIVER_GEO_KEY_PREFIX + region.name().toLowerCase();

        driverTemplate.opsForHash().put(driverKey, driver.getId(), driver);
        geoTemplate.opsForGeo().add(driverGeoKey,
                new Point(driver.getLongitude(), driver.getLatitude()),
                driver.getId());
    }

    public Driver findDriverById(Region region, String driverId, ConsistencyLevel consistencyLevel) {
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, consistencyLevel);
        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        return (Driver) driverTemplate.opsForHash().get(driverKey, driverId);
    }


    public void updateDriverLocation(Region region, String driverId, Double longitude, Double latitude) {
        RedisTemplate<String, String> geoTemplate = router.getGeoTemplate(region, ConsistencyLevel.STRONG);
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, ConsistencyLevel.STRONG);

        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        String driverGeoKey = DRIVER_GEO_KEY_PREFIX + region.name().toLowerCase();

        geoTemplate.opsForGeo().add(driverGeoKey, new Point(longitude, latitude), driverId);

        Driver driver = (Driver) driverTemplate.opsForHash().get(driverKey, driverId);
        if (driver != null) {
            driver.setLongitude(longitude);
            driver.setLatitude(latitude);
            // Status is not changed here, assuming location update doesn't change status
            driverTemplate.opsForHash().put(driverKey, driverId, driver);
        }
    }

    // New method to update driver status
    public void updateDriverStatus(Region region, String driverId, DriverStatus status) {
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, ConsistencyLevel.STRONG);
        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        Driver driver = (Driver) driverTemplate.opsForHash().get(driverKey, driverId);
        if (driver != null) {
            driver.setStatus(status);
            driverTemplate.opsForHash().put(driverKey, driverId, driver);
        }
    }

    public List<Driver> findAllDrivers(Region region, ConsistencyLevel consistencyLevel) {
        RedisTemplate<String, Driver> driverTemplate = router.getDriverTemplate(region, consistencyLevel);
        String driverKey = DRIVER_KEY_PREFIX + region.name().toLowerCase();
        // With the ObjectMapper correctly configured, this call now correctly returns List<Driver>.
        return driverTemplate.opsForHash().values(driverKey).stream()
                .map(obj -> (Driver) obj)
                .collect(Collectors.toList());
    }

    public void removeDriverFromGeoIndex(Region region, String driverId) {
        RedisTemplate<String, String> geoTemplate = router.getGeoTemplate(region, ConsistencyLevel.STRONG);
        String driverGeoKey = DRIVER_GEO_KEY_PREFIX + region.name().toLowerCase();
        geoTemplate.opsForGeo().remove(driverGeoKey, driverId);
    }
}
