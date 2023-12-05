package com.example.gps_tracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.gps_tracker.data.models.CreateMovement
import com.example.gps_tracker.data.models.User
import com.example.gps_tracker.data.network.BASE_URL
import com.example.gps_tracker.data.network.HttpClient
import com.google.android.gms.location.LocationServices
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val uuid = sharedPref.getString(getString(R.string.user_uuid_key), "")


        runBlocking {
            try {
                val res: HttpResponse =
                    HttpClient().getHttpClient().post("$BASE_URL/users/enable-track") {
                        header("Authorization", uuid)
                    }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val channel = NotificationChannel(
            "location",
            "Location",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH")
        val filepath = "GpsTracker"
        locationClient
            .getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updatedNotification.build())


                runBlocking {
                    val res: HttpResponse =  HttpClient().getHttpClient().post("$BASE_URL/movements"){
                        header("Authorization", uuid)
                        setBody(CreateMovement(latitude = lat, longitude = long))
                    }
                }
                try {
                    val current = LocalDateTime.now().format(formatter)
                    val filename = "movements-$current.txt"
                    var myExternalFile = File(getExternalFilesDir(filepath), filename)
                    val fileOutPutStream = FileOutputStream(myExternalFile, true)
                    fileOutPutStream.write("[$lat, $long],\n".toByteArray())
                    fileOutPutStream.close()
                    Log.d("write", "zapisal")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val uuid = sharedPref.getString(getString(R.string.user_uuid_key), "")


        runBlocking {
            try {
                val res: HttpResponse =
                    HttpClient().getHttpClient().post("$BASE_URL/users/disable-track") {
                        header("Authorization", uuid)
                    }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}