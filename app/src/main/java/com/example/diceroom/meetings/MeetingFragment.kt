package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.diceroom.databinding.FragmentMeetingBinding
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Utils

class MeetingFragment : Fragment() {
    private val meetingManager = MeetingManager()
    private val utils = Utils()
    private lateinit var bind: FragmentMeetingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        bind = FragmentMeetingBinding.inflate(layoutInflater)

        // TODO: ITS TEST to be deleted later
        val exampleMeeting = MeetingModel(
            title = "Team Meeting",
            game = "Chess",
            location = "Conference Room",
            creationDate = "2024-01-25",
            startDate = "2024-01-30",
            ownerId = "123456789",
            level = "Intermediate",
            description = "Discuss strategy for upcoming chess tournament",
            image = "meeting_image.jpg",
            participants = listOf("123456789")
        )
        bind.addMeetingButton.setOnClickListener{
            meetingManager.addMeeting(exampleMeeting) { isSuccess, message ->
                utils.handleFirebaseResult(
                    isSuccess,
                    message,
                    requireContext(),
                    "Meeting successfully created",
                    "Meeting creation failure"
                )
            }
        }


        return bind.root
    }
}