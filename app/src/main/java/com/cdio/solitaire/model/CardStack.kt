package com.cdio.solitaire.model

class CardStack {
    var head: Card? = null
    var tail: Card? = null
    var size: Int = 0
    //val listID: Int

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
        // card.listID = listID
    }

    fun popCard(): Card? {
        val poppedCard: Card? = tail
        when (size) {
            0 -> {
                print("Trying to pop empty list.")
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
        return poppedCard
    }

    private fun resetCardStack() { // Used solely to prevent faulty use of stacks considered discarded.
        head = null
        tail = null
        size = 0
    }

    fun pushStack(stack: CardStack) { // Push a CardStack to tail
        if (stack.size == 0) return
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

        val poppedStack = CardStack() // use -1 as listID, as this list is considered temporary
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
}