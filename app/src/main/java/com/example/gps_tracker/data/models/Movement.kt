package com.example.gps_tracker.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMovement(
    @SerialName("latitude")
    val latitude: String,
    @SerialName("longitude")
    val longitude: String
)