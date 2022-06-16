package com.cdio.solitaire.model


enum class MoveType {
    DEAL_CARDS,
    MOVE_STACK,
    MOVE_TO_FOUNDATION,
    MOVE_FROM_FOUNDATION,
    MOVE_FROM_TALON,
    FLIP_TALON,
    DRAW_STOCK,
    GAME_WON,
    GAME_LOST
}


class Move(val moveType: MoveType,val sourceStack: CardStack? =null,var targetStack: CardStack?= null,val sourceCard: Card?= null) { // Needed for toString() method, and for checking validity
    var cardToUpdate: Card?= null
    var prev:Move?=null
    var next:Move?=null
    // TODO: Make toString() method and pictureNeeded() method

    override fun toString(): String {
        return "{ moveType: ${moveType.toString()}, sourceStack: ${sourceStack.toString()}, targetStack: ${targetStack.toString()}, sourceCard: ${sourceCard.toString()}, cardToUpdate: ${cardToUpdate.toString()}}"
    }

    fun toStringDanish(): String {
        return when (moveType) {
            MoveType.MOVE_STACK, MoveType.MOVE_FROM_TALON, MoveType.MOVE_FROM_FOUNDATION -> sourceCard!!.toStringDanish() + "-" + targetStack!!.stackID + ","
            MoveType.MOVE_TO_FOUNDATION -> sourceCard!!.rank.ordinal.toString() + sourceCard.suit.shortDanish() + "-F,"
            MoveType.DRAW_STOCK -> "T,"
            MoveType.FLIP_TALON -> "S,"
            else -> ""
        }
    }
}
