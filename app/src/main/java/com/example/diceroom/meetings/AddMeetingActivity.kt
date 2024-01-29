package com.example.diceroom.meetings

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.databinding.FragmentAddMeetingBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Utils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddMeetingActivity : AppCompatActivity() {
    private lateinit var bind: FragmentAddMeetingBinding
    private val utils = Utils()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = FragmentAddMeetingBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            try {
                selectedImageUri = it
                bind.meetingImageView.setImageURI(it)
            } catch (e: Exception) {
                utils.showToast(this, "Error occurred while picking image")
            }
        }

        bind.meetingImageView.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        bind.meetingDate.setOnClickListener { showDatePickerDialog() }

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
                utils.showToast(this, "You need to fill in all fields")
            } else {
                if (selectedImageUri == null) {
                    utils.showToast(this, "You need to pick an avatar")
                } else {
                    utils.uploadImageToFirebaseStorage(selectedImageUri) { isImageUploadSuccess, imageUploadMessage ->
                        utils.handleFirebaseResult(
                            isImageUploadSuccess,
                            imageUploadMessage,
                            this,
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
    }


    private fun addAndHandleMeeting(meetingModel: MeetingModel) {
        val meetingManager = MeetingManager()
        meetingManager.addMeeting(meetingModel) { isSuccessful, message ->
            utils.handleFirebaseResult(
                isSuccessful,
                message,
                this,
                "Meeting created!",
                "Meeting creation failed"
            )

            if (isSuccessful) {
                finish()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val selectedCalendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                        ?: Date()
                }
                if (selectedCalendar.timeInMillis < System.currentTimeMillis()) {
                    utils.showToast(this, "Please select a valid future date.")
                } else {
                    bind.meetingDate.setText(selectedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            show()
        }
    }

}