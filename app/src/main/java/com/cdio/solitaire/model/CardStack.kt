package com.cdio.solitaire.model

// TODO: Consider adding nullable suit var for foundations
// TODO: Consider adding isEmpty method for code brevity

class CardStack(val stackID: Int) {
    var head: Card? = null
    var tail: Card? = null
    var size: Int = 0
    var hiddenCards: Int = 0

    /**
     * Standard push, pushing an element to the tail of the stack
     */
    fun pushCard(card: Card) {
        when (size) {
            0 -> {
                head = card
                tail = card
            }
            else -> {
                tail!!.next = card
                card.prev = tail
                tail = card
            }
        }
        size++
        if (card.rank == Rank.NA) hiddenCards++
        card.stackID = stackID
    }

    /**
     * Standard pop, popping and returning the tail element of the stack
     */
    fun popCard(): Card {
        val poppedCard: Card? = tail
        when (size) {
            0 -> throw Exception("Empty stack pop.")
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
        if (poppedCard.rank.ordinal == 0) hiddenCards--
        poppedCard.stackID = -1
        return poppedCard
    }

    /**
     * Reset the cardStack attributes (used when a pop method results in an empty stack).
     */
    private fun resetCardStack() { // Used solely to prevent faulty use of stacks considered discarded.
        head = null
        tail = null
        size = 0
    }

    /**
     * Standard push, pushing a cardStack to the tail of the stack
     */
    fun pushStack(stack: CardStack) { // Push a CardStack to tail
        if (stack.size == 0) return
        var card = stack.head
        for (i in 1..stack.size) {
            card!!.stackID = stackID
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

    /**
     * Standard pop, popping a stack off the tail of the stack.
     */
    fun popStack(cardsToPop: Int): CardStack {
        if (size == 0 || size < cardsToPop) throw Exception("Empty stack pop")

        val poppedStack = CardStack(-1)
        if (size - cardsToPop == 0) poppedStack.pushStack(this)
        else {
            poppedStack.tail = tail
            poppedStack.head = tail
            // TODO: The following for loop is NOT needed if you already know the address of
            //  poppedStack.head, changing this will make this O(1), instead of O(n)
            for (i in 1 until cardsToPop) poppedStack.head = poppedStack.head!!.prev
            tail = poppedStack.head!!.prev
            tail!!.next = null
            poppedStack.head!!.prev = null
            poppedStack.size = cardsToPop
            size -= cardsToPop
        }
        return poppedStack
    }

    /**
     * Special push used for stock specifically when flipping talon. This pushes the stack to head
     * instead of tail.
     */
    fun pushStackToHead(stack: CardStack) {
        if (stack.size == 0) {
            throw Exception("Trying to push empty stack, stackID: " + stack.stackID.toString())
        }

        val reversedStack = CardStack(-1)
        while (stack.size > 0)
            reversedStack.pushCard(stack.popCard())

        var card = reversedStack.head
        for (i in 1..reversedStack.size) {
            card!!.stackID = stackID
            card = card.next
        }

        if (size == 0) {
            head = reversedStack.head
            tail = reversedStack.tail
            size = reversedStack.size

        } else {
            head!!.prev = reversedStack.tail
            reversedStack.tail!!.next = head
            head = reversedStack.head
            size += reversedStack.size
        }
        stack.resetCardStack()
    }

    fun copyOf(): CardStack {
        val newStack = CardStack(stackID)
        var cardToCopy = head
        while (cardToCopy != null) {
            newStack.pushCard(cardToCopy.copyOf())
            cardToCopy = cardToCopy.next
        }
        return newStack
    }

    /**
     * Gets the first instance of a revealed card in a stack.
     */
    fun getStackHighCard(): Card? {
        return when (hiddenCards) {
            0 -> head // Can be null
            else -> {
                var card = tail
                while (true) {
                    if (card!!.prev!!.suit == Suit.NA) break
                    else card = card.prev
                }
                card
            }
        }
    }
    fun checkHiddenCards(): Int{
        var hiddenCardsInStack = 0
        var temp = this.head
        while(temp!=null){
            if(temp.rank==Rank.NA||temp.suit==Suit.NA){
                hiddenCardsInStack++
            }
        temp=temp.next
        }
        return hiddenCardsInStack
    }

    override fun toString(): String {
        var ret = "["
        var cursor = head
        while (cursor != null) {
            ret += cursor.toString()
            if (cursor.next != null)
                ret += ", "
            cursor = cursor.next
        }
        ret += "]"

        return ret
    }
}
