package com.example.arkki

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hanhipasi.luontoarkki.R


class GameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startGame = view.findViewById<Button>(R.id.start)
        val quitGame = view.findViewById<Button>(R.id.quit)
        Log.d("xdlsd", "buttons initialised")
        val gameFrag = PlayGameFragment()
        val fManager = fragmentManager
        val transaction = fManager?.beginTransaction()

        startGame?.setOnClickListener {
            Log.d("xdlsd", "Start game")
            transaction?.replace(R.id.game_fragments, gameFrag)
            transaction?.addToBackStack(null)
            transaction?.commit()

        }

        quitGame?.setOnClickListener {
            Log.d("xdlsd", "Quit game")
            activity?.finish()

        }
    }



}
