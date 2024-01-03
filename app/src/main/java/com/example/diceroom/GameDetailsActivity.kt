package com.example.diceroom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroom.databinding.ActivityGameDetailsBinding
import com.example.diceroom.models.GameManager

class GameDetailsActivity : AppCompatActivity() {
    private lateinit var bind: ActivityGameDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGameDetailsBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val id = intent.getStringExtra("GAME_ID")
        val utils = Utils()

        val gameManager = GameManager()

        if (id == null) {
            runOnUiThread {
                utils.showToast(this, "Loading game details failed")
            }
        } else {
            gameManager.fetchGameDetailsById(id) { gameInfo, exception ->
                runOnUiThread {
                    if (exception == null && gameInfo != null) {
                        bind.gameName.text = gameInfo.name
                        bind.minPlayers.text = gameInfo.minPlayers.toString()
                        bind.maxPlayers.text = gameInfo.maxPlayers.toString()
                        bind.minAge.text = gameInfo.minAge.toString()
                        bind.yearPublished.text = gameInfo.yearPublished.toString()
                        bind.description.text = gameInfo.description

                        bind.moreGameInfo.setOnClickListener {
                            val url = "https://boardgamegeek.com/boardgame/$id/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    } else {
                        utils.showToast(this, "Loading game details failed")
                    }
                }
            }
        }
    }
}
