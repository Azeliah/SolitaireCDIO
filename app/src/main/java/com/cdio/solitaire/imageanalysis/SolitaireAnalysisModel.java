package com.cdio.solitaire.imageanalysis;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static java.lang.Double.max;
import static java.lang.Double.min;

import android.graphics.Bitmap;

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
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SolitaireAnalysisModel {

    // Load in OpenCV library
    static {
        System.loadLibrary("opencv_java4");
    }

    // Method for extracting suit and rank from every card in a Solitaire game deck
    public Bitmap[] extractSolitaire(Mat src) {

        // Extracts a cropout of the Solitaire game in the source picture
        Mat game = extractGame(src);
        if (game != null) {
            // Extracts content in form of talon and columns of the Solitaire game as an array of ContentNodes
            ContentNode[] content = extractContent(game);
            if (content != null && content.length == 8) {

                // Extracts the suit and rank of the talon card
                Mat talon = extractCard(content[0].content);

                // Copies the indexes 1 to 7 into a column array and sorts it in order of position along the x-axis
                ContentNode[] column = Arrays.copyOfRange(content,1,8);
                Arrays.sort(column, (n1, n2) -> (int) (n1.position.x - n2.position.x));

                // Extracts suit and rank of the most forward card in each of the columns 1 to 7
                Mat[] columns = new Mat[7];
                for (int i = 0; i < 7; i++) {
                    columns[i] = extractCard(column[i].content);
                }

                // Convert to array of BitMap and release the Mat objects still in memory
                src.release();
                game.release();
                Bitmap[] bitmapArr = new Bitmap[8];
                for (int i = 0; i < 8; i++) {
                    if (columns[i] != null) {
                        Bitmap bitmap = Bitmap.createBitmap(30, 90, Bitmap.Config.ARGB_8888);
                        if (i == 0) {
                            Utils.matToBitmap(talon, bitmap);
                            bitmapArr[i] = bitmap;
                            talon.release();
                        } else {
                            Utils.matToBitmap(columns[i-1], bitmap);
                            bitmapArr[i] = bitmap;
                            columns[i-1].release();
                        }
                    }
                    bitmapArr[i] = null;
                }
                return bitmapArr;
            } else {
                src.release();
                game.release();
                System.out.println("Something was wrong with the game content!");
                return null;
            }
        } else {
            src.release();
            System.out.println("No suitable game was found!");
            return null;
        }
    }

    // Method for extracting a cropout of a Solitaire game
    private Mat extractGame(Mat src) {

        // Detection of edges using Canny edge detection
        Mat edge = cannyEdge(src);

        // Arraylist of MatOfPoints found using findContours on external contours
        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("Game contours: " + points.size());

        // If no points/contours where found
        if (points.size() == 0) {
            src.release();
            edge.release();
            contours.release();
            return null;
        }

        //Finds the biggest contour in the image
        int index = 0;
        double maxim = 0;
        for (int contourId = 0; contourId < points.size(); contourId++)
        {
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(contourId).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);
            double area = rect.size.height * rect.size.width;

            if(maxim < area)
            {
                maxim=area;
                index=contourId;
            }
        }

        // Create rectangle using the biggest contour points
        MatOfPoint2f cnt = new MatOfPoint2f();
        points.get(index).convertTo(cnt, CvType.CV_32FC2);
        RotatedRect rect = Imgproc.minAreaRect(cnt);

        // Contour is drawn onto the image
        Point[] vertices = new Point[4];
        rect.points(vertices);
        MatOfPoint newPoints = new MatOfPoint(vertices);
        Imgproc.drawContours(edge, Collections.singletonList(newPoints), -1, new Scalar(81, 190, 0), 4);

        double minAreaTolerance = 100000;
        boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

        // If minimum tolerance is upheld
        if (valid) {

            // Manuel reshape of height and width to fit output format
            rect.size.set(new double[]{rect.size.width + 30, rect.size.height + 30});
            int height = (int) rect.size.width;
            int width = (int) rect.size.height;

            // Reference points to fit output format
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
            if (rect.size.height < rect.size.width) {
                Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
            }

            // Release Mat objects still in memory
            src.release();
            edge.release();
            contours.release();
            box.release();
            warpMat.release();
            return warp;
        } else {

            src.release();
            edge.release();
            contours.release();
            return null;
        }
    }

    // Method for extracting content in form of talon and columns of the Solitaire game
    private ContentNode[] extractContent(Mat src) {

        // Detection of edges using Canny edge detection
        Mat edge = cannyEdge(src);

        // Dilates the edges using MORPH_CROSS to enhance the edge thickness
        Imgproc.dilate(edge, edge, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2 * 4 + 1, 2 * 4 + 1), new Point(4, 4)));

        // Arraylist of MatOfPoints found using findContours on external contours
        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_LIST , Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("Content contours: " + points.size());

        // If less than 8 points/contours where found
        if (points.size() < 8) {
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
        ContentNode[] matArr = new ContentNode[8];
        for (int i = 0; i < 8; i++) {

            // Create rectangle using the biggest contour points
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(nodeArr[i+1].index).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);

            // Contour is drawn onto the image
            Point[] vertices = new Point[4];
            rect.points(vertices);
            MatOfPoint newPoints = new MatOfPoint(vertices);
            Imgproc.drawContours(edge, Collections.singletonList(newPoints), -1, new Scalar(81, 190, 0), 4);

            double minAreaTolerance = 100000;
            boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

            // If minimum tolerance is upheld
            if (valid) {

                // Manuel reshape of height and width to fit output format
                rect.size.set(new double[]{rect.size.width - 30, rect.size.height - 30});
                int height = (int) rect.size.width;
                int width = (int) rect.size.height;

                // Reference points to fit output format
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
                    Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
                }

                // Release Mat objects still in memory and add content to array
                box.release();
                warpMat.release();
                matArr[i] = new ContentNode(warp,nodeArr[i+1].center);
            } else {

                src.release();
                edge.release();
                contours.release();
                return null;
            }
        }

        // Release Mat objects still in memory and return array
        src.release();
        edge.release();
        contours.release();
        return matArr;
    }

    // Method for extracting card suit and rank in an image
    private Mat extractCard(Mat src) {

        // Detection of edges using Canny edge detection
        Mat edge = cannyEdge(src);

        // Arraylist of MatOfPoints found using findContours on external contours
        List<MatOfPoint> points = new ArrayList<>();
        Mat contours = new Mat();
        Imgproc.findContours(edge, points, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("Column contours: " + points.size());

        // If no points/contours where found
        if (points.size() == 0) {
            src.release();
            edge.release();
            contours.release();
            return null;
        }

        // Finds the biggest contour in the image within specific angle and area limits
        double maxim = 0;
        int index = 0;
        for (int contourId = 0; contourId < points.size(); contourId++) {
            MatOfPoint2f cnt = new MatOfPoint2f();
            points.get(contourId).convertTo(cnt, CvType.CV_32FC2);
            RotatedRect rect = Imgproc.minAreaRect(cnt);
            double area = rect.size.height * rect.size.width;

            if (maxim < area && (rect.angle > 80 && rect.angle < 110) && area < ((src.width()*src.height())/100f)*90f) {
                maxim = area;
                index = contourId;
            }
        }

        // Create rectangle using the biggest contour points
        MatOfPoint2f cnt = new MatOfPoint2f();
        points.get(index).convertTo(cnt, CvType.CV_32FC2);
        RotatedRect rect = Imgproc.minAreaRect(cnt);

        // Contour is drawn onto the image
        Point[] vertices = new Point[4];
        rect.points(vertices);
        MatOfPoint newPoints = new MatOfPoint(vertices);
        Imgproc.drawContours(edge, Collections.singletonList(newPoints), -1, new Scalar(81, 190, 0), 4);

        double minAreaTolerance = 50000;
        boolean valid = rect.size.height * rect.size.width > minAreaTolerance;

        // If minimum tolerance is upheld
        if (valid) {

            // Adapt height or width to a fixed size depending on which is highest. The both sides are adjusted accordingly.
            int width;
            int height;
            if (rect.size.height > rect.size.width) {
                height = 57 * 4;
                width = (int) (((57 * 4) / rect.size.width) * rect.size.height);
            } else {
                height = (int) (((57 * 4) / rect.size.height) * rect.size.width);
                width = 57 * 4;
            }

            // Reference points to fit output format
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
                Core.rotate(warp, warp, Core.ROTATE_90_COUNTERCLOCKWISE);
            }

            // Get alpha layer
            Mat alphaLayer = new Mat();
            Imgproc.cvtColor(warp, alphaLayer, Imgproc.COLOR_BGR2BGRA);

            // Extraction of card icon containing suit and rank from the bottom of a card, followed by a resize and 180-degree rotation
            Mat icon = extractIcon(alphaLayer);
            Mat resize = resizeIcon(icon);
            Core.rotate(resize, resize, Core.ROTATE_180);

            // Release Mat objects that are still in memory and return resized icon
            src.release();
            edge.release();
            contours.release();
            box.release();
            warpMat.release();
            warp.release();
            alphaLayer.release();

            return resize;
        } else {
            System.out.println("Card was not valid!");
            System.out.println("Width: " + rect.size.width + " ,height: " + rect.size.height);
            src.release();
            edge.release();
            contours.release();
            return null;
        }
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

    // Method for extracting card icon from bottom right corner containing suit and rank
    private Mat extractIcon(Mat src) {
        Mat original = src.clone();
        Rect rect_min = new Rect();
        rect_min.x = original.width() - 35;
        rect_min.y = original.height() - 90;
        rect_min.width = 30;
        rect_min.height = 90;
        return original.submat(rect_min);
    }

    // Method for converting card icon to binary colors and resizing to 30x90 pixels
    private Mat resizeIcon(Mat src) {
        Imgproc.cvtColor(src,src,Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(src,src, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 9);
        Size sz = new Size(30,90);
        Imgproc.resize( src, src, sz );
        return src;
    }
}
