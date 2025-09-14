export type Region = 'US' | 'EU' | 'ASIA';

export type DriverStatus = 'AVAILABLE' | 'ON_RIDE' | 'OFFLINE';
export type PassengerStatus = 'ONLINE' | 'ON_RIDE'| 'OFFLINE';
export type RideStatus = 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export interface Driver {
  id: string;
  name: string;
  status: DriverStatus;
  latitude: number;
  longitude: number;
}

export interface Passenger {
  id: string;
  name: string;
  status: PassengerStatus;
  latitude: number;
  longitude: number;
}

export interface Ride {
  id: string;
  passengerId: string;
  driverId: string;
  status: RideStatus;
  startTime: number;
  endTime?: number;
}
