package com.appliedrec.facerecognition.r300.core

import android.graphics.Bitmap
import android.graphics.PointF
import com.appliedrec.verid3.common.Face

class FaceAlignment {

    init {
        System.loadLibrary("FaceRecognitionR300Core")
    }

    /**
     * Align face for face template extraction
     *
     * @param image Image with the face
     * @param face Face to align
     * @return Image with the aligned face
     */
    fun alignFace(image: Bitmap, face: Face): Bitmap {
        require(face.noseTip != null) {
            "Missing nose coordinate"
        }
        require(face.mouthCentre != null || (face.mouthLeftCorner != null && face.mouthRightCorner != null)) {
            "Missing mouth coordinates"
        }
        val landmarks = mutableListOf<PointF>(face.leftEye, face.rightEye, face.noseTip!!)
        if (face.mouthLeftCorner != null && face.mouthRightCorner != null) {
            landmarks.add(face.mouthLeftCorner!!)
            landmarks.add(face.mouthRightCorner!!)
        } else {
            landmarks.add(face.mouthCentre!!)
        }
        return alignFace(image, landmarks.toTypedArray())
    }

    /**
     * Align face for face template extraction
     *
     * @param image Image with the face
     * @param landmarks Face landmarks – must be an array of points in the following order:
     *  1. Left eye
     *  2. Right eye
     *  3. Nose tip
     *  4. Left mouth corner
     *  5. Right mouth corner
     *
     *  Alternatively, you can supply mouth centre instead of left and right mouth corners:
     *  1. Left eye
     *  2. Right eye
     *  3. Nose tip
     *  4. Mouth centre
     * @return Image with the aligned face
     */
    external fun alignFace(image: Bitmap, landmarks: Array<PointF>): Bitmap
}