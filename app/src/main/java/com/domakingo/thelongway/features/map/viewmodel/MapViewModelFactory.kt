package com.domakingo.thelongway.features.map.viewmodel

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory(private val sensorManager: SensorManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(sensorManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
