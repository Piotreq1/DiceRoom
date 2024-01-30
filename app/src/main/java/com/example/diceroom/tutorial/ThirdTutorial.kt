package com.example.diceroom.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.diceroom.R

class ThirdTutorial : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third_tutorial, container, false)

        val gifImageView: ImageView = view.findViewById(R.id.imageView)
        Glide.with(this)
            .load(R.drawable.online_learning)
            .placeholder(R.drawable.logo)
            .into(gifImageView)
        return view
    }
}