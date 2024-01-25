package com.example.diceroom.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.diceroom.databinding.SigninActivityViewBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.managers.UserModel
import com.example.diceroom.utils.Utils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var bind: SigninActivityViewBinding
    private val utils = Utils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = SigninActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.goToLoginButton1.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        bind.loginButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.emailEditText.text) || TextUtils.isEmpty(bind.passwordEditText.text) || TextUtils.isEmpty(
                    bind.confirmPasswordEditText.text
                )
            ) {
                utils.showToast(this, "You need to fill in all fields")
            } else if (bind.passwordEditText.text.toString() != bind.confirmPasswordEditText.text.toString()) {
                utils.showToast(this, "Passwords need to be the same")
            } else {
                registerUser()
            }
        }
    }

    private fun registerUser() {
        val authManager = AuthManager()

        authManager.register(
            bind.emailEditText.text.toString(), bind.passwordEditText.text.toString()
        ) { isSuccess, message ->
            utils.handleFirebaseResult(
                isSuccess,
                message,
                this,
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
                                        this, "Error occurred - register again"
                                    )
                                } else {
                                    utils.showToast(this, "Failed to delete account")
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
                    utils.showToast(this, "User ID is null")
                }
            }

        }
    }

    private suspend fun saveCredential(username: String, password: String) {
        val credentialManager = CredentialManager.create(this)

        try {
            credentialManager.createCredential(
                request = CreatePasswordRequest(username, password), context = this
            )

        } catch (_: CreateCredentialCancellationException) {
        } catch (e: CreateCredentialException) {
            utils.showToast(this, "Credential save error")
        }
    }
}