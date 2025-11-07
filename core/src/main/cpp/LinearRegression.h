//
// Created by Jakub Dolejs on 03/04/2025.
//

#ifndef FACE_RECOGNITION_LINEARREGRESSION_H
#define FACE_RECOGNITION_LINEARREGRESSION_H

#include <vector>

class LinearRegression {
private:
    std::vector<std::vector<double>> data;
    std::vector<double> results;

    std::vector<std::vector<double>> transpose(const std::vector<std::vector<double>>& m) {
        size_t rows = m.size(), cols = m[0].size();
        std::vector<std::vector<double>> t(cols, std::vector<double>(rows));
        for (size_t i = 0; i < rows; ++i)
            for (size_t j = 0; j < cols; ++j)
                t[j][i] = m[i][j];
        return t;
    }

    std::vector<std::vector<double>> multiply(const std::vector<std::vector<double>>& a,
                                              const std::vector<std::vector<double>>& b) {
        size_t aRows = a.size(), aCols = a[0].size(), bCols = b[0].size();
        std::vector<std::vector<double>> result(aRows, std::vector<double>(bCols, 0));
        for (size_t i = 0; i < aRows; ++i)
            for (size_t j = 0; j < bCols; ++j)
                for (size_t k = 0; k < aCols; ++k)
                    result[i][j] += a[i][k] * b[k][j];
        return result;
    }

    std::vector<std::vector<double>> invert4x4(const std::vector<std::vector<double>>& m) {
        // Hardcoded 4x4 matrix inversion for simplicity (your case is always 4x4)
        // Can be replaced with a general-purpose matrix inversion if needed
        const int N = 4;
        std::vector<std::vector<double>> a = m;
        std::vector<std::vector<double>> inv(N, std::vector<double>(N, 0));
        for (int i = 0; i < N; ++i) inv[i][i] = 1.0;

        for (int i = 0; i < N; ++i) {
            double pivot = a[i][i];
            if (std::abs(pivot) < 1e-10)
                throw std::runtime_error("Singular matrix");

            for (int j = 0; j < N; ++j) {
                a[i][j] /= pivot;
                inv[i][j] /= pivot;
            }

            for (int k = 0; k < N; ++k) {
                if (k == i) continue;
                double factor = a[k][i];
                for (int j = 0; j < N; ++j) {
                    a[k][j] -= factor * a[i][j];
                    inv[k][j] -= factor * inv[i][j];
                }
            }
        }

        return inv;
    }

public:
    void add(double result, double x, double y, double a, double b) {
        data.push_back({x, y, a, b});
        results.push_back(result);
    }

    std::vector<double> compute() {
        size_t rowCount = data.size();
        const size_t colCount = 4;

        if (rowCount < colCount) {
            return {};
        }

        // A: rowCount x 4, b: rowCount x 1
        auto At = transpose(data);                          // 4 x rowCount
        auto AtA = multiply(At, data);                      // 4 x 4
        std::vector<std::vector<double>> b(rowCount, std::vector<double>(1));
        for (size_t i = 0; i < rowCount; ++i) {
            b[i][0] = results[i];
        }
        auto Atb = multiply(At, b);

        auto AtAinv = invert4x4(AtA);                       // 4 x 4
        auto x = multiply(AtAinv, Atb);                     // 4 x 1

        std::vector<double> result;
        result.reserve(4);
        for (int i = 0; i < 4; ++i) {
            result.push_back(x[i][0]);
        }

        return result;
    }
};

#endif //FACE_RECOGNITION_LINEARREGRESSION_H
