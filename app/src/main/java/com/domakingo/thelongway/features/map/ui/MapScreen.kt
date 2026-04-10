package com.domakingo.thelongway.features.map.ui

import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domakingo.thelongway.features.map.viewmodel.MapViewModel
import com.domakingo.thelongway.features.map.viewmodel.MapViewModelFactory
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(sensorManager))
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val styleUrl by viewModel.styleUrl.collectAsState()

    remember { MapLibre.getInstance(context) }

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle(styleUrl)
            }
        }
    }

    // Update style when URL changes (Light Observer using it)
    DisposableEffect(styleUrl) {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl)
        }
        onDispose { }
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
