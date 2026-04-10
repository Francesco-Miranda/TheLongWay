package com.domakingo.thelongway.features.map.viewmodel

import androidx.lifecycle.ViewModel
import com.domakingo.thelongway.BuildConfig
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel(private val sensorManager: SensorManager) : ViewModel(), SensorEventListener {
    private val apiKey = BuildConfig.STADIA_MAPS_API_KEY
    private val _styleUrl = MutableStateFlow(getStyleUrl(isDark = false))
    val styleUrl: StateFlow<String> = _styleUrl.asStateFlow()

    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var isCurrentDark = false

    init {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val lux = event?.values?.get(0) ?: return
        val isDark = lux < LUX_THRESHOLD
        
        if (isDark != isCurrentDark) {
            isCurrentDark = isDark
            _styleUrl.value = getStyleUrl(isDark)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getStyleUrl(isDark: Boolean): String {
        val style = if (isDark) "alidade_smooth_dark" else "alidade_smooth"
        return "https://tiles.stadiamaps.com/styles/$style.json?api_key=$apiKey"
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    companion object {
        private const val LUX_THRESHOLD = 15.0f
    }
}
