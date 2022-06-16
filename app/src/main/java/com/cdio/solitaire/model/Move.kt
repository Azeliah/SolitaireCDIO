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


class Move(
    val moveType: MoveType,
    val sourceStack: CardStack? = null,
    var targetStack: CardStack? = null,
    val sourceCard: Card? = null,
    val cardToUpdateParam: Card? = null
) { // Needed for toString() method, and for checking validity
    var cardToUpdate: Card? = cardToUpdateParam
    var prev: Move? = null
    var next: Move? = null
    fun toStringDanish(): String {
        return when (moveType) {
            MoveType.MOVE_STACK, MoveType.MOVE_FROM_TALON, MoveType.MOVE_FROM_FOUNDATION -> sourceCard!!.toStringDanish() + "-" + targetStack!!.stackID + ","
            MoveType.MOVE_TO_FOUNDATION -> sourceCard!!.rank.ordinal.toString() + sourceCard.suit.shortDanish() + "-F,"
            MoveType.DRAW_STOCK -> "T,"
            MoveType.FLIP_TALON -> "S,"
            else -> ""
        }
    }

    override fun toString(): String {
        return when (moveType) {
            MoveType.MOVE_STACK, MoveType.MOVE_FROM_TALON, MoveType.MOVE_FROM_FOUNDATION -> "Move " + sourceCard!!.toString() + " to " + getCardStackName(
                targetStack!!.stackID
            ) + targetStack!!.stackID
            MoveType.MOVE_TO_FOUNDATION -> "Move " + sourceCard!!.toString() + " to foundation"
            MoveType.DRAW_STOCK -> "Draw cards from stock"
            MoveType.FLIP_TALON -> "Flip talon"
            MoveType.DEAL_CARDS -> "Deal Cards"
        }
    }

    private fun getCardStackName(id: Int): String {
        return when (id) {
            in 1..7 -> "Tableaux"
            in 8..11 -> "Foundation"
            else -> ""
        }
    }
}
