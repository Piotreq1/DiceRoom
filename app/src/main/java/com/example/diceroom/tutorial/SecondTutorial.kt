package com.example.diceroom.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.diceroom.R
import com.example.diceroom.utils.Utils


class SecondTutorial : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second_tutorial, container, false)
        Utils().loadGlide(
            requireContext(),
            R.drawable.consultation,
            view.findViewById(R.id.imageView)
        )
        return view
    }
}