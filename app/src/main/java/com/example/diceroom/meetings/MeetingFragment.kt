package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentMeetingBinding


class MeetingFragment : Fragment() {
    private lateinit var bind: FragmentMeetingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingBinding.inflate(layoutInflater)


        bind.addMeetingButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_addMeetingActivity)
        }

        return bind.root
    }
}