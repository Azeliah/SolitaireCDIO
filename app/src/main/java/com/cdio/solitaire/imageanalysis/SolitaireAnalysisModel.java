package com.cdio.solitaire.imageanalysis;

import static java.lang.Double.max;
import static java.lang.Double.min;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class SolitaireAnalysisModel {

    // Method for detecting edges in an image using Canny edge detection
    private Mat cannyEdge(Mat src) {

        // Blur image to remove small details and imperfections
        Mat blur = new Mat();
        Imgproc.medianBlur(src, blur, 5);

        // Add bilateral filer to straighten edges/shapes
        Mat bilateral = new Mat();
        Imgproc.bilateralFilter(blur, bilateral, 5, 35, 35);

        // Convert image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(bilateral, gray, Imgproc.COLOR_RGB2GRAY);

        // Upper and lower thresholds using median of grayscale image
        double median = gray.get(0, 0)[0];
        double s = 0.33;
        double upper = min(255, (1.0 + s) * median);
        double lower = max(0, (1.0 - s) * median);

        // Edge detection using Canny edge detection with upper and lower thresholds
        Mat edge = new Mat();
        Imgproc.Canny(gray, edge, lower, upper);

        // Release Mat objects still in memory and return detected edges
        blur.release();
        bilateral.release();
        gray.release();

        return edge;
    }
}
