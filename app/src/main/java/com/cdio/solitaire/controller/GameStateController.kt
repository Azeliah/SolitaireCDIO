package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*

class GameStateController {
    var gameState: GameState

    /**
     * Creates the initial gameState. Refer to this for cardStack IDs.
     */
    init {
        val deck = createNullCardStack()
        val foundations = Array(4) { i -> CardStack(i + 8) } // 8, 9, 10, 11
        val tableaux = Array(7) { i -> CardStack(i + 1) } // 1, 2, 3, 4, 5, 6, 7
        val stock = CardStack(12)
        val talon = CardStack(0)
        dealOutDeck(deck, tableaux, stock)
        gameState =
            GameState(foundations, tableaux, talon, stock, mutableListOf(Move(MoveType.DEAL_CARDS)))
    }

    /**
     * Deals out cards in compliance with solitaire rules.
     */
    private fun dealOutDeck(
        deck: CardStack,
        tableaux: Array<CardStack>,
        stock: CardStack
    ) {
        for (i in 0..6) {
            for (j in i..6) tableaux[j].pushCard(deck.popCard())
        }
        stock.pushStack(deck)
    }

    /**
     * Used to create anonymous (hidden) cards.
     */
    private fun createNullCardStack(): CardStack {
        val cardStack = CardStack(-1)
        for (i in 0 until 52) cardStack.pushCard(Card(-1))
        return cardStack
    }

    /**
     * Retrieve a cardStack using the ID. The numbering is in compliance with initial gameState.
     */
    fun getCardStackFromID(stackID: Int): CardStack { // Added for ease, might be deleted later
        return when (stackID) {
            in 1..7 -> gameState.tableaux[stackID - 1]
            in 8..11 -> gameState.foundations[stackID - 8]
            12 -> gameState.stock
            0 -> gameState.talon
            else -> throw Exception("Invalid stackID: $stackID.")
        }
    }

    /**
     * Flips the talon by pushing the talon to the head end of stock.
     */
    private fun flipTalon() {
        gameState.stock.pushStackToHead(gameState.talon)
    }

    /**
     * Draw 3 cards from stock. Do accounting on hiddenCards, and, if needed, signal card recognition.
     */
    private fun drawFromStock(): Card? {
        if (gameState.stock.size < 3) throw Exception("EmptyStackPop: Not enough cards in stock to draw to talon.")
        // Cannot use gsc.moveStack here, because we want to check talon.tail, not stock.tail - and move cards one at a time.
        repeat(3) {
            val cardToPush = gameState.stock.popCard()
            gameState.talon.pushCard(cardToPush)
        }
        return cardToUpdate(gameState.talon)
    }

    /**
     * If the rank of the tail card is 0 (== NA), card recognition is needed.
     */
    private fun cardToUpdate(stack: CardStack): Card? {
        return if (stack.tail == null) null
        else if (stack.tail!!.rank == Rank.NA) {
            stack.tail
        } else null
    }

    /**
     * moveStack moves a stack and adds cardToUpdate information to the move object.
     */
    private fun moveStack(
        sourceStack: CardStack,
        cardsToMove: Int,
        targetStack: CardStack
    ): Card? {
        targetStack.pushStack(sourceStack.popStack(cardsToMove))
        return cardToUpdate(sourceStack)
    }

    private fun moveToFoundation(sourceStack: CardStack, sourceCard: Card): Card? {
        return moveCard(sourceStack, getFoundation(sourceCard.suit))
    }

    private fun moveCard(sourceStack: CardStack, targetStack: CardStack): Card? {
        return moveStack(sourceStack, 1, targetStack)
    }

