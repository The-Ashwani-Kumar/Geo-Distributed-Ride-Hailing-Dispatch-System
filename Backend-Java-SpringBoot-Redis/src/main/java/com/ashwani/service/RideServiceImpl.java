package com.ashwani.service;

import com.ashwani.entity.Driver;
import com.ashwani.entity.Passenger;
import com.ashwani.entity.Ride;
import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.DriverStatus;
import com.ashwani.enums.PassengerStatus;
import com.ashwani.enums.Region;
import com.ashwani.enums.RideStatus;
import com.ashwani.exception.AlreadyExistsException;
import com.ashwani.exception.NotFoundException;
import com.ashwani.exception.PassengerNotFoundException;
import com.ashwani.exception.RideNotFoundException;
import com.ashwani.repository.DriverRepository;
import com.ashwani.repository.PassengerRepository;
import com.ashwani.repository.RideRepository;
import com.ashwani.sharding.ConsistencyContext;
import com.ashwani.sharding.RegionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService {

    private static final Logger logger = LoggerFactory.getLogger(RideServiceImpl.class);

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public Ride bookRide(Region region, String passengerId) {
        logger.info("Booking ride for passenger ID: {} in region {}", passengerId, region);
        // Write operations should always use STRONG consistency.
        ConsistencyLevel consistency = ConsistencyLevel.STRONG;

        Passenger passenger = passengerRepository.findById(region, passengerId, consistency);
        if (passenger == null) {
            throw new PassengerNotFoundException("Passenger not found with ID: " + passengerId);
        }
        if (PassengerStatus.ON_RIDE.equals(passenger.getStatus())) {
            throw new AlreadyExistsException("Passenger is already on a ride");
        }

        Double lat = passenger.getLatitude();
        Double lon = passenger.getLongitude();

        logger.info("Searching for nearby drivers at lat: {}, lon: {} with strong consistency in region {}", lat, lon, region);
        GeoResults<GeoLocation<String>> nearbyDrivers = rideRepository.getNearByDrivers(region, lat, lon, ConsistencyLevel.STRONG);
        logger.info("Found {} nearby drivers.", nearbyDrivers != null ? nearbyDrivers.getContent().size() : 0);

        if (nearbyDrivers == null || nearbyDrivers.getContent().isEmpty()) {
            throw new RideNotFoundException("Sorry, No drivers nearby!");
        }

        String nearestDriverId = null;

        for (GeoResult<GeoLocation<String>> geoResult : nearbyDrivers) {
            String driverId = geoResult.getContent().getName();
            logger.info("Checking driver ID: {}", driverId);

            Driver driver = driverRepository.findDriverById(region, driverId, ConsistencyLevel.STRONG);
            if (driver != null) {
                logger.info("Driver {} status: {}", driverId, driver.getStatus());
                if (DriverStatus.AVAILABLE.equals(driver.getStatus())) {
                    nearestDriverId = driver.getId();
                    logger.info("Found available driver: {}", nearestDriverId);
                    break;
                }
            } else {
                logger.warn("Driver {} found in GEO index but not in hash. Skipping.", driverId);
            }
        }

        if (nearestDriverId == null) {
            throw new NotFoundException("Sorry, No AVAILABLE drivers nearby!");
        }

        Ride ride = new Ride();
        ride.setId(UUID.randomUUID().toString());
        ride.setPassengerId(passengerId);
        ride.setDriverId(nearestDriverId);
        ride.setStatus(RideStatus.ONGOING);
        ride.setStartTime(System.currentTimeMillis());

        passengerRepository.updatePassengerStatus(region, passengerId, PassengerStatus.ON_RIDE);
        rideRepository.save(region, ride);
        rideRepository.updateDriverStatus(region, nearestDriverId, DriverStatus.ON_RIDE);

        return ride;
    }


    @Override
    public Ride endRide(Region region, String rideId) {
        // Write operations should always use STRONG consistency.
        ConsistencyLevel consistency = ConsistencyLevel.STRONG;
        Ride ride = rideRepository.findById(region, rideId, consistency);
        if (ride == null) {
            throw new RideNotFoundException("Ride not found with ID: " + rideId);
        }
        if (RideStatus.COMPLETED.equals(ride.getStatus())) {
            throw new AlreadyExistsException("Ride already completed with ID: " + rideId);
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setEndTime(System.currentTimeMillis());
        rideRepository.save(region, ride);

        passengerRepository.updatePassengerStatus(region, ride.getPassengerId(), PassengerStatus.ONLINE);
        rideRepository.updateDriverStatus(region, ride.getDriverId(), DriverStatus.AVAILABLE);

        return ride;
    }

    @Override
    public List<Ride> getAllRides() {
        Region region = RegionContext.getRegion();
        ConsistencyLevel consistencyLevel = ConsistencyContext.getConsistencyLevel();
        return rideRepository.findAll(region, consistencyLevel);
    }

    @Override
    public Ride getRideById(String rideId) {
        Region region = RegionContext.getRegion();
        ConsistencyLevel consistencyLevel = ConsistencyContext.getConsistencyLevel();
        return rideRepository.findById(region, rideId, consistencyLevel);
    }
}
