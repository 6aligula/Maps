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

  const requestLocationPermission = async () => {
    try {
      setLoading(true); // Mostrar indicador de carga

      // Solicitar permisos de ubicación en primer plano
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Permiso de Localización',
          message: 'Esta aplicación necesita acceso a tu ubicación para funcionar correctamente.',
          buttonNeutral: 'Preguntar después',
          buttonNegative: 'Cancelar',
          buttonPositive: 'Aceptar',
        }
      );

      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        if (Platform.OS === 'android' && Platform.Version >= 29) {
          // Solicitar permiso de ubicación en segundo plano (Android 10+)
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

        // Obtener ubicación actual
        Geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;

            // Abrir el mapa con la ubicación actual
            openMap({
              latitude,
              longitude,
              zoom: 15,
            });
            setLoading(false); // Ocultar indicador de carga
          },
          (error) => {
            setLoading(false); // Ocultar indicador de carga
            Alert.alert('Error', 'No se pudo obtener la ubicación: ' + error.message);
          },
          { enableHighAccuracy: true, timeout: 20000, maximumAge: 1000 }
        );
      } else {
        setLoading(false); // Ocultar indicador de carga
        Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación.');
      }
    } catch (err) {
      setLoading(false); // Ocultar indicador de carga
      Alert.alert('Error', `Hubo un error al solicitar los permisos: ${err.message}`);
    }
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
