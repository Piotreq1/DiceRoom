package com.example.diceroom.meetings

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentAddMeetingBinding
import com.example.diceroom.fcm.NotificationHandler
import com.example.diceroom.fcm.NotificationBody
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.ChatManager
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMeetingFragment : Fragment() {
    private lateinit var bind: FragmentAddMeetingBinding
    private val utils = Utils()
    private var selectedImageUri: Uri? = null
    private lateinit var selectedLevel: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentAddMeetingBinding.inflate(layoutInflater)


        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            try {
                selectedImageUri = it
                bind.meetingImageView.setImageURI(it)
            } catch (e: Exception) {
                utils.showToast(requireContext(), "Error occurred while picking image")
            }
        }

        bind.meetingImageView.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        bind.meetingDate.setOnClickListener {
            utils.showDatePickerDialog(
                requireContext(), bind.meetingDate, true
            )
        }

        val authManager = AuthManager()
        val currentUserId = authManager.getCurrentUser()?.uid!!

        configureLevelSpinner()

        bind.addMeetingButton.setOnClickListener {
            val meetingTitle = bind.meetingTitle.text
            val gameName = bind.gameName.text
            val meetingDate = bind.meetingDate.text
            val meetingsMembersNumber = bind.meetingsMembersNumber.text
            val meetingLocation = bind.meetingLocation.text
            val meetingDescription = bind.meetingsDescription.text

            if (TextUtils.isEmpty(selectedLevel) || TextUtils.isEmpty(meetingTitle) || TextUtils.isEmpty(
                    gameName
                ) || TextUtils.isEmpty(meetingDate) || TextUtils.isEmpty(
                    meetingsMembersNumber
                ) || TextUtils.isEmpty(meetingLocation)
            ) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else {
                if (selectedImageUri == null) {
                    utils.showToast(requireContext(), "You need to pick an avatar")
                } else {
                    bind.addMeetingButton.isEnabled = false
                    utils.uploadImageToFirebaseStorage(selectedImageUri) { isImageUploadSuccess, imageUploadMessage ->
                        utils.handleFirebaseResult(
                            isImageUploadSuccess,
                            imageUploadMessage,
                            requireContext(),
                            "Image upload successful",
                            "Image upload failed"
                        )
                        if (isImageUploadSuccess) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val currentDate = dateFormat.format(Date())

                            val meetingModel = MeetingModel(
                                meetingTitle.toString(),
                                gameName.toString(),
                                meetingLocation.toString(),
                                currentDate,
                                meetingDate.toString(),
                                currentUserId,
                                selectedLevel,
                                meetingDescription.toString(),
                                imageUploadMessage.toString(),
                                Integer.parseInt(meetingsMembersNumber.toString()),
                                mutableListOf(currentUserId)
                            )

                            addAndHandleMeeting(meetingModel)
                        } else {
                            bind.addMeetingButton.isEnabled = true
                        }
                    }
                }
            }

        }

        return bind.root
    }

    private fun configureLevelSpinner() {
        val meetingLevel = bind.meetingLevel
        val meetingLevels = Constants.MeetingLevel.entries.toTypedArray()
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, meetingLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        meetingLevel.adapter = adapter

        meetingLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long
            ) {
                val selectedLevel = meetingLevels[position]
                this@AddMeetingFragment.selectedLevel = selectedLevel.toString()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }


    private fun addAndHandleMeeting(meetingModel: MeetingModel) {
        val meetingManager = MeetingManager()
        meetingManager.addMeeting(meetingModel) { isSuccessful, message ->
            utils.handleFirebaseResult(
                isSuccessful,
                message,
                requireContext(),
                "Meeting created!",
                "Meeting creation failed"
            )

            if (isSuccessful) {
                message?.let { createChatForMeeting(it) }
                val joinedNotification =
                    NotificationBody("Congrats!", "Successfully joined ${meetingModel.title}")
                message?.let { NotificationHandler().createMessagingTopicForMeeting(this.requireContext(),it, joinedNotification) }
            }
        }
    }

    private fun createChatForMeeting(meetingId: String) {
        ChatManager().createChat(
            AuthManager().getCurrentUser()?.uid!!, meetingId
        ) { isSuccess, message ->
            utils.handleFirebaseResult(
                isSuccess,
                message,
                requireContext(),
                "Chat for meeting created!",
                "Chat creation failed"
            )
            if (isSuccess) {
                findNavController().popBackStack(R.id.mainMenuFragment, false)
            }
        }
    }
}