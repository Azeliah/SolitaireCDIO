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
    private var currentMoveQueue: MoveQueue? = null;
    var gameIsWon = false

    fun nextMove(): Move {

        val move = decideMove()
        println(move)

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


    private fun decideMove(): Move {
        if (gsc.isGameWon())
            return Move(MoveType.GAME_WON)

        if ((gsc.gameState.talon.hiddenCards() + gsc.gameState.stock.hiddenCards()) > 0 && currentMoveQueue != null && currentMoveQueue!!.size <= 0) {
            if ((gsc.gameState.talon.size + gsc.gameState.stock.size) % 3 != 0)
                return discoverStock()
        }

        if (currentMoveQueue == null || currentMoveQueue!!.isEmpty()) {
            val moves = getAllMoves()
            if (moves.isEmpty()) {
                return Move(MoveType.GAME_LOST)
            }
            print("Generating new moves: ")
            for (foo in moves)
                print("(size: ${foo.size}, value: ${foo.moveSequenceValue}) ")
            println()

            currentMoveQueue = moves.poll()
        }

        return currentMoveQueue!!.pop()
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
        // println("discoverStock called")

        // val stock = gsc.gameState.stock
        // val talon = gsc.gameState.talon

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
     * gets the total of empty columns plus columns where a king is bottom card in the stack ( No hidden cards).
     * @return Int , amount of empty columns+king columns.
     */
    private fun getEmptyColumnsPlusColumnsWithKings(): Int {
        var emptyColumnAndKingCounter = 0
        for (column in gsc.gameState.tableaux) {
            if (column.size == 0) {
                emptyColumnAndKingCounter++
            } else if (column.hiddenCards() == 0 && column.getStackHighCard()!!.rank.ordinal == 13) {
                emptyColumnAndKingCounter++
            }
        }
        //Er det større eller lig 4.
        return emptyColumnAndKingCounter
    }

    /**
     * Gets the total of kings without its own column, I.E a King that has hidden cards under itself.
     * @return Int , amount of kings with hidden cards.
     */
    private fun getKingsWithHiddenCardsInPlay(): Int {
        var kingsWithHiddenCardsInPlay = 0
        for (column in gsc.gameState.tableaux) {
            if (column.size >1) {
                if (column.getStackHighCard()!!.rank == Rank.KING) {
                    kingsWithHiddenCardsInPlay++
                }
            }
        }

        return kingsWithHiddenCardsInPlay
    }

    /**
     *checks if there is a conditional move from talon.
     * @return MoveQueue , returns the first instance of a movequeue with 2 moves in the case of a conditional move, else returns null.
     */
    private fun checkConditionalMoveFromTalon(): MoveQueue? {
        if (gsc.gameState.talon.tail != null) {
            //val moveQueue = MoveQueue(gsc.gameState)
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
                        if (conditionalColumn.size == 1 && getKingsWithHiddenCardsInPlay() > 0) {
                            if (conditionalColumn.size != 1 && conditionalCard.rank.ordinal + 1 == talonCard.rank.ordinal && conditionalCard.suit.offSuit(
                                    talonCard.suit
                                )
                                || (conditionalColumn.size == 1 && getKingsWithHiddenCardsInPlay() > 0) && conditionalCard.rank.ordinal + 1 == talonCard.rank.ordinal && conditionalCard.suit.offSuit(
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
                                val moveQueue = MoveQueue(gsc.gameState)
                                moveQueue.moveSequenceValue = 23 + conditionalColumn.hiddenCards()
                                moveQueue.push(move1)
                                moveQueue.push(move2)
                                return moveQueue
                                //Move is possible
                            }
                        }
                    }
                }
            }

        }
        return null
    }

    fun getAGoodMoveQueueFromTalonAndStock() {
        var foundAGoodMoveInTalon = false
        var distanceFromHeadTalon = 0
        var foundGoodMoveInStock = false
        var distanceFromHeadStock = 0
        //If Stock and Talon are completely discovered.
        if (gsc.gameState.talon.head != null) {
            var tempTalonCard = gsc.gameState.talon.head
            while (tempTalonCard != null) {
                var foundation = gsc.getFoundation(tempTalonCard.suit)
                if (foundation.size == 0 && tempTalonCard.rank == Rank.ACE) {
                    foundAGoodMoveInTalon = true
                    break
                    //TempTalon to Foundation is now a good move.
                } else if (foundation.size != 0 && foundation.tail!!.rank.ordinal == tempTalonCard.rank.ordinal - 1) {
                    foundAGoodMoveInTalon = true
                    break
                    //TempTalon to Foundation is now a good move.
                }
                tempTalonCard = tempTalonCard.next
                distanceFromHeadTalon++
            }
        }
        if (!foundAGoodMoveInTalon) {
            if (gsc.gameState.stock.head != null) {
                var tempStockCard = gsc.gameState.talon.head
                while (tempStockCard != null) {
                    var foundation = gsc.getFoundation(tempStockCard.suit)
                    if (foundation.size == 0 && tempStockCard.rank == Rank.ACE) {
                        foundGoodMoveInStock = true
                        break
                    } else if (foundation.size != 0 && foundation.tail!!.rank.ordinal == tempStockCard.rank.ordinal - 1) {
                        foundGoodMoveInStock = true
                        break
                    }
                    tempStockCard = tempStockCard.next
                    distanceFromHeadStock++
                }
            }
        }
    }

    /**
     *This puts a sequence of moves into a MoveQueue, and then into a PriorityQueue and compares them by moveSequenceValue.
     * Note that it is possible for a MoveQueue to only have one element.
     */
    private fun getAllMoves(): PriorityQueue<MoveQueue> {
        //var move: Move?
        //var moveQueue: MoveQueue
        val moves: PriorityQueue<MoveQueue> = PriorityQueue<MoveQueue>(compareMoveQueue)
        //Possible moves from Talon to foundation.
        val move = Move(
            MoveType.MOVE_TO_FOUNDATION,
            gsc.gameState.talon,
            sourceCard = gsc.gameState.talon.tail
        )
        if (gsc.gameState.talon.size != 0 && gsc.isMoveLegal(move)) {
            if (checkFoundationPlusTwoRule(gsc.gameState.talon.tail!!)) {
                val moveQueue = MoveQueue(gsc.gameState)
                moveQueue.moveSequenceValue = 49
                moveQueue.push(move)
                moves.add(moveQueue)

            } else {
                val moveQueue = MoveQueue(gsc.gameState)
                moveQueue.moveSequenceValue = 12
                moveQueue.push(move)
                moves.add(moveQueue)

            }
        }
        for (column in gsc.gameState.tableaux) {
            //Possible moves from column to foundation.
            if (column.size != 0) {
                val move = Move(
                    MoveType.MOVE_TO_FOUNDATION,
                    sourceStack = column,
                    sourceCard = column.tail
                )
                if (gsc.isMoveLegal(move)) {
                    if (checkFoundationPlusTwoRule(column.tail!!)) {
                        val moveQueue = MoveQueue(gsc.gameState)
                        moveQueue.moveSequenceValue = 50
                        moveQueue.push(move)
                        moves.add(moveQueue)
                        //moveQueue.clearMoveQueue()
                    } else {
                        if (column.hiddenCards() > 0) {
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 6 + column.hiddenCards()
                            moveQueue.push(move)
                            moves.add(moveQueue)
                            //moveQueue.clearMoveQueue()
                        }
                    }
                }
            }
            //Possible moves from talon to a column.
            val move = Move(
                MoveType.MOVE_FROM_TALON,
                targetStack = column,
                sourceCard = gsc.gameState.talon.tail,
                sourceStack = gsc.gameState.talon
            )
            if (gsc.gameState.talon.size != 0 && gsc.isMoveLegal(move)) {
                if (gsc.gameState.talon.tail!!.rank.ordinal == 13 && isQueenOppositeColorAvailable(
                        gsc.gameState.talon.tail!!
                    ) || (gsc.gameState.talon.tail!!.rank.ordinal == 13 && getEmptyColumnsPlusColumnsWithKings() >= 4)
                ) {
                    val moveQueue = MoveQueue(gsc.gameState)
                    moveQueue.moveSequenceValue = 43
                    moveQueue.push(move)
                    moves.add(moveQueue)
                } else if (gsc.gameState.talon.tail!!.rank != Rank.KING) { //(gsc.gameState.talon.size + gsc.gameState.stock.size) % 3 == 0
                    val moveQueue = MoveQueue(gsc.gameState)
                    moveQueue.moveSequenceValue = 12
                    moveQueue.push(move)
                    moves.add(moveQueue)
                } else {
                    //It is a king
                    val moveQueue = MoveQueue(gsc.gameState)
                    moveQueue.moveSequenceValue = 5
                    moveQueue.push(move)
                    moves.add(moveQueue)
                }
                //moveQueue.clearMoveQueue()
            }
            for (targetColumn in gsc.gameState.tableaux) {
                //Possible moves between columns
                if ((column.size != 0 && column.hiddenCards() > 0) || (column.size == 1 && column.getStackHighCard()!!.rank != Rank.KING)) {
                    val move =
                        Move(
                            MoveType.MOVE_STACK,
                            column,
                            targetColumn,
                            column.getStackHighCard()
                        )
                    if (gsc.isMoveLegal(move)) {
                        //If the card is a king
                        if (column.getStackHighCard()!!.rank == Rank.KING && isQueenOppositeColorAvailable(
                                column.getStackHighCard()!!
                            ) ||
                            column.getStackHighCard()!!.rank == Rank.KING && getEmptyColumnsPlusColumnsWithKings() >= 4
                        ) {
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 44
                            moveQueue.push(move)
                            moves.add(moveQueue)
                        } else if (column.getStackHighCard()!!.rank != Rank.KING) { // We do not want to move a king here.
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 30 + 2 * column.hiddenCards() //30-42
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
        if (gsc.gameState.stock.size >= 3) {
            val moveQueue = MoveQueue(gsc.gameState)
            val move = Move(MoveType.DRAW_STOCK)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
            //moveQueue.clearMoveQueue()
        } else if ((gsc.gameState.stock.size < 3 && (gsc.gameState.stock.size + gsc.gameState.talon.size) > 3)) {
            val moveQueue = MoveQueue(gsc.gameState)
            val move = Move(MoveType.FLIP_TALON)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
            //moveQueue.clearMoveQueue()
        }
        if (gsc.gameState.stock.size + gsc.gameState.talon.size == 3 && gsc.gameState.talon.size < 3 && gsc.gameState.stock.size != 3) {
            println("Doing FLIP->STOCK_DRAW  stockSize: " + gsc.gameState.stock.size + " talonSize: " + gsc.gameState.talon.size)
            val move1 = Move(
                MoveType.FLIP_TALON
            )
            val move2 = Move(
                MoveType.DRAW_STOCK
            )
            val moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 60
            moveQueue.push(move1)
            moveQueue.push(move2)
            moves.add(moveQueue)
        } else if (gsc.gameState.stock.size == 3 && gsc.gameState.talon.size == 0) {
            println("Doing STOCK_DRAW  stockSize: " + gsc.gameState.stock.size + " talonSize: " + gsc.gameState.talon.size)
            val move = Move(MoveType.DRAW_STOCK)
            val moveQueue = MoveQueue(gsc.gameState)
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
