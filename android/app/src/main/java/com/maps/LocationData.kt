package com.maps

object LocationData {
    @Volatile
    var latestLatitude: Double? = null
        private set

    @Volatile
    var latestLongitude: Double? = null
        private set

    fun updateLocation(latitude: Double, longitude: Double) {
        latestLatitude = latitude
        latestLongitude = longitude
    }
}
