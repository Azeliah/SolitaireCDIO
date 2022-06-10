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
    val listOfMoves: LinkedList<Array<Move>> = getAllMoves()
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
        var move: Move?
        val moves: LinkedList<Array<Move>> = LinkedList()
        for(column in gsc.gameState.tableaux){
                //Possible moves from column to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION, column, sourceCard=column.tail)
                if(gsc.isMoveLegal(move)){
                    moves.add(arrayOf(move))
                }
                //Possible moves from Talon to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION,gsc.gameState.talon,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)) {
                    moves.add(arrayOf(move))
                }
                //Possible moves from talon to a column.
                move = Move(MoveType.MOVE_FROM_TALON,targetStack = column,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)){
                    moves.add(arrayOf(move))
                }
            for(targetColumn in gsc.gameState.tableaux){
                //Possible moves between columns
                move = Move(MoveType.MOVE_STACK,column,targetColumn, column.getStackHighCard())
                if(gsc.isMoveLegal(move)){
                    moves.add(arrayOf(move))
                }
                //Possible Conditional moves, currently waiting for Michael with ghost/dummy state for checks.
                //TODO finish this part when Michael's code is ready.
                move = Move(MoveType.MOVE_FROM_TALON, sourceCard = gsc.gameState.talon.tail, targetStack = targetColumn)
                if(gsc.isMoveLegal(move)){
                    move = Move(MoveType.MOVE_STACK,)
                }
            }
        }
        return moves
    }
}
