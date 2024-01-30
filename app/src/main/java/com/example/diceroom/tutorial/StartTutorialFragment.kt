package com.example.diceroom.tutorial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R


class StartTutorialFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences = requireActivity().getSharedPreferences("isFirstRun", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)


        if (isFirstRun) {
            findNavController().navigate(R.id.action_startTutorial_to_tutorial)
        } else {
            findNavController().navigate(R.id.action_startTutorial_to_selectLogin)
        }


        return inflater.inflate(R.layout.start_tutorial_fragment, container, false)
    }
}