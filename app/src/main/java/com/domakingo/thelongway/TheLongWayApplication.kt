package com.domakingo.thelongway

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class TheLongWayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Global initialization of MapLibre
        MapLibre.getInstance(
            this,
            BuildConfig.STADIA_MAPS_API_KEY,
            WellKnownTileServer.MapLibre
        )
    }
}
