package com.cdio.solitaire.model

class MoveQueue(var gameState: GameState) {
    var head: Move? = null
    var tail: Move? = null
    var size: Int = 0

    fun push(move: Move) { // Push to tail
        when (size) {
            0 -> {
                head = move
                tail = move
            }
            else -> {
                tail!!.next = move
                move.prev = tail
                tail = move
            }
        }
        size++
    }

    fun pop(): Move { // Pop from head
        val poppedMove: Move
        when (size) {
            0 -> throw Exception("Popping empty queue")
            1 -> {
                poppedMove = head!!
                head = null
                tail = null
            }
            else -> {
                poppedMove = head!!
                head = poppedMove.next
                head!!.prev = null
                poppedMove.next = null
            }
        }
        size--
        return poppedMove
    }
}