package com.example.diceroom.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.diceroom.MainMenuActivity
import com.example.diceroom.databinding.LoginActivityViewBinding
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: LoginActivityViewBinding
    private val utils = Utils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = LoginActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        bind.goToSignUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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
                utils.showToast(this@LoginActivity, "You need to fill in all fields")
            } else {
                loginUser(emailField.text.toString(), passwordField.text.toString())
                emailField.setText("")
                passwordField.setText("")
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val authManager = AuthManager()
        authManager.login(
            email, password
        ) { isSuccess, message ->
            utils.handleFirebaseResult(
                isSuccess, message, this, "Login success", "Login failed"
            )
            if (isSuccess) {
                val userManager = UserManager()
                if (message != null) {
                    userManager.getUserById(message) { user ->
                        if (user != null) {
                            if (user.nickname == "") {
                                val intent = Intent(this, ProfileConfigActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }

                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private suspend fun getCredential(): PasswordCredential? {
        val credentialManager = CredentialManager.create(this)
        try {
            val getCredRequest = GetCredentialRequest(
                listOf(GetPasswordOption())
            )
            val credentialResponse = credentialManager.getCredential(
                request = getCredRequest, context = this
            )
            return credentialResponse.credential as? PasswordCredential
        } catch (e: GetCredentialCancellationException) {
            return null
        } catch (e: NoCredentialException) {
            return null
        } catch (e: GetCredentialException) {
            utils.showToast(this, "Error getting credential")
            return null
        }
    }
}