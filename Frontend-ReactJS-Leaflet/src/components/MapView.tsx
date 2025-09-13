import React, { useEffect, useState, useRef } from "react";
import { Button } from "react-bootstrap";
import { toast, ToastContainer } from "react-toastify";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  ZoomControl,
  Polyline,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import MarkerClusterGroup from "react-leaflet-markercluster";
import {
  passengerOnIcon,
  driverOnIcon,
  passengerOffIcon,
  driverOffIcon,
} from "../assets/icons";
import { useRideContext } from "../context/RideContext";
import {
  endRide,
  bookRide,
  getDrivers,
  getPassengers,
  getRides,
} from "../services/api";
import { Ride } from "../types";

// Distance helper (Haversine formula)
const getDistance = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const R = 6371e3;
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const φ1 = toRad(lat1);
  const φ2 = toRad(lat2);
  const Δφ = toRad(lat2 - lat1);
  const Δλ = toRad(lon2 - lon1);

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
};

// Driver movement
const generateNewLocation = (
  lat: number,
  lon: number,
  targetLat?: number,
  targetLon?: number
): [number, number] => {
  if (targetLat !== undefined && targetLon !== undefined) {
    const distance = getDistance(lat, lon, targetLat, targetLon);
    // console.log("Distance to target: " + distance);

    if (distance < 20) {
      // console.log("driver came less than 20 meters : " + distance);
      // Snap to passenger
      return [targetLat, targetLon];
    }

    const step = Math.min(Math.max(distance * 0.1, 30), 150) / 111_111;
    const dirLat = Math.sign(targetLat - lat);
    const dirLon = Math.sign(targetLon - lon);

    const newLat = lat + Math.min(Math.abs(targetLat - lat), step) * dirLat;
    const newLon = lon + Math.min(Math.abs(targetLon - lon), step) * dirLon;

    return [newLat, newLon];
  }

  // Random wandering
  const latOffset = (Math.random() - 0.5) * 0.0015;
  const lonOffset = (Math.random() - 0.5) * 0.0015;
  return [lat + latOffset, lon + lonOffset];
};