    /**
     * performMove is a fork function on moveType of the move object.
     * It maps each moveType to their respective methods.
     */
    fun performMove(move: Move) {
        var cardToUpdate: Card? = null
        when (move.moveType) {
            MoveType.MOVE_STACK -> cardToUpdate = moveStack(
                move.sourceStack!!,
                getCardPosition(move.sourceCard, move.sourceStack),
                move.targetStack!!
            )
            MoveType.MOVE_FROM_FOUNDATION,
            MoveType.MOVE_FROM_TALON -> moveCard(move.sourceStack!!, move.targetStack!!)
            MoveType.MOVE_TO_FOUNDATION -> cardToUpdate =
                moveToFoundation(move.sourceStack!!, move.sourceCard!!)
            MoveType.FLIP_TALON -> flipTalon()
            MoveType.DRAW_STOCK -> {
                drawFromStock()
                cardToUpdate = cardToUpdate(gameState.talon)
            }
            MoveType.DEAL_CARDS -> throw Exception("DEAL_CARDS is not for use here.")
            else -> {} // Do nothing for GAME_WIN and GAME_LOSS
        }
        move.cardToUpdate = cardToUpdate
        gameState.moves.add(move)
    }

    /**
     * Searches for the position of a card in a given cardStack, measured from the tail element.
     */
    private fun getCardPosition(card: Card?, stack: CardStack): Int {
        if (card!!.stackID != stack.stackID) {
            throw Exception("CardNotInStack: Card stackID differs from stack stackID")
        }
        var stackCard = stack.tail!!
        var i = 1
        while (stackCard.rank.ordinal != card.rank.ordinal) {
            i++
            stackCard = stackCard.prev!!
        }
        return i
    }

    fun getLastMove(): Move {
        return gameState.moves.last()
    }


    /**
     * Used to ensure consistency in tableauOrdering when performing moves.
     */
    private fun tableauOrdering(card: Card, targetStack: CardStack): Boolean =
        if (targetStack.stackID !in 1..7) false
        else when (targetStack.size) {
            0 -> card.rank.ordinal == 13 // Only kings can go on an empty tableau
            else -> {
                val targetCard = targetStack.tail!!
                val ranksMatch = targetCard.rank.ordinal - card.rank.ordinal == 1
                val offColor = targetCard.suit.offSuit(card.suit)
                ranksMatch && offColor
            }
        }

    /**
     * Assesses whether a stack move is legal.
     */
    private fun verifyMoveStack(move: Move): Boolean =
        if (move.sourceStack == move.targetStack) {
            false
        } else if (move.moveType == MoveType.MOVE_FROM_TALON || move.sourceStack != null && move.targetStack != null) {
            tableauOrdering(move.sourceCard!!, move.targetStack!!)
        } else throw Exception("IllegalMoveError: You need to specify a sourceStack and targetStack.")

    /**
     * Assesses whether a move from foundation is legal.
     */
    private fun verifyMoveFromFoundation(move: Move): Boolean {
        val foundation = move.sourceStack
        if (foundation == null || move.targetStack == null)
            throw Exception("You need to specify a sourceStack and targetStack.")
        return if (foundation.stackID in 8..11 && foundation.tail == move.sourceCard) {
            tableauOrdering(move.sourceCard!!, move.targetStack!!)
        } else {
            false
        }
    }

    /**
     * Assesses whether a move from talon is legal.
     */
    private fun verifyMoveFromTalon(move: Move): Boolean =
        if (gameState.talon.tail != move.sourceCard) {
            false
        } else if (move.targetStack!!.stackID in 8..11 && getFoundation(move.sourceCard!!.suit) == move.targetStack) {
            true
        } else if (move.targetStack!!.stackID in 1..7) {
            verifyMoveStack(move)
        } else false

    /**
     * Gets the foundation which has been used for the suit. If none exist, return the first empty foundation.
     */
    fun getFoundation(suit: Suit): CardStack {
        var foundation: CardStack? = null
        for (stack in gameState.foundations) {
            if (stack.tail == null) {
                foundation = stack
                break
            } else if (stack.tail!!.suit == suit) {
                foundation = stack
                break
            }
        }
        if (foundation == null) throw Exception("Foundation not found. This shouldn't happen. Ever. Good job.")
        return foundation
    }

