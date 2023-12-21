package com.example.diceroom.authentication

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.Utils
import com.example.diceroom.databinding.ProfileConfigActivityViewBinding
import com.example.diceroom.models.UserManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileConfigActivity : AppCompatActivity() {
    private lateinit var bind: ProfileConfigActivityViewBinding
    private var selectedImageUri: Uri? = null
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ProfileConfigActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val authManager = AuthManager()
        val userManager = UserManager()
        val currentUserId = authManager.getCurrentUser()?.uid

        if (currentUserId != null) {
            userManager.getUserById(currentUserId) { user ->
                if (user != null) {
                    bind.birthdateEditText.setText(user.birthdate)
                    bind.nameEditText.setText(user.firstname)
                    bind.nicknameEditText.setText(user.nickname)
                    utils.downloadImageFromFirebaseStorage(this, user.avatar) { isSuccess, file ->
                        if (isSuccess) {
                            bind.profileImageView.setImageURI(Uri.fromFile(file))
                        }
                    }
                }
            }
        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            try {
                selectedImageUri = it
                bind.profileImageView.setImageURI(it)
            } catch (e: Exception) {
                utils.showToast(this, "Error occurred while picking image")
            }
        }

        bind.confirmProfileButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.nicknameEditText.text) || TextUtils.isEmpty(bind.nameEditText.text) || TextUtils.isEmpty(
                    bind.birthdateEditText.text
                )
            ) {
                utils.showToast(this, "You need to fill in all fields")
            } else {
                val updatedFields = mutableMapOf(
                    "nickname" to bind.nicknameEditText.text.toString(),
                    "firstname" to bind.nameEditText.text.toString(),
                    "birthdate" to bind.birthdateEditText.text.toString()
                )

                if (currentUserId != null) {
                    utils.uploadImageToFirebaseStorage(selectedImageUri) { isImageUploadSuccess, imageUploadMessage ->
                        utils.handleFirebaseResult(
                            isImageUploadSuccess,
                            imageUploadMessage,
                            this,
                            "Image upload successful",
                            "Image upload failed"
                        )

                        if (isImageUploadSuccess) {
                            updatedFields["avatar"] = imageUploadMessage.toString()

                            userManager.updateUserFields(
                                currentUserId, updatedFields
                            ) { isUserUpdateSuccess, userUpdateMessage ->
                                utils.handleFirebaseResult(
                                    isUserUpdateSuccess,
                                    userUpdateMessage,
                                    this,
                                    "Update successful",
                                    "Update failed"
                                )
                            }
                        }
                    }
                } else {
                    utils.showToast(this, "User not found")
                }

            }
        }

        bind.profileImageView.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    // TODO: implement on back pressed -> do nothing if first login // confirm -> main screen // handle if not first login

    fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val selectedCalendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                        ?: Date()
                }

                if (selectedCalendar.timeInMillis > System.currentTimeMillis()) {
                    utils.showToast(this, "Invalid birthdate. Please select a valid date.")
                } else {
                    bind.birthdateEditText.setText(selectedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

}