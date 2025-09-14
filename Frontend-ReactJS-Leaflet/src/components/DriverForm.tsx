// src/components/DriverForm.tsx
import React, { useState } from "react";
import { useRideContext } from "../context/RideContext";
import { Button, Form, Spinner } from "react-bootstrap";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import "../styles/Form.css";
import { addDriver } from "../services/api";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css"; 

const LocationPicker = ({
  onSelect,
  initial,
}: {
  onSelect: (latitude: number, longitude: number) => void;
  initial: [number, number] | null;
}) => {
  const [position, setPosition] = useState<[number, number] | null>(initial);

  useMapEvents({
    click(e) {
      const newPos: [number, number] = [e.latlng.lat, e.latlng.lng];
      setPosition(newPos);
      onSelect(newPos[0], newPos[1]);
    },
  });

  return position ? <Marker position={position} /> : null;
};

const DriverForm = () => {
  const { drivers, setDrivers, currentLocation } = useRideContext();
  const [name, setName] = useState("");
  const [location, setLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(
    currentLocation
      ? { latitude: currentLocation[0], longitude: currentLocation[1] }
      : null
  );
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !location) {
      return toast.error("Please provide both name and location.");
    }
    setLoading(true);

    const newDriver = {
      name: name,
      latitude: location.latitude,
      longitude: location.longitude
    };
    
    try {
      
      // save to backend
       const savedDriver = await addDriver(newDriver);


      toast.success(`Drive safely ${name}!`);
      // Add Driver to Drivers
      setDrivers([...drivers, savedDriver]);

      // reset form
      setName("");
      setLocation(
        currentLocation
          ? { latitude: currentLocation[0], longitude: currentLocation[1] }
          : null
      );
      console.log("Driver Data : " + JSON.stringify(savedDriver));
    } catch (err) {
      console.error("Error adding driver", err);
      toast.error("❌ Failed to add driver");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Toast container */}
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
      <Form onSubmit={handleSubmit}>
        {/* Floating Label Input */}
        <div className="floating-label mb-3">
          <input
            type="text"
            className="form-control"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            placeholder=" " // 👈 required for floating effect
          />
          <label>Driver Name</label>
        </div>

        <Form.Group className="mb-3">
          <Form.Label>Select Location (Click on Map)</Form.Label>
          <div
            style={{ height: "200px", borderRadius: "8px", overflow: "hidden" }}
          >
            {currentLocation && (
              <MapContainer
                center={currentLocation}
                zoom={13}
                style={{ height: "100%", width: "100%" }}
              >
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution="&copy; OpenStreetMap contributors"
                />
                <LocationPicker
                  initial={
                    location
                      ? [location.latitude, location.longitude]
                      : currentLocation
                  }
                  onSelect={(latitude, longitude) =>
                    setLocation({ latitude, longitude })
                  }
                />
              </MapContainer>
            )}
          </div>
        </Form.Group>

        <Button
          type="submit"
          className="w-100 d-flex align-items-center justify-content-center gap-2"
          variant="primary"
          disabled={loading}
        >
          {loading && <Spinner as="span" animation="border" size="sm" />}
          {loading ? (
            "Adding..."
          ) : (
            <>
              <i className="bi bi-taxi-front me-2"></i> Add Driver
            </>
          )}
        </Button>
      </Form>
    </>
  );
};

export default DriverForm;
