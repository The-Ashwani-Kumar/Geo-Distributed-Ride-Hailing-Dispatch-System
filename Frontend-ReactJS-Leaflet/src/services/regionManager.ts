// src/services/regionManager.ts
import { Region } from '../types';

const REGION_KEY = 'region';

export const regionManager = {
  get: (): Region => {
    return (localStorage.getItem(REGION_KEY) as Region) || 'US';
  },
  set: (region: Region) => {
    localStorage.setItem(REGION_KEY, region);
  },
};
