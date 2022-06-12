package com.cdio.solitaire.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.cdio.solitaire.R
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MLHelpers{

    private val models = ModelPredictions()
    // TODO: The min confidence should be at least 90-95,
    //  however its 85 currently so it can be used for test, since the model is not fine tuned yet.
    private val minConfidence = 85

    /**
     * Returns -1 if the confidence is NOT high enough
     * Else returns the index of the card.
     */
    fun getMaxIndex(tensorBufferOutput: TensorBuffer) : Int{
        val confidences = tensorBufferOutput.floatArray
        var maxIndex = -1
        var maxConfidence = 0.0.toFloat()

        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxIndex = i
            }
        }

        if (maxConfidence < minConfidence) {
            return -1
        }

        return maxIndex
    }

    /**
     * @source https://stackoverflow.com/posts/10600736/revisions
     * @date_retrieved 6th of June 2022
     * Used for testing (takes a jpg. from the drawable folder and converts to bitmap)
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Testing for prediction
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun testRankModel(context: Context) {
        val sixBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.six, context.theme))
        Log.d("Model prediction for rank", models.predictRank(sixBitmap!!, context).toString())
        val aceBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.ace, context.theme))
        Log.d("Model prediction for rank", models.predictRank(aceBitmap!!, context).toString())
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun testSuitModel(context: Context) {
        val sixBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.six, context.theme))
        Log.d("Model prediction for suit", models.predictSuit(sixBitmap!!, context).toString())
        val aceBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.ace, context.theme))
        Log.d("Model prediction for suit", models.predictSuit(aceBitmap!!, context).toString())
    }
}