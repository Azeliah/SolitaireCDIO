package com.cdio.solitaire.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.cdio.solitaire.R
import com.cdio.solitaire.controller.GameStateController
import com.cdio.solitaire.controller.StrategyController
import com.cdio.solitaire.model.*
import com.google.android.material.textfield.TextInputLayout

class MovesFragment : Fragment() {

    private lateinit var moveText: TextView

    private lateinit var revealedCardText: TextView

    private lateinit var nextButton: Button

    private lateinit var wrongCardButton: Button

    private lateinit var inputField: TextInputLayout

    private lateinit var movesMadeText: TextView

    private lateinit var hiddenCardsText: TextView

    private var revealedCards: MutableList<Card> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        moveText = view.findViewById(R.id.move_text)
        revealedCardText = view.findViewById(R.id.revealed_card_text)
        revealedCardText.isVisible = false

        inputField = view.findViewById(R.id.input_field)
        inputField.isVisible = false

        nextButton = view.findViewById(R.id.open_camera_button)
        nextButton.setOnClickListener { getNextMove(view) }

        wrongCardButton = view.findViewById(R.id.wrong_card_button)
        wrongCardButton.isVisible = false
        wrongCardButton.setOnClickListener { getCardInput(view) }

        movesMadeText = view.findViewById(R.id.moves_counter)
        movesMadeText.text = getString(R.string.moves_made, StrategyController.gsc.gameState.moves.size)

        hiddenCardsText = view.findViewById(R.id.hidden_cards)
        hiddenCardsText.text = getString(R.string.hidden_cards, StrategyController.gsc.getNumberOfHiddenCards())

        changeLastRevealedCard(view)
    }

    private fun navigateToCamera(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_moves_to_camera)
    }

    private fun changeNextMoveText(str: String) {
        moveText.text = str
    }

    /**
     * Get next move from moveQueue, and display it. If camera is needed, change button function
     */
    private fun getNextMove(view: View) {
        val nextMove = StrategyController.nextMove()

        wrongCardButton.isVisible = false

        changeNextMoveText(nextMove.toString())

        movesMadeText.text = getString(R.string.moves_made, StrategyController.gsc.gameState.moves.size)
        // If the game is won or lost, reset the GameState to allow new game
        if (nextMove.moveType == MoveType.GAME_WON || nextMove.moveType == MoveType.GAME_LOST) {
            StrategyController.gsc.resetGameState()
            nextButton.text = getString(R.string.play_again)
            nextButton.setOnClickListener { navigateToCamera(view) }
        } else if (nextMove.cardToUpdate != null || nextMove.moveType == MoveType.DEAL_CARDS) {
            flipButton(view, getString(R.string.open_camera))
        }
    }

    /**
     * Change button to navigate to the camera, when there are no more moves to display
     */
    private fun flipButton(view: View, text: String) {
        nextButton.text = text
        nextButton.setOnClickListener { navigateToCamera(view) }
    }

    /**
     * Get user input, to change a card
     * Format for changing card: [cardIndex(1-7)] [cardRank(1-13)][cardSuit("H", "C"...)]
     * The cardIndex is only needed if it's the first 7 seven card, for others, just put rank/suit
     */
    private fun getCardInput(view: View) {

        nextButton.text = getString(R.string.ok)

        inputField.isVisible = true

        nextButton.setOnClickListener {
            val card = inputField.editText?.text.toString().uppercase()

            // If nothing has been input, do nothing
            if (card != "") {
                val words = card.split(" ")

                var cardIndex = 0

                val rank = words[words.size - 1].dropLast(1).toIntOrNull()
                val suit = words[words.size - 1].last().toString()

                // Check the input, and do nothing if it's invalid
                if (words.size == 2) {
                    if (words[0].toIntOrNull() == null || words[0].toInt() < 1 || words[0].toInt() > 7) {
                        // Exit function
                        return@setOnClickListener
                    }
                    // If user specified which card to update, change which cardIndex is being updated
                    cardIndex = words[0].toInt() - 1
                }
                if (rank == null || rank < 1 || rank > 13 || suit !in "HSDC") {
                    // Exit function
                    return@setOnClickListener
                }

                val newSuit = when (suit) {
                    "C" -> 1
                    "D" -> 2
                    "H" -> 3
                    "S" -> 4
                    else -> 0
                }

                revealedCards[cardIndex].rank = Rank.values()[rank]
                revealedCards[cardIndex].suit = Suit.values()[newSuit]

                changeLastRevealedCard(view)

                inputField.isVisible = false
                nextButton.text = getString(R.string.next_move)

                // Since card input will only happen after taking a photo, get the next move
                nextButton.setOnClickListener {
                    getNextMove(view)
                }
            }
        }
    }

    /**
     * Get the last card that was revealed, to allow user to change it.
     * On the first round, it displays all 7 cards
     */
    private fun changeLastRevealedCard(view: View) {
        revealedCards.clear()

        // If it's the first scan of cards, all all new 7 cards, else only add newest card revealed
        if (StrategyController.gsc.getLastMove().moveType == MoveType.DEAL_CARDS) {
            for (i in StrategyController.gsc.gameState.tableaux) {
                revealedCards.add(i.tail!!)
            }
        } else {
            revealedCards.add(StrategyController.gsc.getLastMove().cardToUpdate!!)
        }

        var firstScan = true
        for (card in revealedCards) {
            if (card.suit != Suit.NA) {
                firstScan = false
            }
        }
        if (firstScan) {
            flipButton(view, getString(R.string.start_game))
        } else {
            wrongCardButton.isVisible = true
            revealedCardText.isVisible = true

            var newText = ""

            for (card: Card in revealedCards) {
                newText += "$card - "
            }

            // Remove the last " - " from the text
            revealedCardText.text = newText.dropLast(3)
        }
    }
}
