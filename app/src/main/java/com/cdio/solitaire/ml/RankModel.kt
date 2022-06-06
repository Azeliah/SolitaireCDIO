package com.cdio.solitaire.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.cdio.solitaire.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.sql.Types.FLOAT


class RankModel {
    fun predict(bitmap: Bitmap, context: Context): Int {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfImage = TensorBuffer.createFrom(TensorImage.fromBitmap(newBitmap).tensorBuffer, DataType.FLOAT32)
        Log.d("tfImage shape", tfImage.buffer.toString())

        val model = Rank.newInstance(context)
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 25, 13, 3), DataType.FLOAT32)
        Log.d("inputFeature shape", inputFeature0.buffer.toString())
        inputFeature0.loadBuffer(tfImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        var maxIndex = -1
        var maxConfidence = 0.0.toFloat()

        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxIndex = i
            }
        }

        // TODO: Add confidence limits here.

        // Releases model resources if no longer used.
        model.close()
        return maxIndex // TODO: Change return type?
    }

    /**
     * @source https://stackoverflow.com/posts/10600736/revisions
     * @date_retrieved 6th of June 2022
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


    @SuppressLint("UseCompatLoadingForDrawables")
    fun test(context: Context) {
        val sixBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.six, context.theme))
        Log.d("Model prediction", predict(sixBitmap!!, context).toString())
        val aceBitmap = drawableToBitmap(context.resources.getDrawable(R.drawable.ace, context.theme))
        Log.d("Model prediction", predict(aceBitmap!!, context).toString())
    }
}