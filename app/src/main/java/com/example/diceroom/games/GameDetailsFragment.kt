package com.example.diceroom.games

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentGameDetailsBinding
import com.example.diceroom.managers.GameDetails
import com.example.diceroom.managers.GameManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Constants.Companion.GAME_ID
import com.example.diceroom.utils.Constants.Companion.IS_GAME_FAVOURITE_KEY
import com.example.diceroom.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GameDetailsFragment : Fragment() {
    private lateinit var bind: FragmentGameDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentGameDetailsBinding.inflate(inflater, container, false)
        val args = arguments
        val id = args?.getString(GAME_ID)
        var isGameFavourite = args?.getBoolean(IS_GAME_FAVOURITE_KEY, false) ?: false
        val utils = Utils()
        val userManager = UserManager()
        val currUserId = FirebaseAuth.getInstance().currentUser?.uid

        lifecycleScope.launch {
            val gameInfo = fetchGameDetails(id)
            if (gameInfo != null) {
                bind.gameName.text = gameInfo.name
                bind.minPlayers.text = gameInfo.minPlayers.toString()
                bind.maxPlayers.text = gameInfo.maxPlayers.toString()
                bind.minAge.text = gameInfo.minAge.toString()
                bind.yearPublished.text = gameInfo.yearPublished.toString()
                bind.description.text = gameInfo.description

                gameInfo.thumbnail?.let {
                    utils.loadGlide(
                        requireContext(),
                        it,
                        bind.gameThumbnail
                    )
                }

                bind.moreGameInfo.setOnClickListener {
                    val url = "${Constants.BASE_BOARD_GAME_URL}$id/"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }

                val img: Int =
                    if (isGameFavourite) R.drawable.favourite64 else R.drawable.favourite_empty64
                utils.loadGlide(requireContext(), img, bind.favouriteImage)

                bind.favouriteImage.setOnClickListener {
                    if (!isGameFavourite) {
                        bind.favouriteImage.tag = R.drawable.favourite64
                        if (currUserId != null) {
                            userManager.addToFavourites(
                                currUserId,
                                id!!
                            ) { isSuccess, message ->
                                utils.handleFirebaseResult(
                                    isSuccess,
                                    message,
                                    requireContext(),
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
                                id!!
                            ) { isSuccess, message ->
                                utils.handleFirebaseResult(
                                    isSuccess,
                                    message,
                                    requireContext(),
                                    "Deleted from favourites",
                                    "Failed to delete from favourites"
                                )
                            }
                        }
                        isGameFavourite = false
                    }

                    utils.loadGlide(
                        requireContext(),
                        bind.favouriteImage.tag,
                        bind.favouriteImage
                    )
                }
            }
            else{
                utils.showToast(requireContext(), "Loading game details failed")
            }
        }
        return bind.root
    }

    private suspend fun fetchGameDetails(id: String?): GameDetails? {
        return suspendCoroutine { continuation ->
            if (id == null) {
                continuation.resume(null)
            } else {
                GameManager().fetchGameDetailsById(id) { gameInfo, _ ->
                    continuation.resume(gameInfo)
                }
            }
        }
    }
}
