package com.maps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.content.SharedPreferences
import com.maps.utils.Constants

class LocationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {
    
    private lateinit var sharedPreferences: SharedPreferences
    private var latestLatitude: Double? = null
    private var latestLongitude: Double? = null

    init {
        reactContext.addLifecycleEventListener(this)
        setupLocationListener()
        setupTrackingListener()
        sharedPreferences = reactContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getName(): String {
        return "LocationModule"
    }

    // Métodos addListener y removeListeners
    @ReactMethod
    fun addListener(eventName: String) {
        Log.d("LocationModule", "addListener llamado para el evento: $eventName")
        // No-op. Necesario para NativeEventEmitter
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        Log.d("LocationModule", "removeListeners llamado para eliminar $count listeners.")
        // No-op. Necesario para NativeEventEmitter
    }

    @ReactMethod
    fun startLocationService() {
        Log.d("LocationModule", "Método startLocationService llamado desde JavaScript.")
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)

        // Verifica la versión de la API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Para Android 12 y superior, muestra una notificación o pide confirmación
            val pendingIntent =
                    PendingIntent.getForegroundService(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

            // Envía una notificación que requiere la acción del usuario
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "location_service_prompt"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                        NotificationChannel(
                                channelId,
                                "Confirmación de ubicación",
                                NotificationManager.IMPORTANCE_HIGH
                        )
                notificationManager.createNotificationChannel(channel)
            }

            val notification =
                    NotificationCompat.Builder(context, channelId)
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
        Log.d("LocationModule", "Método stopLocationService llamado desde JavaScript.")
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }

    @ReactMethod
    fun getLastSentLocation(promise: Promise) {
        Log.d("LocationModule", "Llamada a getLastSentLocation desde JavaScript.")
        //val latitude = LocationData.latestLatitude
        //val longitude = LocationData.latestLongitude
        val latitude = sharedPreferences.getFloat(Constants.PREF_LAST_LATITUDE, 0f).toDouble()
        val longitude = sharedPreferences.getFloat(Constants.PREF_LAST_LONGITUDE, 0f).toDouble()

        if (latitude != 0.0 && longitude != 0.0) {
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

    @ReactMethod
    fun isLocationServiceRunning(promise: Promise) {
        val isRunning = sharedPreferences.getBoolean(Constants.PREF_IS_TRACKING, false)
        promise.resolve(isRunning)
    }

    private fun setupLocationListener() {
        Log.d("LocationModule", "Configurando listener de ubicación.")
        LocationData.setLocationListener { latitude, longitude ->
            latestLatitude = latitude
            latestLongitude = longitude
            Log.d("LocationModule", "Ubicación actualizada: Lat: $latitude, Lng: $longitude")
            sendLocationUpdateEvent(latitude, longitude)
        }
    }

    private fun setupTrackingListener() {
        Log.d("LocationModule", "Configurando listener de estado de rastreo.")
        LocationData.setTrackingListener { isTracking ->
            Log.d("LocationModule", "Estado de rastreo actualizado: $isTracking")
            sendTrackingStateEvent(isTracking)
        }
    }
    
    private fun sendLocationUpdateEvent(latitude: Double, longitude: Double) {
        Log.d("LocationModule", "Emitiendo evento LocationUpdated.")
        val params = Arguments.createMap()
        params.putDouble("latitude", latitude)
        params.putDouble("longitude", longitude)
        sendEvent("LocationUpdated", params)
    }
    
    private fun sendTrackingStateEvent(isTracking: Boolean) {
        Log.d("LocationModule", "Emitiendo evento TrackingStateChanged: isTracking = $isTracking")
        val params = Arguments.createMap()
        params.putBoolean("isTracking", isTracking)
        sendEvent("TrackingStateChanged", params)
    }    

    private fun sendEvent(eventName: String, params: WritableMap?) {
        Log.d("LocationModule", "Enviando evento: $eventName con params: $params")
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }    

    // Métodos de LifecycleEventListener (opcional)
    override fun onHostResume() {
        Log.d("LocationModule", "onHostResume llamado.")
    }
    
    override fun onHostPause() {
        Log.d("LocationModule", "onHostPause llamado.")
    }
    
    override fun onHostDestroy() {
        Log.d("LocationModule", "onHostDestroy llamado. Eliminando listener de ubicación.")
        LocationData.removeLocationListener()
    }
    
}
