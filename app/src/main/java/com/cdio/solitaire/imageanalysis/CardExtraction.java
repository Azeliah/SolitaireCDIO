package com.cdio.solitaire.imageanalysis;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static java.lang.Double.max;
import static java.lang.Double.min;

public class CardExtraction {
    static {
        System.loadLibrary("opencv_java4");
    }

    public static Mat extractCard(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);

        Mat blur = new Mat();
        Imgproc.medianBlur(src, blur, 5);

        Mat noise = new Mat();
        Imgproc.bilateralFilter(blur, noise, 5, 35, 35);

        Mat gray = new Mat();
        Imgproc.cvtColor(noise, gray, Imgproc.COLOR_RGB2GRAY);
        double median = gray.get(0, 0)[0];
        double s = 0.33;
        double upper = min(255, (1.0 + s) * median);
        double lower = max(0, (1.0 - s) * median);

        Mat edge = new Mat();
        Imgproc.Canny(gray, edge, lower, upper);

        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println(points.size());

        if (points.size() == 0) {
            src.release();
            blur.release();
            noise.release();
            edge.release();
            contours.release();
            return null;
        }

        //Biggest contour in image
        int index = 0;
        double maxim = 0;
        double cardDimensions = 2.5 / 3.5;
        double tolerance = 0.12;
        for (int contourId = 0; contourId < points.size(); contourId++) {
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(contourId).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);
            double temp = rect.size.height * rect.size.width;
            double entryDimensions;

            if (rect.size.height > rect.size.width) {
                entryDimensions = rect.size.width / rect.size.height;
            } else {
                entryDimensions = rect.size.height / rect.size.width;
            }

            if (maxim < temp && (cardDimensions - entryDimensions < tolerance && cardDimensions - entryDimensions > -tolerance)) {
                maxim = temp;
                index = contourId;
            }
        }

        MatOfPoint2f cnt = new MatOfPoint2f();
        points.get(index).convertTo(cnt, CvType.CV_32FC2);
        RotatedRect rect = Imgproc.minAreaRect(cnt);

        Point[] vertices = new Point[4];
        rect.points(vertices);
        MatOfPoint newPoints = new MatOfPoint(vertices);
        Imgproc.drawContours(edge, Collections.singletonList(newPoints), -1, new Scalar(81, 190, 0), 4);

        double minAreaTolerance = 100000;
        boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

        if (valid) {
            MatOfPoint2f reference = new MatOfPoint2f(
                    new Point(0, 0),
                    new Point(57 * 4, 0),
                    new Point(57 * 4, 87 * 4),
                    new Point(0, 87 * 4)
            );

            Mat box = new Mat();
            Imgproc.boxPoints(rect, box);

            Mat warpMat = Imgproc.getPerspectiveTransform(box, reference);

            Mat warp = new Mat();
            Imgproc.warpPerspective(src, warp, warpMat, new Size(57 * 4, 87 * 4));

            if (rect.size.height > rect.size.width) {
                Imgproc.resize(warp, warp, new Size(87 * 4, 57 * 4));
                Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
            }

            Mat alphaLayer = new Mat();
            Imgproc.cvtColor(warp, alphaLayer, Imgproc.COLOR_BGR2BGRA);

            Mat icon = extractIcon(alphaLayer);
            Mat resize = resizeIcon(icon);

            box.release();
            warpMat.release();
            warp.release();
            alphaLayer.release();
            src.release();
            blur.release();
            noise.release();
            edge.release();
            contours.release();

            return resize;
        } else {
            System.out.println("Card was not valid!");
            System.out.println("Width: " + rect.size.width + ", height: " + rect.size.height);
            src.release();
            blur.release();
            noise.release();
            edge.release();
            contours.release();

            return null;
        }
    }

    public static Mat extractIcon(Mat src) {
        Mat original = src.clone();
        Rect rect_min = new Rect();
        rect_min.x = 5;
        rect_min.y = 5;
        rect_min.width = 30;
        rect_min.height = 90;
        return original.submat(rect_min);
    }

    public static Mat resizeIcon(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 10);
        Size sz = new Size(15, 45);
        Imgproc.resize(src, src, sz);
        return src;
    }

    public static Mat extractRank(Mat src) {
        Mat original = src.clone();
        Rect rect_min = new Rect();
        rect_min.x = 1;
        rect_min.y = 0;
        rect_min.width = 13;
        rect_min.height = 25;
        return original.submat(rect_min);
    }
}
