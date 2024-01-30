package com.example.diceroom.authentication

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.diceroom.databinding.FragmentForgotPasswordBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.utils.Utils

class ForgotPasswordFragment : Fragment() {
    private lateinit var bind: FragmentForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentForgotPasswordBinding.inflate(layoutInflater)

        val utils = Utils()
        val authManager = AuthManager()

        bind.resetButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.emailEditText.text)) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else {
                authManager.resetPassword(bind.emailEditText.text.toString()) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        requireContext(),
                        "Email sent",
                        "Failed to send password reset email"
                    )
                }
            }
        }

        return bind.root
    }
}