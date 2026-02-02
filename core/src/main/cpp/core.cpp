#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "RotatedBox.h"
#include "FaceAlignment.h"
#include "AffineMatrix.h"

std::vector<float> cropAlignToTensor(JNIEnv* env, jobject bitmap, const RotatedBox& box) {
    const int outW = 112;
    const int outH = 112;

    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        throw std::runtime_error("Failed to get bitmap info");
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        throw std::runtime_error("Bitmap must be ARGB_8888");
    }

    void* pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        throw std::runtime_error("Failed to lock bitmap");
    }
    auto* input = static_cast<uint32_t*>(pixels);

    std::vector<float> tensor(1 * 3 * outH * outW, 0.0f); // [1, 3, 112, 112]

    double scale = outW / box.width;
    AffineMatrix matrix = buildTransform(box.center, box.angle, box.width, outW, outH);
    AffineMatrix invMatrix = invert(matrix);

    for (int y = 0; y < outH; ++y) {
        for (int x = 0; x < outW; ++x) {
            double srcX = invMatrix.m[0][0] * x + invMatrix.m[0][1] * y + invMatrix.m[0][2];
            double srcY = invMatrix.m[1][0] * x + invMatrix.m[1][1] * y + invMatrix.m[1][2];

            // Bilinear
            int x0 = static_cast<int>(std::floor(srcX));
            int y0 = static_cast<int>(std::floor(srcY));
            int x1 = x0 + 1;
            int y1 = y0 + 1;

            float dx = srcX - x0;
            float dy = srcY - y0;

            auto sample = [&](int x, int y) -> std::array<float, 3> {
                if (x >= 0 && x < static_cast<int>(info.width) &&
                    y >= 0 && y < static_cast<int>(info.height)) {
                    uint32_t pixel = input[y * info.stride / 4 + x];
                    auto b = static_cast<float>((pixel >> 0) & 0xFF);
                    auto g = static_cast<float>((pixel >> 8) & 0xFF);
                    auto r = static_cast<float>((pixel >> 16) & 0xFF);
                    return {r, g, b};
                }
                return {0, 0, 0};  // fallback to black
            };

            std::array<float, 3> c00 = sample(x0, y0);
            std::array<float, 3> c10 = sample(x1, y0);
            std::array<float, 3> c01 = sample(x0, y1);
            std::array<float, 3> c11 = sample(x1, y1);

            std::array<float, 3> rgb{};
            for (int i = 0; i < 3; ++i) {
                float top = c00[i] * (1 - dx) + c10[i] * dx;
                float bottom = c01[i] * (1 - dx) + c11[i] * dx;
                rgb[i] = top * (1 - dy) + bottom * dy;
            }

            int idx = y * outW + x;
            tensor[idx] = rgb[0];
            tensor[outH * outW + idx] = rgb[1];
            tensor[2 * outH * outW + idx] = rgb[2];
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return tensor;
}

std::vector<Point> pointVectorFromObject(JNIEnv *env, jobjectArray points) {
    jint pointCount = env->GetArrayLength(points);
    if (pointCount != 4) {
        throw std::exception();
    }
    jobject leftEyeObj = env->GetObjectArrayElement(points, 0);
    jclass pointClass = env->GetObjectClass(leftEyeObj);
    jfieldID xFieldId = env->GetFieldID(pointClass, "x", "F");
    jfieldID yFieldId = env->GetFieldID(pointClass, "y", "F");
    std::vector<Point> pointVec(pointCount);
    env->EnsureLocalCapacity(pointCount);
    for (int i=0; i<pointCount; i++) {
        jobject pt = env->GetObjectArrayElement(points, i);
        Point point = {env->GetFloatField(pt, xFieldId), env->GetFloatField(pt, yFieldId)};
        env->DeleteLocalRef(pt);
        pointVec[i] = point;
    }
    return pointVec;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_appliedrec_facerecognition_r300_core_FaceAlignment_alignFace(
        JNIEnv *env, jobject thiz, jobject image, jobjectArray landmarks, jdouble scale, jdouble verticalOffset) {
    int landmarkCount = env->GetArrayLength(landmarks);
    if (landmarkCount < 4 || landmarkCount > 5) {
        throw std::runtime_error("Invalid number of face landmarks");
    }
    jclass pointClass = env->FindClass("android/graphics/PointF");
    jfieldID xFieldId = env->GetFieldID(pointClass, "x", "F");
    jfieldID yFieldId = env->GetFieldID(pointClass, "y", "F");
    std::vector<Point> points(landmarkCount);
    for (int i=0; i<landmarkCount; i++) {
        jobject pt = env->GetObjectArrayElement(landmarks, i);
        points[i] = Point{env->GetFloatField(pt, xFieldId), env->GetFloatField(pt, yFieldId)};
    }
    RotatedBox rotatedBox = alignFace(points, scale, verticalOffset);
    std::vector<float> inputTensor = cropAlignToTensor(env, image, rotatedBox);

    int size = 112;

    // === 1. Get Bitmap class and createBitmap method ===
    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapCls, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // === 2. Get Bitmap.Config.ARGB_8888 ===
    jclass configCls = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfMethod = env->GetStaticMethodID(configCls, "valueOf",
                                                     "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jstring argbStr = env->NewStringUTF("ARGB_8888");
    jobject bitmapConfig = env->CallStaticObjectMethod(configCls, valueOfMethod, argbStr);

    // === 3. Create the Bitmap object ===
    jobject bitmap = env->CallStaticObjectMethod(bitmapCls, createBitmapMethod, size, size, bitmapConfig);

    // === 4. Lock pixels ===
    AndroidBitmapInfo info;
    void* pixels;
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0 || AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        return nullptr;
    }

    // === 5. Fill pixels with your data ===
    float* rPlane = inputTensor.data();
    float* gPlane = inputTensor.data() + size * size;
    float* bPlane = inputTensor.data() + 2 * size * size;

    for (int y = 0; y < size; ++y) {
        auto* line = reinterpret_cast<uint32_t*>((uint8_t*)pixels + y * info.stride);
        for (int x = 0; x < size; ++x) {
            int idx = y * size + x;

            uint8_t r = static_cast<uint8_t>(std::clamp(rPlane[idx], 0.0f, 255.0f));
            uint8_t g = static_cast<uint8_t>(std::clamp(gPlane[idx], 0.0f, 255.0f));
            uint8_t b = static_cast<uint8_t>(std::clamp(bPlane[idx], 0.0f, 255.0f));
            uint8_t a = 255;

            line[x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return bitmap;
}