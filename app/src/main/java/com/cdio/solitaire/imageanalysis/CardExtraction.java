package com.cdio.solitaire.imageanalysis;

import android.media.Image;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class CardExtraction {
    public static void extractCard(ImageProxy src, String imageOutPath, String notValidOut) {
        System.loadLibrary("opencv_java4");

        Mat image = convertYUVtoMat(src);

        //File image = new File(imagePath);
        //Mat src = Imgcodecs.imread(imagePath);

        String imageName = "TestCard";

        Mat blur = new Mat();
        Imgproc.medianBlur(image, blur, 65);

        Mat noise = new Mat();
        Imgproc.bilateralFilter(blur, noise, 10, 75, 75);

        Mat edge = new Mat();
        Imgproc.Canny(noise, edge, 15, 35);

        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println(points.size());

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
            Imgproc.warpPerspective(image, warp, warpMat, new Size(57 * 4, 87 * 4));

            if (rect.size.height > rect.size.width) {
                Imgproc.resize(warp, warp, new Size(87 * 4, 57 * 4));
                Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
            }

            Mat alphaLayer = new Mat();
            Imgproc.cvtColor(warp, alphaLayer, Imgproc.COLOR_BGR2BGRA);

            Mat icon = extractIcon(alphaLayer);
            Mat resize = resizeIcon(icon);

            Imgcodecs.imwrite(imageOutPath + "card_edge_icon_" + imageName, resize);
            box.release();
            warpMat.release();
            warp.release();
            alphaLayer.release();
            icon.release();
            resize.release();
        } else {
            //System.out.println("Card " + imageName + " was not valid!");
            System.out.println("Width: " + rect.size.width + " ,height: " + rect.size.height);
            Imgcodecs.imwrite(notValidOut + imageName, image);
            Imgcodecs.imwrite(notValidOut + "NOT_VALID_" + imageName, edge);
        }
        image.release();
        blur.release();
        noise.release();
        edge.release();
        contours.release();
    }

    private static Mat convertYUVtoMat(@NonNull ImageProxy img) {
        byte[] nv21;

        ByteBuffer yBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = img.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat yuv = new Mat(img.getHeight() + img.getHeight() / 2, img.getWidth(), CvType.CV_8UC1);
        yuv.put(0, 0, nv21);
        Mat rgb = new Mat();
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3);
        Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE);
        return rgb;
    }

    public static Mat extractIcon(Mat src) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat original = src.clone();
        Rect rect_min = new Rect();
        rect_min.x = 5;
        rect_min.y = 5;
        rect_min.width = 30;
        rect_min.height = 90;
        return original.submat(rect_min);
    }

    public static Mat resizeIcon(Mat src) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 10);
        Size sz = new Size(15, 45);
        Imgproc.resize(src, src, sz);
        return src;
    }


}
