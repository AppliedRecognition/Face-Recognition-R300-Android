//
// Created by Jakub Dolejs on 03/04/2025.
//

#ifndef FACE_RECOGNITION_ROTATEDBOX_H
#define FACE_RECOGNITION_ROTATEDBOX_H

struct Point {
    double x;
    double y;
};

struct RotatedBox {
    Point center;
    double angle; // radians
    double width;
    double height;
};

#endif //FACE_RECOGNITION_ROTATEDBOX_H
