package com.example.gps_tracker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.widget.ToggleButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )
        setContentView(R.layout.activity_main)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvImei = findViewById<TextView>(R.id.tvUuid)
        val tb = findViewById<ToggleButton>(R.id.tb)

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val name = sharedPref.getString(getString(R.string.user_name_key), "")
        val uuid = sharedPref.getString(getString(R.string.user_uuid_key), "")

        tvName.text = "name: ${name}"
        tvImei.text = "uuid: ${uuid}"

        tb.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    startService(this)
                }
            } else {
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    startService(this)
                }
            }
        }

    }
}

//package com.plcoding.backgroundlocationtracking
//
//import android.Manifest
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.material.Button
//import androidx.compose.material.Text
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import com.plcoding.backgroundlocationtracking.ui.theme.BackgroundLocationTrackingTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//            ),
//            0
//        )
//        var text = ""
//        setContent {
//            BackgroundLocationTrackingTheme {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    Button(onClick = {
//                        Intent(applicationContext, LocationService::class.java).apply {
//                            action = LocationService.ACTION_START
//                            startService(this)
//                        }
//                    }) {
//                        Text(text = "Start")
//                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(onClick = {
//                        Intent(applicationContext, LocationService::class.java).apply {
//                            action = LocationService.ACTION_STOP
//                            startService(this)
//                        }
//                    }) {
//                        Text(text = "Stop")
//                    }
//                }
//            }
//        }
//    }
//}