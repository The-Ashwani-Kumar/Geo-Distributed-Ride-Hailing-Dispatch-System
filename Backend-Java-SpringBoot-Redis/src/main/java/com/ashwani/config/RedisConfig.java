package com.ashwani.config;

import com.ashwani.entity.Driver;
import com.ashwani.entity.Passenger;
import com.ashwani.entity.Ride;
import com.ashwani.enums.Region;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class RedisConfig {

    //<editor-fold desc="Host and Port Properties">
    @Value("${spring.redis.us.master.host:redis-us-master}")
    private String usMasterHost;
    @Value("${spring.redis.us.master.port:6379}")
    private int usMasterPort;

    @Value("${spring.redis.us.replica.host:redis-us-slave}")
    private String usReplicaHost;
    @Value("${spring.redis.us.replica.port:6379}")
    private int usReplicaPort;

    @Value("${spring.redis.eu.master.host:redis-eu-master}")
    private String euMasterHost;
    @Value("${spring.redis.eu.master.port:6379}")
    private int euMasterPort;

    @Value("${spring.redis.eu.replica.host:redis-eu-slave}")
    private String euReplicaHost;
    @Value("${spring.redis.eu.replica.port:6379}")
    private int euReplicaPort;

    @Value("${spring.redis.asia.master.host:redis-asia-master}")
    private String asiaMasterHost;
    @Value("${spring.redis.asia.master.port:6379}")
    private int asiaMasterPort;

    @Value("${spring.redis.asia.replica.host:redis-asia-slave}")
    private String asiaReplicaHost;
    @Value("${spring.redis.asia.replica.port:6379}")
    private int asiaReplicaPort;
    //</editor-fold>

    //<editor-fold desc="Connection Factory Beans">
    @Bean
    public Map<Region, JedisConnectionFactory> masterConnectionFactories() {
        Map<Region, JedisConnectionFactory> map = new EnumMap<>(Region.class);
        map.put(Region.US, createJedisConnectionFactory(usMasterHost, usMasterPort));
        map.put(Region.EU, createJedisConnectionFactory(euMasterHost, euMasterPort));
        map.put(Region.ASIA, createJedisConnectionFactory(asiaMasterHost, asiaMasterPort));
        return map;
    }

    @Bean
    public Map<Region, JedisConnectionFactory> replicaConnectionFactories() {
        Map<Region, JedisConnectionFactory> map = new EnumMap<>(Region.class);
        map.put(Region.US, createJedisConnectionFactory(usReplicaHost, usReplicaPort));
        map.put(Region.EU, createJedisConnectionFactory(euReplicaHost, euReplicaPort));
        map.put(Region.ASIA, createJedisConnectionFactory(asiaReplicaHost, asiaReplicaPort));
        return map;
    }

    private JedisConnectionFactory createJedisConnectionFactory(String host, int port) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.afterPropertiesSet(); // Ensure factory is initialized
        return factory;
    }
    //</editor-fold>

    //<editor-fold desc="Redis Template Generation">
    private <V> RedisTemplate<String, V> createRedisTemplate(JedisConnectionFactory factory, Class<V> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<V> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, valueType);

        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    private RedisTemplate<String, String> createStringRedisTemplate(JedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    private <V> Map<Region, RedisTemplate<String, V>> createRedisTemplateMap(Map<Region, JedisConnectionFactory> factories, Class<V> valueType) {
        return factories.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> createRedisTemplate(entry.getValue(), valueType),
                        (u, v) -> u,
                        () -> new EnumMap<>(Region.class)
                ));
    }

    private Map<Region, RedisTemplate<String, String>> createStringRedisTemplateMap(Map<Region, JedisConnectionFactory> factories) {
        return factories.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> createStringRedisTemplate(entry.getValue()),
                        (u, v) -> u,
                        () -> new EnumMap<>(Region.class)
                ));
    }
    //</editor-fold>

    //<editor-fold desc="Master RedisTemplate Beans">
    @Bean
    public Map<Region, RedisTemplate<String, Driver>> masterDriverRedisTemplates() {
        return createRedisTemplateMap(masterConnectionFactories(), Driver.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, Ride>> masterRideRedisTemplates() {
        return createRedisTemplateMap(masterConnectionFactories(), Ride.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, Passenger>> masterPassengerRedisTemplates() {
        return createRedisTemplateMap(masterConnectionFactories(), Passenger.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, String>> masterGeoRedisTemplates() {
        return createStringRedisTemplateMap(masterConnectionFactories());
    }
    //</editor-fold>

    //<editor-fold desc="Replica RedisTemplate Beans">
    @Bean
    public Map<Region, RedisTemplate<String, Driver>> replicaDriverRedisTemplates() {
        return createRedisTemplateMap(replicaConnectionFactories(), Driver.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, Ride>> replicaRideRedisTemplates() {
        return createRedisTemplateMap(replicaConnectionFactories(), Ride.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, Passenger>> replicaPassengerRedisTemplates() {
        return createRedisTemplateMap(replicaConnectionFactories(), Passenger.class);
    }

    @Bean
    public Map<Region, RedisTemplate<String, String>> replicaGeoRedisTemplates() {
        return createStringRedisTemplateMap(replicaConnectionFactories());
    }
    //</editor-fold>
}
