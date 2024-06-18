package com.example.diceroom.meetings

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentChatBinding
import com.example.diceroom.fcm.FCMNotifications
import com.example.diceroom.fcm.NotificationBody
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.ChatManager
import com.example.diceroom.managers.ChatMessage
import com.example.diceroom.managers.MeetingManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants
import com.example.diceroom.utils.Utils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ChatFragment : Fragment(), ChatListAdapter.OnItemClickListener {

    private lateinit var bind: FragmentChatBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var meetingId: String
    private val userId = AuthManager().getCurrentUser()?.uid!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = FragmentChatBinding.inflate(layoutInflater)
        bind = view

        val args = arguments
        meetingId = args?.getString(Constants.MEETING_ID) ?: ""

        recyclerView = bind.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatListAdapter(recyclerView, this)

        recyclerView.adapter = adapter

        ChatManager().getChatMessages(meetingId) { newMessage ->
            lifecycleScope.launch {
                val updatedList = adapter.getData().toMutableList()
                updatedList.add(newMessage)
                adapter.setData(updatedList)
                recyclerView.scrollToPosition(updatedList.size - 1)
                //buildNotification(meetingId, newMessage.senderId)
            }
        }

        setSendListener()
        return bind.root
    }

    override fun onItemClick(chatId: String) {
        TODO("Not yet implemented")
    }
    private fun buildNotification(meetingId: String, senderId: String) {
        var meetingTitle: String
        var userNickname: String
        runBlocking { meetingTitle = fetchMeetingTitle(meetingId).toString() }
        runBlocking { userNickname = fetchUsername(senderId).toString() }

        val joinedNotification = NotificationBody(
            "New message in $meetingTitle", "Successfully joined $userNickname"
        )
        FCMNotifications().sendMessageToTopic(requireContext(), meetingId, joinedNotification)
    }

    private suspend fun fetchMeetingTitle(meetingId: String): String? {
        return suspendCoroutine { continuation ->
            MeetingManager().getMeetingTitleById(meetingId) { title ->
                continuation.resume(title)
            }
        }
    }

    private suspend fun fetchUsername(userId: String): String? {
        return suspendCoroutine { continuation ->
            UserManager().getUserById(userId) { user ->
                if (user != null) {
                    continuation.resume(user.nickname)
                }
            }
        }
    }
    private fun setSendListener() {
        bind.sendMessage.setOnClickListener {
            if (TextUtils.isEmpty(bind.newMessage.text)) {
                return@setOnClickListener
            } else {
                val templateMessage = ChatMessage(
                    senderId = userId,
                    message = bind.newMessage.text.toString(),
                    timestamp = System.currentTimeMillis()
                )

                ChatManager().addChatMessage(meetingId, templateMessage) { isSuccess, _ ->
                    if (!isSuccess) {
                        Utils().showToast(requireContext(), "Sending message failed!")
                    }
                    bind.newMessage.setText("")
                  //  buildNotification(meetingId, userId)
                }
            }
        }
    }
}


class ChatListAdapter(
    private val recyclerView: RecyclerView, private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {
    private var chatList: List<ChatMessage> = emptyList()

    interface OnItemClickListener {
        fun onItemClick(chatId: String)
    }

    fun setData(newChatList: List<ChatMessage>) {
        chatList = newChatList
        notifyDataSetChanged()
    }

    fun getData(): List<ChatMessage> {
        return chatList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_message_item, parent, false)
        view.setOnClickListener {
            /*  val position = recyclerView.getChildAdapterPosition(view)
              if (position != RecyclerView.NO_POSITION) {
                  val chat = chatList[position]
                  itemClickListener.onItemClick(Chat.id)
              }*/
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.senderImage)
        private val messageTextView: TextView = itemView.findViewById(R.id.chatMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.chatTimeStamp)
        private val messageCardView: CardView = itemView.findViewById(R.id.message)
        fun bind(chat: ChatMessage) {
            val currUser = AuthManager().getCurrentUser()?.uid!!
            UserManager().getUserById(chat.senderId) {
                if (it != null) {
                    val messageColour = if (chat.senderId == currUser) R.color.message_owner else R.color.messages
                    messageCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, messageColour))
                    Utils().loadGlide(itemView, it.avatar, thumbnailImageView)
                    messageTextView.text = chat.message
                    val formattedDate = formatDate(chat.timestamp)
                    timestampTextView.text = formattedDate
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }
}

