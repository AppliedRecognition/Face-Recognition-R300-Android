//
// Created by Jakub Dolejs on 20/08/2024.
//

#ifndef FACE_RECOGNITION_FACETEMPLATE_H
#define FACE_RECOGNITION_FACETEMPLATE_H

#include <utility>
#include <vector>

namespace verid {

    struct FaceTemplate {
        FaceTemplate(std::vector<float> data, int version) {
            this->data = std::move(data);
            this->version = version;
        }
        ~FaceTemplate() = default;
        std::vector<float> data;
        int version;
    };

} // verid

#endif //FACE_RECOGNITION_FACETEMPLATE_H
