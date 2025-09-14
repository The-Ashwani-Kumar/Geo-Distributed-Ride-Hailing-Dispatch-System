// src/utils/randomData.ts
import { v4 as uuidv4 } from "uuid";
import { Driver, Passenger } from "../types";

/**
 * Generates a random coordinate within a given radius (in km) from a center point
 */
const randomCoordinate = (lat: number, lon: number, radiusKm: number) => {
  const radiusInDegrees = radiusKm / 111; // Approx conversion
  const u = Math.random();
  const v = Math.random();
  const w = radiusInDegrees * Math.sqrt(u);
  const t = 2 * Math.PI * v;
  const latOffset = w * Math.cos(t);
  const lonOffset = w * Math.sin(t) / Math.cos((lat * Math.PI) / 180);
  return [lat + latOffset, lon + lonOffset];
};

export const generateRandomDrivers = (
  count: number,
  centerLat: number,
  centerLon: number,
  radiusKm: number
): Driver[] => {
  const drivers: Driver[] = [];
  for (let i = 0; i < count; i++) {
    const [lat, lon] = randomCoordinate(centerLat, centerLon, radiusKm);
    drivers.push({
      id: uuidv4(),
      name: `Driver_${i + 1}`,
      latitude: lat,
      longitude: lon,
      status: "AVAILABLE",
    });
  }
  return drivers;
};

export const generateRandomPassengers = (
  count: number,
  centerLat: number,
  centerLon: number,
  radiusKm: number
): Passenger[] => {
  const passengers: Passenger[] = [];
  for (let i = 0; i < count; i++) {
    const [lat, lon] = randomCoordinate(centerLat, centerLon, radiusKm);
    passengers.push({
      id: uuidv4(),
      name: `Passenger_${i + 1}`,
      latitude: lat,
      longitude: lon,
      status: "ONLINE",
    });
  }
  return passengers;
};
