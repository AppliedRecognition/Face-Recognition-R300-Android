//
// Created by Jakub Dolejs on 03/04/2025.
//

#ifndef FACE_RECOGNITION_FACEALIGNMENT_H
#define FACE_RECOGNITION_FACEALIGNMENT_H

#include "LinearRegression.h"
#include "RotatedBox.h"
#include <vector>

RotatedBox alignFace(const std::vector<Point>& pts, double scale = 2.85) {
    LinearRegression reg;
    const double yofs = 0.35;
    const double y0 = yofs - 0.5;
    const double y1 = yofs + 0.04;
    const double y2 = yofs + 0.5;

    reg.add(pts[0].x, -0.46, -y0, 1.0, 0.0);
    reg.add(pts[0].y,  y0, -0.46, 0.0, 1.0);

    reg.add(pts[1].x,  0.46, -y0, 1.0, 0.0);
    reg.add(pts[1].y,  y0,  0.46, 0.0, 1.0);

    reg.add(pts[2].x,  0.0, -y1, 1.0, 0.0);
    reg.add(pts[2].y,  y1,  0.0, 0.0, 1.0);

    if (pts.size() == 4) {
        reg.add(pts[3].x, 0.0, -y2, 1.0, 0.0);
        reg.add(pts[3].y, y2,  0.0, 0.0, 1.0);
    } else {
        reg.add(pts[3].x, -0.39, -y2, 1.0, 0.0);
        reg.add(pts[3].y,  y2, -0.39, 0.0, 1.0);

        reg.add(pts[4].x,  0.39, -y2, 1.0, 0.0);
        reg.add(pts[4].y,  y2,  0.39, 0.0, 1.0);
    }

    std::vector<double> c = reg.compute();
    if (c.size() != 4) {
        throw std::runtime_error("Linear regression failed");
    }

    double centerX = c[2];
    double centerY = c[3];
    double angle = atan2(c[1], c[0]);
    double widthHeight = scale * std::sqrt(c[0] * c[0] + c[1] * c[1]);

    return RotatedBox{{centerX, centerY}, angle, widthHeight, widthHeight};
}


#endif //FACE_RECOGNITION_FACEALIGNMENT_H
