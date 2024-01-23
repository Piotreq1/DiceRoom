package com.example.diceroom.authentication

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.MainMenuActivity
import com.example.diceroom.databinding.ProfileConfigActivityViewBinding
import com.example.diceroom.models.UserManager
import com.example.diceroom.utils.Utils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileConfigActivity : AppCompatActivity() {
    private lateinit var bind: ProfileConfigActivityViewBinding
    private var currentUserId: String? = null
    private var selectedImageUri: Uri? = null
    private val utils = Utils()
    private var isFirstTimeConfiguration: Boolean = false
    private val userManager = UserManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ProfileConfigActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val authManager = AuthManager()
        currentUserId = authManager.getCurrentUser()?.uid!!

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFirstTimeConfiguration) {
                    utils.showToast(bind.root.context, "You need to configure your profile!")
                } else {
                    finish()
                }
            }
        })

        if (currentUserId != null) {
            userManager.getUserById(currentUserId!!) { user ->
                if (user != null) {
                    isFirstTimeConfiguration = TextUtils.isEmpty(user.nickname)
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

                if (selectedImageUri == null) {
                    if (!isFirstTimeConfiguration) {
                        updateUserInfo(updatedFields)
                    } else {
                        utils.showToast(this, "You need to pick an avatar")
                    }
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
                            updatedFields["avatar"] = imageUploadMessage.toString()
                            updateUserInfo(updatedFields)
                        }
                    }
                }
            }
        }

        bind.profileImageView.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
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
        ).apply {
            show()
        }
    }

    private fun updateUserInfo(updatedFields: Map<String, String>) {
        currentUserId?.let {
            userManager.updateUserFields(
                it, updatedFields
            ) { isUserUpdateSuccess, userUpdateMessage ->
                utils.handleFirebaseResult(
                    isUserUpdateSuccess,
                    userUpdateMessage,
                    this,
                    "Update successful",
                    "Update failed"
                )

                if (isUserUpdateSuccess) {
                    isFirstTimeConfiguration = false
                    val intent = Intent(this, MainMenuActivity::class.java)
                    intent.putExtra("currentItem", 2)
                    startActivity(intent)
                }
            }
        }
    }

}