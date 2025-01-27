package com.maps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.Manifest
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.PendingIntent

class LocationService : Service() {

    private var isTracking = false // Nueva variable para controlar el estado

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastKnownLocation: Location? = null // Nueva variable para almacenar la última ubicación

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeLocationCallback()
    }

    private fun initializeLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                handleLocationResult(locationResult)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d("LocationService", "Disponibilidad de ubicación: ${locationAvailability.isLocationAvailable}")
            }
        }
    }

    private fun handleLocationResult(locationResult: LocationResult) {
        locationResult.locations.forEach { location ->
            Log.d("LocationService", "Ubicación recibida: ${location.latitude}, ${location.longitude}")
            lastKnownLocation = location
            // Actualizar el Singleton en lugar de enviar un broadcast
            LocationData.updateLocation(location.latitude, location.longitude)
            updateNotification(location)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> {
                if (!isTracking) {
                    isTracking = true
                    startLocationUpdates() // Comienza el rastreo
                } else {
                    Log.d("LocationService", "El rastreo ya está activo.")
                }
            }
            "STOP_TRACKING" -> {
                isTracking = false
                stopSelf() // Detiene el servicio
            }
            else -> {
                if (!isTracking) {
                    startForegroundService() // Muestra la notificación inicial
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
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
    
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50000) // Cada 10 segundos
            .setMinUpdateDistanceMeters(10f) // Opcional: actualizar cada 10 metros
            .build()
    
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            .addOnSuccessListener {
                Log.d("LocationService", "Actualizaciones de ubicación iniciadas.")
            }
            .addOnFailureListener {
                Log.e("LocationService", "Error al iniciar actualizaciones de ubicación: ${it.message}")
            }
    }

    private val notificationId = 1

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val channelName = "Location Service"
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(notificationChannel)
        }
    
        val confirmationIntent = Intent(this, ConfirmationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val confirmationPendingIntent = PendingIntent.getActivity(
            this, 0, confirmationIntent, PendingIntent.FLAG_IMMUTABLE
        )
    
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle("Activar servicio de ubicación")
            .setContentText("Pulsa para confirmar.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(confirmationPendingIntent)
            .setAutoCancel(true)
            .build()
    
        startForeground(notificationId, notification)
    }    

    private fun updateNotification(location: Location) {
        val channelId = "location_service_channel"
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ubicación activa")
            .setContentText("Lat: ${location.latitude}, Lng: ${location.longitude}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }

    private fun sendLocationToServer(location: Location) {
        Log.d("LocationService", "Enviando ubicación al servidor: Lat: ${location.latitude}, Lng: ${location.longitude}")
        // Lógica de envío al backend con Retrofit, Volley o tu preferido.
    } 
    
}
