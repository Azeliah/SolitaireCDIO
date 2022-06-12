package com.cdio.solitaire.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer



class ModelPredictions {

    private val helpers = MLHelpers()

    /**
     * Returns -1 if the confidence is NOT high enough
     * Else returns the index of the card.
     */
    fun predictRank(bitmap: Bitmap, context: Context): Int {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfImage = TensorBuffer.createFrom(TensorImage.fromBitmap(newBitmap).tensorBuffer, DataType.FLOAT32)
        Log.d("tfImage shape", tfImage.buffer.toString())

        val model = Rank.newInstance(context)
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(25, 13, 3), DataType.FLOAT32)
        Log.d("inputFeature shape", inputFeature0.buffer.toString())
        inputFeature0.loadBuffer(tfImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        model.close()

        return helpers.getMaxIndex(outputFeature0)
    }

    /**
     * Returns -1 if the confidence is NOT high enough
     * Else returns the index of the card.
     */
    fun predictSuit(bitmap: Bitmap, context: Context) : Int{
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfImage = TensorBuffer.createFrom(TensorImage.fromBitmap(newBitmap).tensorBuffer, DataType.FLOAT32)
        Log.d("tfImage shape", tfImage.buffer.toString())

        // TODO: Change to suit model instance.
        // val model = Suit.newInstance(context)
        val model = Rank.newInstance(context)
        // Creates inputs for reference.
        // TODO: test intArrayOf is correct.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(21, 15, 3), DataType.FLOAT32)
        Log.d("inputFeature shape", inputFeature0.buffer.toString())
        inputFeature0.loadBuffer(tfImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        model.close()

        return helpers.getMaxIndex(outputFeature0)
    }
}