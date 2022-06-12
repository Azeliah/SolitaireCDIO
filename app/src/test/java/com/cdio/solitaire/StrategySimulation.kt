package com.cdio.solitaire

import androidx.core.os.persistableBundleOf
import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.Card
import com.cdio.solitaire.model.CardStack
import com.cdio.solitaire.model.Rank
import com.cdio.solitaire.model.Suit
import org.junit.Test

class DataSource {
    // This essentially mimics the user actions with the physical cards that still need to be recognized
    private val sortedDeck: Array<Card>
    private val shuffledDeck: Array<Card>
    val tableaux: Array<CardStack>
    val stock: CardStack
    val talon: CardStack

    init {
        // Shuffle deck of cards
        sortedDeck = createOrderedDeck()
        shuffledDeck = sortedDeck.clone()
        shuffledDeck.shuffle()
        val deck = CardStack(-1)
        for (card in shuffledDeck) deck.pushCard(card)

        // Create cardStacks
        tableaux = Array(7) { i -> CardStack(i + 1) } // 1, 2, 3, 4, 5, 6, 7
        stock = CardStack(12)
        talon = CardStack(0)

        // Deal out cards
        for (i in 0..6) {
            for (j in i..6) tableaux[j].pushCard(deck.popCard())
        }
        stock.pushStack(deck)
        stock.hiddenCards = stock.size
    }

    private fun createOrderedDeck(): Array<Card> {
        return Array(52) { i ->
            Card(
                -1,
                Rank.values()[i % 13 + 1],
                Suit.values()[i % 4 + 1]
            )
        } // +1 is used for offsets in enums.
    }

    fun discoverCard(stackID: Int): Card {
        return when (stackID) {
            in 1..7 -> tableaux[stackID - 1].popCard()
            0 -> talon.tail!!
            else -> throw Exception("No hidden cards here.")
        }
    }
}

class StrategySimulation {
    @Test
    fun simulateGame() {
        val dataSource = DataSource()
        val strategyController = StrategyController()
        var gameFinished = false
        var rounds = 1000
        while (!gameFinished && rounds != 0) {
            rounds--
            strategyController.decideMove()
        }


    }


}