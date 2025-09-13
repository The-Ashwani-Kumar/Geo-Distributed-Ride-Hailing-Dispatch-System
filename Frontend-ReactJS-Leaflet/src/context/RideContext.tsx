// src/context/RideContext.tsx
import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
} from "react";
import { Driver, Passenger, Ride } from "../types";
import {
  consistencyManager,
  ConsistencyLevel,
} from "../services/consistencyManager";

interface RideContextType {
  drivers: Driver[];
  passengers: Passenger[];
  rides: Ride[];
  currentLocation: [number, number] | null;
  consistencyLevel: ConsistencyLevel;
  setDrivers: React.Dispatch<React.SetStateAction<Driver[]>>;
  setPassengers: React.Dispatch<React.SetStateAction<Passenger[]>>;
  setRides: React.Dispatch<React.SetStateAction<Ride[]>>;
  setCurrentLocation: React.Dispatch<React.SetStateAction<[number, number] | null>>;
  setConsistencyLevel: (level: ConsistencyLevel) => void;
}

const RideContext = createContext<RideContextType | undefined>(undefined);

export const RideProvider = ({ children }: { children: ReactNode }) => {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [passengers, setPassengers] = useState<Passenger[]>([]);
  const [rides, setRides] = useState<Ride[]>([]);
  const [currentLocation, setCurrentLocation] = useState<[number, number] | null>(
    null
  );
  const [consistencyLevel, setReactConsistencyLevel] = useState<ConsistencyLevel>(
    consistencyManager.get()
  );

  // Subscribe to the consistencyManager to keep React state in sync
  useEffect(() => {
    const unsubscribe = consistencyManager.subscribe(setReactConsistencyLevel);
    return unsubscribe; // Cleanup subscription on component unmount
  }, []);

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

  // Function to update the consistency level
  const setConsistencyLevel = (level: ConsistencyLevel) => {
    consistencyManager.set(level);
  };

  return (
    <RideContext.Provider
      value={{
        drivers,
        passengers,
        rides,
        currentLocation,
        consistencyLevel,
        setDrivers,
        setPassengers,
        setRides,
        setCurrentLocation,
        setConsistencyLevel,
      }}
    >
      {children}
    </RideContext.Provider>
  );
};

export const useRideContext = () => {
  const context = useContext(RideContext);
  if (!context) {
    throw new Error("useRideContext must be used within RideProvider");
  }
  return context;
};
