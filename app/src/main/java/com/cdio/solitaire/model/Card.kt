package com.cdio.solitaire.model

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

    fun offSuit(suitToCompare: Suit): Boolean {
        if (suitToCompare == NA) throw Exception("UndefinedSuitException: Suit is NA")
        return when (this) {
            CLUBS, SPADES -> (suitToCompare == DIAMONDS || suitToCompare == HEARTS)
            DIAMONDS, HEARTS -> (suitToCompare == CLUBS || suitToCompare == SPADES)
            NA -> throw Exception("UndefinedSuitException: Suit is NA")
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

data class Card(
    var stackID: Int,
    var rank: Rank = Rank.NA,
    var suit: Suit = Suit.NA,
    var prev: Card? = null,
    var next: Card? = null
)
