package com.maps

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import com.facebook.react.bridge.*

class LocationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "LocationModule"
    }

    @ReactMethod
    fun startLocationService() {
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)

        // Verifica la versión de la API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Para Android 12 y superior, muestra una notificación o pide confirmación
            val pendingIntent = PendingIntent.getForegroundService(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Envía una notificación que requiere la acción del usuario
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "location_service_prompt"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Confirmación de ubicación",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Activar servicio de ubicación")
                .setContentText("Presiona para activar el servicio de ubicación.")
                .setContentIntent(pendingIntent) // Asocia el PendingIntent al botón
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Para Android 8 a 11
            context.startForegroundService(intent)
        } else {
            // Para versiones anteriores a Android 8
            context.startService(intent)
        }
    }    

    @ReactMethod
    fun stopLocationService() {
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }

    @ReactMethod
    fun getLastSentLocation(promise: Promise) {
        Log.d("LocationModule", "Llamada a getLastSentLocation")
        val latitude = LocationData.latestLatitude
        val longitude = LocationData.latestLongitude

        if (latitude != null && longitude != null) {
            Log.d("LocationModule", "Devolviendo ubicación: Lat: $latitude, Lng: $longitude")
            val locationMap = Arguments.createMap()
            locationMap.putDouble("latitude", latitude)
            locationMap.putDouble("longitude", longitude)
            promise.resolve(locationMap)
        } else {
            Log.d("LocationModule", "Ubicación no disponible.")
            promise.reject("LOCATION_ERROR", "No se encontró ubicación.")
        }
    }
}
