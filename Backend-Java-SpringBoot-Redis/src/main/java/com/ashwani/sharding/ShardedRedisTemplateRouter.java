package com.ashwani.sharding;

import com.ashwani.entity.Driver;
import com.ashwani.entity.Passenger;
import com.ashwani.entity.Ride;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.Region;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ShardedRedisTemplateRouter {

    //<editor-fold desc="Redis Template Maps">
    @Resource(name = "masterDriverRedisTemplates")
    private Map<Region, RedisTemplate<String, Driver>> masterDriverRedisTemplates;

    @Resource(name = "replicaDriverRedisTemplates")
    private Map<Region, RedisTemplate<String, Driver>> replicaDriverRedisTemplates;

    @Resource(name = "masterRideRedisTemplates")
    private Map<Region, RedisTemplate<String, Ride>> masterRideRedisTemplates;

    @Resource(name = "replicaRideRedisTemplates")
    private Map<Region, RedisTemplate<String, Ride>> replicaRideRedisTemplates;

    @Resource(name = "masterPassengerRedisTemplates")
    private Map<Region, RedisTemplate<String, Passenger>> masterPassengerRedisTemplates;

    @Resource(name = "replicaPassengerRedisTemplates")
    private Map<Region, RedisTemplate<String, Passenger>> replicaPassengerRedisTemplates;

    @Resource(name = "masterGeoRedisTemplates")
    private Map<Region, RedisTemplate<String, String>> masterGeoRedisTemplates;

    @Resource(name = "replicaGeoRedisTemplates")
    private Map<Region, RedisTemplate<String, String>> replicaGeoRedisTemplates;
    //</editor-fold>

    public RedisTemplate<String, Driver> getDriverTemplate(Region region, ConsistencyLevel consistencyLevel) {
        return getTemplate(region, consistencyLevel, masterDriverRedisTemplates, replicaDriverRedisTemplates);
    }

    public RedisTemplate<String, Ride> getRideTemplate(Region region, ConsistencyLevel consistencyLevel) {
        return getTemplate(region, consistencyLevel, masterRideRedisTemplates, replicaRideRedisTemplates);
    }

    public RedisTemplate<String, Passenger> getPassengerTemplate(Region region, ConsistencyLevel consistencyLevel) {
        return getTemplate(region, consistencyLevel, masterPassengerRedisTemplates, replicaPassengerRedisTemplates);
    }

    public RedisTemplate<String, String> getGeoTemplate(Region region, ConsistencyLevel consistencyLevel) {
        return getTemplate(region, consistencyLevel, masterGeoRedisTemplates, replicaGeoRedisTemplates);
    }

    private <V> RedisTemplate<String, V> getTemplate(Region region, ConsistencyLevel consistencyLevel, Map<Region, RedisTemplate<String, V>> masterMap, Map<Region, RedisTemplate<String, V>> replicaMap) {
        if (region == null) {
            // Default to a primary region if no region is specified in the request
            region = Region.US; // Or throw an exception, depending on desired behavior
        }

        if (ConsistencyLevel.STRONG.equals(consistencyLevel)) {
            return masterMap.get(region);
        } else {
            return replicaMap.get(region);
        }
    }
}
