package com.appliedrec.facerecognition.r300.core

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FaceTemplateSerializer : KSerializer<FaceTemplateR300> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FaceTemplateArcFace") {
        element<Int>("version")
        element("data", buildSerialDescriptor("FloatArray", StructureKind.LIST) {
            element("element", PrimitiveSerialDescriptor("Float", PrimitiveKind.FLOAT))
        })
    }

    override fun serialize(encoder: Encoder, value: FaceTemplateR300) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.version.id)
        composite.encodeSerializableElement(
            descriptor,
            1,
            ListSerializer(Float.serializer()),
            value.data.toList()
        )
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): FaceTemplateR300 {
        val composite = decoder.beginStructure(descriptor)
        var versionId: Int? = null
        var values: List<Float>? = null

        loop@ while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                0 -> versionId = composite.decodeIntElement(descriptor, 0)
                1 -> values = composite.decodeSerializableElement(
                    descriptor,
                    1,
                    ListSerializer(Float.serializer())
                )
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        composite.endStructure(descriptor)

        if (versionId != FaceTemplateVersionR300.id) {
            throw SerializationException("Invalid version ID: expected ${FaceTemplateVersionR300.id}, got $versionId")
        }

        return FaceTemplateR300(values?.toFloatArray() ?: throw SerializationException("Missing data"))
    }
}