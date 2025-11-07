package com.appliedrec.facerecognition.r300.cloud

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val serverUrl: String,
    val apiKey: String
)
