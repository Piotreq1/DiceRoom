package com.example.diceroom.authentication

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentLoginBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants.Companion.FCM_TOKEN_TAG
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var bind: FragmentLoginBinding
    private val utils = Utils()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentLoginBinding.inflate(layoutInflater)

        bind.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        bind.goToSignUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        val emailField: EditText = bind.emailEditText
        val passwordField: EditText = bind.passwordEditText

        lifecycleScope.launch {
            val credential = getCredential()
            if (credential != null) {
                loginUser(credential.id, credential.password)
            }
        }


        bind.loginButton.setOnClickListener {
            if (TextUtils.isEmpty(emailField.text) || TextUtils.isEmpty(passwordField.text)) {
                utils.showToast(requireContext(), "You need to fill in all fields")
            } else {
                loginUser(emailField.text.toString(), passwordField.text.toString())
                emailField.setText("")
                passwordField.setText("")
            }
        }

        return bind.root
    }

    private fun loginUser(email: String, password: String) {
        val authManager = AuthManager()
        authManager.login(
            email, password
        ) { isSuccess, message ->
            utils.handleFirebaseResult(
                isSuccess, message, requireContext(), "Login success", "Login failed"
            )
            if (isSuccess) {
                val userManager = UserManager()
                if (message != null) {
                    userManager.getUserById(message) { user ->
                        if (user != null) {
                            saveTokenAfterAuthentication(message)
                            if (user.nickname == "") {
                                findNavController().navigate(R.id.action_loginFragment_to_profileConfigFragment)
                            } else {
                                findNavController().popBackStack(R.id.loginFragment, false)
                                findNavController().navigate(R.id.action_loginFragment_to_mainMenuFragment)
                            }
                        }
                    }
                }

            }
        }
    }

    private suspend fun getCredential(): PasswordCredential? {
        val credentialManager = CredentialManager.create(requireContext())
        try {
            val getCredRequest = GetCredentialRequest(
                listOf(GetPasswordOption())
            )
            val credentialResponse = credentialManager.getCredential(
                request = getCredRequest, context = requireContext()
            )
            return credentialResponse.credential as? PasswordCredential
        } catch (e: GetCredentialCancellationException) {
            return null
        } catch (e: NoCredentialException) {
            return null
        } catch (e: GetCredentialException) {
            utils.showToast(requireContext(), "Error getting credential")
            return null
        }
    }

    private fun saveTokenAfterAuthentication(uID: String) {
        val sharedPreferences =
            this.requireContext().getSharedPreferences(FCM_TOKEN_TAG, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(FCM_TOKEN_TAG, "") ?: return
        UserManager().saveTokenOnDatabase(uID, token)
    }

}