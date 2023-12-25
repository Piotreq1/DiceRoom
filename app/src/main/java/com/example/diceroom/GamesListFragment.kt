package com.example.diceroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diceroom.models.GameInfo
import com.example.diceroom.models.GameManager

class GamesListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GameListAdapter()

        recyclerView.adapter = adapter

        val gameManager = GameManager()
        gameManager.fetchGamesInfo { gameInfoList, exception ->
            if (exception == null && gameInfoList != null) {
                adapter.setData(gameInfoList)
            } else {
                val utils = Utils()
                this.context?.let { utils.showToast(it, "Loading games failed") }
            }
        }

        return view
    }
}

class GameListAdapter :
    RecyclerView.Adapter<GameListAdapter.GameViewHolder>() {
    private var gameList: List<GameInfo> = emptyList()

    fun setData(newGameList: List<GameInfo>) {
        gameList = newGameList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.game_info_item, parent, false)
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
                .apply(RequestOptions().placeholder(R.drawable.logout_64)).into(thumbnailImageView)

            nameTextView.text = game.name
            yearPublishedTextView.text = game.yearPublished
        }
    }
}
