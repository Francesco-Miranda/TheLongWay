package com.domakingo.thelongway.features.map.viewmodel

import android.hardware.SensorManager
import com.domakingo.thelongway.core.location.LocationProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory(
    private val sensorManager: SensorManager,
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(sensorManager, locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
