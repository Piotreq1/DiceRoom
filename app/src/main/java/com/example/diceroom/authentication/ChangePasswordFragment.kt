package com.example.diceroom.authentication

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentChangePasswordBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.utils.Utils

class ChangePasswordFragment : Fragment() {
    private lateinit var bind: FragmentChangePasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentChangePasswordBinding.inflate(layoutInflater)
        val utils = Utils()
        val authManager = AuthManager()

        bind.changePasswordButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.oldPasswordEditText.text) || TextUtils.isEmpty(bind.passwordEditText.text) || TextUtils.isEmpty(
                    bind.confirmPasswordEditText.text
                )
            ) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else if (bind.passwordEditText.text.toString() != bind.confirmPasswordEditText.text.toString()) {
                utils.showToast(requireContext(), "Passwords need to be the same")
            } else {
                authManager.changePassword(
                    bind.oldPasswordEditText.text.toString(), bind.passwordEditText.text.toString()
                ) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        requireContext(),
                        "Password changed successfully",
                        "Password change failed"
                    )

                    if (isSuccess) {
                        findNavController().popBackStack(R.id.mainMenuFragment, false)
                    }
                }
            }
        }
        return bind.root

    }
}