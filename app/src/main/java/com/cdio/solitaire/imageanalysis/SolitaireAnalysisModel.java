package com.cdio.solitaire.imageanalysis;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static java.lang.Double.max;
import static java.lang.Double.min;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolitaireAnalysisModel {

    private final String TAG = "SolitaireAnalysisModel";

    /**
     * Method for extracting suit and rank icon from every card in a Solitaire game image as Bitmaps
     */
    public Bitmap[] extractSolitaire(Mat src) {
        // Convert to BGR colors
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);

        // Extract all 8 cards of the Solitaire game image as and array of ContentNodes
        ContentNode[] game = extractCards(src, 8);

        // If Solitaire game image is valid
        if (game != null) {
            // Solitaire cards are sorted, first by position along the y-axes (talon), and then by position along the x-axes (Columns 1 to 7)
            Arrays.sort(game, (n1, n2) -> (int) (n1.position.y - n2.position.y));
            ContentNode talon = game[0];
            ContentNode[] columns = Arrays.copyOfRange(game, 1, 8);
            Arrays.sort(columns, (n1, n2) -> (int) (n1.position.x - n2.position.x));

            // Convert to array of BitMaps and release the Mat objects still in memory
            Bitmap[] bitmapArr = new Bitmap[8];
            for (int i = 0; i < 8; i++) {
                Bitmap bitmap = Bitmap.createBitmap(40, 100, Bitmap.Config.ARGB_8888);
                if (i == 0) {
                    Utils.matToBitmap(talon.content, bitmap);
                    bitmapArr[i] = bitmap;
                    talon.content.release();
                } else {
                    Utils.matToBitmap(columns[i - 1].content, bitmap);
                    bitmapArr[i] = bitmap;
                    columns[i - 1].content.release();
                }
            }
            src.release();
            return bitmapArr;
        } else {
            src.release();
            Log.e(TAG, "No suitable game was found!");
            return null;
        }
    }

    /**
     * Method for extracting a specific number of card suit and rank icons in an image
     * Inspiration: https://github.com/geaxgx/playing-card-detection/blob/master/creating_playing_cards_dataset.ipynb
     */
    public ContentNode[] extractCards(Mat src, int n) {

        // Detection of edges using Canny edge detection
        Mat edge = cannyEdge(src);

        // Arraylist of MatOfPoints found using findContours on external contours
        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // If less than n points/contours where found, return null
        if (points.size() < n) {
            src.release();
            edge.release();
            contours.release();
            return null;
        }

        // Find all contours in the image and store them in a ContourNode array
        // Inspiration: https://stackoverflow.com/questions/38759925/how-to-find-largest-contour-in-java-opencv
        ContourNode[] nodeArr = new ContourNode[points.size()];
        for (int contourId = 0; contourId < points.size(); contourId++) {
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(contourId).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);
            double area = rect.size.height * rect.size.width;

            nodeArr[contourId] = new ContourNode(contourId, area, rect.center);
        }

        // Sort ContourNode array by area, such that card contours come first
        Arrays.sort(nodeArr, (n1, n2) -> (int) n2.area - (int) n1.area);

        // Store image icons for the n first card contours (talon and columns) in an array of ContentNode nodes
        ContentNode[] matArr = new ContentNode[n];
        for (int i = 0; i < n; i++) {

            // Create rectangle using the index entry contour points
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(nodeArr[i].index).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);

            double minAreaTolerance = 50000;
            boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

            // If minimum tolerance is upheld
            if (valid) {
                int width = 57 * 4;
                int height = 87 * 4;

                // Reference points to fit output format
                // Inspiration: https://stackoverflow.com/questions/40688491/opencv-getperspectivetransform-and-warpperspective-java
                MatOfPoint2f reference = new MatOfPoint2f(
                        new Point(0, 0),
                        new Point(width, 0),
                        new Point(width, height),
                        new Point(0, height)
                );

                // Wrap source image to rectangle using reference points for size
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

                // Extract card icon containing suit and rank from the top left corner of the card, followed by a resize to 40x100 pixels
                Mat icon = extractIcon(warp);
                Mat resize = resizeIcon(icon);

                // Add ContentNode containing a card position and a Mat image of the icon crop to array
                matArr[i] = new ContentNode(resize, nodeArr[i].center);

                // Release Mat objects that are still in memory
                box.release();
                warpMat.release();
                warp.release();
            } else {
                src.release();
                edge.release();
                contours.release();
                Log.e(TAG, "Card " + i + " was not valid: " + "Width: " + rect.size.width + " ,height: " + rect.size.height);
                return null;
            }
        }

        // Release Mat objects and return ContentNode array
        src.release();
        edge.release();
        contours.release();
        return matArr;
    }

    /**
     * Method for detecting edges in an image using Canny edge detection
     */
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
        // Inspiration: https://stackoverflow.com/questions/41893029/opencv-canny-edge-detection-not-working-properly
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

    /**
     * Method for extracting card icon from upper left corner containing suit and rank
     */
    public Mat extractIcon(Mat src) {
        Mat original = src.clone();
        Rect rect = new Rect();
        rect.x = 0;
        rect.y = 0;
        rect.width = 40;
        rect.height = 100;
        return original.submat(rect);
    }

    /**
     * Method for converting card icon to binary colors and resizing to 40x100 pixels
     */
    public Mat resizeIcon(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(src, src, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 9);
        Size sz = new Size(40, 100);
        Imgproc.resize(src, src, sz);
        return src;
    }

    /**
     * Method for cropping an array og Bitmaps to a specific offset and size
     */
    public Bitmap[] cropIcon(Bitmap[] bitMapArr, int xOffset, int yOffset, int width, int height) {
        int length = bitMapArr.length;
        Bitmap[] newBitMap = new Bitmap[length];
        for (int i = 0; i < length; i++) {
            newBitMap[i] = Bitmap.createBitmap(bitMapArr[i], xOffset, yOffset, width, height);
        }
        return newBitMap;
    }
}
