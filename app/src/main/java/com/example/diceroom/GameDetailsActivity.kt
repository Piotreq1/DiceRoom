package com.example.diceroom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diceroom.databinding.ActivityGameDetailsBinding
import com.example.diceroom.models.GameManager
import com.example.diceroom.models.UserManager
import com.google.firebase.auth.FirebaseAuth

class GameDetailsActivity : AppCompatActivity() {
    private lateinit var bind: ActivityGameDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGameDetailsBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val id = intent.getStringExtra("gameId")
        var isGameFavourite = intent.getBooleanExtra("isFavourite", false)
        val utils = Utils()
        val userManager = UserManager()
        val currUserId = FirebaseAuth.getInstance().currentUser?.uid
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

                        Glide.with(this).load(gameInfo.thumbnail)
                            .apply(RequestOptions().placeholder(R.drawable.loading))
                            .into(bind.gameThumbnail)

                        bind.moreGameInfo.setOnClickListener {
                            val url = "https://boardgamegeek.com/boardgame/$id/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }

                        val img: Int = if (isGameFavourite) R.drawable.favourite64 else R.drawable.favourite_empty64

                        Glide.with(this).load(img)
                            .apply(RequestOptions().placeholder(R.drawable.favourite_empty64))
                            .into(bind.favouriteImage)

                        bind.favouriteImage.setOnClickListener {
                            if (!isGameFavourite) {
                                bind.favouriteImage.tag = R.drawable.favourite64
                                if (currUserId != null) {
                                    userManager.addToFavourites(
                                        currUserId,
                                        id
                                    ) { isSuccess, message ->
                                        utils.handleFirebaseResult(
                                            isSuccess,
                                            message,
                                            this,
                                            "Added to favourites",
                                            "Failed to add to favourites"
                                        )
                                    }
                                }
                                isGameFavourite = true
                            } else {
                                bind.favouriteImage.tag = R.drawable.favourite_empty64
                                if (currUserId != null) {
                                    userManager.deleteFromFavourites(
                                        currUserId,
                                        id
                                    ) { isSuccess, message ->
                                        utils.handleFirebaseResult(
                                            isSuccess,
                                            message,
                                            this,
                                            "Deleted from favourites",
                                            "Failed to delete from favourites"
                                        )
                                    }
                                }
                                isGameFavourite = false
                            }

                            Glide.with(this).load(bind.favouriteImage.tag)
                                .apply(RequestOptions().placeholder(R.drawable.favourite_empty64))
                                .into(bind.favouriteImage)
                        }
                    } else {
                        utils.showToast(this, "Loading game details failed")
                    }
                }
            }
        }
    }
}
