import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
  useRef,
  useCallback,
} from "react";
import { Driver, Passenger, Ride, Region } from "../types";
import {
  consistencyManager,
  ConsistencyLevel,
} from "../services/consistencyManager";
import { regionManager } from "../services/regionManager";

interface RideContextType {
  drivers: Driver[];
  passengers: Passenger[];
  rides: Ride[];
  currentLocation: [number, number] | null;
  consistencyLevel: ConsistencyLevel;
  region: Region;
  setDrivers: React.Dispatch<React.SetStateAction<Driver[]>>;
  setPassengers: React.Dispatch<React.SetStateAction<Passenger[]>>;
  setRides: React.Dispatch<React.SetStateAction<Ride[]>>;
  setCurrentLocation: React.Dispatch<React.SetStateAction<[number, number] | null>>;
  setConsistencyLevel: (level: ConsistencyLevel) => void;
  setRegion: (region: Region) => void;
  refreshData: () => void;
  registerFetchData: (fetchFn: () => void) => void;
  unregisterFetchData: (fetchFn: () => void) => void;
}

const RideContext = createContext<RideContextType | undefined>(undefined);

export const RideProvider = ({ children }: { children: ReactNode }) => {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [passengers, setPassengers] = useState<Passenger[]>([]);
  const [rides, setRides] = useState<Ride[]>([]);
  const [currentLocation, setCurrentLocation] = useState<[number, number] | null>(
    null
  );
  const [region, setReactRegion] = useState<Region>(regionManager.get());

  const [consistencyLevel, setReactConsistencyLevel] = useState<ConsistencyLevel>(
    consistencyManager.get() || "STRONG"
  );

  // Use a Set to store multiple fetchData functions
  const fetchFnsRef = useRef<Set<() => void>>(new Set());

  // Function to register a fetchData function
  const registerFetchData = useCallback((fetchFn: () => void) => {
    fetchFnsRef.current.add(fetchFn);
    console.log("RideContext: Registered fetch function.", fetchFnsRef.current.size);
  }, []);

  // Function to unregister a fetchData function
  const unregisterFetchData = useCallback((fetchFn: () => void) => {
    fetchFnsRef.current.delete(fetchFn);
    console.log("RideContext: Unregistered fetch function.", fetchFnsRef.current.size);
  }, []);

  // Function to be exposed to components to trigger a data refresh
  const refreshData = useCallback(() => {
    console.log("RideContext: refreshData triggered.");
    fetchFnsRef.current.forEach(fetchFn => {
      console.log("RideContext: Calling registered fetch function.");
      fetchFn();
    });
  }, []);

  // Subscribe to the consistencyManager to keep React state in sync
  useEffect(() => {
    const handleConsistencyChange = (level: ConsistencyLevel) => {
      setReactConsistencyLevel(level);
    };
    const unsubscribe = consistencyManager.subscribe(handleConsistencyChange);
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

  // Function to update region
  const setRegion = (newRegion: Region) => {
    console.log("RideContext: Setting region to:", newRegion);
    regionManager.set(newRegion);
    setReactRegion(newRegion);
    // Trigger a data refresh when region changes
    refreshData();
  };

  return (
    <RideContext.Provider
      value={{
        drivers,
        passengers,
        rides,
        currentLocation,
        consistencyLevel,
        region,
        setDrivers,
        setPassengers,
        setRides,
        setCurrentLocation,
        setConsistencyLevel,
        setRegion,
        refreshData,
        registerFetchData,
        unregisterFetchData,
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
