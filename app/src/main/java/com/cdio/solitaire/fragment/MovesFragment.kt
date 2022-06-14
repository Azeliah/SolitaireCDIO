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
import com.google.android.material.textfield.TextInputLayout

class MovesFragment : Fragment() {

    private var moveText: TextView? = null

    private var revealedCardText: TextView? = null

    private var nextButton: Button? = null

    private var wrongCardButton: Button? = null

    private var inputField: TextInputLayout? = null

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

        inputField = view.findViewById(R.id.input_field)
        inputField?.isVisible = false

        nextButton = view.findViewById(R.id.open_camera_button)
        nextButton?.setOnClickListener { (navigateToCamera(view)) }

        wrongCardButton = view.findViewById(R.id.wrong_card_button)
        wrongCardButton?.setOnClickListener { getCardInput(view) }
    }

    private fun navigateToCamera(view: View) {
        nextButton?.text = getString(R.string.next_move)
        nextButton?.setOnClickListener { getNextMove(view) }
        Navigation.findNavController(view).navigate(R.id.action_moves_to_camera)
    }

    private fun changeNextMoveText(str: String) {
        moveText?.text = str
    }

    private fun getNextMove(view: View) {
        val nextMove = GameStateController.getLastMove()
        changeNextMoveText(nextMove.toString())

        if (nextMove.NewPictureNeeded) {
            flipButton(view)
        }
    }

    private fun flipButton(view: View) {
        nextButton?.text = getString(R.string.open_camera)
        nextButton?.setOnClickListener { navigateToCamera(view) }
    }

    // If a card can't be recognized, make the user input the card
    private fun getCardInput(view: View) {

        nextButton?.text = getString(R.string.ok)

        inputField?.isVisible = true

        nextButton?.setOnClickListener {
            val card = inputField?.editText?.text.toString()

            // If nothing has been input, do nothing
            if (card != "") {

                //TODO: Do something with the card
                revealedCardText?.text = card

                inputField?.isVisible = false
                nextButton?.text = getString(R.string.next_move)

                // Since card input will only happen after taking a photo, get the next move
                nextButton?.setOnClickListener {
                    getNextMove(view)
                }
            }
        }
    }
}
