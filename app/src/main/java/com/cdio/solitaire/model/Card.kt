package com.cdio.solitaire.model

data class Card(
    var rank: Int = 0, // TODO: enum class for rank (short format)
    var suit: Int = 0, // TODO: enum class for suit (short format)
    var listID: Int,
    var prev: Card? = null,
    var next: Card? = null
)
