package com.appliedrec.facerecognition.r300.core

import android.graphics.Bitmap
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.FaceRecognition
import com.appliedrec.verid3.common.FaceTemplate
import com.appliedrec.verid3.common.IImage
import com.appliedrec.verid3.common.serialization.toBitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class FaceRecognitionR300Core: FaceRecognition<FaceTemplateVersionR300, FloatArray> {
    override val version: FaceTemplateVersionR300 = FaceTemplateVersionR300
    override val defaultThreshold: Float = 0.6f

    private val faceAlignment = FaceAlignment()

    override suspend fun createFaceRecognitionTemplates(
        faces: List<Face>,
        image: IImage
    ): List<FaceTemplate<FaceTemplateVersionR300, FloatArray>> {
        val bitmap = image.toBitmap()
        val imageList = faces.map { face ->
            require(face.noseTip != null && face.mouthLeftCorner != null && face.mouthRightCorner != null)
            val aligned = faceAlignment.alignFace(bitmap, face)
            aligned
        }
        return createFaceTemplatesFromAlignedFaceImages(imageList)
    }

    override suspend fun compareFaceRecognitionTemplates(
        faceRecognitionTemplates: List<FaceTemplate<FaceTemplateVersionR300, FloatArray>>,
        template: FaceTemplate<FaceTemplateVersionR300, FloatArray>
    ): FloatArray = coroutineScope {
        require(faceRecognitionTemplates.all { it.data.size == template.data.size }) {
            "Face recognition templates must have the same length"
        }
        val a = template.data
        faceRecognitionTemplates.map { it.data }
            .chunked(100)
            .map { chunk ->
                async {
                    FloatArray(chunk.size) { idx ->
                        val b = chunk[idx]
                        val cos = innerProduct(a, b)
                        cos.coerceIn(0f, 1f)
                    }
                }
            }
            .awaitAll()
            .reduce { acc, arr -> acc + arr }
    }

    abstract suspend fun createFaceTemplatesFromAlignedFaceImages(faceImages: List<Bitmap>): List<FaceTemplate<FaceTemplateVersionR300, FloatArray>>

    protected fun innerProduct(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { a, b -> a * b }.sum()
    }
}