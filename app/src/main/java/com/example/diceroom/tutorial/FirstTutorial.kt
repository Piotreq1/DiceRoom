package com.example.diceroom.tutorial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.diceroom.R


class FirstTutorial : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_tutorial, container, false)

        val gifImageView: ImageView = view.findViewById(R.id.imageView)
        Glide.with(this)
            .load(R.drawable.conference_room)
            .placeholder(R.drawable.logo)
            .into(gifImageView)
        return view
    }
}