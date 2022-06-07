package com.cdio.solitaire.controller

import android.util.Log
import com.cdio.solitaire.model.*

class GameStateController {
    lateinit var gameState: GameState

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
        for (i in 0..cardCount) cardStack.pushCard(Card(listID = listID))
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

    private fun drawFromStock(): Boolean {
        if (gameState.stock.size < 3) {
            Log.e("EmptyStackPop", "Not enough cards in stock to draw to talon.")
            return false
        }
        moveStack(gameState.stock, 3, gameState.talon)
        return true // TODO: cardRevealed Boolean
    }

    private fun moveStack(
        sourceStack: CardStack,
        cardsToMove: Int,
        targetStack: CardStack
    ): Boolean {
        targetStack.pushStack(sourceStack.popStack(cardsToMove)!!)
        // TODO: Missing functionality for tracking hidden cards.
        return true
    }

    private fun moveCard(sourceStack: CardStack, targetStack: CardStack): Boolean {
        if (sourceStack.listID < 7 || sourceStack.listID == 12) TODO("Check if on top of hidden card.")
        // TODO: Finish this part
        return false
    }

    fun performMove(move: Move) {
        var cardRevealed = false
        when (move.moveType) {
            MoveType.MOVE_STACK -> cardRevealed = moveStack(
                move.sourceStack!!,
                move.cardsMoved,
                move.targetStack!!
            )
            MoveType.MOVE_FROM_FOUNDATION,
            MoveType.MOVE_FROM_TALON,
            MoveType.MOVE_TO_FOUNDATION -> cardRevealed =
                moveCard(move.sourceStack!!, move.targetStack!!)
            MoveType.FLIP_TALON -> flipTalon()
            MoveType.DRAW_STOCK -> cardRevealed = drawFromStock()
            else -> Log.e("MoveTypeNotDefined", "This cannot happen.")
        }
        move.cardRevealed = cardRevealed
        gameState.moves.add(move)
    }

    // TODO: Make updateCard - should update a single card in the game.
    //  Might not belong here, could be app level

    fun getLastMove(): Move {
        return gameState.moves.last()
    }
}