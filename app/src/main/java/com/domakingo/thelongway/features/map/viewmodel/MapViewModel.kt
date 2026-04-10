package com.domakingo.thelongway.features.map.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val _styleUrl = MutableStateFlow("https://demotiles.maplibre.org/style.json")
    val styleUrl: StateFlow<String> = _styleUrl.asStateFlow()

    fun updateStyle(url: String) {
        _styleUrl.value = url
    }
}
