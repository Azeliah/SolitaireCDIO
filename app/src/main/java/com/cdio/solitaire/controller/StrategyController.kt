package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*
import java.lang.Exception
import java.util.*
import kotlin.Comparator

/*
 * StrategyController is used to handle the logic behind the strategy, which is then sent to
 * GameStateController as a Move using performMove, where GSC will then update the data accordingly.
 */
object StrategyController {
    val gsc = GameStateController()
    private val compareMoveQueue: Comparator<MoveQueue> = compareBy { 0 - it.moveSequenceValue }
    private var currentMoveQueue: MoveQueue? = null
    var gameIsWon = false

    fun nextMove(): Move {

        val move = decideMove()
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
        return emptyColumnAndKingCounter
    }

    /**
     * Gets the total of kings without its own column, I.E a King that has hidden cards under itself.
     * @return Int , amount of kings with hidden cards.
     */
    private fun getKingsWithHiddenCardsInPlay(): Int {
        var kingsWithHiddenCardsInPlay = 0
        for (column in gsc.gameState.tableaux) {
            if (column.size > 1) {
                if (column.getStackHighCard()!!.rank == Rank.KING) {
                    kingsWithHiddenCardsInPlay++
                }
            }
        }
        return kingsWithHiddenCardsInPlay
    }

    /**
     *checks if there is a conditional move from talon.
     * @return MoveQueue , returns the first instance of a MoveQueue with 2 moves in the case of a conditional move, else returns null.
     */

