// src/utils/icons.ts
import L from "leaflet";
import passenger_off from "../assets/passenger-off.png";
import passenger_on from "../assets/passenger-on.png";
import driver_off from "../assets/driver-off.png";
import driver_on from "../assets/driver-on.png";
import "leaflet/dist/leaflet.css";


L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
  iconUrl: require("leaflet/dist/images/marker-icon.png"),
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

export const passengerOffIcon = new L.Icon({
  iconUrl: passenger_off,
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  popupAnchor: [0, -35],
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
  shadowSize: [41, 41],
});

export const passengerOnIcon = new L.Icon({
  iconUrl: passenger_on,
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  popupAnchor: [0, -35],
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
  shadowSize: [41, 41],
});

export const driverOnIcon = new L.Icon({
  iconUrl: driver_on,
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  popupAnchor: [0, -35],
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
  shadowSize: [41, 41],
});

export const driverOffIcon = new L.Icon({
  iconUrl: driver_off,
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  popupAnchor: [0, -35],
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
  shadowSize: [41, 41],
});


