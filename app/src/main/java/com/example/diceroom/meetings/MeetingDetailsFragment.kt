package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentMeetingDetailsBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MeetingDetailsFragment : Fragment() {
    private lateinit var bind: FragmentMeetingDetailsBinding
    private lateinit var meeting: MeetingModel
    private var utils = Utils()
    private var meetingManager = MeetingManager()
    private lateinit var meetingId: String
    private val userId = AuthManager().getCurrentUser()?.uid!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingDetailsBinding.inflate(inflater, container, false)
        val args = arguments
        val id = args?.getString(Constants.MEETING_ID)

        if (id == null) {
            utils.showToast(requireContext(), "Error occurred with meeting id")
            findNavController().popBackStack(R.id.mainMenuFragment, false)
        } else {
            meetingId = id
            lifecycleScope.launch {
                val tempMeeting = fetchMeetingInfo()
                if (tempMeeting == null) {
                    utils.showToast(requireContext(), "Error occurred with meeting")
                    findNavController().popBackStack(R.id.mainMenuFragment, false)
                } else {
                    meeting = tempMeeting

                    checkIfJoined(meeting.participants)
                    setOnData()
                    setOnJoinClick(meeting.maxMembers, meeting.participants)
                    setOnLeaveClick(meeting.participants)
                }
            }
        }
        return bind.root
    }

    private fun setOnLeaveClick(participants: List<String>?) {
        if (participants != null) {
            bind.leaveCv.setOnClickListener {
                meetingManager.deleteParticipant(meetingId, userId) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        requireContext(),
                        "You left this meeting!",
                        "Something went wrong!"
                    )
                    if (isSuccess) {
                        bind.notJoinedLayout.visibility = VISIBLE
                        bind.joinedLayout.visibility = GONE
                    }
                }
            }
        }
    }

    private fun checkIfJoined(participants: List<String>?) {
        if (participants != null) {
            if (participants.contains(userId)) {
                bind.notJoinedLayout.visibility = GONE
                bind.joinedLayout.visibility = VISIBLE
            }
        }
    }

    private fun setOnJoinClick(maxMembers: Int, participants: List<String>?) {
        bind.joinMeeting.setOnClickListener {
            if (participants != null) {
                if (participants.size >= maxMembers) {
                    utils.showToast(requireContext(), "Meeting is full!")
                    return@setOnClickListener
                }
            }

            meetingManager.addParticipant(meetingId, userId) { isSuccess, message ->
                utils.handleFirebaseResult(
                    isSuccess,
                    message,
                    requireContext(),
                    "You joined this meeting!",
                    "Something went wrong!"
                )

                if (isSuccess) {
                    bind.notJoinedLayout.visibility = GONE
                    bind.joinedLayout.visibility = VISIBLE
                }
            }
        }
    }

    private suspend fun fetchMeetingInfo(): MeetingModel? {
        return suspendCoroutine { continuation ->
            meetingManager.getMeetingById(meetingId) {
                continuation.resume(it)
            }
        }
    }

    private fun setOnData() {
        bind.address.text = meeting.location
        bind.titleLabel.text = meeting.title
        bind.gameName.text = meeting.game
        bind.meetingDate.text = meeting.startDate
        bind.meetingLevel.text = meeting.level
        bind.description.text = meeting.description
        utils.loadGlide(requireContext(), meeting.image, bind.meetingThumbnail)
    }
}