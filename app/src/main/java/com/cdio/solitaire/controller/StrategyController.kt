package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*
import java.lang.Exception
import java.util.*
import kotlin.Comparator

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */
class StrategyController {
    val gsc = GameStateController()
    private val compareMoveQueue: Comparator<MoveQueue> = compareBy { 0 - it.moveSequenceValue }
    private lateinit var listOfMoves: PriorityQueue<MoveQueue>
    private var currentMoveQueue: MoveQueue? = null
    var gameIsWon = false
    private var stockDiscovered = false
    private var talonCounter: Int = 0

    fun nextMove(): Move {

        val move = decideMove()
        //if (move.sourceCard != null && move.targetStack != null) {
        //    println("Moving Card: " + move.sourceCard!!.toStringDanish() + " to " + move.targetStack!!.tail!!.toStringDanish() + " with the movetype: " + move.moveType)
        //} else {
        //}
        println(move.toStringDanish() + move.moveType)
        if (move.moveType == MoveType.DRAW_STOCK) {
            println("Modolus check " + (gsc.gameState.talon.size + gsc.gameState.stock.size) % 3)
        }
        gsc.performMove(move)
        return move
    }

    fun decideMove(): Move {
        stockDiscovered =
            gsc.gameState.stock.checkHiddenCards() + gsc.gameState.talon.checkHiddenCards() == 0
        var stockMove: Move? = null

        if (!stockDiscovered) {
            if ((gsc.gameState.stock.size + gsc.gameState.talon.size) % 3 != 0)
                return discoverStock()
        }

        var cardsInFoundation = 0
        for (foundation in gsc.gameState.foundations) {
            cardsInFoundation += foundation.size
        }
        println("Currently " + cardsInFoundation + " cards in foundation.")
        if (cardsInFoundation == 51) {
            gameIsWon = true
            return Move(MoveType.GAME_WON)
        }

        if (currentMoveQueue == null || currentMoveQueue!!.size == 0) {
            val temp = getAllMoves(compareMoveQueue)
            currentMoveQueue = MoveQueue(gsc.gameState)
            val tempMove = getAllMoves(compareMoveQueue).poll()?.pop()
            if (tempMove != null) {
                currentMoveQueue!!.push(tempMove)
            }
        }

        if (currentMoveQueue!!.size > 0) {
            //listOfMoves.clear()
            listOfMoves = getAllMoves(compareMoveQueue)
            currentMoveQueue = listOfMoves.poll()
            //listOfMoves.clear()
            return currentMoveQueue!!.pop()
        } else {
            return Move(MoveType.GAME_LOST)
        }
    }

