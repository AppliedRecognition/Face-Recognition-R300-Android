package com.appliedrec.facerecognition.r300.cloud

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.appliedrec.facerecognition.r300.core.FaceRecognitionR300Core
import com.appliedrec.facerecognition.r300.core.FaceTemplateR300
import com.appliedrec.facerecognition.r300.core.FaceTemplateVersionR300
import com.appliedrec.verid3.common.FaceTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class FaceRecognitionR300(
    val apiToken: String,
    val serverUrl: HttpUrl
) : FaceRecognitionR300Core() {

    constructor(apiToken: String, serverUrl: String) : this(apiToken, serverUrl.toHttpUrl())

    constructor(context: Context) : this(
        resolveMetaData(
            context,
            "com.appliedrec.face-recognition-r300.apiKey"
        ),
        resolveMetaData(
            context,
            "com.appliedrec.face-recognition-r300.serverUrl"
        )
    )

    companion object {
        private fun resolveMetaData(context: Context, key: String): String {
            val appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val value = appInfo.metaData?.getString(key)
            require(!value.isNullOrEmpty()) {
                "Missing or empty meta-data for key: $key"
            }
            return value
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    override suspend fun createFaceTemplatesFromAlignedFaceImages(
        faceImages: List<Bitmap>
    ): List<FaceTemplate<FaceTemplateVersionR300, FloatArray>> = withContext(Dispatchers.IO) {
        val imageList = faceImages.map {
            bitmapToJpeg(it)
        }
        val requestBody = Json.encodeToString(RequestBody(imageList))
        val request = Request.Builder()
            .url(serverUrl)
            .header("x-api-key", apiToken)
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        val body = httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP code ${response.code}")
            }
            response.body.string()
        }
        Json.decodeFromString<List<FaceTemplateR300>>(body).map { template ->
            FaceTemplateR300(normalize(template.data))
        }
    }

    private fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.toByteArray()
        }
    }

    private fun normalize(v: FloatArray): FloatArray {
        val norm = norm(v)
        return v.map { it / norm }.toFloatArray()
    }

    private fun norm(v: FloatArray): Float {
        return sqrt(innerProduct(v, v))
    }
}