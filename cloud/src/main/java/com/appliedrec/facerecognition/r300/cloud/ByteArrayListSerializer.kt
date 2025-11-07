package com.appliedrec.facerecognition.r300.cloud

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Base64

object ByteArrayListSerializer : KSerializer<List<ByteArray>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(ByteArrayAsBase64Serializer).descriptor

    override fun serialize(encoder: Encoder, value: List<ByteArray>) {
        val base64List = value.map { Base64.getEncoder().encodeToString(it) }
        encoder.encodeSerializableValue(ListSerializer(String.serializer()), base64List)
    }

    override fun deserialize(decoder: Decoder): List<ByteArray> {
        val base64List = decoder.decodeSerializableValue(ListSerializer(String.serializer()))
        return base64List.map { Base64.getDecoder().decode(it) }
    }
}

private object ByteArrayAsBase64Serializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Base64ByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(Base64.getEncoder().encodeToString(value))
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return Base64.getDecoder().decode(decoder.decodeString())
    }
}