package com.ashwani.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import com.ashwani.entity.Driver;
import com.ashwani.entity.Passenger;
import com.ashwani.entity.Ride;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final Environment env;

    public RedisConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    public LettuceConnectionFactory masterLettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(env.getProperty("spring.redis.master.host"));
        redisStandaloneConfiguration.setPort(Integer.parseInt(env.getProperty("spring.redis.master.port")));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public LettuceConnectionFactory replicaLettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(env.getProperty("spring.redis.replica.host"));
        redisStandaloneConfiguration.setPort(Integer.parseInt(env.getProperty("spring.redis.replica.port")));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> masterRedisTemplate(@Qualifier("masterLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createStringTemplate(factory);
    }

    @Bean
    public RedisTemplate<String, String> replicaRedisTemplate(@Qualifier("replicaLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createStringTemplate(factory);
    }

    @Bean
    public RedisTemplate<String, Driver> masterDriverRedisTemplate(@Qualifier("masterLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Driver.class);
    }

    @Bean
    public RedisTemplate<String, Driver> replicaDriverRedisTemplate(@Qualifier("replicaLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Driver.class);
    }

    @Bean
    public RedisTemplate<String, Passenger> masterPassengerRedisTemplate(@Qualifier("masterLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Passenger.class);
    }

    @Bean
    public RedisTemplate<String, Passenger> replicaPassengerRedisTemplate(@Qualifier("replicaLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Passenger.class);
    }

    @Bean
    public RedisTemplate<String, Ride> masterRideRedisTemplate(@Qualifier("masterLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Ride.class);
    }

    @Bean
    public RedisTemplate<String, Ride> replicaRideRedisTemplate(@Qualifier("replicaLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createJsonTemplate(factory, Ride.class);
    }

    /**
     * Generic RedisTemplate for JSON serialization
     */
    public <T> RedisTemplate<String, T> createJsonTemplate(RedisConnectionFactory factory, Class<T> clazz) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Generic RedisTemplate for String serialization
     */
    public RedisTemplate<String, String> createStringTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, String> masterGeoRedisTemplate(@Qualifier("masterLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createStringTemplate(factory);
    }

    @Bean
    public RedisTemplate<String, String> replicaGeoRedisTemplate(@Qualifier("replicaLettuceConnectionFactory") RedisConnectionFactory factory) {
        return createStringTemplate(factory);
    }

    /**
     * RedisTemplate for GEO (member type = String)
     */
    public RedisTemplate<String, String> createGeoTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
