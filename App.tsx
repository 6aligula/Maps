// src/App.tsx

import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Button,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';
import {
  requestLocationPermission,
  requestNotificationPermission,
} from './services/permissions'; // Importa las funciones de permisos

// Importar interfaces desde archivos separados
import {
  TrackingStateChangedEvent,
  LocationUpdatedEvent,
} from './types/events';
import { LocationModuleType } from './types/locationModule';

// Obtener el módulo nativo y tiparlo con la interfaz importada
const LocationModule = NativeModules.LocationModule as LocationModuleType;

// Crear una instancia tipada de NativeEventEmitter
const locationEventEmitter = new NativeEventEmitter(LocationModule);

const App: React.FC = () => {
  // Estado para indicar si el rastreo está activo
  const [isTracking, setIsTracking] = useState<boolean>(false);

  // Estado para almacenar la ubicación actual
  const [currentLocation, setCurrentLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);

  useEffect(() => {
    // Solicitar permisos al iniciar la aplicación
    const initializePermissions = async () => {
      console.log('Solicitando permisos de ubicación y notificación.');
      await requestLocationPermission();
      console.log('Permisos de ubicación solicitados.');
      await requestNotificationPermission();
      console.log('Permisos de notificación solicitados.');
    };

    initializePermissions();

    // Suscribirse al evento TrackingStateChanged
    const trackingStateListener = locationEventEmitter.addListener(
      'TrackingStateChanged',
      (event: TrackingStateChangedEvent) => {
        console.log('Evento TrackingStateChanged recibido:', event);
        setIsTracking(event.isTracking);
      }
    );

    // Suscribirse al evento LocationUpdated
    const locationUpdateListener = locationEventEmitter.addListener(
      'LocationUpdated',
      (event: LocationUpdatedEvent) => {
        console.log('Evento LocationUpdated recibido:', event);
        setCurrentLocation({
          latitude: event.latitude,
          longitude: event.longitude,
        });
      }
    );

    // Obtener la última ubicación enviada (si existe)
    LocationModule.getLastSentLocation()
      .then((location) => {
        console.log('getLastSentLocation retornó:', location);
        if (
          location &&
          location.latitude !== 0.0 &&
          location.longitude !== 0.0
        ) {
          setCurrentLocation(location);
        }
      })
      .catch((error) => {
        console.error('Error al obtener la última ubicación:', error);
      });

    // Limpiar las suscripciones al desmontar el componente
    return () => {
      trackingStateListener.remove();
      locationUpdateListener.remove();
      console.log('Listeners de eventos removidos.');
    };
  }, []);

  // Función para iniciar el servicio de ubicación
  const startLocationService = () => {
    try {
      console.log('Invocando startLocationService en el módulo nativo.');
      LocationModule.startLocationService();
      console.log('startLocationService invocado.');
    } catch (error) {
      console.error('Error al iniciar el servicio de ubicación:', error);
    }
  };

  // Función para detener el servicio de ubicación
  const stopLocationService = () => {
    try {
      console.log('Invocando stopLocationService en el módulo nativo.');
      LocationModule.stopLocationService();
      console.log('stopLocationService invocado.');
    } catch (error) {
      console.error('Error al detener el servicio de ubicación:', error);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {isTracking
          ? currentLocation
            ? `Rastreando ubicación...\nLat: ${currentLocation.latitude}\nLng: ${currentLocation.longitude}`
            : 'Rastreando ubicación...'
          : 'El rastreo de ubicación está detenido.'}
      </Text>
      {!currentLocation && isTracking && (
        <Text style={styles.loadingText}>Obteniendo ubicación...</Text>
      )}
      <Button
        title="Iniciar Rastreo"
        onPress={startLocationService}
        disabled={isTracking} // Deshabilitar si ya está rastreando
      />
      <Button
        title="Detener Rastreo"
        onPress={stopLocationService}
        disabled={!isTracking} // Deshabilitar si no está rastreando
      />
    </View>
  );
};

// Definir los estilos
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  text: {
    fontSize: 18,
    color: '#333',
    marginBottom: 20,
    textAlign: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: '#555',
    marginTop: 10,
  },
});

export default App;
