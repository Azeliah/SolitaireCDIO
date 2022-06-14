package com.cdio.solitaire.imageanalysis;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.util.ArrayUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class CardDataCreationModel {

    private final String TAG = "CardDataCreationModel";

    /** Method for extracting the icons of 14 cards and two card backside (16 cards).
     *  This method is used for extracting image icons for the ML dataset.
     *  Cards of a specific suit is laid out on a background of contrast
     *  in a 2x8 formation, from where the icon (suit and rank) from each
     *  card can be extracted.
     * */
    public Bitmap[] extractSuitIcons(Mat src) {
        // Convert to BGR colors
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);

        // Extract all 16 cards from the src image
        SolitaireAnalysisModel model = new SolitaireAnalysisModel();
        ContentNode[] game = model.extractCards(src, 16);

        // If all 16 cards are present and valid
        if (game != null) {
            // Sort ContentNode array by their y-axes position
            Arrays.sort(game, (n1, n2) -> (int) (n1.position.y - n2.position.y));

            // Split the array into an upper and a lower array and sort them by position along the x-axes
            ContentNode[] upper = Arrays.copyOfRange(game,0,8);
            Arrays.sort(upper, (n1, n2) -> (int) (n1.position.x - n2.position.x));
            ContentNode[] lower = Arrays.copyOfRange(game,8,16);
            Arrays.sort(lower, (n1, n2) -> (int) (n1.position.x - n2.position.x));

            // Concat the upper and lower arrays together again
            ContentNode[] SortedArr = ArrayUtils.concat(upper, lower);

            // Convert to array of BitMaps and release the Mat objects still in memory
            Bitmap[] bitmapArr = new Bitmap[16];
            for (int i = 0; i < 16; i++) {
                Bitmap bitmap = Bitmap.createBitmap(40, 100, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(SortedArr[i].content, bitmap);
                bitmapArr[i] = bitmap;
                SortedArr[i].content.release();
            }
            src.release();
            return bitmapArr;
        } else {
            src.release();
            Log.e(TAG,"No complete suit of cards was found!");
            return null;
        }
    }

}
