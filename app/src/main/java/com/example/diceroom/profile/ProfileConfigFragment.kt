package com.example.diceroom.profile

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentProfileConfigBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Utils


class ProfileConfigFragment : Fragment() {
    private lateinit var bind: FragmentProfileConfigBinding
    private var currentUserId: String? = null
    private var selectedImageUri: Uri? = null
    private val utils = Utils()
    private var isFirstTimeConfiguration: Boolean = false
    private val userManager = UserManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentProfileConfigBinding.inflate(layoutInflater)


        val authManager = AuthManager()
        currentUserId = authManager.getCurrentUser()?.uid!!

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isFirstTimeConfiguration) {
                utils.showToast(bind.root.context, "You need to configure your profile!")
            } else {
                findNavController().popBackStack(R.id.mainMenuFragment, false)
            }
        }



        if (currentUserId != null) {
            userManager.getUserById(currentUserId!!) { user ->
                if (user != null) {
                    isFirstTimeConfiguration = TextUtils.isEmpty(user.nickname)
                    bind.birthdateEditText.setText(user.birthdate)
                    bind.nameEditText.setText(user.firstname)
                    bind.nicknameEditText.setText(user.nickname)
                    utils.downloadImageFromFirebaseStorage(
                        requireContext(),
                        user.avatar
                    ) { isSuccess, file ->
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
                utils.showToast(requireContext(), "Error occurred while picking image")
            }
        }

        bind.confirmProfileButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.nicknameEditText.text) || TextUtils.isEmpty(bind.nameEditText.text) || TextUtils.isEmpty(
                    bind.birthdateEditText.text
                )
            ) {
                utils.showToast(requireContext(), "You need to fill in all fields")
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
                        utils.showToast(requireContext(), "You need to pick an avatar")
                    }
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

        bind.birthdateEditText.setOnClickListener {
            utils.showDatePickerDialog(
                requireContext(),
                bind.birthdateEditText,
                false
            )
        }
        return bind.root
    }

    private fun updateUserInfo(updatedFields: Map<String, String>) {
        currentUserId?.let {
            userManager.updateUserFields(
                it, updatedFields
            ) { isUserUpdateSuccess, userUpdateMessage ->
                utils.handleFirebaseResult(
                    isUserUpdateSuccess,
                    userUpdateMessage,
                    requireContext(),
                    "Update successful",
                    "Update failed"
                )

                if (isUserUpdateSuccess) {
                    if (isFirstTimeConfiguration) {
                        isFirstTimeConfiguration = false
                        findNavController().navigate(R.id.action_profileConfigFragment_to_mainMenuFragment)
                    } else {
                        isFirstTimeConfiguration = false
                        findNavController().popBackStack(R.id.mainMenuFragment, false)
                    }
                }
            }
        }
    }

}