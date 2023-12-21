package com.example.diceroom.authentication

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.Utils
import com.example.diceroom.databinding.ForgotPasswordActivityViewBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var bind: ForgotPasswordActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ForgotPasswordActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val utils = Utils()
        val authManager = AuthManager()

        bind.goToSignUpButton.setOnClickListener {
            finish()
        }

        bind.resetButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.emailEditText.text)) {
                utils.showToast(this, "You need to fill in all fields")
            } else {
                authManager.resetPassword(bind.emailEditText.text.toString()) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        this,
                        "Email sent",
                        "Failed to send password reset email"
                    )
                }
            }
        }
    }
}