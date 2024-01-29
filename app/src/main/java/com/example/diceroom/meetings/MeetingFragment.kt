package com.example.diceroom.meetings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.diceroom.databinding.FragmentMeetingBinding


class MeetingFragment : Fragment() {
    private lateinit var bind: FragmentMeetingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingBinding.inflate(layoutInflater)


        bind.addMeetingButton.setOnClickListener {
            val intent = Intent(requireContext(), AddMeetingActivity::class.java)
            startActivity(intent)
        }

        return bind.root
    }
}