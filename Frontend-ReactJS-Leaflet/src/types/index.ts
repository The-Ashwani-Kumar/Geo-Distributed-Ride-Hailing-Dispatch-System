export interface Driver {
  id : string;
  name: string;
  status : string;
  latitude: number;
  longitude: number;
}

export interface Passenger {
  id : string;
  name: string;
  status: string;
  latitude: number;
  longitude: number;
}


export interface Ride {
  id: string;
  passengerId: string;
  driverId: string;
  status: string;
  startTime: number;
  endTime?: number;
}
