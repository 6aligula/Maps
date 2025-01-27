// import { NativeModules } from 'react-native';
// import { sendLocationToServer } from '../utils/api';
// import { Coordinates } from '../types';

// const { LocationModule } = NativeModules;

// export const startLocationService = (): (() => void) | undefined => {
//   try {
//     LocationModule.startLocationService();
//     console.log('Servicio de ubicación iniciado.');

//     const intervalId = setInterval(async () => {
//       try {
//         const location: Coordinates = await LocationModule.getLastSentLocation();
//         if (location) {
//           await sendLocationToServer(location.latitude, location.longitude);
//           console.log(`Ubicación enviada: Lat ${location.latitude}, Lng ${location.longitude}`);
//         } else {
//           console.log('No hay actualizaciones de ubicación.');
//         }
//       } catch (error) {
//         console.error('Error al obtener la última ubicación:', error);
//       }
//     }, 10000);

//     return () => clearInterval(intervalId); // Limpia el intervalo
//   } catch (error) {
//     console.error('Error al iniciar el servicio de ubicación:', error);
//     return undefined;
//   }
// };

// export const stopLocationService = (): void => {
//   try {
//     LocationModule.stopLocationService();
//     console.log('Servicio de ubicación detenido.');
//   } catch (error) {
//     console.error('Error al detener el servicio de ubicación:', error);
//   }
// };
