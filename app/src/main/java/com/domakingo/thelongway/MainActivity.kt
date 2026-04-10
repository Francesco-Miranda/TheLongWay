package com.domakingo.thelongway

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domakingo.thelongway.core.location.LocationProvider
import com.domakingo.thelongway.core.ui.theme.TheLongWayTheme
import com.domakingo.thelongway.features.map.ui.MapScreen
import com.domakingo.thelongway.features.map.viewmodel.MapViewModel
import com.domakingo.thelongway.features.map.viewmodel.MapViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheLongWayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
                    val locationProvider = remember { LocationProvider(context) }
                    val viewModel: MapViewModel = viewModel(
                        factory = MapViewModelFactory(sensorManager!!, locationProvider)
                    )

                    MapScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
