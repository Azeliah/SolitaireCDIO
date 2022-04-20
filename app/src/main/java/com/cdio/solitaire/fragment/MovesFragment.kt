package com.cdio.solitaire.fragment

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import com.cdio.solitaire.R

class MovesFragment : Fragment() {

    private var linLayout : LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linLayout = view.findViewById(R.id.lin_layout)

        addPossibleMove("8S -> F1")
        addPossibleMove("1C -> F2")
        addPossibleMove("5H -> F3")

        view.findViewById<Button>(R.id.open_camera_button).setOnClickListener { (navigateToCamera(view)) }
    }

    private fun navigateToCamera(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_moves_to_camera)
    }

    private fun addPossibleMove(str : String){
        val newText = TextView(view?.context)

        newText.text = str
        newText.textSize = 20f

        //FIXME: Find out why this line makes the app crash
        //newText.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        linLayout?.addView(newText)
    }
}
