import React, { useState } from "react";
import { Button, Form, Modal, Offcanvas } from "react-bootstrap";
import { useRideContext } from "../context/RideContext";
import DriverForm from "./DriverForm";
import PassengerForm from "./PassengerForm";
import RideList from "./RideList";
import RandomUserGenerator from "./RandomUserGenerator";
import { Region } from "../types";

const SideBar = () => {
  const {
    consistencyLevel,
    setConsistencyLevel,
    region,
    setRegion,
  } = useRideContext();
  const [showDriver, setShowDriver] = useState(false);
  const [showPassenger, setShowPassenger] = useState(false);
  const [showRides, setShowRides] = useState(false);

  // For mobile drawer
  const [showMenu, setShowMenu] = useState(false);

  const regionSelector = (
    <div className="p-2 rounded bg-light border mt-4">
      <Form.Group controlId="region-select">
        <Form.Label className="fw-bold">Region</Form.Label>
        <Form.Select
          value={region}
          onChange={(e) => setRegion(e.target.value as Region)}
        >
          <option value="US">US</option>
          <option value="EU">EU</option>
          <option value="ASIA">ASIA</option>
        </Form.Select>
      </Form.Group>
    </div>
  );

  const consistencyToggle = (
    <div className="p-2 rounded bg-light border mt-4">
      <Form.Check
        type="switch"
        id="consistency-switch"
        label={
          <div className="ms-2">
            <strong className="d-block">
              {consistencyLevel === "STRONG"
                ? "Strong Consistency"
                : "Eventual Consistency"}
            </strong>
            <small className="text-muted">
              {consistencyLevel === "STRONG"
                ? "Slower, guaranteed reads"
                : "Faster, risk of stale data"}
            </small>
          </div>
        }
        checked={consistencyLevel === "STRONG"}
        onChange={(e) => {
          setConsistencyLevel(e.target.checked ? "STRONG" : "EVENTUAL");
        }}
      />
    </div>
  );

  return (
    <>
      {/* Desktop Sidebar */}
      <div
        className="d-none d-md-flex flex-column p-3 bg-light shadow-sm vh-100"
        style={{ width: "280px" }}
      >
        <h4 className="fw-bold mb-4 text-center">🚖 Ride Hailing</h4>

        <Button
          variant="primary"
          className="d-flex align-items-center mb-3"
          onClick={() => setShowDriver(true)}
        >
          <i className="bi bi-taxi-front me-2"></i>
          Add Driver
        </Button>

        <Button
          variant="success"
          className="d-flex align-items-center mb-3"
          onClick={() => setShowPassenger(true)}
        >
          <i className="bi bi-person-plus me-2"></i>
          Add Passenger
        </Button>

        <Button
          variant="warning"
          className="d-flex align-items-center"
          onClick={() => setShowRides(true)}
        >
          <i className="bi bi-people me-2"></i>
          All Rides
        </Button>

        {regionSelector}
        {consistencyToggle}

        <div className="mt-auto w-100">
          <RandomUserGenerator />
        </div>
      </div>

      {/* Mobile Topbar (fixed at top) */}
      <div className="d-flex d-md-none justify-content-end align-items-center p-2 bg-light shadow-sm position-fixed top-0 start-0 w-100 z-3">
        <Button
          variant="outline-primary"
          size="sm"
          onClick={() => setShowMenu(true)}
        >
          <i className="bi bi-list"></i>
        </Button>
      </div>

      {/* Mobile Drawer */}
      <Offcanvas
        show={showMenu}
        onHide={() => setShowMenu(false)}
        placement="start"
      >
        <Offcanvas.Header closeButton>
          <Offcanvas.Title>Menu</Offcanvas.Title>
        </Offcanvas.Header>
        <Offcanvas.Body className="d-flex flex-column">
          <Button
            variant="primary"
            className="d-flex align-items-center mb-3"
            onClick={() => {
              setShowDriver(true);
              setShowMenu(false);
            }}
          >
            <i className="bi bi-taxi-front me-2"></i>
            Add Driver
          </Button>

          <Button
            variant="success"
            className="d-flex align-items-center mb-3"
            onClick={() => {
              setShowPassenger(true);
              setShowMenu(false);
            }}
          >
            <i className="bi bi-person-plus me-2"></i>
            Add Passenger
          </Button>

          <Button
            variant="warning"
            className="d-flex align-items-center"
            onClick={() => {
              setShowRides(true);
              setShowMenu(false);
            }}
          >
            <i className="bi bi-people me-2"></i>
            All Rides
          </Button>

          {regionSelector}
          {consistencyToggle}

          <div className="mt-auto w-100">
            <RandomUserGenerator />
          </div>
        </Offcanvas.Body>
      </Offcanvas>

      {/* Driver Modal */}
      <Modal show={showDriver} onHide={() => setShowDriver(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Add Driver</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <DriverForm />
        </Modal.Body>
      </Modal>

      {/* Passenger Modal */}
      <Modal
        show={showPassenger}
        onHide={() => setShowPassenger(false)}
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Add Passenger</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <PassengerForm />
        </Modal.Body>
      </Modal>

      {/* All Rides Modal */}
      <Modal
        show={showRides}
        onHide={() => setShowRides(false)}
        size="lg"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>All Rides</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <RideList />
        </Modal.Body>
      </Modal>
    </>
  );
};

export default SideBar;
