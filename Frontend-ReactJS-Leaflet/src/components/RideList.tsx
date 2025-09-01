import React, { useEffect, useState } from "react";
import { getRides, endRide } from "../services/api";
import { useRideContext } from "../context/RideContext";
import "bootstrap-icons/font/bootstrap-icons.css";

import { Ride } from "../types";
import { toast, ToastContainer } from "react-toastify";

const RideList: React.FC = () => {
  const { rides, setRides } = useRideContext();
  const [loading, setLoading] = useState(false);
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());

  // Fetch rides
  const fetchRides = async () => {
    setLoading(true);
    try {
      const response = await getRides();
      setRides(response);
    } catch (err) {
      console.error("Error fetching rides:", err);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchRides();
    const interval = setInterval(fetchRides, 5000);
    return () => clearInterval(interval);
  }, []);

  // End ride
  const handleEndRide = async (rideId: string) => {
    try {
      await endRide(rideId);
      setRides(
        rides.map((r) =>
          r.id === rideId
            ? { ...r, status: "completed", endTime: Date.now() }
            : r
        )
      );
    } catch (err) {
      console.error("Error ending ride:", err);
    }
  };

  // Toggle expand Ride ID
  const toggleExpand = (rideId: string) => {
    const newSet = new Set(expandedIds);
    if (newSet.has(rideId)) {
      newSet.delete(rideId);
    } else {
      newSet.add(rideId);
    }
    setExpandedIds(newSet);
  };

  // Copy Ride ID
  const copyToClipboard = (rideId: string) => {
    navigator.clipboard.writeText(rideId);
    toast.success("Ride ID copied!");
  };

  return (
    <div className="position-relative p-3">
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
      <h4 className="mb-3">Ongoing Rides</h4>

      {/* Scrollable table container */}
      <div
        className="table-responsive"
        style={{ maxHeight: "400px", overflowY: "auto" }}
      >
        <table className="table table-sm table-striped table-bordered text-center">
          <thead className="table-dark">
            <tr>
              <th>S. No.</th>
              <th>Ride ID</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {rides.map((ride: Ride, index: number) => (
              <tr key={ride.id}>
                <td>{index + 1}</td>
                <td style={{ maxWidth: "200px", position: "relative" }}>
                  {expandedIds.has(ride.id)
                    ? ride.id
                    : `${ride.id.slice(0, 8)}...`}
                  <i
                    className={`bi ${
                      expandedIds.has(ride.id)
                        ? "bi-chevron-up"
                        : "bi-chevron-down"
                    }`}
                    style={{
                      cursor: "pointer",
                      marginLeft: "5px",
                      color: "#0d6efd",
                    }}
                    onClick={() => toggleExpand(ride.id)}
                    title="Expand/Collapse"
                  ></i>
                  <i
                    className="bi bi-clipboard"
                    style={{
                      cursor: "pointer",
                      marginLeft: "5px",
                      color: "#198754",
                    }}
                    onClick={() => copyToClipboard(ride.id)}
                    title="Copy Ride ID"
                  ></i>
                </td>
                <td>{new Date(ride.startTime).toLocaleString()}</td>
                <td>
                  {ride.endTime ? new Date(ride.endTime).toLocaleString() : "-"}
                </td>
                <td>
                  <button
                    className={`btn btn-sm btn-${
                      ride.status === "completed" ? "success" : "danger"
                    }`}
                    onClick={() => handleEndRide(ride.id)}
                    disabled={ride.status === "completed"}
                  >
                    {ride.status === "completed"
                      ? "Ride Completed"
                      : "End Ride"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Bottom-center spinner for loading rides */}
      {loading && (
        <div
          className="position-absolute"
          style={{
            bottom: 0,
            left: 0,
            right: 0,
            zIndex: 10,
            textAlign: "center",
            padding: "10px",
          }}
        >
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading rides...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default RideList;