    private fun getConditionalMoveQueueFromTalonCard(
        talonCard: Card
    ): MoveQueue? {
        if (gsc.gameState.talon.tail != null) {
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
                                moveQueue.moveSequenceValue =
                                    23 + conditionalColumn.hiddenCards()
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
     * Gets a list of cards in reach, from stock and talon. List is ascending order based on moves to reach the card.
     */
    private fun getListOfCardsInReach(gsc: GameStateController): LinkedList<Card> {
        val listOfCardsInReach: LinkedList<Card> = LinkedList()
        val copyOfGameState = gsc.copyGameState()
        val gscCopy = GameStateController()
        gscCopy.gameState = copyOfGameState
        if ((gscCopy.gameState.talon.size + gscCopy.gameState.stock.size) > 3) { //If Stock and talon is not bigger than three, cannot shift through.
            if (gscCopy.gameState.talon.tail != null) {
                listOfCardsInReach.add(gscCopy.gameState.talon.tail!!)
            }

            var newCardsInReach = true
            while (newCardsInReach) {
                if (gscCopy.gameState.stock.size >= 3) {
                    gscCopy.performMove(Move(MoveType.DRAW_STOCK))
                    if (listOfCardsInReach.contains(gscCopy.gameState.talon.tail!!)) {
                        newCardsInReach = false
                    } else {
                        listOfCardsInReach.add(gscCopy.gameState.talon.tail!!)
                    }
                } else {
                    gscCopy.performMove(Move(MoveType.FLIP_TALON))
                }
            }
        }
        return listOfCardsInReach
    }

    /**
     * Gets a good MoveQueue from talon/stock if one exists. If not, returns null.
     */
    fun getAGoodMoveQueueFromTalonAndStock(): MoveQueue? {
        val listOfCardsInReach = getListOfCardsInReach(gsc)
        val moveQueue = MoveQueue(gsc.gameState)
        val copyOfGameState = gsc.copyGameState()
        val gscCopy = GameStateController()
        val cardNotFound = true
        gscCopy.gameState = copyOfGameState
        /*

        if (modolusThreeOfTalonAndStock() == 1) {
            if (listOfCardsInReach.size > 0) { //modolusThreeOfTalonAndStock() != 1 &&
                //Conditional MoveQueue. Talon->Column, Column->Column. Reveal 1 card.
                for (talonCard in listOfCardsInReach) {
                    val conditionalMoveQueue =
                        getConditionalMoveQueueFromTalonCard(talonCard)
                    //Is There a conditional Move from talon?
                    if (conditionalMoveQueue != null) {
                        while (cardNotFound) {
                            if (gscCopy.gameState.stock.size >= 3) {
                                val move = Move(MoveType.DRAW_STOCK)
                                gscCopy.performMove(move)
                                moveQueue.push(move)
                                if (gscCopy.gameState.talon.tail != null && gscCopy.gameState.talon.tail!!.rank == talonCard.rank && gscCopy.gameState.talon.tail!!.suit == talonCard.suit) {
                                    //cardNotFound = false
                                    repeat(2) { moveQueue.push(conditionalMoveQueue.pop()) }
                                    moveQueue.moveSequenceValue = 25
                                    return moveQueue
                                }
                            } else {
                                gscCopy.performMove(Move(MoveType.FLIP_TALON))
                                val move = Move(MoveType.FLIP_TALON)
                                moveQueue.push(move)
                            }
                        }
                    }
                }
                //Moves from talon to foundation.
                for (talonCard in listOfCardsInReach) {
                    val foundation = gsc.getFoundation(talonCard.suit)
                    if ((foundation.size == 0 && talonCard.rank == Rank.ACE) ||
                        foundation.size != 0 && foundation.tail!!.rank.ordinal == talonCard.rank.ordinal - 1 && checkFoundationPlusTwoRule(
                            talonCard
                        )
                    ) {
                        while (cardNotFound) {
                            if (gscCopy.gameState.stock.size >= 3) {
                                val move = Move(MoveType.DRAW_STOCK)
                                gscCopy.performMove(move)
                                moveQueue.push(move)
                                if (gscCopy.gameState.talon.tail != null && gscCopy.gameState.talon.tail!!.rank == talonCard.rank && gscCopy.gameState.talon.tail!!.suit == talonCard.suit) {
                                    //cardNotFound = false
                                    val tempGameState = gsc.copyGameState()
                                    val tempgsc = GameStateController()
                                    tempgsc.gameState = tempGameState
                                    val move = Move(
                                        MoveType.MOVE_TO_FOUNDATION,
                                        sourceStack = gsc.gameState.talon,
                                        sourceCard = talonCard,
                                        targetStack = gsc.getFoundation(talonCard.suit)
                                    )
                                    moveQueue.moveSequenceValue = 20
                                    moveQueue.push(move)
                                    var tempMove = moveQueue.head
                                    tempgsc.gameState = tempGameState
                                    while (tempMove != null) {
                                        if (tempMove.moveType == MoveType.MOVE_TO_FOUNDATION) {
                                            val tempMove = Move(
                                                MoveType.MOVE_TO_FOUNDATION,
                                                sourceStack = tempgsc.gameState.talon,
                                                sourceCard = talonCard,
                                                targetStack = tempgsc.getFoundation(talonCard.suit)
                                            )
                                            tempgsc.performMove(tempMove)
                                            tempMove.next = null
                                        } else {
                                            tempgsc.performMove(tempMove)
                                        }
                                        tempMove = tempMove.next
                                    }
                                    //Now tempGameState should be updated with the desired move.
                                    //Need to check if there is a possible move from talon.
                                    val listOfCardsInReachInTempGameState =
                                        getListOfCardsInReach(tempgsc)
                                    for (talonCard in listOfCardsInReachInTempGameState) {
                                        cardNotFound = true
                                        while (cardNotFound) {
                                            if (tempgsc.gameState.stock.size >= 3) {
                                                val move = Move(MoveType.DRAW_STOCK)
                                                tempgsc.performMove(move)
                                                if (tempgsc.gameState.talon.tail != null && tempgsc.gameState.talon.tail!!.rank == talonCard.rank && tempgsc.gameState.talon.tail!!.suit == talonCard.suit) {
                                                    cardNotFound = false
                                                    for (column in tempgsc.gameState.tableaux) {
                                                        val move = Move(
                                                            MoveType.MOVE_FROM_TALON,
                                                            targetStack = column,
                                                            sourceCard = tempgsc.gameState.talon.tail,
                                                            sourceStack = tempgsc.gameState.talon
                                                        )
                                                        if (tempgsc.isMoveLegal(move)) {
                                                            return moveQueue
                                                        }
                                                    }
                                                }
                                            } else {
                                                tempgsc.performMove(Move(MoveType.FLIP_TALON))
                                                val move = Move(MoveType.FLIP_TALON)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

            }

            return null
        } else {
        */
            if (listOfCardsInReach.size > 0) { //modolusThreeOfTalonAndStock() != 1 &&
                //Conditional MoveQueue. Talon->Column, Column->Column. Reveal 1 card.
                for (talonCard in listOfCardsInReach) {
                    val conditionalMoveQueue =
                        getConditionalMoveQueueFromTalonCard(talonCard)
                    //Is There a conditional Move from talon?
                    if (conditionalMoveQueue != null) {
                        while (cardNotFound) {
                            if (gscCopy.gameState.stock.size >= 3) {
                                val move = Move(MoveType.DRAW_STOCK)
                                gscCopy.performMove(move)
                                moveQueue.push(move)
                                if (gscCopy.gameState.talon.tail != null && gscCopy.gameState.talon.tail!!.rank == talonCard.rank && gscCopy.gameState.talon.tail!!.suit == talonCard.suit) {
                                    //cardNotFound = false
                                    repeat(2) { moveQueue.push(conditionalMoveQueue.pop()) }
                                    moveQueue.moveSequenceValue = 25
                                    return moveQueue
                                }
                            } else {
                                gscCopy.performMove(Move(MoveType.FLIP_TALON))
                                val move = Move(MoveType.FLIP_TALON)
                                moveQueue.push(move)
                            }
                        }
                    }
                }
                //Moves from talon to foundation.
                for (talonCard in listOfCardsInReach) {
                    val foundation = gsc.getFoundation(talonCard.suit)
                    if ((foundation.size == 0 && talonCard.rank == Rank.ACE) ||
                        foundation.size != 0 && foundation.tail!!.rank.ordinal == talonCard.rank.ordinal - 1 && checkFoundationPlusTwoRule(
                            talonCard
                        )
                    ) {
                        while (cardNotFound) {
                            if (gscCopy.gameState.stock.size >= 3) {
                                val move = Move(MoveType.DRAW_STOCK)
                                gscCopy.performMove(move)
                                moveQueue.push(move)
                                if (gscCopy.gameState.talon.tail != null && gscCopy.gameState.talon.tail!!.rank == talonCard.rank && gscCopy.gameState.talon.tail!!.suit == talonCard.suit) {
                                    //cardNotFound = false
                                    val move1 = Move(
                                        MoveType.MOVE_TO_FOUNDATION,
                                        sourceStack = gsc.gameState.talon,
                                        sourceCard = talonCard
                                    )
                                    moveQueue.moveSequenceValue = 20
                                    moveQueue.push(move1)
                                    return moveQueue
                                }
                            } else {
                                gscCopy.performMove(Move(MoveType.FLIP_TALON))
                                val move = Move(MoveType.FLIP_TALON)
                                moveQueue.push(move)
                            }
                        }
                    }
                }
            }
//        }
        return null
    }


    /**
     *This puts a sequence of moves into a MoveQueue, and then into a PriorityQueue and compares them by moveSequenceValue.
     * Note that it is possible for a MoveQueue to only have one element.
     */
    private fun getAllMoves(): PriorityQueue<MoveQueue> {
        //var move: Move?
        //var moveQueue: MoveQueue
        val moves: PriorityQueue<MoveQueue> =
            PriorityQueue<MoveQueue>(compareMoveQueue)
        //Possible moves from Talon to foundation.

        if (gsc.gameState.talon.size != 0) {
            val move = Move(
                MoveType.MOVE_TO_FOUNDATION,
                gsc.gameState.talon,
                sourceCard = gsc.gameState.talon.tail,
                targetStack = gsc.getFoundation(gsc.gameState.talon.tail!!.suit)
            )

            if (gsc.isMoveLegal(move)&&checkFoundationPlusTwoRule(gsc.gameState.talon.tail!!)) {
                val moveQueue = MoveQueue(gsc.gameState)
                moveQueue.moveSequenceValue = 49
                moveQueue.push(move)
                moves.add(moveQueue)
            } else if (gsc.isMoveLegal(move)&&(gsc.gameState.stock.size+gsc.gameState.talon.size)%3==0) {
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
                    } else {
                        if (column.hiddenCards() > 0) {
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 6 + column.hiddenCards()
                            moveQueue.push(move)
                            moves.add(moveQueue)
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
            }
            for (targetColumn in gsc.gameState.tableaux) {
                //Possible moves between columns
                if ((column.size != 0 && column.hiddenCards() > 0) || (column.size == 1 && column.getStackHighCard()!!.rank != Rank.KING)) {
                    val move1 =
                        Move(
                            MoveType.MOVE_STACK,
                            column,
                            targetColumn,
                            column.getStackHighCard()
                        )
                    if (gsc.isMoveLegal(move1)) {
                        //If the card is a king
                        if (column.getStackHighCard()!!.rank == Rank.KING && isQueenOppositeColorAvailable(
                                column.getStackHighCard()!!
                            ) ||
                            column.getStackHighCard()!!.rank == Rank.KING && getEmptyColumnsPlusColumnsWithKings() >= 4
                        ) {
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 44
                            moveQueue.push(move1)
                            moves.add(moveQueue)
                        } else if (column.getStackHighCard()!!.rank == Rank.KING && column.size > 1) {
                            //If a King Move can reveal a card, give it value based on hidden cards underneath.
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue = 20 + column.hiddenCards()
                            moveQueue.push(move1)
                            moves.add(moveQueue)
                        } else if (column.getStackHighCard()!!.rank != Rank.KING) { // We do not want to move a king here.
                            val moveQueue = MoveQueue(gsc.gameState)
                            moveQueue.moveSequenceValue =
                                30 + 2 * column.hiddenCards() //30-42
                            moveQueue.push(move1)
                            moves.add(moveQueue)
                        }
                    }
                }
            }
        }
        if (gsc.gameState.talon.tail != null) {
            val conditionalMoveQueue =
                getConditionalMoveQueueFromTalonCard(gsc.gameState.talon.tail!!)
            if (conditionalMoveQueue != null) {
                moves.add(conditionalMoveQueue)
            }
        }
//        if ((gsc.gameState.talon.hiddenCards() + gsc.gameState.stock.hiddenCards()) == 0) {
//            val goodTalonMoveQueue = getAGoodMoveQueueFromTalonAndStock()
//            if (goodTalonMoveQueue != null) {
//                moves.add(goodTalonMoveQueue)
//            }
//        }
        //Should make a check here to see if stock+talon is unchanged and no moves can be found.
        if (gsc.gameState.stock.size >= 3) {
            val moveQueue = MoveQueue(gsc.gameState)
            val move = Move(MoveType.DRAW_STOCK)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
        } else if ((gsc.gameState.stock.size < 3 && (gsc.gameState.stock.size + gsc.gameState.talon.size) > 3)) {
            val moveQueue = MoveQueue(gsc.gameState)
            val move = Move(MoveType.FLIP_TALON)
            moveQueue.moveSequenceValue = 8
            moveQueue.push(move)
            moves.add(moveQueue)
        }
        if (gsc.gameState.stock.size + gsc.gameState.talon.size == 3 && gsc.gameState.talon.size < 3 && gsc.gameState.stock.size != 3) {
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
            val move = Move(MoveType.DRAW_STOCK)
            val moveQueue = MoveQueue(gsc.gameState)
            moveQueue.moveSequenceValue = 60
            moveQueue.push(move)
            moves.add(moveQueue)
        }
        return moves
    }

    fun reset() {
        gsc.resetGameState()
        currentMoveQueue = null
        gameIsWon = false
    }
}
