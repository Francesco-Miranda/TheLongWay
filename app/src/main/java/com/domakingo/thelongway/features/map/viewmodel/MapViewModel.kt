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
import com.domakingo.thelongway.core.network.GeocodingFeature
import com.domakingo.thelongway.core.network.GeocodingService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MapViewModel(
    private val sensorManager: SensorManager,
    private val locationProvider: LocationProvider
) : ViewModel(), SensorEventListener {
    private val apiKey = BuildConfig.STADIA_MAPS_API_KEY
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val geocodingService = Retrofit.Builder()
        .baseUrl("https://api.stadiamaps.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeocodingService::class.java)

    private val _styleUrl = MutableStateFlow(getStyleUrl(isDark = false))
    val styleUrl: StateFlow<String> = _styleUrl.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _isLocationPermissionGranted = MutableStateFlow(false)
    val isLocationPermissionGranted: StateFlow<Boolean> = _isLocationPermissionGranted.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<GeocodingFeature>>(emptyList())
    val suggestions: StateFlow<List<GeocodingFeature>> = _suggestions.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation.asStateFlow()

    private val _searchResults = MutableSharedFlow<LatLng>()
    val searchResults: SharedFlow<LatLng> = _searchResults.asSharedFlow()

    // Routing state
    private val _isRoutingMode = MutableStateFlow(false)
    val isRoutingMode: StateFlow<Boolean> = _isRoutingMode.asStateFlow()

    private val _originQuery = MutableStateFlow("")
    val originQuery: StateFlow<String> = _originQuery.asStateFlow()

    private val _destinationQuery = MutableStateFlow("")
    val destinationQuery: StateFlow<String> = _destinationQuery.asStateFlow()

    private val _originLocation = MutableStateFlow<LatLng?>(null)
    val originLocation: StateFlow<LatLng?> = _originLocation.asStateFlow()

    private val _destinationLocation = MutableStateFlow<LatLng?>(null)
    val destinationLocation: StateFlow<LatLng?> = _destinationLocation.asStateFlow()

    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var isCurrentDark: Boolean? = null
    private var locationJob: Job? = null
    private var autocompleteJob: Job? = null

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
        handleAutocomplete(query)
    }

    fun onOriginQueryChange(query: String) {
        _originQuery.value = query
        handleAutocomplete(query)
    }

    fun onDestinationQueryChange(query: String) {
        _destinationQuery.value = query
        handleAutocomplete(query)
    }

    private fun handleAutocomplete(query: String) {
        autocompleteJob?.cancel()
        if (query.length < 3) {
            _suggestions.value = emptyList()
            return
        }

        autocompleteJob = viewModelScope.launch {
            delay(300) // Debounce
            try {
                val focus = _userLocation.value
                val response = geocodingService.autocomplete(
                    text = query,
                    apiKey = apiKey,
                    focusLat = focus?.latitude,
                    focusLon = focus?.longitude
                )
                _suggestions.value = response.features
            } catch (e: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    fun onSearch(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            try {
                val focus = _userLocation.value
                val response = geocodingService.search(
                    text = query,
                    apiKey = apiKey,
                    focusLat = focus?.latitude,
                    focusLon = focus?.longitude
                )
                
                response.features.firstOrNull()?.let { feature ->
                    selectFeature(feature)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onSuggestionSelected(feature: GeocodingFeature) {
        val label = feature.properties.label
        val coords = feature.geometry.coordinates
        val latLng = LatLng(coords[1], coords[0])

        if (_isRoutingMode.value) {
            // Determine which field is being edited. 
            // Simplified: if destinationQuery matches label, it was destination, else origin if it's being typed.
            // For now, let's assume we need to know which one was focused. 
            // To keep it simple, if destination is empty or we just started routing, set destination.
            if (_destinationQuery.value.isEmpty() || _selectedLocation.value == latLng) {
                _destinationQuery.value = label
                _destinationLocation.value = latLng
            } else {
                _originQuery.value = label
                _originLocation.value = latLng
            }
        } else {
            _searchQuery.value = label
            _destinationQuery.value = label
            _destinationLocation.value = latLng
            selectFeature(feature)
        }
        _suggestions.value = emptyList()
    }

    private fun selectFeature(feature: GeocodingFeature) {
        val coords = feature.geometry.coordinates
        val latLng = LatLng(coords[1], coords[0])
        _selectedLocation.value = latLng
        viewModelScope.launch {
            _searchResults.emit(latLng)
        }
    }

    fun toggleRoutingMode() {
        _isRoutingMode.value = !_isRoutingMode.value
        if (_isRoutingMode.value) {
            _destinationQuery.value = _searchQuery.value
            _destinationLocation.value = _selectedLocation.value
        }
    }

    fun setOriginToCurrentLocation() {
        _userLocation.value?.let { location ->
            _originQuery.value = "Current Location"
            _originLocation.value = LatLng(location.latitude, location.longitude)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _suggestions.value = emptyList()
        _selectedLocation.value = null
        _isRoutingMode.value = false
        _originQuery.value = ""
        _destinationQuery.value = ""
        _originLocation.value = null
        _destinationLocation.value = null
    }

    fun calculateRoute() {
        val origin = _originLocation.value
        val destination = _destinationLocation.value
        if (origin != null && destination != null) {
            // TODO: Implement actual routing call
            println("Calculating route from $origin to $destination")
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
        locationJob?.cancel()
    }

    companion object {
        private const val LUX_THRESHOLD = 50.0f
    }
}
