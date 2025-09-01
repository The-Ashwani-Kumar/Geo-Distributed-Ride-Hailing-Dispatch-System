package com.ashwani.service;

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

import java.util.List;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService{

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public Ride bookRide(String passengerId) {
        System.out.println("Booking ride for passenger ID: " + passengerId);
        // 1. Get passenger from repository
        Passenger passenger = passengerRepository.findPassengerById(passengerId);
        if (passenger == null) {
            throw new PassengerNotFoundException("Passenger not found with ID: " + passengerId);
        }
        if(passenger.getStatus().equals("on_ride")){
            throw new AlreadyExistsException("Passenger is already on a ride");
        }

        Double lat = passenger.getLatitude();
        Double lon = passenger.getLongitude();

        // 2. Find nearby drivers (sorted by distance)
        GeoResults<GeoLocation<String>> nearbyDrivers = rideRepository.getNearByDrivers(lat, lon);
        if (nearbyDrivers == null || nearbyDrivers.getContent().isEmpty()) {
            throw new RideNotFoundException("Sorry, No drivers nearby!");
        }

        String nearestDriverId = null;

        // 3. Iterate through drivers and pick the first available
        for (GeoResult<GeoLocation<String>> geoResult : nearbyDrivers) {
            String driverId = geoResult.getContent().getName();

            Driver driver = driverRepository.findDriverById(driverId); // fetch from hash
            if (driver != null && "available".equalsIgnoreCase(driver.getStatus())) {
                nearestDriverId = driver.getId();
                break;
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
        passengerRepository.savePassenger(passenger);

        // Save ride in Redis
        rideRepository.bookRide(ride);

        // 5. Update driver status -> on_ride
        rideRepository.updateDriverStatus(nearestDriverId, "on_ride");

        return ride;
    }


    @Override
    public Ride endRide(String rideId) {
        Ride ride = (Ride) rideRepository.findRideById(rideId);
        if (ride == null){
            throw new RideNotFoundException("Ride not found with ID: " + rideId);
        }
        if(ride.getStatus().equals("completed")){
            throw new RideNotFoundException("Ride already completed with ID: " + rideId);
        }

        ride.setStatus("completed");
        ride.setEndTime(System.currentTimeMillis());
        rideRepository.endRide(rideId, ride);

        // Free up passenger
        Passenger passenger = passengerRepository.findPassengerById(ride.getPassengerId());
        if (passenger != null) {
            passenger.setStatus("online");
            passengerRepository.savePassenger(passenger);
        }

        // Free up driver
        rideRepository.updateDriverStatus(ride.getDriverId(), "available");

        return ride;
    }

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAllRides();
    }
}
