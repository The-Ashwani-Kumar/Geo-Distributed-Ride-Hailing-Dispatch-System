// src/context/RideContext.tsx
import { createContext, useContext, useState, ReactNode, useEffect } from "react";
import { Driver, Passenger, Ride } from "../types";

interface RideContextType {
  drivers: Driver[];
  passengers: Passenger[];
  rides: Ride[];
  currentLocation: [number, number] | null;
  setDrivers: React.Dispatch<React.SetStateAction<Driver[]>>;
  setPassengers: React.Dispatch<React.SetStateAction<Passenger[]>>;
  setRides: React.Dispatch<React.SetStateAction<Ride[]>>;
  setCurrentLocation: React.Dispatch<React.SetStateAction<[number, number] | null>>;
}


const RideContext = createContext<RideContextType | undefined>(undefined);

export const RideProvider = ({ children }: { children: ReactNode }) => {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [passengers, setPassengers] = useState<Passenger[]>([]);
  const [rides, setRides] = useState<Ride[]>([]);
  const [currentLocation, setCurrentLocation] = useState<[number, number] | null>(null);

  // Get browser current location once
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setCurrentLocation([pos.coords.latitude, pos.coords.longitude]);
        },
        () => {
          // fallback → Chennai
          setCurrentLocation([12.983317, 80.242695]);
        }
      );
    } else {
      setCurrentLocation([12.983317, 80.242695]);
    }
  }, []);

  return (
    <RideContext.Provider
      value={{
        drivers,
        passengers,
        rides,
        currentLocation,
        setDrivers,
        setPassengers,
        setRides,
        setCurrentLocation,
      }}
    >
      {children}
    </RideContext.Provider>
  );
};

export const useRideContext = () => {
  const context = useContext(RideContext);
  if (!context) throw new Error("useRideContext must be used within RideProvider");
  return context;
};
