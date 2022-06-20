package com.cdio.solitaire.data

object CardData {
    val rankPredictions: Array<MutableList<Int>> = Array(8) { mutableListOf() }
    val suitPredictions: Array<MutableList<Int>> = Array(8) { mutableListOf() }
    val predictionOutput: MutableList<Array<Int>> = mutableListOf()

    fun mode(mutableList: MutableList<Int>): Int {
        var result = Int.MIN_VALUE
        var highCount = 0
        var countedElements = 0
        val elements: MutableList<Int> = mutableListOf()
        for (i in mutableList) {
            if (i !in elements)
                elements.add(i)
            val elementsCounted = mutableList.count { it == i }
            countedElements += elementsCounted
            if (elementsCounted > highCount) {
                result = i
                highCount = elementsCounted
            }
            if (countedElements == mutableList.size) break
        }
        return result
    }

    fun limitReached(): Boolean {
        return suitPredictions[0].size > 15 // just a limit we can adjust as we please.
    }

    fun isDataConsistent(): Boolean {
        for (i in suitPredictions.indices) {
            val mostPredictedSuit = mode(suitPredictions[i])
            val mostPredictedRank = mode(rankPredictions[i])
            val modeRatio = ((suitPredictions[i].count { it == mostPredictedSuit })
                    + (rankPredictions[i].count { it == mostPredictedRank })).toFloat() / (suitPredictions[i].size * 2).toDouble()
            if (modeRatio < 0.8) {
                reset()
                return false
            }
            predictionOutput.add(arrayOf(mostPredictedSuit, mostPredictedRank))
        }
        return true
    }

    fun reset() {
        for (i in rankPredictions.indices) {
            rankPredictions[i].clear()
            suitPredictions[i].clear()
            predictionOutput.clear()
        }
    }
}
