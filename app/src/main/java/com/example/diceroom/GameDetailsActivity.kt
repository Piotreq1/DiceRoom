package com.example.diceroom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.databinding.ActivityGameDetailsBinding

class GameDetailsActivity: AppCompatActivity() {
    private lateinit var bind: ActivityGameDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGameDetailsBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val id = intent.getStringExtra("GAME_ID")
        bind.gameName.text = "GAME ID: $id"
    }
}
