package com.example.diceroom.authentication

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentRegisterBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.managers.UserModel
import com.example.diceroom.utils.Utils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var bind: FragmentRegisterBinding
    private val utils = Utils()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentRegisterBinding.inflate(layoutInflater)

        bind.goToLoginButton1.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        bind.loginButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.emailEditText.text) || TextUtils.isEmpty(bind.passwordEditText.text) || TextUtils.isEmpty(
                    bind.confirmPasswordEditText.text
                )
            ) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else if (bind.passwordEditText.text.toString() != bind.confirmPasswordEditText.text.toString()) {
                utils.showToast(requireContext(), "Passwords need to be the same")
            } else {
                registerUser()
            }
        }
        return bind.root
    }

    private fun registerUser() {
        val authManager = AuthManager()

        authManager.register(
            bind.emailEditText.text.toString(), bind.passwordEditText.text.toString()
        ) { isSuccess, message ->
            utils.handleFirebaseResult(
                isSuccess,
                message,
                requireContext(),
                "Register success! You can now log in",
                "Registration failed"
            )
            if (isSuccess) {
                val userManager = UserManager()
                if (message != null) {
                    userManager.addUser(message, UserModel()) { isSuccess1, _ ->
                        if (!isSuccess1) {
                            val user = Firebase.auth.currentUser
                            user?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    utils.showToast(
                                        requireContext(), "Error occurred - register again"
                                    )
                                } else {
                                    utils.showToast(requireContext(), "Failed to delete account")
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                saveCredential(
                                    bind.emailEditText.text.toString(),
                                    bind.passwordEditText.text.toString()
                                )
                            }
                        }
                    }
                } else {
                    utils.showToast(requireContext(), "User ID is null")
                }
            }

        }
    }

    private suspend fun saveCredential(username: String, password: String) {
        val credentialManager = CredentialManager.create(requireContext())

        try {
            credentialManager.createCredential(
                request = CreatePasswordRequest(username, password), context = requireContext()
            )

        } catch (_: CreateCredentialCancellationException) {
        } catch (e: CreateCredentialException) {
            utils.showToast(requireContext(), "Credential save error")
        }
    }
}