package com.example.diceroom.meetings

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentAddMeetingBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Utils
import java.text.DateFormat
import java.util.Date

class AddMeetingFragment : Fragment() {
    private lateinit var bind: FragmentAddMeetingBinding
    private val utils = Utils()
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
                requireContext(),
                bind.meetingDate,
                true
            )
        }

        val authManager = AuthManager()
        val currentUserId = authManager.getCurrentUser()?.uid!!

        val meetingLevel = bind.meetingLevel.text
        val meetingTitle = bind.meetingTitle.text
        val gameName = bind.gameName.text
        val meetingDate = bind.meetingDate.text
        val meetingsMembersNumber = bind.meetingsMembersNumber.text
        val meetingLocation = bind.meetingLocation.text
        val meetingDescription = bind.meetingsDescription.text

        bind.confirmProfileButton.setOnClickListener {
            if (TextUtils.isEmpty(meetingLevel) || TextUtils.isEmpty(meetingTitle) ||
                TextUtils.isEmpty(gameName) || TextUtils.isEmpty(meetingDate) || TextUtils.isEmpty(
                    meetingsMembersNumber
                ) || TextUtils.isEmpty(meetingLocation)
            ) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else {
                if (selectedImageUri == null) {
                    utils.showToast(requireContext(), "You need to pick an avatar")
                } else {
                    utils.uploadImageToFirebaseStorage(selectedImageUri) { isImageUploadSuccess, imageUploadMessage ->
                        utils.handleFirebaseResult(
                            isImageUploadSuccess,
                            imageUploadMessage,
                            requireContext(),
                            "Image upload successful",
                            "Image upload failed"
                        )
                        if (isImageUploadSuccess) {
                            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
                            val currentDate = dateFormat.format(Date())

                            val meetingModel = MeetingModel(
                                meetingTitle.toString(),
                                gameName.toString(),
                                meetingLocation.toString(),
                                currentDate,
                                meetingDate.toString(),
                                currentUserId,
                                meetingLevel.toString(),
                                meetingDescription.toString(),
                                imageUploadMessage.toString(),
                                Integer.parseInt(meetingsMembersNumber.toString()),
                                mutableListOf(currentUserId)
                            )

                            addAndHandleMeeting(meetingModel)
                        }
                    }
                }
            }

        }

        return bind.root
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
                findNavController().popBackStack(R.id.mainMenuFragment, false)
            }
        }
    }
}