const MapView: React.FC = () => {
  const {
    drivers,
    setDrivers,
    passengers,
    setPassengers,
    rides,
    setRides,
    currentLocation,
  } = useRideContext();

  const mapRef = useRef<L.Map | null>(null);
  const [ridePaths, setRidePaths] = useState<
    Record<string, [number, number][]>
  >({});
  const [popupInfo, setPopupInfo] = useState<{
    rideId: string;
    pos: [number, number];
  } | null>(null);

  // Build straight-line ride paths
  const buildRidePaths = (
    fetchedRides: Ride[],
    fetchedDrivers: any[],
    fetchedPassengers: any[]
  ) => {
    const newPaths: Record<string, [number, number][]> = {};
    for (const ride of fetchedRides) {
      if (ride.status === "completed" || ride.status === "cancelled") continue;

      const passenger = fetchedPassengers.find(
        (p) => p.id === ride.passengerId
      );
      const driver = fetchedDrivers.find((d) => d.id === ride.driverId);

      if (passenger && driver) {
        newPaths[ride.id] = [
          [driver.latitude, driver.longitude],
          [passenger.latitude, passenger.longitude],
        ];
      }
    }
    setRidePaths(newPaths);
  };

  // Fetch data from the server
const fetchData = async () => {
    try {
      const fetchedDrivers = await getDrivers();
      const fetchedPassengers = await getPassengers();
      const fetchedRides = await getRides();

      setPassengers(fetchedPassengers);
      setRides(fetchedRides);
      setDrivers(fetchedDrivers);
    } catch (err) {
      toast.error("Failed to fetch data from the server.");
    }
  };

  // End ride manually or auto
  const handleEndRide = async (rideId: string) => {
    try {
      await endRide(rideId);
      toast.success("Ride ended");
      setPopupInfo(null);
      fetchData(); // Refresh state from the server after API call
    } catch (err: any) {
      toast.error(err.message || "Failed to end ride");
    }
  };

  // Effect to handle smooth driver movement
  useEffect(() => {
    const movementInterval = setInterval(() => {
      setDrivers((prevDrivers) => {
        return prevDrivers.map((driver) => {
          if (driver.status === "on_ride") {
            const ride = rides.find((r) => r.driverId === driver.id);
            if (!ride) return driver; // no ride, skip

            const passenger = passengers.find((p) => p.id === ride.passengerId);
            if (
              !passenger ||
              passenger.latitude === undefined ||
              passenger.longitude === undefined
            ) {
              return driver; // missing passenger location, skip
            }

            const [newLat, newLon] = generateNewLocation(
              driver.latitude,
              driver.longitude,
              passenger.latitude,
              passenger.longitude
            );
            if (
              newLat === passenger.latitude &&
              newLon === passenger.longitude
            ) {
              // console.log("Driver has reached the passenger");
              handleEndRide(ride.id);
              fetchData();
            }
            return { ...driver, latitude: newLat, longitude: newLon };
          }

          if (driver.status === "available") {
            const [newLat, newLon] = generateNewLocation(
              driver.latitude,
              driver.longitude
            );
            return { ...driver, latitude: newLat, longitude: newLon };
          }
          return driver;
        });
      });
    }, 3000);
    return () => clearInterval(movementInterval);
  }, [rides, drivers,passengers, setDrivers, setPassengers, setRides]);

  // Effect to check for automatic end-ride condition
  useEffect(() => {
    const checkAutoEndRide = () => {
      rides.forEach((ride) => {
        // console.log("Checking ride: " + ride.id + " status: " + ride.status);
        const driver = drivers.find((d) => d.id === ride.driverId);
        const passenger = passengers.find((p) => p.id === ride.passengerId);
        if (driver && passenger && ride.status === "ongoing") {
          console.log(driver, passenger, ride);
          const distance = getDistance(
            driver.latitude,
            driver.longitude,
            passenger.latitude,
            passenger.longitude
          );
          // console.log(driver.name + " distance to passenger: " + distance);
          if (distance < 20) {
            // console.log("driver came less than 20 meters : " + driver.name);
            handleEndRide(ride.id);
          }
        }
      });
    };

    const interval = setInterval(checkAutoEndRide, 3000);
    return () => clearInterval(interval);
  }, [drivers, passengers, rides]);

  useEffect(() => {
    fetchData();
    // const interval = setInterval(fetchData, 10000);
    // return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    buildRidePaths(rides, drivers, passengers);
  }, [rides, drivers, passengers]);

  const handleRideBooking = async (passengerId: string) => {
    try {
      const ride = await bookRide(passengerId);
      toast.success(`Ride Booked with ${ride.driverId}`);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || "Failed to book ride");
    }
  };

  return (
    <div className="h-100 w-100">
      <ToastContainer position="top-right" autoClose={3000} />
      {!currentLocation ? (
        <div className="d-flex justify-content-center align-items-center h-100 w-100 bg-light">
          <div className="fw-bold text-secondary">🌍 Loading map...</div>
        </div>
      ) : (
        <MapContainer
          center={currentLocation}
          zoom={13}
          scrollWheelZoom={true}
          zoomControl={false}
          style={{ height: "100%", width: "100%" }}
          ref={mapRef}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ZoomControl position="topright" />
          <Marker position={currentLocation} icon={passengerOffIcon}>
            <Popup>📍 You are here</Popup>
          </Marker>

          {/* Ride paths */}
          {rides.map((ride) => {
            const path = ridePaths[ride.id];
            if (!path || path.length < 2) return null;

            return (
              <React.Fragment key={ride.id}>
                <Polyline
                  positions={path}
                  pathOptions={{ color: "green", weight: 4 }}
                  eventHandlers={{
                    click: (e) =>
                      setPopupInfo({
                        rideId: ride.id,
                        pos: [e.latlng.lat, e.latlng.lng],
                      }),
                  }}
                />
                {popupInfo?.rideId === ride.id && (
                  <Popup
                    position={popupInfo.pos}
                    eventHandlers={{ remove: () => setPopupInfo(null) }}
                  >
                    <div className="text-center">
                      <div className="fw-bold mb-2">🚗 Ride in Progress</div>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleEndRide(ride.id)}
                      >
                        End Ride
                      </Button>
                    </div>
                  </Popup>
                )}
              </React.Fragment>
            );
          })}

          <MarkerClusterGroup>
            {/* Drivers */}
            {drivers?.map((driver) => {
              if (!driver?.latitude || !driver?.longitude) return null;

              return (
                <Marker
                  key={driver.id}
                  position={[driver.latitude, driver.longitude]}
                  icon={
                    driver.status === "available" ? driverOffIcon : driverOnIcon
                  }
                >
                  <Popup>
                    <div className="flex align-items-center justify-content-center gap-2">
                      <div className="m-2">Hi, {driver.name} here!</div>
                      {driver.status === "on_ride" && (
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => {
                            const ride = rides.find(
                              (r) => r.driverId === driver.id
                            );
                            if (ride) handleEndRide(ride.id);
                          }}
                        >
                          End Ride
                        </Button>
                      )}
                    </div>
                  </Popup>
                </Marker>
              );
            })}

            {/* Passengers */}
            {passengers?.map((passenger) => {
              if (!passenger?.latitude || !passenger?.longitude) return null;

              return (
                <Marker
                  key={passenger.id}
                  position={[passenger.latitude, passenger.longitude]}
                  icon={
                    passenger.status === "online"
                      ? passengerOffIcon
                      : passengerOnIcon
                  }
                >
                  <Popup>
                    <div className="flex align-items-center justify-content-center gap-2">
                      <div className="m-2">Hi, {passenger.name} here!</div>
                      {passenger.status === "online" ? (
                        <Button
                          onClick={() => handleRideBooking(passenger.id)}
                        >
                          Book Ride
                        </Button>
                      ) : (
                        <Button className="btn-success">
                          I'm on a Ride 🚗
                        </Button>
                      )}
                    </div>
                  </Popup>
                </Marker>
              );
            })}
          </MarkerClusterGroup>
        </MapContainer>
      )}
    </div>
  );
};

export default MapView;
