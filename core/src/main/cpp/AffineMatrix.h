//
// Created by Jakub Dolejs on 03/04/2025.
//

#ifndef FACE_RECOGNITION_AFFINEMATRIX_H
#define FACE_RECOGNITION_AFFINEMATRIX_H

#include "RotatedBox.h"
#include <cmath>

struct AffineMatrix {
    double m[2][3]; // 2x3 affine matrix
};

AffineMatrix buildTransform(Point center, double angle, double scale, int targetW, int targetH) {
    double invScale = targetW / scale; // scaling factor: source region → output
    double cosA = std::cos(-angle);
    double sinA = std::sin(-angle);

    // Apply rotation and scaling
    double a = cosA * invScale;
    double b = -sinA * invScale;
    double c = sinA * invScale;
    double d = cosA * invScale;

    // Translate: center to target center
    double tx = -center.x * a - center.y * b + targetW / 2.0;
    double ty = -center.x * c - center.y * d + targetH / 2.0;

    return {{{a, b, tx}, {c, d, ty}}};
}

AffineMatrix invert(const AffineMatrix& mat) {
    double a = mat.m[0][0], b = mat.m[0][1], tx = mat.m[0][2];
    double c = mat.m[1][0], d = mat.m[1][1], ty = mat.m[1][2];

    double det = a * d - b * c;
    if (std::abs(det) < 1e-8)
        throw std::runtime_error("Singular matrix");

    double invA = d / det;
    double invB = -b / det;
    double invC = -c / det;
    double invD = a / det;
    double invTx = -(invA * tx + invB * ty);
    double invTy = -(invC * tx + invD * ty);

    return {{{invA, invB, invTx}, {invC, invD, invTy}}};
}

#endif //FACE_RECOGNITION_AFFINEMATRIX_H
