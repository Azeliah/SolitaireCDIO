package com.cdio.solitaire.ml

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class MLHelpers {

    private val minConfidence = 0.98

    /**
     * Used to calculated confidences.
     */
    private fun softMax(outputValues: FloatArray): Array<Float> {
        val exponentiatedValues = Array(outputValues.size) { i -> kotlin.math.exp(outputValues[i]) }
        var sum = 0.0.toFloat()
        for (float in exponentiatedValues)
            sum += float
        for (index in exponentiatedValues.indices)
            exponentiatedValues[index] = exponentiatedValues[index] / sum
        return exponentiatedValues
    }

    /**
     * Returns -1 if the confidence is NOT high enough
     * Else returns the index of the card.
     */
    fun getMaxIndex(tensorBufferOutput: TensorBuffer): Int {
        val confidences = softMax(tensorBufferOutput.floatArray)
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
}
