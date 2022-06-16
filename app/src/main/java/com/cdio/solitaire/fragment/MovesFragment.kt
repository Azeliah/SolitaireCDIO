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
import com.cdio.solitaire.model.*
import com.google.android.material.textfield.TextInputLayout

class MovesFragment : Fragment() {

    private lateinit var moveText: TextView

    private lateinit var revealedCardText: TextView

    private lateinit var nextButton: Button

    private lateinit var wrongCardButton: Button

    private lateinit var inputField: TextInputLayout

    // List used for testing
    //private lateinit var moveList: MutableList<Move>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Add moves to test list
//        moveList = mutableListOf(Move(MoveType.MOVE_FROM_TALON, GameStateController.gameState.talon, GameStateController.gameState.tableaux[5], Card(1, Rank.FOUR, Suit.CLUBS)))
//        moveList.add(Move(MoveType.MOVE_STACK, GameStateController.gameState.tableaux[1], GameStateController.gameState.tableaux[3], Card(2, Rank.ACE, Suit.DIAMONDS)))
//        moveList.add(Move(MoveType.MOVE_STACK, GameStateController.gameState.tableaux[3], GameStateController.gameState.tableaux[1], Card(3, Rank.TEN, Suit.HEARTS), Card(3, Rank.TEN, Suit.HEARTS)))

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
        val nextMove = GameStateController.getLastMove()

        wrongCardButton.isVisible = false

        // Use this when testing
//        val nextMove = moveList[0]
//        moveList.removeAt(0)

        changeNextMoveText(nextMove.toString())

        if (nextMove.cardToUpdate != null || nextMove.moveType == MoveType.DEAL_CARDS) {
            flipButton(view)
        }
    }

    /**
     * Change button to navigate to the camera, when there are no more moves to display
     */
    private fun flipButton(view: View) {
        nextButton.text = getString(R.string.open_camera)
        nextButton.setOnClickListener { navigateToCamera(view) }
    }

    /**
     * Get user input, to change a card
     */
    private fun getCardInput(view: View) {

        nextButton.text = getString(R.string.ok)

        inputField.isVisible = true

        nextButton.setOnClickListener {
            val card = inputField.editText?.text.toString()

            // If nothing has been input, do nothing
            if (card != "") {

                //TODO: Do something with the card
                revealedCardText.text = card

                inputField.isVisible = false
                nextButton.text = getString(R.string.next_move)

                // Since card input will only happen after taking a photo, get the next move
                nextButton.setOnClickListener {
                    getNextMove(view)
                }
            }
        }
    }

    fun changeLastRevealedCard(card: String){
        //TODO: Find out how to check what card has been revealed(Maybe variable in GameStateController)
        wrongCardButton.isVisible = true
        revealedCardText.isVisible = true
        revealedCardText.text = card
    }
}
