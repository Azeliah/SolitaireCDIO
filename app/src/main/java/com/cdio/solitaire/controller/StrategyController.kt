package com.cdio.solitaire.controller

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */

class StrategyController {
    val gsc = GameStateController()
    fun discoverStock() {
        // Should be used to reveal the entire stock pile to the system.
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