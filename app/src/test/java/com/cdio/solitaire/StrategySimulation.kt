package com.cdio.solitaire

import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.*
import org.junit.Test

class DataSource {
    // This essentially mimics the user actions with the physical cards that still need to be recognized
    private val sortedDeck: Array<Card>
    val shuffledDeck: Array<Card>
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

    fun drawStock() {
        repeat(3) { talon.pushCard(stock.popCard()) }
    }

    fun flipTalon() {
        stock.pushStackToHead(talon)
    }

    fun updateFirstLayer(): Array<Card?> {
        val cards = Array<Card?>(7) { _ -> null }
        for (i in tableaux.indices) {
            cards[i] = tableaux[i].popCard()
        }
        return cards
    }
}

class StrategySimulation {
    @Test
    fun simulateGame() {
        val iterations = 1000
        var gamesWon = 0
        var movesMade = 0 // in winning games this is incremented
        repeat(iterations) {
            val dataSource = DataSource()
            val strategyController = StrategyController()
            val cards = dataSource.updateFirstLayer()
            for (i in cards.indices) {
                strategyController.gsc.gameState.tableaux[i].tail!!.rank = cards[i]!!.rank
                strategyController.gsc.gameState.tableaux[i].tail!!.suit = cards[i]!!.suit
                strategyController.gsc.gameState.tableaux[i].hiddenCards--
            }
            var gameFinished = false
            var rounds = 500
            val moveCounter = Array(MoveType.values().size) { _ -> 0 }
            while (!gameFinished && rounds != 0) {
                rounds--
                val moveToPlay = strategyController.nextMove()
                //println(moveToPlay!!.toStringDanish() + "\b")

                // Output move to screen
                //System.out.println(moveToPlay.moveType.toString())

                when (moveToPlay.moveType) {
                    MoveType.MOVE_FROM_TALON -> dataSource.talon.popCard()
                    MoveType.DRAW_STOCK -> dataSource.drawStock()
                    MoveType.FLIP_TALON -> dataSource.flipTalon()
                    else -> {}
                }

                moveCounter[moveToPlay.moveType.ordinal]++

                // Is a card discovered? Get its values.
                if (moveToPlay.cardToUpdate != null) {
                    val discoveredCard = dataSource.discoverCard(moveToPlay.cardToUpdate!!.stackID)
                    moveToPlay.cardToUpdate!!.rank = discoveredCard.rank
                    moveToPlay.cardToUpdate!!.suit = discoveredCard.suit
                    // println("New card is $discoveredCard")
                    strategyController.gsc.gameState.talon.hiddenCards--
                }

                if(moveToPlay.moveType==MoveType.GAME_WON){
                    strategyController.gameIsWon = true
                    gameFinished = true
                } else if(moveToPlay.moveType == MoveType.GAME_LOST){
                    gameFinished = true
                }
            }
            if (strategyController.gameIsWon) {
                gamesWon++
                movesMade += 1000 - rounds
                var deckString = ""
                for (i in dataSource.shuffledDeck.indices) deckString += dataSource.shuffledDeck[51 - i].toStringDanish() + ","
                deckString += "\b"
                println("Deck used:")
                println(deckString)
                println("Moves made:")
                println(strategyController.gsc.movesAsString() + "\b")
            }
        }
        if(gamesWon>0){
            println("Games won: $gamesWon")
            println("Win percentage: " + ((gamesWon.toFloat()/iterations.toFloat())*100))
            println("Average moves made: " + (movesMade/gamesWon).toString())
        }

    }
}
