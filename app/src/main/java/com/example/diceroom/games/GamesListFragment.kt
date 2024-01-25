package com.example.diceroom.games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diceroom.R
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.GameInfo
import com.example.diceroom.managers.GameManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GamesListFragment : Fragment(), GameListAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameListAdapter
    private var favouritesGameIdsList: List<String> = emptyList()
    private var allGamesList: List<GameInfo> = emptyList()
    private var favouriteGamesList: List<GameInfo>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GameListAdapter(recyclerView, this)

        recyclerView.adapter = adapter
        lifecycleScope.launch {
            val favouriteGamesList = fetchFavouriteGamesList()
            if (favouriteGamesList != null) {
                favouritesGameIdsList = favouriteGamesList
            }

            allGamesList = fetchGames(false)
            adapter.setData(allGamesList)
        }
        return view
    }

    override fun onItemClick(gameId: String, isFavourite: Boolean) {
        val intent = Intent(requireContext(), GameDetailsActivity::class.java)
        intent.putExtra("gameId", gameId)
        intent.putExtra("isFavourite", isFavourite)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            checkIfFavourites()
        }
    }

    private suspend fun fetchFavouriteGamesList(): List<String>? {
        return suspendCoroutine { continuation ->
            val currentUserId = AuthManager().getCurrentUser()?.uid
            if (currentUserId != null) {
                UserManager().getFavourites(currentUserId) { favouriteGames ->
                    continuation.resume(favouriteGames)
                }
            } else {
                continuation.resume(null)
            }
        }
    }

    private suspend fun checkIfFavourites() {
        val sharedPreferences =
            requireContext().getSharedPreferences("gameListPrefs", Context.MODE_PRIVATE)
        val ifFavouritesView = sharedPreferences.getBoolean("isFavourites", false)
        if (ifFavouritesView) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFavourites", false)
            editor.apply()
            lifecycleScope.launch {
                val favouriteGamesList = fetchFavouriteGamesList()
                if (favouriteGamesList != null) {
                    favouritesGameIdsList = favouriteGamesList
                }
                this@GamesListFragment.favouriteGamesList = fetchGames(true)
                adapter.setData(this@GamesListFragment.favouriteGamesList!!)
            }
        } else {
            if (favouriteGamesList != null) {
                lifecycleScope.launch {
                    val favouriteGamesList = fetchFavouriteGamesList()
                    if (favouriteGamesList != null) {
                        favouritesGameIdsList = favouriteGamesList
                    }
                    allGamesList = fetchGames(false)
                    adapter.setData(allGamesList)
                    this@GamesListFragment.favouriteGamesList = null
                }
            }
        }
    }

    private fun fetchGames(ifFavouriteGames: Boolean): List<GameInfo> {
        val gameManager = GameManager()
        val utils = Utils()
        val future = CompletableFuture<List<GameInfo>>()
        gameManager.fetchGamesInfo(favouritesGameIdsList, ifFavouriteGames) { gameInfoList, exception ->
            if (exception == null && gameInfoList != null) {
                future.complete(gameInfoList)
            } else {
                this.context?.let { utils.showToast(it, "Loading games failed") }
            }
        }
        return try {
            future.get()
        } catch (e: InterruptedException) {
            this.context?.let { utils.showToast(it, "Error waiting for the result") }
            emptyList()
        } catch (e: ExecutionException) {
            this.context?.let { utils.showToast(it, "Error getting the result") }
            emptyList()
        }
    }
}

class GameListAdapter(
    private val recyclerView: RecyclerView,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<GameListAdapter.GameViewHolder>() {
    private var gameList: List<GameInfo> = emptyList()

    interface OnItemClickListener {
        fun onItemClick(gameId: String, isFavourite: Boolean)
    }

    fun setData(newGameList: List<GameInfo>) {
        gameList = newGameList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.game_info_item, parent, false)
        view.setOnClickListener {
            val position = recyclerView.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                val game = gameList[position]
                itemClickListener.onItemClick(game.id, game.isFavourite)
            }
        }
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]
        holder.bind(game)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.gameThumbnail)
        private val nameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val yearPublishedTextView: TextView = itemView.findViewById(R.id.yearPublished)
        fun bind(game: GameInfo) {
            Glide.with(itemView).load(game.thumbnail)
                .apply(RequestOptions().placeholder(R.drawable.loading)).into(thumbnailImageView)

            nameTextView.text = game.name
            yearPublishedTextView.text = game.yearPublished
        }
    }
}