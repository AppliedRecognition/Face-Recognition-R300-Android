# Face recognition for Android using R300 model

## Requirements

Runs on Android 8.0 (API level 26) or newer

## Installation

Add the following dependency in your build.gradle.kts:

```kotlin
implementation("com.appliedred:face-recognition-r300-cloud:1.0.0")
```

## Usage

The library requires an API key. You can obtain the API key by [contacting Applied Recognition](mailto:support@appliedrecognition.com). The project’s test target includes a rate-limited API key to use for testing. You’re welcome to use the API key in your test/demo projects but it’s not suitable for production use.

The library adopts the [`FaceRecognition`](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid3/common/FaceRecognition.kt) interface from the [Ver-ID common types](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android) library, making it compatible with the [Ver-ID SDK](https://github.com/AppliedRecognition/.github/blob/main/profile/Android-SDK.md).

### Examples

Create a face template from a face detected in an image.

```kotlin
import android.content.Context
import android.graphics.Bitmap
import com.appliedrec.facerecognition.r300.core.FaceTemplateVersionR300
import com.appliedrec.verid3.common.FaceTemplate
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import com.appliedrec.verid3.common.use
import com.appliedrec.verid3.facedetection.retinaface.FaceDetectionRetinaFace

/**
 * Detect a face and extract its face template. Face templates are used to compare faces.
 */
suspend fun extractFaceTemplateFromFaceInImage(
    context: Context,
    bitmap: Bitmap
): FaceTemplate<FaceTemplateVersionR300, FloatArray> {
    return FaceDetectionRetinaFace.create(context).use { faceDetection ->
        val image = Image.fromBitmap(bitmap)
        faceDetection.detectFacesInImage(image, 1).firstOrNull()?.let { face ->
            FaceRecognitionR300("<your API key>", "<server URL>").use { faceRecognition ->
                faceRecognition.createFaceRecognitionTemplates(listOf(face), image)
                    .firstOrNull() ?: throw Exception("Failed to extract face template")
            }
        } ?: throw Exception("Failed to detect a face in image")
    }
}
```

Compare face templates.

```kotlin
import android.content.Context
import com.appliedrec.facerecognition.r300.core.FaceTemplateVersionR300
import com.appliedrec.verid3.common.FaceTemplate
import com.appliedrec.verid3.common.use

/**
 * Compare 2 face templates against each other and return a score ranging from 0 to 1.
 * 0 means the face templates represent 2 different people, 1 means the highest confidence
 * that the face templates represent the same person.
 */
suspend fun compareFaceTemplates(
    context: Context,
    template1: FaceTemplate<FaceTemplateVersionR300, FloatArray>,
    template2: FaceTemplate<FaceTemplateVersionR300, FloatArray>
): Float {
    return FaceRecognitionR300("<your API key>", "<server URL>").use { faceRecognition ->
        faceRecognition.compareFaceRecognitionTemplates(
            listOf(template1),
            template2
        ).firstOrNull() ?: throw Exception("Face template comparison failed")
    }
}
```