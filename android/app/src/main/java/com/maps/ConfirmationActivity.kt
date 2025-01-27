package com.maps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class ConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this)
            .setTitle("Confirmar acción")
            .setMessage("¿Quieres activar el servicio de ubicación?")
            .setPositiveButton("Sí") { _, _ ->
                // Iniciar el servicio de ubicación
                val intent = Intent(this, LocationService::class.java).apply {
                    action = "START_TRACKING" // Acción específica para iniciar rastreo
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }

                Toast.makeText(this, "Ubicación accedida", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No") { _, _ ->
                Toast.makeText(this, "Acción cancelada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setOnCancelListener {
                Toast.makeText(this, "Acción cancelada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }
}
