package com.example.diceroom.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.databinding.SigninActivityViewBinding
import com.example.diceroom.models.UserManager
import com.example.diceroom.models.UserModel
import com.example.diceroom.utils.Utils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var bind: SigninActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = SigninActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val utils = Utils()
        val authManager = AuthManager()

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
                authManager.register(
                    bind.emailEditText.text.toString(),
                    bind.passwordEditText.text.toString()
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
                                    user?.delete()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                utils.showToast(
                                                    this,
                                                    "Error occurred - register again"
                                                )
                                            } else {
                                                utils.showToast(this, "Failed to delete account")
                                            }
                                        }
                                }
                            }
                        } else {
                            utils.showToast(this, "User ID is null")
                        }
                    }

                }
            }
        }

    }
}