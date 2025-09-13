package com.ashwani.service;

import com.ashwani.config.ConsistencyContext;
import com.ashwani.entity.Driver;
import com.ashwani.entity.Passenger;
import com.ashwani.entity.Ride;
import com.ashwani.exception.AlreadyExistsException;
import com.ashwani.exception.NotFoundException;
import com.ashwani.exception.PassengerNotFoundException;
import com.ashwani.exception.RideNotFoundException;
import com.ashwani.repository.DriverRepository;
import com.ashwani.repository.PassengerRepository;
import com.ashwani.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService{

    private static final Logger logger = LoggerFactory.getLogger(RideServiceImpl.class);

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

        @Override
    public Ride bookRide(String passengerId) {
        logger.info("Booking ride for passenger ID: {}", passengerId);
        String consistency = ConsistencyContext.getConsistencyLevel();
        logger.info("Consistency level for booking: {}", consistency);

        // 1. Get passenger from repository
        Passenger passenger = passengerRepository.findById(passengerId, consistency);
        if (passenger == null) {
            throw new PassengerNotFoundException("Passenger not found with ID: " + passengerId);
        }
        if(passenger.getStatus().equals("on_ride")){
            throw new AlreadyExistsException("Passenger is already on a ride");
        }

        Double lat = passenger.getLatitude();
        Double lon = passenger.getLongitude();

        // 2. Find nearby drivers (sorted by distance)
        // Always use strong consistency for booking to ensure up-to-date driver availability
        logger.info("Searching for nearby drivers at lat: {}, lon: {} with strong consistency", lat, lon);
        GeoResults<GeoLocation<String>> nearbyDrivers = rideRepository.getNearByDrivers(lat, lon, "strong");
        logger.info("Found {} nearby drivers.", nearbyDrivers != null ? nearbyDrivers.getContent().size() : 0);

        if (nearbyDrivers == null || nearbyDrivers.getContent().isEmpty()) {
            throw new RideNotFoundException("Sorry, No drivers nearby!");
        }

        String nearestDriverId = null;

        // 3. Iterate through drivers and pick the first available
        for (GeoResult<GeoLocation<String>> geoResult : nearbyDrivers) {
            String driverId = geoResult.getContent().getName();
            logger.info("Checking driver ID: {}", driverId);

            // Always use strong consistency for booking to ensure up-to-date driver status
            Driver driver = driverRepository.findDriverById(driverId, "strong"); // fetch from hash
            if (driver != null) {
                logger.info("Driver {} status: {}", driverId, driver.getStatus());
                if ("available".equalsIgnoreCase(driver.getStatus())) {
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

        // 4. Create and save ride object
        Ride ride = new Ride();
        ride.setId(UUID.randomUUID().toString());
        ride.setPassengerId(passengerId);
        ride.setDriverId(nearestDriverId);
        ride.setStatus("ongoing");
        ride.setStartTime(System.currentTimeMillis());

        // Update passenger status -> on_ride
        passenger.setStatus("on_ride");
        passengerRepository.save(passenger);

        // Save ride in Redis
        rideRepository.save(ride);

        // 5. Update driver status -> on_ride
        rideRepository.updateDriverStatus(nearestDriverId, "on_ride");

        return ride;
    }


    @Override
    public Ride endRide(String rideId) {
        String consistency = ConsistencyContext.getConsistencyLevel();
        Ride ride = rideRepository.findById(rideId, consistency);
        if (ride == null){
            throw new RideNotFoundException("Ride not found with ID: " + rideId);
        }
        if(ride.getStatus().equals("completed")){
            throw new RideNotFoundException("Ride already completed with ID: " + rideId);
        }

        ride.setStatus("completed");
        ride.setEndTime(System.currentTimeMillis());
        rideRepository.save(ride);

        // Free up passenger
        Passenger passenger = passengerRepository.findById(ride.getPassengerId(), consistency);
        if (passenger != null) {
            passenger.setStatus("online");
            passengerRepository.save(passenger);
        }

        // Free up driver
        rideRepository.updateDriverStatus(ride.getDriverId(), "available");

        return ride;
    }

    @Override
    public List<Ride> getAllRides() {
        String consistency = ConsistencyContext.getConsistencyLevel();
        return rideRepository.findAll(consistency);
    }
}