package com.example.diceroom.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.Utils
import com.example.diceroom.databinding.LoginActivityViewBinding
import com.example.diceroom.models.UserManager

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: LoginActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = LoginActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val utils = Utils()
        val authManager = AuthManager()

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
        val rememberCheckBox: CheckBox = bind.rememberCheckBox
        val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

        if (rememberMe) {
            val savedEmail = sharedPreferences.getString("email", null)
            val savedPassword = sharedPreferences.getString("password", null)

            if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
                emailField.setText(savedEmail)
                passwordField.setText(savedPassword)
                rememberCheckBox.isChecked = true
            }
        }

        bind.loginButton.setOnClickListener {
            if (TextUtils.isEmpty(emailField.text) || TextUtils.isEmpty(passwordField.text)) {
                utils.showToast(this, "You need to fill in all fields")
            } else {
                authManager.login(
                    emailField.text.toString(),
                    passwordField.text.toString()
                ) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        this,
                        "Login success",
                        "Login failed"
                    )
                    if (isSuccess) {
                        val editor = sharedPreferences.edit()
                        if (rememberCheckBox.isChecked) {
                            editor.putString("email", emailField.text.toString())
                            editor.putString("password", passwordField.text.toString())
                        } else {
                            editor.remove("email")
                            editor.remove("password")
                        }
                        editor.putBoolean("rememberMe", rememberCheckBox.isChecked)
                        editor.apply()
                        emailField.setText("")
                        passwordField.setText("")

                        val userManager = UserManager()
                        if (message != null) {
                            userManager.getUserById(message) { user ->
                                if (user != null) {
                                    if (user.nickname == "") {
                                        val intent =
                                            Intent(this, ProfileConfigActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            }
                        }

                        val intent = Intent(this, ChangePasswordActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}