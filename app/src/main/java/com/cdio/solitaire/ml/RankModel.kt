package com.cdio.solitaire.ml

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class RankModel {
    fun predict(byteBuffer: ByteBuffer, context: Context) : Int {
        val model = Rank.newInstance(context)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 24, 15, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        var maxIndex = -1
        var maxConfidence = 0.0.toFloat()

        for (i in confidences.indices){
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
}