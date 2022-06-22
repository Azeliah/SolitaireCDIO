package com.cdio.solitaire

import android.util.Log
import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.*
import org.junit.Test

class DataSource(deckString: String = "") {
    // This essentially mimics the user actions with the physical cards that still need to be recognized
    private val sortedDeck: Array<Card>
    val shuffledDeck: Array<Card>
    private val tableaux: Array<CardStack>
    private val stock: CardStack
    val talon: CardStack

    init {
        // Shuffle deck of cards
        sortedDeck = createOrderedDeck()
        if (deckString == "") {
            shuffledDeck = sortedDeck.clone()
            shuffledDeck.shuffle()
        } else {
            shuffledDeck = deckFromString(deckString)
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

    private fun deckFromString(deckString: String): Array<Card> {
        val cardStrings = deckString.split(", ").toTypedArray()
        if (cardStrings.size != 52) {
            Log.e("StrategySimulation", "Wrong number of cards in deckString, or wrong format.")
            return sortedDeck
        }
        return Array(cardStrings.size) { i -> cardFromString(cardStrings[i]) }
    }

    private fun cardFromString(cardString: String): Card {
        val suit = when (cardString.first().toString()) {
            "K" -> 0
            "R" -> 1
            "H" -> 2
            "S" -> 3
            else -> throw Exception("Wrong format of string.")
        }
        val rank = cardString.drop(1).toInt() - 1
        return sortedDeck[suit * 13 + rank]
    }

    private fun createOrderedDeck(): Array<Card> {
        return Array(52) { i ->
            Card(
                -1,
                Rank.values()[i % 13 + 1],
                Suit.values()[i / 13 + 1]
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
        val cards = Array<Card?>(7) { null }
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
        val competitionDecks = if (randomSimulation) arrayOf("") else arrayOf( // First is our own.
            "R13, S10, R9, K7, R4, R2, K5, S6, R12, S9, K13, H1, H10, R1, K8, H13, S13, H6, R7, S3, K10, R10, R5, H7, H3, S11, R8, K1, R6, K9, K12, S5, K3, K4, S7, H11, H8, R11, H12, R3, S8, H5, H9, S4, S12, S2, K6, S1, K11, H4, H2, K2",
            "K5, K9, R2, S6, K12, R9, R4, K3, S8, R10, R6, K8, H6, R5, H13, R1, K6, R13, H3, S7, K13, K1, H7, S5, S1, H9, S3, K4, H2, R8, S10, H4, S4, R3, R12, K2, S13, S12, H1, R7, R11, H8, K11, S9, H12, K7, H10, H5, K10, S11, S2, H11",
            "H6, H12, K5, S1, H5, S13, K12, R7, K8, K7, S12, R4, S11, S5, R12, S4, H10, K9, R5, R13, H7, H3, H9, S7, R6, S2, R2, R11, R9, R10, S10, R8, K4, H1, R1, K1, S9, H4, H8, S8, K10, H13, H2, S3, K11, S6, K3, K2, K6, R3, K13, H11",
            "K1, R13, H13, K11, H8, H12, R2, H6, R11, H4, S4, K7, S5, R5, R3, H3, K12, H1, R10, K4, S12, K2, S1, K10, R7, K3, R6, R1, S10, H11, S13, S8, S6, H9, H2, R4, H7, S11, R8, S2, S9, K9, H10, R12, K5, K6, H5, R9, K13, K8, S7, S3",
            "H7, H6, H5, S8, S7, S6, S5, S4, R9, R8, R7, R6, R5, R4, R3, K10, K9, K8, K7, K6, K5, K4, K3, K2, R2, S3, S2, H4, H3, H2, K11, K12, K13, K1, R10, R11, R12, R13, R1, S9, S10, S11, S12, S13, S1, H8, H9, H10, H11, H12, H13, H1",
            "H4, S5, H3, K4, H13, H2, K5, H5, R1, S8, S9, H1, K13, R4, S7, K7, R13, K2, K1, K12, S6, H12, R6, R5, K6, H7, R10, H10, S11, R11, R7, S4, K10, R3, K8, S3, S2, S1, H6, R9, H9, R2, K3, K11, H8, R8, S10, H11, S12, K9, R12, S13",
            "H9, R5, R7, H5, H10, K7, K10, S12, S6, S9, K12, H13, S11, S5, K4, S8, R2, H12, H11, R3, S2, H6, H8, R1, S4, R11, H3, K13, R8, K2, R10, H4, K8, S1, K3, S7, S13, S3, R12, R13, S10, H2, K1, K6, K11, H1, K9, R4, K5, H7, R9, R6",
            "K3, K2, K8, H13, R1, H12, K12, K6, S3, H3, S9, S13, S12, S1, H6, S10, S5, K1, S8, K9, K5, R11, R12, K4, H4, S2, R8, R9, K13, K7, H8, R4, H1, H10, K11, H11, R3, R7, H9, S7, R6, R13, S4, S11, K10, R5, H2, S6, H7, R10, R2, H5",
            "K5, H5, S9, R1, H6, R12, S6, S4, S5, S11, R7, S10, K4, K2, S8, K6, S3, H2, R6, H11, H10, H3, H7, H4, S7, K9, S13, R5, S2, S12, K12, H12, H8, H1, R11, R10, R3, R13, R9, R4, K8, H13, K11, K13, K3, K7, K1, R8, R2, K10, H9, S1",
            "H13, S6, H6, S7, R8, R1, H11, R7, S5, H9, R5, K13, S13, S8, K5, R6, K4, H12, S12, S11, R13, H2, K3, H4, K2, S2, S9, R2, K8, H5, R3, H3, K7, K6, R4, K10, H10, R9, S1, K11, R11, R10, H7, H1, K1, R12, H8, S3, K9, S10, S4, K12",
            "R2, H12, R5, K11, R10, S6, K2, R8, H3, K9, S4, H13, H9, R7, S10, S3, S1, H2, R11, K4, K8, S12, R9, S9, S7, S11, K12, H4, R6, H8, S8, R13, K3, R1, H6, K5, H5, S2, K1, R4, S5, R3, H1, K6, H11, K13, H10, K7, R12, S13, H7, K10",
            "H1, R11, S8, H12, S2, R3, S1, S3, K12, S12, R5, H5, R1, K6, K9, K5, K3, H8, H2, K10, R4, H9, R13, H13, S13, K4, S5, R9, S7, R8, K13, R12, S4, K2, R2, S6, S11, S9, K8, K1, H6, H3, R7, K7, S10, H7, H11, H4, H10, K11, R10, R6",
            "S4, K11, S7, R2, K9, K13, R10, S9, H2, S1, S2, S13, K3, H9, R8, R4, R5, H11, R7, H10, R9, H1, H5, R3, K7, H13, R1, H4, K1, S12, K8, S3, R12, R6, S11, H3, H8, S5, K6, S8, H12, S6, H7, S10, K5, K12, K4, H6, R13, K10, K2, R11",
            "H6, H5, H8, R4, K5, R1, R6, R7, H12, R13, R9, R8, K12, R10, K6, R12, S1, R2, H1, S3, S5, H2, K10, S6, S9, H7, K11, R5, K8, S2, H13, K7, K1, H10, S8, S11, R11, H4, K3, H3, H9, K13, K4, S12, H11, S4, K2, K9, R3, S7, S10, S13",
            "R7, H8, R13, S7, S6, H9, K3, S1, H10, H2, R4, K9, S5, H4, S4, H7, R10, K2, H3, K13, H5, H13, K8, H6, K5, K11, R2, S10, R1, S11, K1, R12, H1, H11, R3, H12, R5, R8, R6, K6, S2, R9, K7, K10, S8, S12, K4, S9, R11, S13, K12, S3",
            "H1, H2, H3, H4, H5, H6, S1, S2, S3, S4, S5, S6, R1, R2, R3, R4, R5, R6, K1, K2, K3, K4, K5, K6, K7, R7, S7, H7, K8, R8, S8, H8, K9, R9, S9, H9, K10, R10, S10, H10, K11, R11, S11, H11, K12, R12, S12, H12, K13, R13, S13, H13",
            "R13, K1, R4, H1, S12, S3, S6, R10, H7, R8, K10, R3, K7, K13, H5, R9, R2, S13, H4, H11, K2, K5, S2, H9, K12, K8, S11, K11, R12, S5, H13, S1, K6, S10, R6, S4, R11, R1, R7, R5, H3, H8, K3, H12, K9, S8, H6, K4, H10, S7, S9, H2",
            "R2, S4, K11, R9, H4, H5, R4, S8, S3, H7, K6, K13, H6, R3, R10, K4, H8, K5, K7, S2, R13, K1, S12, K12, R11, K10, R8, S7, H2, H3, H13, S11, H9, S5, H11, S13, R6, R12, K8, H10, S10, S9, H1, H12, K2, K3, R7, S6, K9, S1, R1, R5"
        )
        val iterations = if (randomSimulation) 50000 else competitionDecks.size
        val strategyController = StrategyController
        var gamesWon = 0
        var movesMade = 0 // in winning games this is incremented
        for (iteration in 0 until iterations) {
            strategyController.reset()
            val dataSource =
                DataSource(competitionDecks[iteration % competitionDecks.size]) // Use false for the handed in deck sorting
            val cards = dataSource.updateFirstLayer()
            for (i in cards.indices) {
                StrategyController.gsc.gameState.tableaux[i].tail!!.rank = cards[i]!!.rank
                StrategyController.gsc.gameState.tableaux[i].tail!!.suit = cards[i]!!.suit
            }
            var gameFinished = false
            var rounds = 400
            val roundsMax = rounds
            val moveCounter = Array(MoveType.values().size) { 0 }
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
            if (StrategyController.gameIsWon) {
                gamesWon++
                movesMade += roundsMax - rounds
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
