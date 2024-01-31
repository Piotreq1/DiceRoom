package com.example.diceroom.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentMeetingBinding
import com.example.diceroom.games.GameListAdapter
import com.example.diceroom.managers.GameInfo
import com.example.diceroom.managers.MeetingModel
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Utils


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


        return view
    }


    override fun onItemClick(meetingId: String, isFavourite: Boolean) {
        //TODO: IMPLEMENT ON CLICK
    }
}


class MeetingListAdapter(
    private val recyclerView: RecyclerView,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<MeetingListAdapter.MeetingViewHolder>() {
    private var meetingList: List<MeetingModel> = emptyList()

    interface OnItemClickListener {
        fun onItemClick(gameId: String, isFavourite: Boolean)
    }

    fun setData(newMeetingList: List<MeetingModel>) {
        meetingList = newMeetingList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.meeting_item, parent, false)
        view.setOnClickListener {
            val position = recyclerView.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                // TODO IMPLEMENT MEETING ID?
                /*val meeting = meetingList[position]
                itemClickListener.onItemClick(meeting.id, game.isFavourite)*/
            }
        }
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetingList[position]
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
            // TODO: SET THIS
          /*  nameTextView.text = game.name
            yearPublishedTextView.text = game.yearPublished*/
        }
    }
}
