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
        if (suitToCompare == NA || this == NA) throw Exception("UndefinedSuitException: Suit is NA")
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

//    override fun toString(): String {
//        return "${suit.short()}/${rank.short()}"
//    }

    fun toStringDanish(): String {
        return suit.shortDanish() + rank.ordinal.toString()
    }

    override fun toString(): String {
        return rank.ordinal.toString() + suit.short()
    }
}
