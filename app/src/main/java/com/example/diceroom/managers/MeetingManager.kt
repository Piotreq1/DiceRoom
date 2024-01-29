package com.example.diceroom.managers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


data class MeetingModel(
    val title: String = "",
    val game: String = "",
    val location: String = "",
    val creationDate: String = "",
    val startDate: String = "",
    val ownerId: String = "",
    val level: String = "",
    val description: String = "",
    val image: String = "",
    val maxMembers: Int = 0,
    val participants: List<String>? = null
)


class MeetingManager {

    private val meetingRef: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("meetings")

    fun addMeeting(meetingModel: MeetingModel, onComplete: (Boolean, String?) -> Unit) {
        meetingRef.push().setValue(meetingModel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    fun updateMeeting(
        meetingId: String, updatedFields: Map<String, Any>, onComplete: (Boolean, String?) -> Unit
    ) {
        meetingRef.child(meetingId).updateChildren(updatedFields).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, it.exception?.message)
            }
        }
    }

    fun getMeetingById(meetingId: String, onComplete: (MeetingModel?) -> Unit) {
        meetingRef.child(meetingId).get().addOnSuccessListener {
            onComplete(it.getValue(MeetingModel::class.java))
        }.addOnFailureListener {
            onComplete(null)
        }
    }

    fun getParticipants(meetingId: String, onComplete: (List<String>?) -> Unit) {
        val meetingParticipantsRef = meetingRef.child(meetingId).child("participants")

        meetingParticipantsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onComplete(snapshot.value as? List<String>)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }


    fun addParticipant(meetingId: String, userId: String, onComplete: (Boolean, String?) -> Unit) {
        val meetingParticipantsRef = meetingRef.child(meetingId).child("participants")

        meetingParticipantsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentParticipants: MutableList<String> = if (snapshot.exists()) {
                    snapshot.value as? MutableList<String> ?: mutableListOf()
                } else {
                    mutableListOf()
                }

                currentParticipants.add(userId)
                meetingParticipantsRef.setValue(currentParticipants).addOnCompleteListener {
                    if (it.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, it.exception?.message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    fun deleteParticipant(
        meetingId: String, userId: String, onComplete: (Boolean, String?) -> Unit
    ) {
        val meetingParticipantsRef = meetingRef.child(meetingId).child("participants")

        meetingParticipantsRef.orderByValue().equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                        onComplete(true, null)
                        return
                    }
                    onComplete(false, "User not found in participants")
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }
}