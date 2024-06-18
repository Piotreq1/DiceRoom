package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentMeetingDetailsBinding
import com.example.diceroom.fcm.FCMNotifications
import com.example.diceroom.fcm.NotificationBody
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingDetailsBinding.inflate(inflater, container, false)
        val args = arguments
        val id = args?.getString(Constants.MEETING_ID)

        bind.chatCv.setOnClickListener {
            findNavController().navigate(
                R.id.action_meetingDetailsFragment_to_chatFragment, bundleOf(
                    Constants.MEETING_ID to id
                )
            )
        }

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
        if (participants == null) return

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
                    setVisibility(VISIBLE, GONE, GONE)
                }
            }
        }
    }

    private fun checkIfJoined(participants: List<String>?) {
        if (participants == null) return
        if (participants.contains(userId)) {
            setVisibility(GONE, VISIBLE, VISIBLE)
        }
    }

    private fun setVisibility(notJoined: Int, joined: Int, participants: Int) {
        bind.notJoinedLayout.visibility = notJoined
        bind.joinedLayout.visibility = joined
        bind.participantsImage.visibility = participants
    }

    private fun setOnJoinClick(maxMembers: Int, participants: List<String>?) {
        if (participants == null) return
        bind.joinMeeting.setOnClickListener {
            if (participants.size >= maxMembers) {
                utils.showToast(requireContext(), "Meeting is full!")
                return@setOnClickListener
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
                    val joinedNotification =
                        NotificationBody("Congrats!", "Successfully joined ${bind.titleLabel.text}")
                    FCMNotifications().createMessagingTopicForMeeting(this.requireContext(), meetingId, joinedNotification)
                    setVisibility(GONE, VISIBLE, VISIBLE)
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