package com.cdio.solitaire.controller

import com.cdio.solitaire.model.Move
import com.cdio.solitaire.model.MoveType

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */

class StrategyController {
    val gsc = GameStateController()
    private val moves = arrayOf(
        Move(MoveType.DEAL_CARDS),
        Move(MoveType.DRAW_STOCK),
        Move(MoveType.MOVE_FROM_TALON)
    )
    var movesLeft = moves.size

    fun nextMove() {
        val move = decideMove()
        gsc.performMove(move)
    }

    fun decideMove(): Move {
        return Move(MoveType.FLIP_TALON)
    }


    fun discoverStock() {
        // Check that stock size + talon size is not 0 modulo 3
        // while (stock.hiddenCards + talon.hiddenCards > 0) flipTalon()
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

    fun playMove(): Move { // Used for testing purposes, should be replaced with
        // whatever method returns the next move to print.
        return moves[moves.size - movesLeft--]
    }

    fun isGameFinished(): Boolean { // Used for testing purposes.
        if (movesLeft == moves.size - 1) {
            println("GSC GAMESTATE:")
            for (i in gsc.gameState.tableaux.indices) {
                println(gsc.gameState.tableaux[i].tail!!.rank.short() + gsc.gameState.tableaux[i].tail!!.suit.short())
            }
        }
        return movesLeft == 0
    }
}
