package com.cdio.solitaire.controller

import android.util.Log
import com.cdio.solitaire.model.GameState

class GameController {
    val gsc = GameStateController()

    fun discoverStock() {
        // Should be used to reveal the entire stock pile to the system.
    }

    fun flipTalon() {
        gsc.flipTalon()
    }

    fun drawFromStock() {
        val gameState = gsc.getCurrentGameState()
        if (gameState.stock.size < 3) {
            Log.e("EmptyStackPop","Not enough cards in stock to draw to talon.")
            return
        }
        gsc.moveStack(gameState.stock, 3, gameState.talon)
    }

    fun checkMoveToFoundation() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

    fun checkMoveToTableau() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

    fun moveFoundationTableau() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

    fun moveKing() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

    fun moveFoundationTalon() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

    fun moveStack() {
        TODO("Analyze code from other repo for data manipulation/evaluation.")
    }

}