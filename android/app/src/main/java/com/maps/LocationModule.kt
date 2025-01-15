package com.maps

import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.Arguments
import android.content.IntentFilter
import android.content.BroadcastReceiver


class LocationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var latestLatitude: Double? = null
    private var latestLongitude: Double? = null

    init {
        val intentFilter = IntentFilter("com.maps.LOCATION_UPDATE")
        reactContext.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                latestLatitude = intent?.getDoubleExtra("latitude", 0.0)
                latestLongitude = intent?.getDoubleExtra("longitude", 0.0)
            }
        }, intentFilter)
    }
    override fun getName(): String {
        return "LocationModule"
    }

    @ReactMethod
    fun startLocationService() {
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)
        context.startService(intent)
    }

    @ReactMethod
    fun stopLocationService() {
        val context: Context = reactApplicationContext
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
    @ReactMethod
    fun getLastSentLocation(promise: Promise) {
        if (latestLatitude != null && latestLongitude != null) {
            val locationMap = Arguments.createMap()
            locationMap.putDouble("latitude", latestLatitude!!)
            locationMap.putDouble("longitude", latestLongitude!!)
            promise.resolve(locationMap)
        } else {
            promise.reject("LOCATION_ERROR", "No se encontró ubicación.")
        }
    }
}
