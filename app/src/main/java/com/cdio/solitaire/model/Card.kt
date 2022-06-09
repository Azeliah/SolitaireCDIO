package com.cdio.solitaire.model

enum class Suit {
    NA, CLUBS, DIAMONDS, HEARTS, SPADES; // Note the order: C = 1, D = 2, H = 3, S = 4.

    fun short(): String {
        return when (this.ordinal) {
            1 -> "C"
            2 -> "D"
            3 -> "H"
            4 -> "S"
            else -> "NA"
        }
    }

    fun isRed(): Boolean {
        return when (this) {
            DIAMONDS, HEARTS -> true
            else -> false
        }
    }
}

enum class Rank {
    NA, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;

    fun short(): String {
        return when (this.ordinal) {
            1 -> "A"
            in 2..9 -> this.ordinal.toString()
            10 -> "T"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> "NA"
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
