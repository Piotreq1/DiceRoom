package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.diceroom.databinding.FragmentMeetingDetailsBinding
import com.example.diceroom.utils.Constants


class MeetingDetailsFragment : Fragment() {
    private lateinit var bind: FragmentMeetingDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingDetailsBinding.inflate(inflater, container, false)
        val args = arguments
        val id = args?.getString(Constants.GAME_ID)
        bind.title.text = "XDDDD + $id"
        return bind.root
    }
}