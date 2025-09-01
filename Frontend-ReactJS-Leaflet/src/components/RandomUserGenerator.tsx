import React, { useState } from "react";
import { v4 as uuidv4 } from "uuid";
import { useRideContext } from "../context/RideContext";
import {
  generateRandomDrivers,
  generateRandomPassengers,
} from "../utils/randomData";
import "../styles/Form.css";
import { addDriver, addPassenger } from "../services/api";
import { toast, ToastContainer } from "react-toastify";
import { point, multiPolygon } from "@turf/helpers";
import { booleanPointInPolygon } from "@turf/boolean-point-in-polygon";
import landData from "../data/world_land.json";

const MAX_RETRIES = 20;

const RandomUserGenerator: React.FC = () => {
  const { currentLocation, setDrivers, setPassengers } = useRideContext();

  const [numDrivers, setNumDrivers] = useState(5);
  const [numPassengers, setNumPassengers] = useState(5);
  const [radius, setRadius] = useState(2); // km

  // Extract the coordinates from each geometry in the GeometryCollection.
  // We are assuming all geometries are Polygons, which is common for land data.
  const allPolygonCoordinates = landData.geometries.map(
    (geom) => geom.coordinates
  );

  // Use turf/helpers to create a single MultiPolygon from the collected coordinates.
  // This single object represents all the landmasses combined.
  const landMultiPolygon = multiPolygon(allPolygonCoordinates);

  // Mock land check - replace with real API for production
  const isLand = (lat: number, lon: number): boolean => {
    const checkPoint = point([lon, lat]);
    return booleanPointInPolygon(checkPoint, landMultiPolygon);
  };

  const generateLandCoordinates = async (count: number, forDriver = true) => {
    if (!currentLocation) return [];
    const [centerLat, centerLon] = currentLocation;
    const coordinates: [number, number][] = [];

    let attempts = 0;
    while (coordinates.length < count && attempts < MAX_RETRIES * count) {
      let pointObj;
      if (forDriver) {
        pointObj = generateRandomDrivers(1, centerLat, centerLon, radius)[0];
      } else {
        pointObj = generateRandomPassengers(1, centerLat, centerLon, radius)[0];
      }

      const point: [number, number] = [pointObj.latitude, pointObj.longitude];

      if (await isLand(point[0], point[1])) {
        coordinates.push(point);
      }
      attempts++;
    }

    return coordinates;
  };

  const addRandomUsers = async () => {
    if (!currentLocation) return;

    const driverCoords = await generateLandCoordinates(numDrivers, true);
    const passengerCoords = await generateLandCoordinates(numPassengers, false);

    const newDrivers = driverCoords.map(([lat, lon], i) => ({
      id: uuidv4(),
      name: `Driver_${i + 1}`,
      latitude: lat,
      longitude: lon,
      status: "available",
    }));

    const newPassengers = passengerCoords.map(([lat, lon], i) => ({
      id: uuidv4(),
      name: `Passenger_${i + 1}`,
      latitude: lat,
      longitude: lon,
      status: "online",
    }));

    try {
      await Promise.all(newDrivers.map((driver) => addDriver(driver)));
      setDrivers((prev) => [...prev, ...newDrivers]);

      await Promise.all(
        newPassengers.map((passenger) => addPassenger(passenger))
      );
      setPassengers((prev) => [...prev, ...newPassengers]);

      toast.success("Random Drivers and Passengers added!");
    } catch (err) {
      console.error(err);
      toast.error("Error adding Random Drivers and Passengers");
    }
  };

  return (
    <div className="gap-2 align-items-center mt-5 flex-wrap">
      <ToastContainer
        position="top-right"
        autoClose={3000} // ✅ auto dismiss after 3s
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
      {/* Floating Label Inputs */}
      <div className="floating-label">
        <input
          type="number"
          className="form-control"
          value={numDrivers}
          onChange={(e) => setNumDrivers(parseInt(e.target.value))}
          required
          placeholder=" "
          min={1}
        />
        <label>Number of Drivers</label>
      </div>

      <div className="floating-label">
        <input
          type="number"
          className="form-control"
          value={numPassengers}
          onChange={(e) => setNumPassengers(parseInt(e.target.value))}
          required
          placeholder=" "
          min={1}
        />
        <label>Number of Passengers</label>
      </div>

      <div className="floating-label">
        <input
          type="number"
          className="form-control"
          value={radius}
          onChange={(e) => setRadius(parseFloat(e.target.value))}
          required
          placeholder=" "
          min={0.1}
          step={0.1}
        />
        <label>Radius (km)</label>
      </div>

      <button className="btn btn-success mt-3" onClick={addRandomUsers}>
        Add Random Users
      </button>
    </div>
  );
};

export default RandomUserGenerator;
