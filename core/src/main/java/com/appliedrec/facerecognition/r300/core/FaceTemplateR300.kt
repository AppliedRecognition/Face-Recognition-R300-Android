package com.appliedrec.facerecognition.r300.core

import com.appliedrec.verid3.common.FaceTemplate
import kotlinx.serialization.Serializable

@Serializable(with = FaceTemplateSerializer::class)
class FaceTemplateR300(data: FloatArray) : FaceTemplate<FaceTemplateVersionR300, FloatArray>(
    FaceTemplateVersionR300, data) {

    override fun equals(other: Any?): Boolean {
        return other is FaceTemplateR300 && other.data.contentEquals(data)
    }

    override fun hashCode(): Int {
        return 31 * version.hashCode() + data.contentHashCode()
    }
}