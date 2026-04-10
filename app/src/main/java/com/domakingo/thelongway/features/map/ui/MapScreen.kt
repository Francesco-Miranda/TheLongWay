package com.domakingo.thelongway.features.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.domakingo.thelongway.core.ui.permissions.PermissionGate
import com.domakingo.thelongway.features.map.viewmodel.MapViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val isPermissionGranted by viewModel.isLocationPermissionGranted.collectAsState()

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    PermissionGate(
        permissions = locationPermissions,
        onPermissionsResult = { granted -> viewModel.onLocationPermissionResult(granted) }
    ) {
        MapContent(
            viewModel = viewModel,
            isPermissionGranted = isPermissionGranted,
            modifier = modifier
        )
    }
}

@Composable
private fun MapContent(
    viewModel: MapViewModel,
    isPermissionGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val styleUrl by viewModel.styleUrl.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val mapView = remember { MapView(context) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var isInitialZoomPerformed by remember { mutableStateOf(false) }

    remember { MapLibre.getInstance(context) }

    LaunchedEffect(mapView, isPermissionGranted, styleUrl) {
        mapView.getMapAsync { map ->
            mapInstance = map
            map.setStyle(styleUrl) { style ->
                if (isPermissionGranted) {
                    setupLocationComponent(context, map, style)
                }
            }
        }
    }

    LaunchedEffect(userLocation) {
        val location = userLocation ?: return@LaunchedEffect
        val map = mapInstance ?: return@LaunchedEffect

        if (!isInitialZoomPerformed) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    15.0
                )
            )
            isInitialZoomPerformed = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

@SuppressLint("MissingPermission")
private fun setupLocationComponent(context: Context, map: MapLibreMap, style: org.maplibre.android.maps.Style) {
    val locationComponent = map.locationComponent
    val activationOptions = LocationComponentActivationOptions.builder(context, style).build()
    
    locationComponent.activateLocationComponent(activationOptions)
    locationComponent.isLocationComponentEnabled = true
    locationComponent.renderMode = RenderMode.COMPASS
    locationComponent.cameraMode = CameraMode.TRACKING
}
