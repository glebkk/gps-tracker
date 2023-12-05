package com.example.gps_tracker.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

class HttpClient {
    fun getHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        expectSuccess = true
        install(ResponseObserver){
            onResponse { response ->
                Log.i("Response", "${response.status.value}")
            }
        }

        install(DefaultRequest){
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}