package com.example.diceroom.meetings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentMeetingBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MeetingFragment : Fragment(), MeetingListAdapter.OnItemClickListener {
    private lateinit var bind: FragmentMeetingBinding
    private lateinit var adapter: MeetingListAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentMeetingBinding.inflate(layoutInflater)
        val view = bind.root

        bind.addMeetingButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_addMeetingActivity)
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MeetingListAdapter(recyclerView, this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            adapter.setData(fetchMeetingsList())
        }

        return view
    }

    private suspend fun fetchMeetingsList(): List<Pair<MeetingModel, String>> {
        return suspendCoroutine { continuation ->
            MeetingManager().getAllMeetings {
                continuation.resume(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            checkIfCurrentUserMeetings()
        }
    }

    private fun checkIfCurrentUserMeetings() {
        val sharedPreferences =
            requireContext().getSharedPreferences(Constants.MEET_PREFS, Context.MODE_PRIVATE)
        val ifCurrentUser = sharedPreferences.getBoolean(Constants.USER_MEETINGS_KEY, false)

        if (ifCurrentUser) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.USER_MEETINGS_KEY, false)
            editor.apply()
            lifecycleScope.launch {
                val meetingsList = fetchMeetingsList()
                val userId = AuthManager().getCurrentUser()?.uid!!
                val currentUserMeetings = meetingsList.filter { meetingPair ->
                    val participants = meetingPair.first.participants
                    participants != null && participants.contains(userId)
                }
                adapter.setData(currentUserMeetings)
            }
        } else {
            lifecycleScope.launch {
                adapter.setData(fetchMeetingsList())
            }
        }

    }

    override fun onItemClick(meetingId: String, isFavourite: Boolean) {
        findNavController().navigate(
            R.id.action_mainMenuFragment_to_meetingDetailsFragment, bundleOf(
                Constants.MEETING_ID to meetingId
            )
        )
    }
}

class MeetingListAdapter(
    private val recyclerView: RecyclerView, private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<MeetingListAdapter.MeetingViewHolder>() {
    private var meetingList: List<Pair<MeetingModel, String>> = emptyList()

    interface OnItemClickListener {
        fun onItemClick(meetingId: String, isFavourite: Boolean)
    }

    fun setData(newMeetingList: List<Pair<MeetingModel, String>>) {
        meetingList = newMeetingList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.meeting_item, parent, false)
        view.setOnClickListener {
            val position = recyclerView.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                val (_, meetingId) = meetingList[position]
                itemClickListener.onItemClick(meetingId, false)
            }
        }
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val (meeting, _) = meetingList[position]
        holder.bind(meeting)
    }

    override fun getItemCount(): Int {
        return meetingList.size
    }

    class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameName: TextView = itemView.findViewById(R.id.gameName)
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.meetingThumbnail)
        private val title: TextView = itemView.findViewById(R.id.meetingTitle)
        private val date: TextView = itemView.findViewById(R.id.meetingDate)
        private val location: TextView = itemView.findViewById(R.id.meetingLocation)
        fun bind(meeting: MeetingModel) {
            Utils().loadGlide(itemView, meeting.image, thumbnailImageView)
            gameName.text = meeting.game
            title.text = meeting.title
            date.text = meeting.startDate
            location.text = meeting.location
        }
    }
}
