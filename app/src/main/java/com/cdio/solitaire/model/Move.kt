package com.cdio.solitaire.model

// TODO: Make into a regular Kotlin class, make toString() method and pictureNeeded() method
enum class MoveType {
    DEAL_CARDS,
    MOVE_STACK,
    MOVE_TO_FOUNDATION,
    MOVE_FROM_FOUNDATION,
    MOVE_FROM_TALON,
    FLIP_TALON,
    DRAW_STOCK
}


data class Move(
    val moveType: MoveType,
    val sourceStack: CardStack? = null,
    var targetStack: CardStack? = null,
    val sourceCard: Card? = null, // Needed for toString() method, and for checking validity
    var cardToUpdate: Card? = null
)


