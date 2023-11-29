package com.example.gps_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gps_tracker.data.models.User
import com.example.gps_tracker.data.network.BASE_URL
import com.example.gps_tracker.data.network.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.util.InternalAPI
import kotlinx.coroutines.runBlocking
import java.util.UUID

import kotlinx.serialization.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val isSetUuid = sharedPref.contains(getString(R.string.user_uuid_key))
        if(isSetUuid){
            val secondActivityIntent = Intent(
                this, MainActivity::class.java
            )
            secondActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            secondActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            finish()
            startActivity(secondActivityIntent)
            return
        }
        setContentView(R.layout.activity_login)

        val input = findViewById<EditText>(R.id.editTextText)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            if(input.text.isNotEmpty()){
                val myUuid = UUID.randomUUID()
                var response: HttpResponse
                runBlocking {
                    val res: HttpResponse =  HttpClient().getHttpClient().post("${BASE_URL}/users"){
                        setBody(User(id = null, uuid = myUuid.toString(), name = input.text.toString()))
                    }
                    response = res
                    Toast.makeText(this@LoginActivity, response.status.toString(), Toast.LENGTH_SHORT).show()
                }

                if(response.status.value != 200) {
                    Toast.makeText(this, @OptIn(InternalAPI::class) response.content.toString(), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                with(sharedPref.edit()) {
                    putString(getString(R.string.user_name_key), input.text.toString())
                    apply()
                }
                with(sharedPref.edit()) {
                    putString(getString(R.string.user_uuid_key), myUuid.toString())
                    apply()
                }
                val secondActivityIntent = Intent(
                    this, MainActivity::class.java
                )
                secondActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                secondActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                finish()
                startActivity(secondActivityIntent)
            }
        }


    }
}

