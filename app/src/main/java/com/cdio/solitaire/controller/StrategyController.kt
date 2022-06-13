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
                if (column.getStackHighCard()!!.rank.ordinal == 12 && column.getStackHighCard()!!.suit.getColor() == Color.RED) {
                    return true
                }
            }
        } else {
            for (column in gsc.gameState.tableaux) {
                if (column.getStackHighCard()!!.rank.ordinal == 12 && column.getStackHighCard()!!.suit.getColor() == Color.BLACK) {
                    return true
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
        val moveQueue = MoveQueue(gsc.gameState)
        val talonCard = gsc.gameState.talon.tail!!
        for (column in gsc.gameState.tableaux) {
            val columnCard = column.tail!!
            if (talonCard.rank.ordinal + 1 == columnCard.rank.ordinal && talonCard.suit.offSuit(
                    columnCard.suit
                )
            ) {
                for (conditionalColumn in gsc.gameState.tableaux) {
                    val conditionalCard = conditionalColumn.getStackHighCard()!!
                    if (conditionalCard.rank.ordinal + 1 == talonCard.rank.ordinal && conditionalCard.suit.offSuit(
                            talonCard.suit
                        )
                    ) {
                        val move1 = Move(
                            MoveType.MOVE_FROM_TALON,
                            targetStack = column,
                            sourceCard = talonCard
                        )
                        val move2 =
                            Move(MoveType.MOVE_STACK, conditionalColumn, column, conditionalCard)
                        moveQueue.moveSequenceValue = 23 + conditionalColumn.hiddenCards
                        moveQueue.push(move1)
                        moveQueue.push(move2)
                        return moveQueue
                        //Move is possible
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
        val moveQueue: MoveQueue = MoveQueue(gsc.gameState)
        val moves: PriorityQueue<MoveQueue> = PriorityQueue<MoveQueue>(comparemoveQueue)
        for (column in gsc.gameState.tableaux) {
            //Possible moves from column to foundation.
            move = Move(MoveType.MOVE_TO_FOUNDATION, column, sourceCard = column.tail)
            if (gsc.isMoveLegal(move)) {
                if (checkFoundationPlusTwoRule(column.tail!!)) {
                    moveQueue.head = move
                    moveQueue.moveSequenceValue = 50
                    moves.add(moveQueue)
                } else {
                    if (column.hiddenCards < 0) {
                        moveQueue.head = move
                        moveQueue.moveSequenceValue = 1 + column.hiddenCards
                        moves.add(moveQueue)
                    }
                }
            }
            //Possible moves from Talon to foundation.
            move = Move(
                MoveType.MOVE_TO_FOUNDATION,
                gsc.gameState.talon,
                sourceCard = gsc.gameState.talon.tail
            )
            if (gsc.isMoveLegal(move)) {
                if (checkFoundationPlusTwoRule(gsc.gameState.talon.tail!!)) {
                    moveQueue.moveSequenceValue = 49
                    moveQueue.head = move
                    moves.add(moveQueue)
                }
            }
            //Possible moves from talon to a column.
            move = Move(
                MoveType.MOVE_FROM_TALON,
                targetStack = column,
                sourceCard = gsc.gameState.talon.tail
            )
            if (gsc.isMoveLegal(move)) {
                if (gsc.gameState.talon.tail!!.rank.ordinal == 13 && isQueenOppositeColorAvailable(
                        gsc.gameState.talon.tail!!
                    )
                ) {
                    moveQueue.moveSequenceValue = 43
                } else if ((gsc.gameState.talon.size + gsc.gameState.stock.size) % 3 == 0) {
                    moveQueue.moveSequenceValue = 10
                }
                moveQueue.head = move
                moves.add(moveQueue)
            }
            for (targetColumn in gsc.gameState.tableaux) {
                //Possible moves between columns
                move = Move(MoveType.MOVE_STACK, column, targetColumn, column.getStackHighCard())
                if (gsc.isMoveLegal(move)) {
                    //If the card is a king
                    if (column.getStackHighCard()!!.rank.ordinal == 13) {
                        if (isQueenOppositeColorAvailable(column.getStackHighCard()!!)) {
                            moveQueue.moveSequenceValue = 44
                        }
                    } else {
                        moveQueue.moveSequenceValue = 30 + 2 * column.hiddenCards //30-42
                    }
                    moveQueue.head = move
                    moves.add(moveQueue)
                }
            }
        }

        val conditionalMoveQueue = checkConditionalMoveFromTalon()
        if (conditionalMoveQueue != null) {
            moves.add(conditionalMoveQueue)
        }
        //Should make a check here to see if stock+talon is unchanged and no moves can be found.
        if (gsc.gameState.stock.size >= 3) {
            move = Move(MoveType.DRAW_STOCK)
            moveQueue.moveSequenceValue = 9
            moveQueue.head = move
            moves.add(moveQueue)
        } else {
            move = Move(MoveType.FLIP_TALON)
            moveQueue.moveSequenceValue = 9
            moveQueue.head = move
            moves.add(moveQueue)
        }
        return moves
    }
}
