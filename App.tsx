import React, { useEffect } from 'react';
import {
  PermissionsAndroid,
  Platform,
  Alert,
  View,
  Text,
  StyleSheet,
  Button,
  NativeModules,
} from 'react-native';
import axios from 'axios';
import { Float } from 'react-native/Libraries/Types/CodegenTypes';

const { LocationModule } = NativeModules; // Módulo nativo de ubicación

const App = () => {
  useEffect(() => {
    requestLocationPermission();
  }, []);

  // Solicitar permisos de ubicación
  const requestLocationPermission = async () => {
    const foregroundGranted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        title: 'Permiso de Localización',
        message: 'Esta aplicación necesita acceso a tu ubicación para funcionar correctamente.',
        buttonNeutral: 'Preguntar después',
        buttonNegative: 'Cancelar',
        buttonPositive: 'Aceptar',
      }
    );

    if (foregroundGranted === PermissionsAndroid.RESULTS.GRANTED) {
      if (Platform.OS === 'android' && Platform.Version >= 29) {
        const backgroundGranted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
          {
            title: 'Permiso de Ubicación en Segundo Plano',
            message: 'Esta aplicación necesita acceso a tu ubicación incluso en segundo plano.',
            buttonNeutral: 'Preguntar después',
            buttonNegative: 'Cancelar',
            buttonPositive: 'Aceptar',
          }
        );
        if (backgroundGranted !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación en segundo plano.');
        }
      }
    } else {
      Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación.');
    }
  };

  // Metodo para enviar la ubicación al servidor
  const sendLocationToServer = async (latitude: Float, longitude: Float) => {
    try {
      // objeto GeoJSON
      const response = await axios.post('http://192.168.1.180:8000/api/geo/add/', {
        name: 'Ubi de prueba',
        coordinates: {
          type: 'Point',
          coordinates: [longitude, latitude],
        },
      });
      console.log('Respuesta del servidor:', response.data);
    } catch (error) {
      console.error('Error al enviar la ubicación al servidor:', error);
    }
  };

  const startLocationService = () => {
    try {
      LocationModule.startLocationService(); // Llama al servicio nativo
      console.log('Servicio de ubicación iniciado. Esperando envío de datos al servidor...');
  
      const checkLocationUpdates = setInterval(async () => {
        try {
          const location = await LocationModule.getLastSentLocation();
          if (location) {
            sendLocationToServer(location.latitude, location.longitude); 
            console.log(`Datos enviados al servidor: Lat ${location.latitude}, Lng ${location.longitude}`);
          } else {
            console.log('No se ha enviado ninguna actualización de ubicación aún.');
          }
        } catch (error) {
          console.error('Error al obtener la última ubicación:', error);
        }
      }, 10000); // Intervalo de verificación (10 segundos)
      
  
      // Limpiar el intervalo al detener el servicio
      return () => clearInterval(checkLocationUpdates);
  
    } catch (error) {
      console.error('Error al iniciar el servicio de ubicación:', error);
    }
  };

  const stopLocationService = () => {
    try {
      LocationModule.stopLocationService(); // Llama al servicio nativo
      console.log('Servicio de ubicación detenido.');
    } catch (error) {
      console.error('Error al detener el servicio de ubicación:', error);
    }
  };
  
  
  return (
    <View style={styles.container}>
      <Text style={styles.text}>La aplicación está rastreando tu ubicación en segundo plano.</Text>
      <Button title="Iniciar Rastreo" onPress={startLocationService} />
      <Button title="Detener Rastreo" onPress={stopLocationService} />
    </View>
  );
};

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
  },
});

export default App;
