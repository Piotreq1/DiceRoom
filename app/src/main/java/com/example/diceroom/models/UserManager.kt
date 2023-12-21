package com.example.diceroom.models

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

data class UserModel(
    val avatar: String = "",
    val nickname: String = "",
    val firstname: String = "",
    val birthdate: String = "",
    val favourites: List<String>? = null
)


class UserManager {
    private val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")


    fun addUser(userId: String, user: UserModel, onComplete: (Boolean, String?) -> Unit) {
        userRef.child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }


    fun updateUserFields(
        userId: String,
        updatedFields: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        userRef.child(userId).updateChildren(updatedFields).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, it.exception?.message)
            }
        }
    }

    fun getUserById(userId: String, onComplete: (UserModel?) -> Unit) {
        userRef.child(userId).get().addOnSuccessListener {
            onComplete(it.getValue(UserModel::class.java))
        }.addOnFailureListener {
            onComplete(null)
        }
    }

}