package com.cdio.solitaire

import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.*
import org.junit.Test

class DataSource (random: Boolean) {
    // This essentially mimics the user actions with the physical cards that still need to be recognized
    private val sortedDeck: Array<Card>
    val shuffledDeck: Array<Card>
    val tableaux: Array<CardStack>
    val stock: CardStack
    val talon: CardStack

    init {
        // Shuffle deck of cards
        sortedDeck = createOrderedDeck()
        if (random) {
            shuffledDeck = sortedDeck.clone()
            shuffledDeck.shuffle()
        } else {
            shuffledDeck = arrayOf(
                sortedDeck[25], sortedDeck[35], sortedDeck[21], sortedDeck[32], sortedDeck[29], sortedDeck[1],
                sortedDeck[4], sortedDeck[31], sortedDeck[37], sortedDeck[47], sortedDeck[12], sortedDeck[26],
                sortedDeck[22], sortedDeck[13], sortedDeck[20], sortedDeck[38], sortedDeck[51], sortedDeck[18],
                sortedDeck[45], sortedDeck[15], sortedDeck[48], sortedDeck[9], sortedDeck[17], sortedDeck[6],
                sortedDeck[2], sortedDeck[23], sortedDeck[33], sortedDeck[0], sortedDeck[5], sortedDeck[8],
                sortedDeck[24], sortedDeck[43], sortedDeck[28], sortedDeck[16], sortedDeck[19], sortedDeck[10],
                sortedDeck[46], sortedDeck[49], sortedDeck[50], sortedDeck[41], sortedDeck[7], sortedDeck[30],
                sortedDeck[34], sortedDeck[3], sortedDeck[11], sortedDeck[27], sortedDeck[44], sortedDeck[39],
                sortedDeck[36], sortedDeck[42], sortedDeck[14], sortedDeck[40]
            )
        }
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
        val printSolutions = false
        val randomSimulation = true
        val iterations = if (randomSimulation) 50000 else 1
        val strategyController = StrategyController
        var gamesWon = 0
        var movesMade = 0 // in winning games this is incremented
        repeat(iterations) {
            strategyController.reset()
            val dataSource = DataSource(randomSimulation) // Use false for the handed in deck sorting
            val cards = dataSource.updateFirstLayer()
            for (i in cards.indices) {
                StrategyController.gsc.gameState.tableaux[i].tail!!.rank = cards[i]!!.rank
                StrategyController.gsc.gameState.tableaux[i].tail!!.suit = cards[i]!!.suit
            }
            var gameFinished = false
            var rounds = 400
            val roundsMax = rounds
            val moveCounter = Array(MoveType.values().size) { _ -> 0 }
            while (!gameFinished && rounds != 0) {
                rounds--
                val moveToPlay = StrategyController.nextMove()
                when (moveToPlay.moveType) {
                    MoveType.MOVE_FROM_TALON -> dataSource.talon.popCard()
                    MoveType.DRAW_STOCK -> dataSource.drawStock()
                    MoveType.FLIP_TALON -> dataSource.flipTalon()
                    MoveType.MOVE_TO_FOUNDATION -> {
                        if (moveToPlay.sourceStack!!.stackID == 0)
                            dataSource.talon.popCard()
                    }
                    else -> {}
                }

                moveCounter[moveToPlay.moveType.ordinal]++

                // Is a card discovered? Get its values.
                if (moveToPlay.cardToUpdate != null) {
                    val discoveredCard = dataSource.discoverCard(moveToPlay.cardToUpdate!!.stackID)
                    moveToPlay.cardToUpdate!!.rank = discoveredCard.rank
                    moveToPlay.cardToUpdate!!.suit = discoveredCard.suit
                }

                if (moveToPlay.moveType == MoveType.GAME_WON) {
                    StrategyController.gameIsWon = true
                    gameFinished = true
                } else if (moveToPlay.moveType == MoveType.GAME_LOST) {
                    gameFinished = true
                }
            }
            if (StrategyController.gameIsWon) { //strategyController.gameIsWon
                gamesWon++
                movesMade += roundsMax-rounds
                if (printSolutions) {
                    var deckString = ""
                    for (i in dataSource.shuffledDeck.indices) deckString += dataSource.shuffledDeck[51 - i].toStringDanish() + ","
                    deckString += "\b"
                    println("Deck used:")
                    println(deckString)
                    println("Moves made:")
                    println(StrategyController.gsc.movesAsString() + "\b")
                }
            }
        }

        println("Games won: $gamesWon")
        if (gamesWon > 0) {
            println("Iterations performed: $iterations")
            println("Win percentage: " + ((gamesWon.toFloat() / iterations.toFloat()) * 100))
            println("Average moves made: " + (movesMade / gamesWon).toString())
        }

    }
}
