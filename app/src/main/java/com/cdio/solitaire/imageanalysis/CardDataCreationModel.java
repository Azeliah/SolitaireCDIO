package com.cdio.solitaire.imageanalysis;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static java.lang.Double.max;
import static java.lang.Double.min;

import android.graphics.Bitmap;

import com.google.android.gms.common.util.ArrayUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CardDataCreationModel {

    // Load in OpenCV library
    static {
        System.loadLibrary("opencv_java4");
    }

    // Method for extracting suit and rank from every card in a Solitaire game deck
    public Bitmap[] extractSolitaire(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);

        // Extracts a cropout of the Solitaire game in the source picture
        ContentNode[] game = extractCards(src);
        if (game != null) {
            Arrays.sort(game, (n1, n2) -> (int) (n1.position.y - n2.position.y));
            ContentNode[] upperColumns = Arrays.copyOfRange(game,0,7);
            Arrays.sort(upperColumns, (n1, n2) -> (int) (n1.position.x - n2.position.x));
            ContentNode[] lowerColumns = Arrays.copyOfRange(game,7,14);
            Arrays.sort(lowerColumns, (n1, n2) -> (int) (n1.position.x - n2.position.x));
            ContentNode[] SortedArr = ArrayUtils.concat(upperColumns, lowerColumns);

            // Convert to array of BitMap and release the Mat objects still in memory
            src.release();
            Bitmap[] bitmapArr = new Bitmap[14];
            for (int i = 0; i < 14; i++) {
                Bitmap bitmap = Bitmap.createBitmap(40, 100, Bitmap.Config.ARGB_8888);
                if (SortedArr[i] != null) {
                    Utils.matToBitmap(SortedArr[i].content, bitmap);
                    bitmapArr[i] = bitmap;
                    SortedArr[i].content.release();
                }
            }
            return bitmapArr;
        } else {
            src.release();
            System.out.println("No suitable game was found!");
            return null;
        }
    }


    // Method for extracting suit and rank from every card in a Solitaire game deck
    public void extractSolitaire(String imagePath, String imageOutPath) {

        // Load in OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat src = Imgcodecs.imread(imagePath);

        // Extracts a cropout of the Solitaire game in the source picture
        ContentNode[] game = extractCards(src);
        if (game != null) {
            Arrays.sort(game, (n1, n2) -> (int) (n1.position.y - n2.position.y));
            ContentNode[] upperColumns = Arrays.copyOfRange(game,0,7);
            Arrays.sort(upperColumns, (n1, n2) -> (int) (n1.position.x - n2.position.x));
            ContentNode[] lowerColumns = Arrays.copyOfRange(game,7,14);
            Arrays.sort(lowerColumns, (n1, n2) -> (int) (n1.position.x - n2.position.x));


            // Releases the Mat objects still in memory
            src.release();
            upperColumns[0].content.release();
            upperColumns[1].content.release();
            upperColumns[2].content.release();
            upperColumns[3].content.release();
            upperColumns[4].content.release();
            upperColumns[5].content.release();
            upperColumns[6].content.release();
            lowerColumns[0].content.release();
            lowerColumns[1].content.release();
            lowerColumns[2].content.release();
            lowerColumns[3].content.release();
            lowerColumns[4].content.release();
            lowerColumns[5].content.release();
            lowerColumns[6].content.release();
        } else {
            src.release();
            System.out.println("No suitable game was found!");
        }
    }

    public ContentNode[] extractCards(Mat src) {

        Mat edge = cannyEdge(src);

        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge,points,contours,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println(points.size());

        // If less than 10 points/contours where found
        if (points.size() < 14) {
            src.release();
            edge.release();
            contours.release();
            return null;
        }

        // Finds the 10 biggest contours in the image and stores them in a ContourNode array
        ContourNode[] nodeArr = new ContourNode[points.size()];
        for (int contourId = 0; contourId < points.size(); contourId++) {
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(contourId).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);
            double area = rect.size.height * rect.size.width;

            nodeArr[contourId] = new ContourNode(contourId,area,rect.center);
        }

        // Sorts array by area
        Arrays.sort(nodeArr, (n1, n2) -> (int) (n2.area - n1.area));

        //  Store talon and columns in an array of ContentNode nodes
        ContentNode[] matArr = new ContentNode[14];
        for (int i = 0; i < 14; i++) {

            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(nodeArr[i].index).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);

            Point[] vertices = new Point[4];
            rect.points(vertices);
            MatOfPoint newPoints = new MatOfPoint(vertices);
            Imgproc.drawContours(edge, Collections.singletonList(newPoints), -1, new Scalar(81, 190, 0), 4);

            double minAreaTolerance = 50000;
            boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

            if (valid) {
                int width = 57 * 4;
                int height = 87 * 4;

                MatOfPoint2f reference = new MatOfPoint2f(
                        new Point(0, 0),
                        new Point(width, 0),
                        new Point(width, height),
                        new Point(0, height)
                );

                Mat box = new Mat();
                Imgproc.boxPoints(rect, box);

                Mat warpMat = Imgproc.getPerspectiveTransform(box, reference);

                Mat warp = new Mat();
                Imgproc.warpPerspective(src, warp, warpMat, new Size(width, height));

                // Rotate if width is greater than height
                if (rect.size.height > rect.size.width) {
                    Imgproc.resize(warp, warp, new Size(height, width));
                    Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
                }

                Mat alphaLayer = new Mat();
                Imgproc.cvtColor(warp, alphaLayer, Imgproc.COLOR_BGR2BGRA);

                Mat icon = extractIcon(alphaLayer);
                Mat resize = resizeIcon(icon);

                matArr[i] = new ContentNode(resize,nodeArr[i].center);
                box.release();
                warpMat.release();
                warp.release();
                alphaLayer.release();
            } else {
                src.release();
                edge.release();
                contours.release();
                System.out.println("Card " + i + "was not valid!");
                System.out.println("Width: " + rect.size.width + " ,height: " + rect.size.height);
                return null;
            }
        }
        src.release();
        edge.release();
        contours.release();
        return matArr;
    }

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

    public static Mat extractIcon(Mat src) {
        Mat original = src.clone();
        Rect rect_min = new Rect();
        rect_min.x = 0;
        rect_min.y = 0;
        rect_min.width = 40;
        rect_min.height = 100;
        return original.submat(rect_min);
    }

    public static Mat resizeIcon(Mat src) {
        Imgproc.cvtColor(src,src,Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(src,src, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 9);
        Size sz = new Size(40,100);
        Imgproc.resize( src, src, sz );
        return src;
    }
}
