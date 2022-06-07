package com.cdio.solitaire.controller

import com.cdio.solitaire.model.*

class GameStateController() {
    var gameStateHistory: MutableList<GameState>? = null

    init {
        initializeGameStateHistory()
    }

    private fun initializeGameStateHistory() {
        val deck = createNullCardStack(52, -1)
        val foundations = Array(4) { i -> CardStack(i+8) }
        val tableaux = Array(7) { i -> CardStack(i+1) }
        val talon = CardStack(13)
        val stock = CardStack(12)
        dealOutDeck(deck, tableaux, stock)
        val initialGameState =
            GameState(foundations, tableaux, talon, stock, Move(MoveType.DEAL_CARDS))
        getFirstCardValues(initialGameState) // TODO: This should be handled by the app and ML system
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

    private fun createNullCardStack(cardCount: Int, listID: Int): CardStack {
        val cardStack = CardStack(listID)
        for (i in 0..cardCount) cardStack.pushCard(Card(listID = listID))
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