# 🚖 Geo-Distributed Ride-Hailing Dispatch System

## Project Overview

This is a full-stack, geo-distributed ride-hailing dispatch system designed to demonstrate advanced concepts in distributed systems, including geo-sharding, consistency models, and real-time data synchronization. The application allows users to view drivers and passengers on a map, book rides, and track their status, all while operating across different geographical regions with configurable consistency levels.

## Key Features

### 1. Geo-Sharding for Regional Data Isolation
- **Regional Data Storage:** All driver, passenger, and ride data is sharded across multiple Redis instances, with each instance dedicated to a specific geographical region (US, EU, ASIA).
- **Region Selector:** A user interface element (dropdown) allows users to select their current operating region. All subsequent API calls from the frontend include an `X-Region` HTTP header, ensuring that data is read from and written to the correct regional Redis instance.
- **Scalability:** This architecture enables horizontal scaling by adding more regional Redis instances as needed, reducing latency for users in specific geographies.

### 2. Configurable Consistency Models
- **Consistency Toggle:** The UI provides a toggle to switch between "Strong" and "Eventual" consistency for read operations.
- **`X-Consistency-Level` Header:** For all read operations (e.g., fetching drivers, passengers, or rides), the frontend includes an `X-Consistency-Level` HTTP header (either `STRONG` or `EVENTUAL`).
- **Master-Replica Architecture:** Each regional Redis setup consists of a master and a replica (slave).
    - **Strong Consistency (Reads):** Requests are routed to the regional Redis master, guaranteeing the most up-to-date data.
    - **Eventual Consistency (Reads):** Requests are routed to the regional Redis replica, offering lower latency but with the possibility of slightly stale data due to replication lag (simulated in the backend).
- **Write Consistency:** All write operations (e.g., adding a driver, booking a ride) are always routed to the regional Redis master to ensure data integrity and prevent conflicts, regardless of the frontend's consistency toggle. A simulated 5-second replication lag is introduced for writes to highlight the eventual consistency model for reads.

### 3. Real-time Tracking & Dispatch
- **Live Map View:** Drivers and passengers are displayed on an interactive map (Leaflet).
- **Ride Booking & Tracking:** Users can book rides, and the system dispatches the nearest available driver within the selected region. Ride progress is tracked and displayed.
- **Status Management:** Drivers, passengers, and rides have distinct statuses (e.g., `AVAILABLE`, `ON_RIDE`, `ONLINE`, `OFFLINE`, `ONGOING`, `COMPLETED`), which are updated and reflected in real-time. The backend now uses type-safe uppercase enum values for statuses.

## Technology Stack

- **Frontend:**
    - **React:** A JavaScript library for building user interfaces.
    - **Leaflet.js:** An open-source JavaScript library for mobile-friendly interactive maps.
    - **React-Leaflet:** React components for Leaflet maps.
    - **Axios:** Promise-based HTTP client for the browser and Node.js.
    - **React-Toastify:** For toast notifications.
    - **React-Bootstrap:** Bootstrap components built with React.
    - **Turf.js:** For geospatial operations (e.g., point in polygon checks).
    - **Nginx:** Used as a static file server for the React build and as a reverse proxy for API requests in the Docker setup.

- **Backend:**
    - **Spring Boot (Java):** A powerful framework for building robust, production-ready applications.
    - **Spring Data Redis:** Simplifies interaction with Redis.
    - **Jedis:** A Java client for Redis.
    - **Redis:** An in-memory data structure store, used for storing driver, passenger, and ride data, and for geospatial indexing (Redis GEO). Multiple instances are used for geo-sharding.
    - **Maven:** Build automation tool for Java projects.

## Project Structure

```
.
├── Backend-Java-SpringBoot-Redis/  # Spring Boot API server
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ashwani/
│   │   │   │       ├── config/             # Spring configurations (Redis, Web)
│   │   │   │       ├── constant/           # Application constants (e.g., Redis keys, ConsistencyLevel enum)
│   │   │   │       ├── controller/         # REST API endpoints
│   │   │   │       ├── entity/             # Data models (Driver, Passenger, Ride)
│   │   │   │       ├── enums/              # Enums (Region, DriverStatus, PassengerStatus, RideStatus)
│   │   │   │       ├── exception/          # Custom exceptions
│   │   │   │       ├── repository/         # Data access layer (Redis interactions)
│   │   │   │       ├── service/            # Business logic layer
│   │   │   │       └── sharding/           # Geo-sharding and consistency context management
│   │   │   └── resources/          # Application properties, etc.
│   │   └── test/
│   └── pom.xml
├── Frontend-ReactJS-Leaflet/   # React + Leaflet client
│   ├── public/                 # HTML template, static assets (icons, manifest)
│   ├── src/
│   │   ├── assets/             # Icons, images
│   │   ├── components/         # Reusable React components (SideBar, MapView, Forms)
│   │   ├── context/            # React Context for global state (RideContext)
│   │   ├── data/               # Static data (e.g., world_land.json)
│   │   ├── services/           # API calls (Axios), consistency/region managers
│   │   ├── styles/             # CSS
│   │   ├── types/              # TypeScript type definitions
│   │   ├── utils/              # Utility functions (random data generation)
│   │   ├── App.tsx             # Main application component
│   │   └── index.tsx           # Entry point
│   ├── Dockerfile              # Dockerfile for building frontend image
│   ├── nginx.conf              # Nginx configuration for serving frontend and proxying API
│   ├── package.json
│   └── tsconfig.json
├── .git/
├── docker-compose.yml          # Docker Compose configuration for all services
└── README.md                   # This file
```

