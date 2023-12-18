package com.example.gps_tracker

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient,
): LocationClient {

    private var kalmanFilter: Kalman
    init {
        kalmanFilter = Kalman(3f)
    }

    var currentSpeed = 0.0f // meters/second

    var runStartTimeInMillis: Long = 0

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long, minDistance: Float): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }


            val request = LocationRequest.Builder(interval)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(minDistance)
                .setWaitForAccurateLocation(true)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    if(GetKalmanFilteredLocation(result.lastLocation!!) == null) return;
                    launch { send(result.lastLocation!!) }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun getLocationAge(newLocation: Location): Long {
        val locationAge: Long = if (Build.VERSION.SDK_INT >= 17) {
            (SystemClock.elapsedRealtimeNanos() / 1000000) - (newLocation.elapsedRealtimeNanos / 1000000)
        } else {
            System.currentTimeMillis() - newLocation.time
        }
        return locationAge
    }

    private fun GetKalmanFilteredLocation(location: Location): Location? {
        val age = getLocationAge(location)
        if (age > 5 * 1000) { //more than 5 seconds
            Log.d(TAG, "Location is old")
            return null
        }
        if (location.accuracy <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.")
            return null
        }

        //setAccuracy(newLocation.getAccuracy());
        val horizontalAccuracy = location.accuracy
        if (horizontalAccuracy > 20) { //10meter filter
            Log.d(TAG, "Accuracy is too low. $horizontalAccuracy")
            return null
        }


        /* Kalman Filter */
        val elapsedTimeInMillis = (location.elapsedRealtimeNanos / 1000000) - runStartTimeInMillis
        val Qvalue: Float = if (currentSpeed == 0.0f) {
            3.0f //3 meters per second
        } else {
            currentSpeed // meters per second
        }
        kalmanFilter.Process(
            location.latitude,
            location.longitude,
            location.accuracy,
            elapsedTimeInMillis,
            Qvalue
        )
        val predictedLat = kalmanFilter!!._lat
        val predictedLng = kalmanFilter!!._lng
        val predictedLocation = Location("") //provider name is unecessary
        predictedLocation.latitude = predictedLat //your coords of course
        predictedLocation.longitude = predictedLng
        val predictedDeltaInMeters = predictedLocation.distanceTo(location)
        if (predictedDeltaInMeters > 60) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter!!.consecutiveRejectCount += 1
            if (kalmanFilter!!.consecutiveRejectCount > 3) {
                kalmanFilter =
                    Kalman(3f) //reset Kalman Filter if it rejects more than 3 times in raw.
            }
            return null
        } else {
            kalmanFilter!!.consecutiveRejectCount = 0
        }

        Log.d(TAG, "Location quality is good enough.")
        currentSpeed = location.speed
        return location
    }
}