package com.cdio.solitaire.model

data class GameState(
    val foundations: Array<CardStack>,
    val tableaux: Array<CardStack>,
    val talon: CardStack,
    val stock: CardStack,
    val moves: MutableList<Move>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!foundations.contentEquals(other.foundations)) return false
        if (!tableaux.contentEquals(other.tableaux)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = foundations.contentHashCode()
        result = 31 * result + tableaux.contentHashCode()
        return result
    }
}
