// src/types/locationModule.ts

/**
 * Interfaz que define los métodos disponibles en el módulo nativo de ubicación.
 */
export interface LocationModuleType {
    /**
     * Inicia el servicio de ubicación.
     */
    startLocationService: () => void;
  
    /**
     * Detiene el servicio de ubicación.
     */
    stopLocationService: () => void;
  
    /**
     * Obtiene la última ubicación enviada.
     * @returns Una promesa que resuelve un objeto con `latitude` y `longitude`.
     */
    getLastSentLocation: () => Promise<{ latitude: number; longitude: number }>;
  
    /**
     * Agrega un listener para un tipo de evento específico.
     * @param eventType - El nombre del evento.
     */
    addListener: (eventType: string) => void;
  
    /**
     * Elimina un número específico de listeners.
     * @param count - La cantidad de listeners a eliminar.
     */
    removeListeners: (count: number) => void;
  }
  