package com.example.diceroom.managers

import android.util.Log
import com.example.diceroom.utils.Constants.Companion.FCM_TOKEN_TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class UserModel(
    val avatar: String = "",
    val nickname: String = "",
    val firstname: String = "",
    val birthdate: String = "",
    val favourites: List<String>? = null,
    val token: String = ""
)


class UserManager {
    private val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")


    fun addUser(userId: String, user: UserModel, onComplete: (Boolean, String?) -> Unit) {
        userRef.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }


    fun updateUserFields(
        userId: String, updatedFields: Map<String, Any>, onComplete: (Boolean, String?) -> Unit
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

    fun getFavourites(userId: String, onComplete: (List<String>?) -> Unit) {
        val userFavouritesRef = userRef.child(userId).child("favourites")

        userFavouritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onComplete(snapshot.value as? List<String>)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }

    fun addToFavourites(userId: String, newItem: String, onComplete: (Boolean, String?) -> Unit) {
        val userFavouritesRef = userRef.child(userId).child("favourites")

        userFavouritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentFavourites: MutableList<String> = if (snapshot.exists()) {
                    snapshot.value as? MutableList<String> ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                currentFavourites.add(newItem)

                userFavouritesRef.setValue(currentFavourites).addOnCompleteListener {
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

    fun deleteFromFavourites(
        userId: String, itemToDelete: String, onComplete: (Boolean, String?) -> Unit
    ) {
        val userFavouritesRef = userRef.child(userId).child("favourites")

        userFavouritesRef.orderByValue().equalTo(itemToDelete)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                        onComplete(true, null)
                        return
                    }
                    onComplete(false, "Item not found in favourites")
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }

    fun getUsersByToken(token: String, onComplete: (List<String>?) -> Unit) {
        userRef.orderByChild("token").equalTo(token)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users: MutableList<String> = mutableListOf()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        if (userId != null) {
                            users.add(userId)
                        }
                    }
                    onComplete(users)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(null)
                }
            })
    }

    fun saveTokenOnDatabase(userId: String, token: String) {
        val updatedFields = mutableMapOf("token" to token)

        UserManager().updateUserFields(
            userId, updatedFields
        ) { isUserUpdateSuccess, userUpdateMessage ->
            if (isUserUpdateSuccess) {
                Log.d(FCM_TOKEN_TAG, "Token saved to database")
            } else {
                Log.w(FCM_TOKEN_TAG, "Failed to save token $userUpdateMessage")
            }
        }
    }

}