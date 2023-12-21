package com.example.diceroom.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.R
import com.google.android.material.button.MaterialButton

class SelectLoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_activity_view)

        val loginButton: MaterialButton = findViewById(R.id.goToLoginButton)
        val signUpButton: MaterialButton = findViewById(R.id.goToSignUpButton)

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