    fun playMove(): Move? { // Used for testing purposes, should be replaced with
        // whatever method returns the next move to print.
        return decideMove()
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
     */
    private fun discoverStock(): Move {
        println("discoverStock called")

        val stock = gsc.gameState.stock
        val talon = gsc.gameState.talon

        // Try to draw from stock
        val move = Move(MoveType.DRAW_STOCK)
        if (gsc.isMoveLegal(move))
            return move

        // Nothing else to do; flip talon
        return Move(MoveType.FLIP_TALON)
    }

    /**
     * This function checks if a card is maximum 2 higher than the opposite foundation.
     * @return Boolean is true if a card follows the +2 Rule, else false.
     */
    private fun checkFoundationPlusTwoRule(cardToCheck: Card): Boolean {
        if (cardToCheck.suit.getColor() == Color.BLACK) {
            if (cardToCheck.rank.ordinal <= gsc.getLowestRedFoundation() + 2) {
                return true
            }
        } else if (cardToCheck.suit.getColor() == Color.RED) {
            if (cardToCheck.rank.ordinal <= gsc.getLowestBlackFoundation() + 2) {
                return true
            }
        }
        return false
    }

    /**
     * This function checks if there is a Queen in play of the opposite color of a specific King.
     * @return Boolean is true if there is a queen of opposite color that is able to move on top of the king.
     */
    private fun isQueenOppositeColorAvailable(king: Card): Boolean {
        if (king.suit.getColor() == Color.BLACK) {
            for (column in gsc.gameState.tableaux) {
                if (column.size != 0) {
                    if (column.size != 0 && column.getStackHighCard()!!.rank.ordinal == 12 && column.getStackHighCard()!!.suit.getColor() == Color.RED) {
                        return true
                    }
                }
            }
        } else {
            for (column in gsc.gameState.tableaux) {
                if (column.size != 0) {
                    if (column.getStackHighCard()!!.rank.ordinal == 12 && column.getStackHighCard()!!.suit.getColor() == Color.BLACK) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     *checks if there is a conditional move from talon.
     * @return MoveQueue , returns the first instance of a movequeue with 2 moves in the case of a conditional move, else returns null.
     */
    private fun checkConditionalMoveFromTalon(): MoveQueue? {
        if (gsc.gameState.talon.tail != null) {
            val moveQueue = MoveQueue(gsc.gameState)
            val talonCard = gsc.gameState.talon.tail!!
            for (column in gsc.gameState.tableaux) {
                if (column.size == 0) continue
                val columnCard = column.tail!!
                if (talonCard.rank.ordinal + 1 == columnCard.rank.ordinal && talonCard.suit.offSuit(
                        columnCard.suit
                    )
                ) {
                    for (conditionalColumn in gsc.gameState.tableaux) {
                        if (conditionalColumn.size == 0) continue
                        val conditionalCard = conditionalColumn.getStackHighCard()!!
                        if (conditionalCard.rank.ordinal + 1 == talonCard.rank.ordinal && conditionalCard.suit.offSuit(
                                talonCard.suit
                            )
                        ) {
                            val move1 = Move(
                                MoveType.MOVE_FROM_TALON,
                                targetStack = column,
                                sourceCard = talonCard,
                                sourceStack = gsc.gameState.talon
                            )
                            val move2 =
                                Move(
                                    MoveType.MOVE_STACK,
                                    conditionalColumn,
                                    column,
                                    conditionalCard
                                )
                            moveQueue.moveSequenceValue = 23 + conditionalColumn.checkHiddenCards()
                            moveQueue.push(move1)
                            moveQueue.push(move2)
                            return moveQueue
                            //Move is possible
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     *This puts a sequence of moves into a MoveQueue, and then into a PriorityQueue and compares them by moveSequenceValue.
     * Note that it is possible for a MoveQueue to only have one element.
     */
    private fun getAllMoves(comparemoveQueue: Comparator<MoveQueue>): PriorityQueue<MoveQueue> {
        var move: Move?
        var moveQueue: MoveQueue = MoveQueue(gsc.gameState)
        val moves: PriorityQueue<MoveQueue> = PriorityQueue<MoveQueue>(comparemoveQueue)
        for (column in gsc.gameState.tableaux) {
            //Possible moves from column to foundation.
            if (column.size != 0) {
                move = Move(
                    MoveType.MOVE_TO_FOUNDATION,
                    sourceStack = column,
                    sourceCard = column.tail
                )
                moveQueue = MoveQueue(gsc.gameState)
                if (gsc.isMoveLegal(move)) {

                    if (checkFoundationPlusTwoRule(column.tail!!)) {
                        moveQueue.moveSequenceValue = 50
                        moveQueue.push(move)
                        moves.add(moveQueue)
                        //moveQueue.clearMoveQueue()
                    } else {
                        if (column.checkHiddenCards() > 0) {
                            moveQueue.moveSequenceValue = 1 + column.checkHiddenCards()
                            moveQueue.push(move)
                            moves.add(moveQueue)
                            //moveQueue.clearMoveQueue()
                        }
                    }
                }
            }
            //Possible moves from Talon to foundation.
            move = Move(
                MoveType.MOVE_TO_FOUNDATION,
                gsc.gameState.talon,
                sourceCard = gsc.gameState.talon.tail
            )
            moveQueue = MoveQueue(gsc.gameState)
            if (gsc.gameState.talon.size != 0 && gsc.isMoveLegal(move)) {
                if (checkFoundationPlusTwoRule(gsc.gameState.talon.tail!!)) {
                    moveQueue.moveSequenceValue = 49
                    moveQueue.push(move)
                    moves.add(moveQueue)
                    //moveQueue.clearMoveQueue()
                }
            }
            //Possible moves from talon to a column.

            moveQueue = MoveQueue(gsc.gameState)
            move = Move(
                MoveType.MOVE_FROM_TALON,
                targetStack = column,
                sourceCard = gsc.gameState.talon.tail,
                sourceStack = gsc.gameState.talon
            )
            if (gsc.gameState.talon.size != 0 && gsc.isMoveLegal(move)) {
                if (gsc.gameState.talon.tail!!.rank.ordinal == 13 && isQueenOppositeColorAvailable(
                        gsc.gameState.talon.tail!!
                    )
                ) {
                    moveQueue.moveSequenceValue = 43
                    moveQueue.push(move)
                    moves.add(moveQueue)
                } else if ((gsc.gameState.talon.size + gsc.gameState.stock.size) % 3 == 0) {
                    moveQueue.moveSequenceValue = 10
                    moveQueue.push(move)
                    moves.add(moveQueue)
                } else {
                    moveQueue.moveSequenceValue = 7
                    moveQueue.push(move)
                    moves.add(moveQueue)
                }
                //moveQueue.clearMoveQueue()
            }
            for (targetColumn in gsc.gameState.tableaux) {
                //Possible moves between columns
                if (column.size != 0 && column.checkHiddenCards() > 0 || column.size == 1 && column.getStackHighCard()!!.rank != Rank.KING) {
                    move =
                        Move(MoveType.MOVE_STACK, column, targetColumn, column.getStackHighCard())
                    moveQueue = MoveQueue(gsc.gameState)
                    if (gsc.isMoveLegal(move)) {
                        //If the card is a king
                        if (column.getStackHighCard()!!.rank.ordinal == 13) {
                            if (isQueenOppositeColorAvailable(column.getStackHighCard()!!)) {
                                moveQueue.moveSequenceValue = 44
                                moveQueue.push(move)
                                moves.add(moveQueue)
                            }
                        } else if (column.getStackHighCard()!!.rank.ordinal != 13) {
                            moveQueue.moveSequenceValue = 30 + 2 * column.hiddenCards //30-42
                            moveQueue.push(move)
                            moves.add(moveQueue)
                        }
                        //moveQueue.clearMoveQueue()
                    }
                }
            }
        }


        val conditionalMoveQueue = checkConditionalMoveFromTalon()
        if (conditionalMoveQueue != null) {
            moves.add(conditionalMoveQueue)
        }
        //Should make a check here to see if stock+talon is unchanged and no moves can be found.
        if (gsc.gameState.stock.size > 3) {
            move = Move(MoveType.DRAW_STOCK)
            moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
            //moveQueue.clearMoveQueue()
        } else if((gsc.gameState.stock.size+gsc.gameState.talon.size)>3) {
            move = Move(MoveType.FLIP_TALON)
            moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
            //moveQueue.clearMoveQueue()
        }
        if (gsc.gameState.stock.size + gsc.gameState.talon.size == 3 && gsc.gameState.talon.size < 3&&gsc.gameState.stock.size!=3) {
            println("Doing FLIP->STOCK_DRAW  stockSize: "+gsc.gameState.stock.size+" talonSize: "+gsc.gameState.talon.size)
            val move1 = Move(
                MoveType.FLIP_TALON
            )
            val move2 = Move(
                MoveType.DRAW_STOCK
            )
            moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 60
            moveQueue.push(move1)
            moveQueue.push(move2)
            moves.add(moveQueue)
        } else if (gsc.gameState.stock.size == 3 && gsc.gameState.talon.size == 0) {
            println("Doing STOCK_DRAW  stockSize: "+gsc.gameState.stock.size+" talonSize: "+gsc.gameState.talon.size)
            val move = Move(MoveType.DRAW_STOCK)
            moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 60
            moveQueue.push(move)
            moves.add(moveQueue)
        }

//if (moves.size == 0)
//    throw Exception("No possible moves found!")
        return moves
    }


//fun isGameFinished(): Boolean { // Used for testing purposes, should be replaced with something else.
//    return endCondition
//}
}
