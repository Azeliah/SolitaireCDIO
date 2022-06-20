package com.cdio.solitaire.data

import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.Rank
import com.cdio.solitaire.model.Suit

object CardData {
    val rankPredictions: Array<MutableList<Int>> = Array(8) { mutableListOf() }
    val suitPredictions: Array<MutableList<Int>> = Array(8) { mutableListOf() }
    private val predictionOutput: MutableList<Array<Int>> = mutableListOf()

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
                    + (rankPredictions[i].count { it == mostPredictedRank })).toDouble() / (suitPredictions[i].size * 2).toDouble()
            if (modeRatio < 0.8) {
                reset()
                return false
            }
            predictionOutput.add(arrayOf(mostPredictedSuit, mostPredictedRank))
        }
        updateCards()
        return true
    }

    private fun updateCards() {
        val gameStateController = StrategyController.gsc
        for (i in predictionOutput.indices) {
            val cardStack = gameStateController.getCardStackFromID(i)
            if (cardStack.tail != null) {
                if (cardStack.tail!!.rank == Rank.NA) { // Only assign values to new cards.
                    cardStack.tail!!.suit = Suit.values()[predictionOutput[i][0]]
                    cardStack.tail!!.rank = Rank.values()[predictionOutput[i][1]]
                }
            }
        }
    }

    private fun reset() {
        for (i in rankPredictions.indices) {
            rankPredictions[i].clear()
            suitPredictions[i].clear()
        }
        predictionOutput.clear()
    }
}
