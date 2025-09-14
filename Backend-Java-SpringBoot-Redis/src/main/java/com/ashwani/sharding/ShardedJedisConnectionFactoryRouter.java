package com.ashwani.sharding;

import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.Region;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ShardedJedisConnectionFactoryRouter {

    @Resource(name = "masterConnectionFactories")
    private Map<Region, JedisConnectionFactory> masterConnectionFactories;

    @Resource(name = "replicaConnectionFactories")
    private Map<Region, JedisConnectionFactory> replicaConnectionFactories;

    public JedisConnectionFactory getConnectionFactory(ConsistencyLevel consistencyLevel) {
        Region region = RegionContextHolder.getRegion();
        if (region == null) {
            // Default to a specific region or throw an error if no region is set
            // For now, let's default to US to avoid NullPointerExceptions
            region = Region.US;
        }

        if (ConsistencyLevel.STRONG.equals(consistencyLevel)) {
            return masterConnectionFactories.get(region);
        } else {
            return replicaConnectionFactories.get(region);
        }
    }
}
