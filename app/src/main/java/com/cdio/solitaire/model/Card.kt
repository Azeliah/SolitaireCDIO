package com.cdio.solitaire.model

enum class Suit {
    NA, CLUBS, DIAMONDS, HEARTS, SPADES;

    fun short(suit: Int): String {
        return when (suit) {
            1 -> "C"
            2 -> "D"
            3 -> "H"
            4 -> "S"
            else -> "NA"
        }
    }

    fun short(suit: Suit): String{
        return short(suit.ordinal)
    }
}

enum class Rank {
    NA, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;

    fun short(rank: Int): String {
        return when (rank) {
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

    fun short(rank: Rank): String{
        return short(rank.ordinal)
    }
}

data class Card(
    var listID: Int,
    var rank: Int = 0, // TODO: enum class for rank (short format)
    var suit: Int = 0, // TODO: enum class for suit (short format)
    var prev: Card? = null,
    var next: Card? = null
)
