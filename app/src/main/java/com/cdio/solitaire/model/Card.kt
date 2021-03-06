package com.cdio.solitaire.model

enum class Color {
    NA, RED, BLACK
}

enum class Suit {
    NA, CLUBS, DIAMONDS, HEARTS, SPADES; // Note the order: C = 1, D = 2, H = 3, S = 4.

    fun short(): String {
        return when (this) {
            CLUBS -> "C"
            DIAMONDS -> "D"
            HEARTS -> "H"
            SPADES -> "S"
            NA -> "NA"
        }
    }

    fun shortDanish(): String {
        return when (this) {
            CLUBS -> "K"
            DIAMONDS -> "R"
            HEARTS -> "H"
            SPADES -> "S"
            NA -> "NA"
        }
    }

    fun offSuit(suitToCompare: Suit): Boolean {
        if (suitToCompare == NA || this == NA) throw Exception("Suit is NA")
        return this.getColor() != suitToCompare.getColor()
    }

    fun getColor(): Color {
        return when (this) {
            NA -> Color.NA
            CLUBS, SPADES -> Color.BLACK
            DIAMONDS, HEARTS -> Color.RED
        }
    }
}

enum class Rank {
    NA, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;

    fun short(): String {
        return when (this) {
            ACE -> "A"
            TEN -> "T"
            JACK -> "J"
            QUEEN -> "Q"
            KING -> "K"
            NA -> "NA"
            else -> this.ordinal.toString()
        }
    }
}

class Card(var stackID: Int, var rank: Rank = Rank.NA, var suit: Suit = Suit.NA) {
    var prev: Card? = null
    var next: Card? = null

    /**
     *  Returns a copy of the card object stripped of stackID and pointers.
     */
    fun copyOf(): Card {
        return Card(-1, this.rank, this.suit)
    }

    fun toStringDanish(): String {
        return suit.shortDanish() + rank.ordinal.toString()
    }

    override fun toString(): String {
        val newSuit = when (suit) {
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
            Suit.HEARTS -> "♥"
            Suit.SPADES -> "♠"
            Suit.NA -> "NA"
        }
        val newRank = when (rank.ordinal) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> rank.ordinal
        }
        return newSuit + newRank
    }
}
