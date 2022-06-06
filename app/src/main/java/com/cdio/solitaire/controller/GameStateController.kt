package com.cdio.solitaire.controller

import com.cdio.solitaire.model.Card
import com.cdio.solitaire.model.CardStack
import com.cdio.solitaire.model.GameState

class GameStateController() {
    var gameStateHistory: MutableList<GameState>? = null

    init {
        initializeGameStateHistory()
    }

    private fun initializeGameStateHistory() {
        val deck = createNullCardStack(52) //  TODO: Add listID
        // TODO: Simulation part
        val foundations = Array<CardStack>(4) { _ -> CardStack() }
        val tableaux = Array<CardStack>(7) { _ -> CardStack() }
        val talon = CardStack()
        val stock = CardStack()
        dealOutDeck(deck, tableaux, stock)
        val initialGameState = GameState(foundations, tableaux, talon, stock)
        getFirstCardValues(initialGameState)
        gameStateHistory = mutableListOf(initialGameState)
    }

    private fun dealOutDeck(
        deck: CardStack,
        tableaux: Array<CardStack>,
        stock: CardStack
    ) {
        for (i in 0..6){
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
}