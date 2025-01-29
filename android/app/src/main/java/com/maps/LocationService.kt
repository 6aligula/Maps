package com.maps

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.maps.api.ApiClient

class LocationService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "com.maps.ACTION_START_TRACKING"
        const val ACTION_CANCEL_TRACKING = "com.maps.ACTION_CANCEL_TRACKING"
    }

    private var isTracking = false // Nueva variable para controlar el estado

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastKnownLocation: Location? =
            null // Nueva variable para almacenar la última ubicación
    private lateinit var apiClient: ApiClient // Añadir la variable apiClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeLocationCallback()
        // Inicializar ApiClient con la URL base de tu servidor
        apiClient = ApiClient("http://192.168.1.180:8000") // Cambia según corresponda
    }

    private fun initializeLocationCallback() {
        locationCallback =
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        handleLocationResult(locationResult)
                    }

                    override fun onLocationAvailability(
                            locationAvailability: LocationAvailability
                    ) {
                        Log.d(
                                "LocationService",
                                "Disponibilidad de ubicación: ${locationAvailability.isLocationAvailable}"
                        )
                    }
                }
    }

    private fun handleLocationResult(locationResult: LocationResult) {
        locationResult.locations.forEach { location ->
            Log.d(
                    "LocationService",
                    "Ubicación recibida: ${location.latitude}, ${location.longitude}"
            )
            lastKnownLocation = location
            // Actualizar el Singleton con la nueva ubicación
            LocationData.updateLocation(location.latitude, location.longitude)
            // Opcional: enviar la ubicación al servidor
            sendLocationToServer(location)
            // Actualizar la notificación con la nueva ubicación
            updateNotification(location)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                if (!isTracking) {
                    isTracking = true
                    startLocationUpdates()
                    updateNotificationWithTracking()
                    LocationData.setTrackingState(true) // Actualizar el estado de rastreo en el Singleton
                } else {
                    Log.d("LocationService", "El rastreo ya está activo.")
                }
            }
            ACTION_CANCEL_TRACKING -> {
                if (isTracking) {
                    isTracking = false
                    stopLocationUpdates()
                    stopSelf()
                    removeNotification()
                    LocationData.setTrackingState(false) // Actualizar el estado de rastreo en el Singleton
                    Log.d("LocationService", "Rastreo cancelado por el usuario.")
                }
            }
            else -> {
                if (!isTracking) {
                    startForegroundService()
                    LocationData.setTrackingState(false) // Asegurarse de que el estado está desactivado
                } else {
                    Log.d("LocationService", "El rastreo ya está activo.")
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        LocationData.setTrackingState(false) // Actualizar el estado de rastreo en el Singleton
        Log.d("LocationService", "Servicio destruido. Rastreo desactivado.")
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val backgroundLocationGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

        return fineLocationGranted && backgroundLocationGranted
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermissions()) {
            Log.e("LocationService", "Permisos de ubicación no concedidos.")
            return
        }

        val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50000) // Cada 10 segundos
                        .setMinUpdateDistanceMeters(10f) // Opcional: actualizar cada 10 metros
                        .build()

        fusedLocationClient
                .requestLocationUpdates(locationRequest, locationCallback, mainLooper)
                .addOnSuccessListener {
                    Log.d("LocationService", "Actualizaciones de ubicación iniciadas.")
                    LocationData.setTrackingState(true) // Actualizar el estado de rastreo en el Singleton
                }
                .addOnFailureListener {
                    Log.e("LocationService","Error al iniciar actualizaciones de ubicación: ${it.message}")
                    LocationData.setTrackingState(false) // Actualizar el estado de rastreo en el Singleton
                }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnSuccessListener {
                Log.d("LocationService", "Actualizaciones de ubicación detenidas.")
                LocationData.setTrackingState(false) // Actualizar el estado de rastreo en el Singleton
            }
            .addOnFailureListener {
                Log.e("LocationService","Error al detener actualizaciones de ubicación: ${it.message}")
            }
    }    

    private val notificationId = 1

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val channelName = "Location Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java)
                    ?.createNotificationChannel(notificationChannel)
        }

        // Intent para confirmar el inicio del rastreo
        val startIntent =
                Intent(this, LocationService::class.java).apply { action = ACTION_START_TRACKING }
        val startPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        startIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

        // Intent para cancelar el rastreo
        val cancelIntent =
                Intent(this, LocationService::class.java).apply { action = ACTION_CANCEL_TRACKING }
        val cancelPendingIntent =
                PendingIntent.getService(
                        this,
                        1,
                        cancelIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

        // Construir la notificación con acciones
        val notification: Notification =
                NotificationCompat.Builder(this, channelId)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentTitle("Activar servicio de ubicación")
                        .setContentText("¿Quieres activar el servicio de ubicación?")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .addAction(
                                NotificationCompat.Action.Builder(0, "Sí", startPendingIntent)
                                        .build()
                        )
                        .addAction(
                                NotificationCompat.Action.Builder(0, "No", cancelPendingIntent)
                                        .build()
                        )
                        .setOngoing(true) // Evita que la notificación se deslice
                        .build()

        startForeground(notificationId, notification)
    }

    private fun updateNotification(location: Location) {
        val channelId = "location_service_channel"
        val notification: Notification =
                NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Ubicación activa")
                        .setContentText("Lat: ${location.latitude}, Lng: ${location.longitude}")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
        Log.d("LocationService", "Notificación actualizada con ubicación: Lat ${location.latitude}, Lng ${location.longitude}")
    }

    private fun sendLocationToServer(location: Location) {
        Log.d(
                "LocationService",
                "Enviando ubicación al servidor: Lat: ${location.latitude}, Lng: ${location.longitude}"
        )
        // Lógica de envío al backend con Retrofit, Volley o tu preferido.
        // Utilizar ApiClient para enviar la ubicación
        apiClient.sendLocation(
                name = "Ubi de prueba",
                latitude = location.latitude,
                longitude = location.longitude,
                onSuccess = { Log.d("LocationService", "Ubicación enviada exitosamente.") },
                onFailure = { error ->
                    Log.e("LocationService", "Error al enviar la ubicación: $error")
                }
        )
    }

    private fun updateNotificationWithTracking() {
        val channelId = "location_service_channel"
        val notification: Notification =
                NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Ubicación activa")
                        .setContentText("El servicio de ubicación está rastreando tu posición.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
        Log.d("LocationService", "Notificación actualizada para indicar rastreo activo.")
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationId)
    }

}
