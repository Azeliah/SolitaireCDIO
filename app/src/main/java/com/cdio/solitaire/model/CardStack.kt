package com.cdio.solitaire.model

import android.util.Log

class CardStack(val listID: Int) {
    var head: Card? = null
    var tail: Card? = null
    var size: Int = 0
    var hiddenCards: Int = 0

    fun pushCard(card: Card) { // Standard push, pushing an element to the tail
        when (size) {
            0 -> {
                head = card
                tail = card
                size++
            }
            else -> {
                tail!!.next = card
                card.prev = tail
                tail = card
                size++
            }
        }
        card.listID = listID
    }

    fun popCard(): Card? {
        val poppedCard: Card? = tail
        when (size) {
            0 -> {
                Log.e("EmptyStackPop", "Stack: $listID")
                return null
            }
            1 -> {
                poppedCard!!.prev = null
                resetCardStack()
            }
            else -> {
                tail = poppedCard!!.prev
                tail!!.next = null
                poppedCard.prev = null
                size--
            }
        }
        poppedCard.listID = -1
        return poppedCard
    }

    private fun resetCardStack() { // Used solely to prevent faulty use of stacks considered discarded.
        head = null
        tail = null
        size = 0
    }

    fun pushStack(stack: CardStack) { // Push a CardStack to tail
        if (stack.size == 0) return
        var card = stack.head
        for (i in 1..stack.size) {
            card!!.listID = listID
            card = card.next
        }

        when (size) {
            0 -> {
                head = stack.head
                tail = stack.tail
            }
            else -> {
                tail!!.next = stack.head
                stack.head!!.prev = tail
                tail = stack.tail
            }
        }
        size += stack.size
        stack.resetCardStack()
    }


    fun popStack(cardsToPop: Int): CardStack? {
        if (size == 0 || size < cardsToPop) return null

        val poppedStack = CardStack(-1)
        if (size - cardsToPop == 0) poppedStack.pushStack(this)
        else {
            poppedStack.tail = tail
            poppedStack.head = tail
            for (i in 1 until cardsToPop) poppedStack.head = poppedStack.head!!.prev
            tail = poppedStack.head!!.prev
            tail!!.next = null
            poppedStack.head!!.prev = null
            poppedStack.size = cardsToPop
            size -= cardsToPop
        }
        return poppedStack
    }

    fun pushStackToHead(stack: CardStack) { // Used when flipping talon
        if (stack.size == 0) {
            Log.e(
                "EmptyStackPush",
                "Trying to push empty stack, listID: " + stack.listID.toString()
            )
            return
        }
        if (size == 0) {
            pushStack(stack)
            return
        }
        var card = stack.head
        for (i in 1..stack.size) {
            card!!.listID = listID
            card = card.next
        }
        head!!.prev = stack.tail
        stack.tail!!.next = head
        head = stack.head
        size += stack.size
        stack.resetCardStack()
    }
}