## Getting Started

To run this project locally, you will need Docker and Docker Compose installed on your system.

1.  **Clone the repository:**
    ```bash
    git clone <repository_url>
    cd Geo-Distributed-Ride-Hailing-Dispatch-System
    ```

2.  **Build and start all services:**
    Navigate to the root directory of the cloned project (where `docker-compose.yml` is located) and run:
    ```bash
    docker-compose up --build -d
    ```
    This command will:
    - Build the Docker images for both the frontend (React app) and the backend (Spring Boot app).
    - Start all services: the Nginx server serving the React app, the Spring Boot backend, and six Redis instances (master and slave for US, EU, and ASIA regions).
    - The `-d` flag runs the containers in detached mode. The `--build` flag ensures images are rebuilt, picking up any code changes.

3.  **Access the application:**
    Once all services are up and running, open your web browser and navigate to:
    ```
    http://localhost
    ```

## How to Test Geo-Sharding and Consistency

1.  **Add Users to Specific Regions:**
    - In the frontend UI, use the **Region Selector** dropdown (in the sidebar) to choose a region (e.g., "US").
    - Use the "Add Random Users" button to add drivers and passengers. These users will be stored in the Redis instances dedicated to the "US" region.
    - Switch to another region (e.g., "EU") and add more users. Repeat for "ASIA".

2.  **Verify Geo-Sharding (Frontend):**
    - After adding users to different regions, switch between regions using the dropdown.
    - You should now **only** see the drivers and passengers relevant to the currently selected region displayed on the map and in the lists. Data from other regions should not appear.

3.  **Verify Geo-Sharding (Backend/Redis CLI - Optional, for deep verification):**
    - Open a new terminal window.
    - Connect to a specific regional Redis master (e.g., for US):
      ```bash
      docker exec -it redis-us-master redis-cli
      ```
    - Inside the Redis CLI, inspect the keys:
      ```redis
      KEYS *
      HGETALL drivers:us
      HGETALL passengers:us
      HGETALL rides:us
      ```
    - Repeat for `redis-eu-master` (using `drivers:eu`, `passengers:eu`, `rides:eu`) and `redis-asia-master` (using `drivers:asia`, `passengers:asia`, `rides:asia`). You should observe that each regional Redis master only contains data for its respective region.

4.  **Verify Consistency Toggling (Frontend & Backend Logs):**
    - Keep your `docker-compose logs -f backend` terminal open to observe backend activity.
    - In the frontend, select a region.
    - Toggle the **Consistency Toggle** (in the sidebar) between "Strong Consistency" and "Eventual Consistency".
    - Perform read operations (e.g., refresh the map, click "All Rides").
    - **Observe Frontend Behavior:**
        - When set to "Strong Consistency", new data (after a write) should appear immediately.
        - When set to "Eventual Consistency", new data might take up to 5 seconds (due to simulated replication lag) to appear, as reads are served from the replica.
    - **Observe Backend Logs:**
        - You should see log messages indicating whether `STRONG` or `EVENTUAL` consistency is being used for fetching data from Redis (e.g., `Fetching all rides with STRONG consistency in region US`).
        - All write operations (adding/booking/ending) will consistently show `STRONG` consistency in the logs, regardless of the frontend toggle.

## Stopping the Application

To stop and remove all the containers, networks, and volumes created by Docker Compose:

```bash
docker-compose down
```

## Global Deployment Considerations

Deploying this system globally involves more advanced cloud infrastructure concepts beyond local Docker Compose:

-   **Container Registry:** Push your Docker images to a cloud-based container registry (e.g., AWS ECR, Google Container Registry, Docker Hub).
-   **Orchestration:** Utilize a container orchestration platform like Kubernetes (EKS, GKE, AKS) or a simpler service like AWS ECS/Google Cloud Run for managing and scaling your services across regions.
-   **Managed Redis:** For production, replace the self-hosted Redis containers with managed Redis services offered by cloud providers (e.g., AWS ElastiCache for Redis, Google Cloud Memorystore for Redis). These services provide high availability, scalability, and automated management.
-   **Dynamic Redis Connection:** Your backend would need to dynamically connect to the appropriate managed Redis instance based on the `X-Region` header, potentially using service discovery or configuration management.
-   **Global Load Balancing & DNS:** Implement global load balancing and intelligent DNS routing (e.g., AWS Route 53 with latency-based routing) to direct user requests to the nearest regional frontend and backend instances.
-   **Secrets Management:** Securely manage API keys and credentials using cloud secrets management services.
-   **Monitoring & Logging:** Set up comprehensive monitoring and centralized logging solutions for distributed tracing and operational insights.
