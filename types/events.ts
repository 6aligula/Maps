// src/types/events.ts

/**
 * Evento emitido cuando cambia el estado de rastreo.
 */
export interface TrackingStateChangedEvent {
    isTracking: boolean;
  }
  
  /**
   * Evento emitido cuando se actualiza la ubicación.
   */
  export interface LocationUpdatedEvent {
    latitude: number;
    longitude: number;
  }
  