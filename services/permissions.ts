// src/services/permissions.ts

import { PermissionsAndroid, Platform, Alert } from 'react-native';

/**
 * Solicita permisos de notificación en Android 13+.
 */
export const requestNotificationPermission = async (): Promise<boolean> => {
  if (Platform.OS === 'android' && Platform.Version >= 33) {
    try {
      // Verificar si ya se ha concedido el permiso
      const isGranted = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
      if (isGranted) {
        console.log('Permiso para notificaciones ya concedido.');
        return true;
      }

      // Solicitar permiso si no está concedido
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
        return true;
      } else {
        Alert.alert(
          'Permiso Denegado',
          'No se pueden mostrar notificaciones sin tu permiso.'
        );
        return false;
      }
    } catch (error) {
      console.error('Error al solicitar permiso de notificaciones:', error);
      return false;
    }
  }
  return true;
};

/**
 * Solicita permisos de ubicación, tanto en primer plano como en segundo plano si es necesario.
 */
export const requestLocationPermission = async (): Promise<boolean> => {
  try {
    // Verificar permiso de ubicación en primer plano
    const isForegroundGranted = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
    let foregroundGranted = isForegroundGranted;
    if (!isForegroundGranted) {
      const response = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Permiso de Localización',
          message: 'Esta aplicación necesita acceso a tu ubicación para funcionar correctamente.',
          buttonNeutral: 'Preguntar después',
          buttonNegative: 'Cancelar',
          buttonPositive: 'Aceptar',
        },
      );

      if (response === PermissionsAndroid.RESULTS.GRANTED) {
        foregroundGranted = true;
        console.log('Permiso de ubicación en primer plano concedido.');
      } else {
        Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación.');
        foregroundGranted = false;
      }
    } else {
      console.log('Permiso de ubicación en primer plano ya concedido.');
    }

    // Verificar permiso de ubicación en segundo plano si es necesario
    let backgroundGranted = true;
    if (Platform.OS === 'android' && Platform.Version >= 29) {
      const isBackgroundGranted = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION);
      if (!isBackgroundGranted) {
        const backgroundResponse = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
          {
            title: 'Permiso de Ubicación en Segundo Plano',
            message: 'Esta aplicación necesita acceso a tu ubicación incluso en segundo plano.',
            buttonNeutral: 'Preguntar después',
            buttonNegative: 'Cancelar',
            buttonPositive: 'Aceptar',
          }
        );

        if (backgroundResponse === PermissionsAndroid.RESULTS.GRANTED) {
          backgroundGranted = true;
          console.log('Permiso de ubicación en segundo plano concedido.');
        } else {
          Alert.alert('Permiso Denegado', 'No se puede acceder a la ubicación en segundo plano.');
          backgroundGranted = false;
        }
      } else {
        console.log('Permiso de ubicación en segundo plano ya concedido.');
      }
    }

    return foregroundGranted && backgroundGranted;
  } catch (error) {
    console.error('Error al solicitar permisos de ubicación:', error);
    return false;
  }
};
