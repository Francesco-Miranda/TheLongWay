package com.domakingo.thelongway.features.map.viewmodel

import androidx.lifecycle.ViewModel
import com.domakingo.thelongway.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val apiKey = BuildConfig.STADIA_MAPS_API_KEY
    private val _styleUrl = MutableStateFlow("https://tiles.stadiamaps.com/styles/alidade_smooth.json?api_key=$apiKey")
    val styleUrl: StateFlow<String> = _styleUrl.asStateFlow()

    fun updateStyle(styleName: String) {
        _styleUrl.value = "https://tiles.stadiamaps.com/styles/$styleName.json?api_key=$apiKey"
    }
}
