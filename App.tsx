import React, { useState } from 'react';
import {
  PermissionsAndroid,
  Platform,
  Alert,
  Button,
  View,
  Text,
  ActivityIndicator,
  StyleSheet,
} from 'react-native';
import openMap from 'react-native-open-maps';
import Geolocation from '@react-native-community/geolocation';

const App = () => {
  const [loading, setLoading] = useState(false); // Estado para el indicador de carga

  // Función para solicitar permisos de ubicación
  const requestLocationPermission = async () => {
    try {
      setLoading(true); // Mostrar indicador de carga

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
          const backgroundGranted = await requestBackgroundLocationPermission();
          if (!backgroundGranted) {
            Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación en segundo plano.');
            setLoading(false);
            return;
          }
        }
        getCurrentLocation(); // Obtener ubicación actual si los permisos fueron otorgados
      } else {
        Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación.');
        setLoading(false);
      }
    } catch (err) {
      setLoading(false);
      Alert.alert('Error', `Hubo un error al solicitar los permisos: ${err.message}`);
    }
  };

  // Función para solicitar permisos de ubicación en segundo plano (Android 10+)
  const requestBackgroundLocationPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
        {
          title: 'Permiso de Ubicación en Segundo Plano',
          message: 'Esta aplicación necesita acceso a tu ubicación incluso en segundo plano.',
          buttonNeutral: 'Preguntar después',
          buttonNegative: 'Cancelar',
          buttonPositive: 'Aceptar',
        }
      );
      return granted === PermissionsAndroid.RESULTS.GRANTED;
    } catch (err) {
      Alert.alert('Error', `Error al solicitar permisos de ubicación en segundo plano: ${err.message}`);
      return false;
    }
  };

  // Función para obtener la ubicación actual
  const getCurrentLocation = () => {
    Geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        openMapWithLocation(latitude, longitude);
      },
      (error) => {
        Alert.alert('Error', 'No se pudo obtener la ubicación: ' + error.message);
        setLoading(false);
      },
      { enableHighAccuracy: true, timeout: 20000, maximumAge: 1000 }
    );
  };

  // Función para abrir el mapa con la ubicación actual
  const openMapWithLocation = (latitude, longitude) => {
    openMap({
      latitude,
      longitude,
      zoom: 15,
    });
    setLoading(false);
  };

  return (
    <View style={styles.container}>
      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#0000ff" />
          <Text style={styles.loadingText}>Obteniendo ubicación...</Text>
        </View>
      ) : (
        <Button
          title="Abrir Mapa con Mi Ubicación"
          onPress={requestLocationPermission}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingContainer: {
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: '#333',
  },
});

export default App;
