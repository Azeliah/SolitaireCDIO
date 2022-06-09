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

    private var button: Button? = null

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
        
        inputField = view.findViewById(R.id.input_field)
        inputField?.isVisible = false

        button = view.findViewById(R.id.open_camera_button)
        button?.setOnClickListener { (navigateToCamera(view)) }
    }

    private fun navigateToCamera(view: View) {
        button?.text = getString(R.string.next_move)
        button?.setOnClickListener { getNextMove(view) }
        Navigation.findNavController(view).navigate(R.id.action_moves_to_camera)
    }

    private fun changeNextMoveText(str : String){
        moveText?.text = str
    }

    private fun getNextMove(view: View) {
        val nextMove = GameController.getLastMove()
        changeNextMoveText(nextMove.toString())
        if(nextMove.NewPictureNeeded){
            flipButton(view)
        }
    }

    private fun flipButton(view: View){
        button?.text = getString(R.string.open_camera)
        button?.setOnClickListener { navigateToCamera(view)}
    }

    // If a card can't be recognized, make the user input the card
    private fun getCardInput(str: String, view: View){
        changeNextMoveText(str)

        button?.text = getString(R.string.ok)

        inputField?.isVisible = true

        button?.setOnClickListener {
            val card = inputField?.editText?.text.toString()

            if(card != ""){

                //TODO: Do something with the card

                inputField?.isVisible = false
                button?.text = getString(R.string.next_move)

                // Since card input will only happen after taking a photo, get the next move
                button?.setOnClickListener {
                    getNextMove(view)
                }
            }
        }
    }
}
