package com.example.diceroom.managers

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

data class ChatMessage(
    val senderId: String = "", val message: String = "", val timestamp: Long = 0
)

class ChatManager {
    private val chatRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chats")

    fun createChat(userId: String, meetingId: String, onComplete: (Boolean, String?) -> Unit) {
        val templateMessage = ChatMessage(
            senderId = userId, message = "Hello Everyone!", timestamp = System.currentTimeMillis()
        )

        chatRef.child(meetingId).push().setValue(templateMessage).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    fun addChatMessage(
        meetingId: String, message: ChatMessage, onComplete: (Boolean, String?) -> Unit
    ) {
        val chatsRef = chatRef.child(meetingId)
        chatsRef.push().setValue(message).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    fun getChatMessages(meetingId: String, onMessageAdded: (ChatMessage) -> Unit) {
        val chatsRef = chatRef.child(meetingId)
        chatsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                chatMessage?.let { onMessageAdded(it) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}