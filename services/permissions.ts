// src/services/permissions.ts

import { PermissionsAndroid, Platform, Alert } from 'react-native';

/**
 * Solicita permisos de notificación en Android 13+.
 */
export const requestNotificationPermission = async (): Promise<void> => {
  if (Platform.OS === 'android' && Platform.Version >= 33) {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        {
          title: 'Permiso para Notificaciones',
          message: 'Esta aplicación necesita permiso para mostrar notificaciones.',
          buttonNeutral: 'Preguntar después',
          buttonNegative: 'Cancelar',
          buttonPositive: 'Aceptar',
        }
      );

      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('Permiso para notificaciones concedido.');
      } else {
        Alert.alert(
          'Permiso denegado',
          'No se pueden mostrar notificaciones sin tu permiso.'
        );
      }
    } catch (error) {
      console.error('Error al solicitar permiso de notificaciones:', error);
    }
  }
};

/**
 * Solicita permisos de ubicación, tanto en primer plano como en segundo plano si es necesario.
 */
export const requestLocationPermission = async (): Promise<void> => {
  try {
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
      console.log('Permiso de ubicación en primer plano concedido.');

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

        if (backgroundGranted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log('Permiso de ubicación en segundo plano concedido.');
        } else {
          Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación en segundo plano.');
        }
      }
    } else {
      Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación.');
    }
  } catch (error) {
    console.error('Error al solicitar permisos de ubicación:', error);
  }
};
