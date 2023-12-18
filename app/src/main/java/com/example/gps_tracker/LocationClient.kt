package com.example.gps_tracker

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long, minDistance: Float): Flow<Location>

    class LocationException(message: String): Exception()
}