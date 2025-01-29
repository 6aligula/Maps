package com.maps.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiClient(private val baseUrl: String) {

    private val client = OkHttpClient()

    fun sendLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val url = "$baseUrl/api/geo/add/"
        val json = JSONObject().apply {
            put("name", name)
            put("coordinates", JSONObject().apply {
                put("type", "Point")
                put("coordinates", listOf(longitude, latitude))
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("ApiClient", "Error al enviar la ubicación: ${e.message}")
                onFailure(e.message ?: "Error desconocido")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { // Asegura que la respuesta se cierra correctamente
                    if (response.isSuccessful) {
                        Log.d("ApiClient", "Ubicación enviada exitosamente.")
                        onSuccess()
                    } else {
                        Log.e("ApiClient", "Error en la respuesta del servidor: ${response.code}")
                        onFailure("Error en la respuesta del servidor: ${response.code}")
                    }
                }
            }
        })
    }
}
