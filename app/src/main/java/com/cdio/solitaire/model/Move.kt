package com.cdio.solitaire.model

// TODO: Make into a regular Kotlin class, make toString() method and pictureNeeded() method
enum class MoveType {
    DEAL_CARDS,
    MOVE_STACK,
    MOVE_TO_FOUNDATION,
    MOVE_FROM_FOUNDATION,
    MOVE_FROM_TALON,
    DRAW_STOCK
}


data class Move(
    val moveType: MoveType,
    val fromStack: CardStack? = null,
    val toStack: CardStack? = null,
    val cardsMoved: Int = 0,
    val highCard: Card? = null // Used to specify the particular card for the popStack, needed for toString()
)


