package com.maps

import android.util.Log

object LocationData {
    @Volatile
    var latestLatitude: Double? = null
        private set

    @Volatile
    var latestLongitude: Double? = null
        private set

    // Listener para notificar cambios de ubicación
    private var locationListener: ((Double, Double) -> Unit)? = null

    // Listener para notificar cambios de estado de rastreo
    private var trackingListener: ((Boolean) -> Unit)? = null

    // Actualizar ubicación y notificar al listener
    fun updateLocation(latitude: Double, longitude: Double) {
        latestLatitude = latitude
        latestLongitude = longitude
        Log.d("LocationData", "Ubicación actualizada: Lat=$latitude, Lng=$longitude")
        locationListener?.invoke(latitude, longitude)
    }

    // Configurar el listener de ubicación
    fun setLocationListener(callback: (Double, Double) -> Unit) {
        locationListener = callback
    }

    // Remover el listener de ubicación
    fun removeLocationListener() {
        locationListener = null
    }

    // Estado de rastreo
    @Volatile
    var isTracking: Boolean = false
        private set

    // Actualizar estado de rastreo y notificar al listener
    fun setTrackingState(tracking: Boolean) {
        isTracking = tracking
        trackingListener?.invoke(tracking)
    }

    // Configurar el listener de estado de rastreo
    fun setTrackingListener(callback: (Boolean) -> Unit) {
        trackingListener = callback
    }

    // Remover el listener de estado de rastreo
    fun removeTrackingListener() {
        trackingListener = null
    }
}
