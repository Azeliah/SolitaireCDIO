package com.cdio.solitaire.model

enum class Suit {
    NA, CLUBS, DIAMONDS, HEARTS, SPADES;

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
            2 -> "2"
            3 -> "3"
            4 -> "4"
            5 -> "5"
            6 -> "6"
            7 -> "7"
            8 -> "8"
            9 -> "9"
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
