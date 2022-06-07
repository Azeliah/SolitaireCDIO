package com.cdio.solitaire.controller

class GameController {
    val gsc = GameStateController()

    fun flipTalon() {
        // While loop can be added to go through the entire talon.
        // Will need a few extra data features, though - hidden card counts.
        gsc.flipTalon()
    }

    fun drawFromStock() {
        TODO("Add data manipulation in GSC")
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