package com.example.diceroom.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.MainMenuActivity
import com.example.diceroom.databinding.ChangePasswordActivityViewBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.utils.Utils

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var bind: ChangePasswordActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ChangePasswordActivityViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val utils = Utils()
        val authManager = AuthManager()

        bind.changePasswordButton.setOnClickListener {
            if (TextUtils.isEmpty(bind.oldPasswordEditText.text) || TextUtils.isEmpty(bind.passwordEditText.text) || TextUtils.isEmpty(
                    bind.confirmPasswordEditText.text
                )
            ) {
                utils.showToast(this, "You need to fill in all fields")
            } else if (bind.passwordEditText.text.toString() != bind.confirmPasswordEditText.text.toString()) {
                utils.showToast(this, "Passwords need to be the same")
            } else {
                authManager.changePassword(
                    bind.oldPasswordEditText.text.toString(),
                    bind.passwordEditText.text.toString()
                ) { isSuccess, message ->
                    utils.handleFirebaseResult(
                        isSuccess,
                        message,
                        this,
                        "Password changed successfully",
                        "Password change failed"
                    )

                    if (isSuccess) {
                        val sharedPreferences =
                            getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
                        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

                        if (rememberMe) {
                            val editor = sharedPreferences.edit()
                            editor.putString("password", bind.passwordEditText.text.toString())
                            editor.apply()
                        }
                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.putExtra("currentItem", 2)
                        startActivity(intent)
                    }
                }
            }
        }

    }
}