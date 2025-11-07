package com.appliedrec.facerecognition.r300.cloud

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appliedrec.facerecognition.r300.core.FaceTemplateVersionR300
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import com.appliedrec.verid3.facedetection.retinaface.FaceDetectionRetinaFace
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceRecognitionTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.appliedrec.facerecognition.r300.cloud.test", appContext.packageName)
    }

    @Test
    fun extractFaceTemplate(): Unit = runBlocking {
        val faceRecognition = createFaceRecognition()
        val faceDetection = FaceDetectionRetinaFace.create(InstrumentationRegistry.getInstrumentation().targetContext)
        val face = faceDetection.detectFacesInImage(testImage, 1).first()
        val faceTemplates = faceRecognition.createFaceRecognitionTemplates(listOf(face), testImage)
        assertEquals(1, faceTemplates.count())
        assertEquals(FaceTemplateVersionR300, faceTemplates[0].version)
        assertEquals(512, faceTemplates[0].data.count())
    }

    private fun createFaceRecognition(): FaceRecognitionR300 {
        val context = InstrumentationRegistry.getInstrumentation().context
        val config = context.assets.open("config.json").use { inputStream ->
            Json.decodeFromString<Config>(inputStream.reader().readText())
        }
        return FaceRecognitionR300(config.apiKey, config.serverUrl)
    }

    private val testImage: Image by lazy {
        val context = InstrumentationRegistry.getInstrumentation().context
        val bitmap = context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        Image.fromBitmap(bitmap)
    }
}