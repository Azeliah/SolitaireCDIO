package com.cdio.solitaire.controller

import android.util.Log
import com.cdio.solitaire.model.*

class GameStateController {
    lateinit var gameState: GameState
    val sortedDeck = arrayOfNulls<Card>(52) // Use this to track cards in the game.

    // TODO: Populate sortedDeck as cards are revealed.
    init {
        initializeGameState()
    }

    private fun initializeGameState() {
        val deck = createNullCardStack(52, -1)
        val foundations = Array(4) { i -> CardStack(i + 8) } // 8, 9, 10, 11
        val tableaux = Array(7) { i -> CardStack(i + 1) } // 1, 2, 3, 4, 5, 6, 7
        val stock = CardStack(12)
        val talon = CardStack(0)
        dealOutDeck(deck, tableaux, stock)
        gameState =
            GameState(foundations, tableaux, talon, stock, mutableListOf(Move(MoveType.DEAL_CARDS)))
    }

    private fun dealOutDeck(
        deck: CardStack,
        tableaux: Array<CardStack>,
        stock: CardStack
    ) {
        for (i in 0..6) {
            for (j in i..6) {
                tableaux[j].pushCard(deck.popCard()!!)
                tableaux[j].hiddenCards++
            }
        }
        stock.pushStack(deck)
        stock.hiddenCards = stock.size
    }

    private fun createNullCardStack(cardCount: Int, stackID: Int): CardStack {
        val cardStack = CardStack(stackID)
        for (i in 0..cardCount) cardStack.pushCard(Card(stackID))
        return cardStack
    }

    private fun getCardStackFromID(stackID: Int): CardStack? { // Added for ease, might be deleted later
        return when (stackID) {
            1, 2, 3, 4, 5, 6, 7 -> gameState.tableaux[stackID]
            8, 9, 10, 11 -> gameState.foundations[stackID - 7]
            12 -> gameState.stock
            0 -> gameState.talon
            else -> null
        }
    }

    private fun flipTalon() {
        if (gameState.stock.size < 3) { // TODO: Prevent stock end condition from occurring.
            gameState.stock.pushStackToHead(gameState.talon)
            gameState.stock.hiddenCards += gameState.talon.hiddenCards
            gameState.talon.hiddenCards = 0
        } else {
            Log.e(
                "StockThresholdError",
                "Stock size is: " + gameState.stock.size.toString()
            )
        }
    }

    private fun drawFromStock(): Card? {
        if (gameState.stock.size < 3) {
            Log.e("EmptyStackPop", "Not enough cards in stock to draw to talon.")
            return null
        }
        // Cannot use gsc.moveStack here, because we want to check talon.tail, not stock.tail.
        gameState.talon.pushStack(gameState.stock.popStack(3)!!)
        var hiddenCardsMoved = 0
        var cardToCheck = gameState.talon.tail
        for (i in 0..2) {
            if (cardToCheck!!.rank == 0) hiddenCardsMoved++
            cardToCheck = cardToCheck.prev
        }
        gameState.talon.hiddenCards += hiddenCardsMoved
        gameState.stock.hiddenCards -= hiddenCardsMoved
        return cardToUpdate(gameState.talon)
    }

    private fun cardToUpdate(stack: CardStack): Card? {
        return if (stack.tail == null) null
        else if (stack.tail!!.rank == 0) stack.tail
        else null
    }

    private fun moveStack(
        sourceStack: CardStack,
        cardsToMove: Int,
        targetStack: CardStack
    ): Card? {
        targetStack.pushStack(sourceStack.popStack(cardsToMove)!!)
        return cardToUpdate(sourceStack)
    }

    private fun moveCard(sourceStack: CardStack, targetStack: CardStack): Card? {
        return moveStack(sourceStack, 1, targetStack)
    }

    fun performMove(move: Move) {
        var cardToUpdate: Card? = null
        when (move.moveType) {
            MoveType.MOVE_STACK -> cardToUpdate = moveStack(
                move.sourceStack!!,
                move.cardsMoved,
                move.targetStack!!
            )
            MoveType.MOVE_FROM_FOUNDATION,
            MoveType.MOVE_FROM_TALON,
            MoveType.MOVE_TO_FOUNDATION -> cardToUpdate =
                moveCard(move.sourceStack!!, move.targetStack!!)
            MoveType.FLIP_TALON -> flipTalon()
            MoveType.DRAW_STOCK -> cardToUpdate = drawFromStock()
            else -> Log.e("MoveTypeNotDefined", "This cannot happen.")
        }
        move.cardToUpdate = cardToUpdate
        gameState.moves.add(move)
    }

    fun getLastMove(): Move {
        return gameState.moves.last()
    }

    fun updateSortedDeck(card: Card) {
        sortedDeck[card.suit * 13 + card.rank - 1] = card
    }
}