package com.cdio.solitaire.model


enum class MoveType {
    DEAL_CARDS,
    MOVE_STACK,
    MOVE_TO_FOUNDATION,
    MOVE_FROM_FOUNDATION,
    MOVE_FROM_TALON,
    FLIP_TALON,
    DRAW_STOCK
}


class Move(val moveType: MoveType) {
    val sourceStack: CardStack? = null
    var targetStack: CardStack? = null
    val sourceCard: Card? = null // Needed for toString() method, and for checking validity
    var cardToUpdate: Card? = null

    // TODO: Make toString() method and pictureNeeded() method
}


