package com.domakingo.thelongway.features.map.viewmodel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domakingo.thelongway.BuildConfig
import com.domakingo.thelongway.core.location.LocationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MapViewModel(
    private val sensorManager: SensorManager,
    private val locationProvider: LocationProvider
) : ViewModel(), SensorEventListener {
    private val apiKey = BuildConfig.STADIA_MAPS_API_KEY
    
    private val _styleUrl = MutableStateFlow(getStyleUrl(isDark = false))
    val styleUrl: StateFlow<String> = _styleUrl.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _isLocationPermissionGranted = MutableStateFlow(false)
    val isLocationPermissionGranted: StateFlow<Boolean> = _isLocationPermissionGranted.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var isCurrentDark: Boolean? = null
    private var locationJob: Job? = null

    init {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _isLocationPermissionGranted.value = granted
        if (granted) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (locationJob != null) return
        
        locationJob = locationProvider.locationFlow()
            .onEach { location -> _userLocation.value = location }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearch(query: String) {
        // TODO: Implement search logic
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
        locationJob?.cancel()
    }

    companion object {
        private const val LUX_THRESHOLD = 50.0f
    }
}
