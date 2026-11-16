package com.example.spacedefender.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Lê o acelerômetro via SensorManager e entrega a inclinação lateral do aparelho.
 * Com o celular em pé (retrato): inclinar para a direita => valor positivo.
 */
class AccelerometerManager(
    context: Context,
    private val onTilt: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var filtered = 0f

    val isAvailable: Boolean
        get() = accelerometer != null

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        filtered = 0f
        onTilt(0f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // No eixo X do Android, inclinar para a direita gera valor negativo; invertemos.
        val raw = -event.values[0]
        // Filtro passa-baixa para suavizar o movimento
        filtered += ALPHA * (raw - filtered)
        onTilt(filtered)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val ALPHA = 0.25f
    }
}
