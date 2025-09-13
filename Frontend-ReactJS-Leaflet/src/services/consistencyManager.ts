// src/services/consistencyManager.ts
export type ConsistencyLevel = 'strong' | 'eventual';

interface ConsistencyManager {
  level: ConsistencyLevel;
  get: () => ConsistencyLevel;
  set: (newLevel: ConsistencyLevel) => void;
  subscribe: (callback: (level: ConsistencyLevel) => void) => () => void;
}

// A simple pub/sub model to notify subscribers (like React components) of changes.
const subscribers = new Set<(level: ConsistencyLevel) => void>();

export const consistencyManager: ConsistencyManager = {
  level: 'strong', // Default consistency level

  get() {
    return this.level;
  },

  set(newLevel: ConsistencyLevel) {
    if (this.level !== newLevel) {
      this.level = newLevel;
      // Notify all subscribers of the change
      subscribers.forEach(callback => callback(newLevel));
    }
  },

  subscribe(callback: (level: ConsistencyLevel) => void) {
    subscribers.add(callback);
    // Return an unsubscribe function
    return () => {
      subscribers.delete(callback);
    };
  }
};
