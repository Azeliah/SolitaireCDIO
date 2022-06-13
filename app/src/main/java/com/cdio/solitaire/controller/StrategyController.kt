package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*
import java.util.*
import kotlin.Comparator

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */

class StrategyController {
    val gsc = GameStateController()
    val compareMoveQueue: Comparator<MoveQueue> = compareBy{0-it.moveSequenceValue}
    val listOfMoves: PriorityQueue<MoveQueue> = getAllMoves(compareMoveQueue)
    fun nextMove() {
        val move = decideMove()
        gsc.performMove(move)
    }

    fun decideMove(): Move {
        //listOfMoves.poll()
        return Move(MoveType.FLIP_TALON)
    }


    fun discoverStock() {
        // Check that stock size + talon size is not 0 modulo 3
        // while (stock.hiddenCards + talon.hiddenCards > 0) flipTalon()
    }

    fun checkMoveToFoundation() {
        TODO("Analyze code from other repo for data manipulation/eval   uation.")
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
    fun checkFoundationPlusTwoRule(cardToCheck: Card):Boolean{
        if(cardToCheck.suit.getColor() == Color.BLACK){
            if(cardToCheck.rank.ordinal<gsc.getLowestRedFoundation()+2){
                return true
            }
        } else if(cardToCheck.suit.getColor() == Color.RED){
            if(cardToCheck.rank.ordinal<gsc.getLowestBlackFoundation()+2){
                return true
            }
        }
        return false
    }
    fun isQueenOppositeColorAvailable(king: Card): Boolean{
        if(king.suit.getColor()==Color.BLACK){
            for(column in gsc.gameState.tableaux){
                if(column.getStackHighCard()!!.rank.ordinal==12&&column.tail!!.suit.getColor()==Color.BLACK){
                    return true
                }
            }
        } else{
            for(column in gsc.gameState.tableaux){
                if(column.getStackHighCard()!!.rank.ordinal==12&&column.tail!!.suit.getColor()==Color.RED){
                    return true
                }
            }
        }
        return false
    }
    fun getAllMoves(comparemoveQueue: Comparator<MoveQueue>): PriorityQueue<MoveQueue>{
        var move: Move?
        val moveQueue: MoveQueue = MoveQueue(gsc.gameState)
        val moves: PriorityQueue<MoveQueue> = PriorityQueue<MoveQueue>(comparemoveQueue)
        for(column in gsc.gameState.tableaux){
                //Possible moves from column to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION, column, sourceCard=column.tail)
                if(gsc.isMoveLegal(move)){
                    if(checkFoundationPlusTwoRule(column.tail!!)){
                        moveQueue.head = move
                        moveQueue.moveSequenceValue = 50
                        moves.add(moveQueue)
                    } else{
                        if(column.hiddenCards<0){
                            moveQueue.head = move
                            moveQueue.moveSequenceValue = 1+column.hiddenCards
                            moves.add(moveQueue)
                        }
                    }
                }
                //Possible moves from Talon to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION,gsc.gameState.talon,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)) {
                    if(checkFoundationPlusTwoRule(gsc.gameState.talon.tail!!)){
                        moveQueue.moveSequenceValue = 49
                        moveQueue.head = move
                        moves.add(moveQueue)
                    }
                }
                //Possible moves from talon to a column.
                move = Move(MoveType.MOVE_FROM_TALON,targetStack = column,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)){
                    if(gsc.gameState.talon.tail!!.rank.ordinal==13&&isQueenOppositeColorAvailable(gsc.gameState.talon.tail!!)){
                        moveQueue.moveSequenceValue = 43
                    } else if(gsc.gameState.talon.size+gsc.gameState.stock.size%3==0){
                        moveQueue.moveSequenceValue = 10
                    }
                    moveQueue.head = move
                    moves.add(moveQueue)
                }
            for(targetColumn in gsc.gameState.tableaux){
                //Possible moves between columns
                move = Move(MoveType.MOVE_STACK,column,targetColumn, column.getStackHighCard())
                if(gsc.isMoveLegal(move)){
                    //If the card is a king
                    if(column.getStackHighCard()!!.rank.ordinal==13){
                        if(isQueenOppositeColorAvailable(column.getStackHighCard()!!)){
                            moveQueue.moveSequenceValue = 44
                        } else if(!isQueenOppositeColorAvailable(column.getStackHighCard()!!)){
                            moveQueue.moveSequenceValue = 8
                            }
                    } else{
                        moveQueue.moveSequenceValue = 30+2*column.hiddenCards //30-42
                    }
                    moveQueue.head = move
                    moves.add(moveQueue)
                }
                //Possible Conditional moves, currently waiting for Michael with ghost/dummy state for checks.
                //TODO finish this part when Michael's code is ready.
                val gameStateCopy = gsc.copyGameState()
                move = Move(MoveType.MOVE_FROM_TALON, sourceCard = gsc.gameState.talon.tail, targetStack = targetColumn)
                if(gsc.isMoveLegal(move)){
                    gsc.performMove(move)
                    move = Move(MoveType.MOVE_STACK,)
                }
            }
        }
        //Potentielle betingede moves.
        val gameStateCopy = gsc.copyGameState()
        for(column in gsc.gameState.tableaux){
            if(column.hiddenCards>0){
                move = Move(MoveType.MOVE_FROM_TALON,sourceCard = gameStateCopy.talon.tail,targetStack = column)
                if(gsc.isMoveLegal(move)){
                    //Now perform the move.
                    gsc.performMove(move)
                }
            }
        }
        //Should make a check here to see if stock+talon is unchanged and no moves can be found.
        if(gsc.gameState.stock.size>=3){
            move = Move(MoveType.DRAW_STOCK)
            moveQueue.moveSequenceValue = 9
            moveQueue.head = move
            moves.add(moveQueue)
        } else{
            move = Move(MoveType.FLIP_TALON)
            moveQueue.moveSequenceValue = 9
            moveQueue.head = move
            moves.add(moveQueue)
        }
        return moves
    }
}
