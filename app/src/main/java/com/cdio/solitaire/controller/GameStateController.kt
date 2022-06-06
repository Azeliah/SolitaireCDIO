package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*

class GameStateController() {
    var gameStateHistory: MutableList<GameState>? = null

    init {
        initializeGameStateHistory()
    }

    private fun initializeGameStateHistory() {
        val deck = createNullCardStack(52) //  TODO: Add listID
        // TODO: Simulation part
        val foundations = Array(4) { _ -> CardStack() }
        val tableaux = Array(7) { _ -> CardStack() }
        val talon = CardStack()
        val stock = CardStack()
        dealOutDeck(deck, tableaux, stock)
        val initialGameState =
            GameState(foundations, tableaux, talon, stock, Move(MoveType.DEAL_CARDS))
        // TODO: Add check on first game state
        getFirstCardValues(initialGameState)
        gameStateHistory = mutableListOf(initialGameState)
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

    private fun createNullCardStack(cardCount: Int): CardStack { // TODO: Add listID
        val cardStack = CardStack() // TODO: Parse listID
        for (i in 0..cardCount) cardStack.pushCard(Card())
        return cardStack
    }

    private fun getFirstCardValues(gameState: GameState) {
        //captureInitialGameState()
        TODO("Not yet implemented. Other dev branch.")
    }

    // TODO: Make updateCard - should update a single card in the game.

    fun getCurrentGameState(): GameState {
        return gameStateHistory!![gameStateHistory!!.size - 1]
    }

    fun getLastMove(): Move {
        return getCurrentGameState().move
    }
}