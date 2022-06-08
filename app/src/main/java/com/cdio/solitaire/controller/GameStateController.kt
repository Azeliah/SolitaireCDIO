package com.cdio.solitaire.controller

import android.util.Log
import com.cdio.solitaire.model.*

class GameStateController {
    lateinit var gameState: GameState
    val sortedDeck = Array(52) { _ -> Card(0) } // Use this to track cards in the game.
    // TODO: Populate sortedDeck as cards are revealed.
    init {
        initializeGameStateHistory()
    }

    private fun initializeGameStateHistory() {
        val deck = createNullCardStack(52, -1)
        val foundations = Array(4) { i -> CardStack(i + 7) } // 7, 8, 9, 10
        val tableaux = Array(7) { i -> CardStack(i) } // 0, 1, 2, 3, 4, 5, 6
        val stock = CardStack(11)
        val talon = CardStack(12)
        dealOutDeck(deck, tableaux, stock)
        val initialGameState =
            GameState(foundations, tableaux, talon, stock, mutableListOf(Move(MoveType.DEAL_CARDS)))
        getFirstCardValues() // TODO: This should be handled by the app and ML system
        gameState = initialGameState
    }

    private fun dealOutDeck(
        deck: CardStack,
        tableaux: Array<CardStack>,
        stock: CardStack
    ) {
        for (i in 0..6) {
            for (j in i..6) {
                tableaux[j].pushCard(deck.popCard()!!)
            }
        }
        stock.pushStack(deck)
    }

    private fun createNullCardStack(cardCount: Int, listID: Int): CardStack {
        val cardStack = CardStack(listID)
        for (i in 0..cardCount) cardStack.pushCard(Card(listID))
        return cardStack
    }

    private fun getFirstCardValues() {
        //captureInitialGameState()
        TODO("Not yet implemented. Other dev branch.")
    }

    private fun getCardStackFromID(listID: Int): CardStack? { // Added for ease, might be deleted later
        return when (listID) {
            0, 1, 2, 3, 4, 5, 6 -> gameState.tableaux[listID]
            7, 8, 9, 10 -> gameState.foundations[listID - 7]
            11 -> gameState.stock
            12 -> gameState.talon
            else -> null
        }
    }

    private fun flipTalon() {
        if (gameState.stock.size < 3) {
            gameState.stock.pushStackToHead(gameState.talon)
            // TODO: Prevent stock end condition from occurring.
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

    // TODO: Make updateCard - should update a single card in the game.
    //  Might not belong here, could be app level

    fun getLastMove(): Move {
        return gameState.moves.last()
    }
}