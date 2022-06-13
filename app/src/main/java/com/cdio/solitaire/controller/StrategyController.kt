package com.cdio.solitaire.controller

import com.cdio.solitaire.model.Move
import com.cdio.solitaire.model.MoveQueue
import com.cdio.solitaire.model.MoveType
import java.util.*
import kotlin.Comparator

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */
class StrategyController {
    val gsc = GameStateController()
    val compareMoveQueue: Comparator<MoveQueue> = compareBy { 0 - it.moveSequenceValue }
    val listOfMoves: PriorityQueue<MoveQueue> = getAllMoves(compareMoveQueue)
    fun nextMove() {
        val move = decideMove()
        gsc.performMove(move)
    }

    fun decideMove(): Move {

        //listOfMoves.poll()
        return Move(MoveType.FLIP_TALON)
    }

    /**
     * Function for discovering all cards in the stock.
     * The idea is that it can be called in `getAllMoves` to check if it should be done before
     * anything else.
     *
     * The function returns a move that, if performed, may help discover more cards in the stock.
     * In order to discover new cards, we attempt to remove a card from talon, if the number of
     * cards in stock and talon is divisible by 3.
     *
     * @return Move necessary for discovering more of stock.
     *         If null, all cards in stock/talon has been discovered.
     */
    private fun discoverStock(): Move? {

        val stock = gsc.gameState.stock
        val talon = gsc.gameState.talon

        if (stock.hiddenCards + talon.hiddenCards == 0)
            return null

        if ((stock.size + talon.size) % 3 == 0 && talon.size > 0) {

            // Try to move card from talon
            for (targetColumn in gsc.gameState.tableaux) {
                val move = Move(MoveType.MOVE_FROM_TALON, talon, targetColumn, talon.tail)

                if (gsc.isMoveLegal(move))
                    return move;
            }
        }

        // Try to draw from stock
        val move = Move(MoveType.DRAW_STOCK)
        if (gsc.isMoveLegal(move))
            return move

        // Nothing else to do; flip talon
        // TODO: What to do if stock and talon does not contain enough cards to flip talon?
        return Move(MoveType.FLIP_TALON)
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

    fun getAllMoves(comparemoveQueue: Comparator<MoveQueue>): PriorityQueue<MoveQueue>{
        var move: Move?
        val moveQueue: MoveQueue = MoveQueue(gsc.gameState)
        val moves: PriorityQueue<MoveQueue> = PriorityQueue<MoveQueue>(comparemoveQueue)
        for(column in gsc.gameState.tableaux){
                //Possible moves from column to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION, column, sourceCard=column.tail)
                if(gsc.isMoveLegal(move)){

                }
                //Possible moves from Talon to foundation.
                move = Move(MoveType.MOVE_TO_FOUNDATION,gsc.gameState.talon,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)) {
                    moveQueue.head = move
                    moves.add(moveQueue)
                }
                //Possible moves from talon to a column. If drawpile %3!=0
                move = Move(MoveType.MOVE_FROM_TALON,targetStack = column,sourceCard = gsc.gameState.talon.tail)
                if(gsc.isMoveLegal(move)){
                    moveQueue.head = move
                    moves.add(moveQueue)

                }
            for(targetColumn in gsc.gameState.tableaux){
                //Possible moves between columns
                move = Move(MoveType.MOVE_STACK,column,targetColumn, column.getStackHighCard())
                if(gsc.isMoveLegal(move)){
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
        return moves
    }
}
