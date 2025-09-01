// src/services/api.ts
import axios, { AxiosError } from "axios";
import { Driver, Passenger, Ride } from "../types";

const API_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor for global error handling
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response) {
      // Server responded with an error
      console.error("API Error:", error.response.status, error.response.data);
      return Promise.reject(error.response.data); // JSON from Spring exception
    } else if (error.request) {
      console.error("Network Error:", error.request);
      return Promise.reject({ message: "Network error, please try again" });
    } else {
      console.error("Unexpected Axios error:", error.message);
      return Promise.reject({ message: error.message });
    }
  }
);

// API Calls
export const getDrivers = async (): Promise<Driver[]> => {
  const response = await api.get("/drivers");
  return response.data;
};

export const getPassengers = async (): Promise<Passenger[]> => {
  const response = await api.get("/passengers");
  return response.data;
};

export const bookRide = async (passengerId: string): Promise<Ride> => {
  const response = await api.post(`/rides/book?id=${passengerId}`);
  return response.data;
};

export const endRide = async (rideId: string): Promise<Ride> => {
  const response = await api.post(`/rides/end?id=${rideId}`);
  return response.data;
};

export const addDriver = async (driver: any) => {
  const response = await api.post("/drivers", driver);
  return response.data;
};

export const addPassenger = async (passenger: any) => {
  const response = await api.post("/passengers", passenger);
  return response.data;
};

export const getRides = async (): Promise<Ride[]> => {
  const response = await api.get("/rides");
  return response.data;
};

export default api;