    /**
     * Assesses whether a move to foundation is legal.
     */
    private fun verifyMoveToFoundation(move: Move): Boolean {

        return if (move.sourceStack!!.tail != move.sourceCard) false
        else when (move.sourceCard!!.rank.ordinal) {
            1 -> {
                move.targetStack = getFoundation(move.sourceCard.suit)
                true
            }
            in 2..13 -> {
                val targetStack = getFoundation(move.sourceCard.suit)
                if (targetStack.tail == null) {
                    false
                } else if (move.sourceCard.rank.ordinal - targetStack.tail!!.rank.ordinal == 1) {
                    move.targetStack = targetStack
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    /**
     * Assesses whether flipping the talon is legal.
     */
    private fun verifyFlipTalon(): Boolean {
        return if (gameState.stock.size >= 3) {
            false
        } else gameState.stock.size + gameState.talon.size >= 3
    }

    /**
     * Assesses whether drawing cards from stock is legal.
     */
    private fun verifyDrawStock(): Boolean {
        return gameState.stock.size >= 3
    }

    /**
     * Fork function to check if a given move is legal.
     */
    fun isMoveLegal(move: Move): Boolean {
        return when (move.moveType) {
            MoveType.MOVE_STACK -> verifyMoveStack(move)
            MoveType.MOVE_FROM_FOUNDATION -> verifyMoveFromFoundation(move)
            MoveType.MOVE_FROM_TALON -> verifyMoveFromTalon(move)
            MoveType.MOVE_TO_FOUNDATION -> verifyMoveToFoundation(move)
            MoveType.FLIP_TALON -> verifyFlipTalon()
            MoveType.DRAW_STOCK -> verifyDrawStock()
            MoveType.DEAL_CARDS -> throw Exception("DEAL_CARDS is reserved for initialization.")
            MoveType.GAME_LOST -> true
            MoveType.GAME_WON -> true
        }
    }


    fun getLowestBlackFoundation(): Int {
        var blackCounter = 0
        var lowestFoundation = 0
        for (foundation in gameState.foundations) {
            if (foundation.tail != null && foundation.tail!!.suit.getColor() == Color.BLACK) {
                if (-foundation.tail!!.rank.ordinal < -lowestFoundation) {
                    lowestFoundation = foundation.tail!!.rank.ordinal
                }
                blackCounter++
            }
        }
        return if (blackCounter == 0 || blackCounter == 1) {
            0
        } else {
            lowestFoundation

        }
    }

    fun getLowestRedFoundation(): Int {
        var redCounter = 0
        var lowestFoundation = 0
        for (foundation in gameState.foundations) {
            if (foundation.tail != null && foundation.tail!!.suit.getColor() == Color.RED) {
                if (-foundation.tail!!.rank.ordinal < -lowestFoundation) {
                    lowestFoundation = foundation.tail!!.rank.ordinal
                }
                redCounter++
            }
        }
        return if (redCounter == 0 || redCounter == 1) {
            0
        } else {
            lowestFoundation
        }
    }


    fun movesAsString(): String {
        var resultString = ""
        for (move in gameState.moves) resultString += move.toStringDanish()
        return resultString
    }

    fun isGameWon(): Boolean {
        for (foundation in gameState.foundations) if (foundation.size != 13) return false
        return true
    }

    fun resetGameState() {
        val deck = createNullCardStack()
        val foundations = Array(4) { i -> CardStack(i + 8) } // 8, 9, 10, 11
        val tableaux = Array(7) { i -> CardStack(i + 1) } // 1, 2, 3, 4, 5, 6, 7
        val stock = CardStack(12)
        val talon = CardStack(0)
        dealOutDeck(deck, tableaux, stock)
        gameState =
            GameState(foundations, tableaux, talon, stock, mutableListOf(Move(MoveType.DEAL_CARDS)))
    }

    fun getNumberOfHiddenCards(): Int {
        var hiddenCards = 0
        hiddenCards += gameState.talon.hiddenCards()
        hiddenCards += gameState.stock.hiddenCards()
        for (stack in gameState.tableaux) {
            hiddenCards += stack.hiddenCards()
        }
        return hiddenCards
    }
}
