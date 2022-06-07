package com.cdio.solitaire.controller

import android.util.Log
import com.cdio.solitaire.model.*

class GameStateController() {
    var gameStateHistory: MutableList<GameState>? = null

    init {
        initializeGameStateHistory()
    }

    private fun initializeGameStateHistory() {
        val deck = createNullCardStack(52, -1)
        val foundations = Array(4) { i -> CardStack(i + 7) }
        val tableaux = Array(7) { i -> CardStack(i) }
        val stock = CardStack(11)
        val talon = CardStack(12)
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

    private fun getCardStackFromID(listID: Int): CardStack? { // Added for ease, might be deleted later
        return when (listID) {
            0, 1, 2, 3, 4, 5, 6 -> getCurrentGameState().tableaux[listID]
            7, 8, 9, 10 -> getCurrentGameState().foundations[listID - 7]
            11 -> getCurrentGameState().stock
            12 -> getCurrentGameState().talon
            else -> null
        }
    }

    fun flipTalon() {
        if (getCurrentGameState().stock.size < 3) {
            getCurrentGameState().stock.pushStackToHead(getCurrentGameState().talon)
        } else {
            Log.e(
                "Talon cannot be flipped, cards in stock",
                getCurrentGameState().stock.size.toString()
            )
        }
    }

    // TODO: Make updateCard - should update a single card in the game.
    //  Might not belong here, could be app level

    fun getCurrentGameState(): GameState {
        return gameStateHistory!![gameStateHistory!!.size - 1]
    }

    fun getLastMove(): Move {
        return getCurrentGameState().move
    }
}