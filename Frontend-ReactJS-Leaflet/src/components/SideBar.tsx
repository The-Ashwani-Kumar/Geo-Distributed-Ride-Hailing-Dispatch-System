import React, { useState } from "react";
import { Button, Modal, Offcanvas } from "react-bootstrap";
import DriverForm from "./DriverForm";
import PassengerForm from "./PassengerForm";
import RideList from "./RideList";
import RandomUserGenerator from "./RandomUserGenerator";

const SideBar = () => {
  const [showDriver, setShowDriver] = useState(false);
  const [showPassenger, setShowPassenger] = useState(false);
  const [showRides, setShowRides] = useState(false);

  // For mobile drawer
  const [showMenu, setShowMenu] = useState(false);

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
          Ongoing Rides
        </Button>
        <RandomUserGenerator/>
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
            Ongoing Rides
          </Button>
          <RandomUserGenerator/>
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

      {/* Ongoing Rides Modal */}
      <Modal
        show={showRides}
        onHide={() => setShowRides(false)}
        size="lg"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Ongoing Rides</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <RideList />
        </Modal.Body>
      </Modal>
    </>
  );
};

export default SideBar;
