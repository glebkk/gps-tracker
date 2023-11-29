package com.example.gps_tracker.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @SerialName("id")
    val id: Int?,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("name")
    val name: String
)