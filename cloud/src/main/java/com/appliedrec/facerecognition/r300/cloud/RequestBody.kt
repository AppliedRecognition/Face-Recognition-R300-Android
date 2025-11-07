package com.appliedrec.facerecognition.r300.cloud

import kotlinx.serialization.Serializable

@Serializable
data class RequestBody(
    @Serializable(with = ByteArrayListSerializer::class)
    val images: List<ByteArray>
)
