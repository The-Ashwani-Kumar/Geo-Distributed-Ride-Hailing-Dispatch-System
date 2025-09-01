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

  const fetchUsers = async () => {
    try {
      const fetchedDrivers = await getDrivers();
      setDrivers(fetchedDrivers);
      const fetchedPassengers = await getPassengers();
      setPassengers(fetchedPassengers);
      const fetchedRides = await getRides();
      setRides(fetchedRides);

      // build paths for rides
      for (const ride of fetchedRides) {
        const passenger = fetchedPassengers.find(
          (p) => p.id === ride.passengerId
        );
        const driver = fetchedDrivers.find((d) => d.id === ride.driverId);

        if (passenger && driver) {
          const res = await fetch(
            `https://router.project-osrm.org/route/v1/driving/${driver.longitude},${driver.latitude};${passenger.longitude},${passenger.latitude}?geometries=geojson`
          );
          const data = await res.json();

          if (data.routes && data.routes[0]) {
            const coords = data.routes[0].geometry.coordinates.map(
              (c: [number, number]) => [c[1], c[0]] as [number, number]
            );
            setRidePaths((prev) => ({ ...prev, [ride.id]: coords }));
          }
        }
      }
    } catch (err) {
      toast.error("Failed to fetch data from server, Router site under maintenance");
    }
  };

  useEffect(() => {
    fetchUsers();
    const interval = setInterval(fetchUsers, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleRideBooking = async (passengerId: string) => {
    try {
      const ride = await bookRide(passengerId);
      toast.success(`Ride Booked with ${ride.driverId}`);
      fetchUsers(); // refresh data
    } catch (err: any) {
      toast.error(err.message || "Failed to book ride");
    }
  };

  const handleEndRide = async (rideId: string) => {
    try {
      await endRide(rideId);
      toast.success("✅ Ride ended");
      setRides((prev) => prev.filter((r) => r.id !== rideId));
      setRidePaths((prev) => {
        const copy = { ...prev };
        delete copy[rideId];
        return copy;
      });
      setPopupInfo(null);
    } catch (err: any) {
      toast.error(err.message || "Failed to end ride");
    }
  };

  return (
    <div className="h-100 w-100">
      {/* Toast container */}
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
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
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <ZoomControl position="topright" />

          {/* Current User */}
          <Marker position={currentLocation} icon={passengerOffIcon}>
            <Popup>📍 You are here</Popup>
          </Marker>

          {/* Ride Paths */}
          {rides.map(
            (ride) =>
              ridePaths[ride.id] && (
                <React.Fragment key={ride.id}>
                  <Polyline
                    positions={ridePaths[ride.id]}
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
                      eventHandlers={{
                        remove: () => setPopupInfo(null),
                      }}
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
              )
          )}

          {/* Drivers */}
          {drivers.length > 0 &&
            drivers
              .filter(
                (driver) =>
                  driver && driver.latitude != null && driver.longitude != null
              )
              .map((driver) => (
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
                      {driver.status !== "available" && (
                        <Button className="d-flex align-items-center justify-content-center m-2 btn-success">
                          I'm on a Ride 🚗
                        </Button>
                      )}
                    </div>
                  </Popup>
                </Marker>
              ))}

          {/* Passengers */}
          {passengers.length > 0 &&
            passengers
              .filter((p) => p && p.latitude != null && p.longitude != null)
              .map((passenger) => (
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
                        <div className="d-flex align-items-center justify-content-center">
                          <Button
                            onClick={() => handleRideBooking(passenger.id)}
                          >
                            Book Ride
                          </Button>
                        </div>
                      ) : (
                        <Button className="d-flex align-items-center justify-content-center m-2 btn-success">
                          I'm on a Ride 🚗
                        </Button>
                      )}
                    </div>
                  </Popup>
                </Marker>
              ))}
        </MapContainer>
      )}
    </div>
  );
};

export default MapView;
