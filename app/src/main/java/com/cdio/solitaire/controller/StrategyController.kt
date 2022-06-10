package com.cdio.solitaire.controller

import com.cdio.solitaire.model.Move
import com.cdio.solitaire.model.MoveType
import java.util.*

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */

class StrategyController {
    val gsc = GameStateController()

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

    fun getAllMoves(): LinkedList<Array<Move>>{
        var moveArray: Array<Move>?
        var move: Move?
        val moves: LinkedList<Array<Move>> = LinkedList()
        for(sourceColumn in gsc.gameState.tableaux){
            for(targetFoundation in gsc.gameState.foundations){
                move = Move(MoveType.MOVE_TO_FOUNDATION, sourceColumn, sourceCard=sourceColumn.tail)
                if(gsc.isMoveLegal(move)){
                    moves.add(arrayOf(move))
                }
            }
            for(targetColumn in gsc.gameState.tableaux){
                move = Move(MoveType.MOVE_STACK,sourceColumn,targetColumn, sourceColumn.getStackHighCard())
                if(gsc.isMoveLegal(move)){
                    moves.add(arrayOf(move))
                }
                move = Move(MoveType.MOVE_FROM_TALON, sourceCard = gsc.gameState.talon.tail, targetStack = targetColumn)
                if(gsc.isMoveLegal(move)){
                    move = Move(MoveType.MOVE_STACK,)
                }
            }
        }
        return moves
    }
}
