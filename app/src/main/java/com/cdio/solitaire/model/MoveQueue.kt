package com.cdio.solitaire.model

class MoveQueue {
    private var head: Move? = null
    private var tail: Move? = null
    var size: Int = 0
    var moveSequenceValue = 0

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

    fun isEmpty(): Boolean {
        if (this.size < 0)
            throw Exception("Size is less than zero")
        return this.size == 0
    }
}