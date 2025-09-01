// src/App.tsx
import React from "react";
import { RideProvider } from "./context/RideContext";
import SideBar from "./components/SideBar";
import MapView from "./components/MapView";

function App() {
  return (
    <RideProvider>
      <div className="position-relative" style={{ height: "100vh", width: "100vw" }}>
        {/* Fullscreen Map */}
        <MapView />

        {/* Sidebar / Topbar overlay */}
        <div className="position-absolute top-0 start-0 h-100" style={{ zIndex: 1000 }}>
          <SideBar />
        </div>
      </div>
    </RideProvider>
  );
}

export default App;
