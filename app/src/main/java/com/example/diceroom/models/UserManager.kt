package com.example.diceroom.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

data class UserModel(
    val avatar: String = "",
    val nickname: String = "",
    val firstname: String = "",
    val birthdate: String = "",
    val favourites: MutableList<String>? = mutableListOf()
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

    fun getFavourites(userId: String, onComplete: (List<String>?) -> Unit) {
        userRef.child(userId).child("favourites").get().addOnSuccessListener { dataSnapshot ->
            val favoritesData =
                dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
            onComplete(favoritesData)
        }.addOnFailureListener {
            onComplete(null)
        }
    }

    fun addToFavourites(userId: String, newItem: String, onComplete: (Boolean, String?) -> Unit) {
        userRef.child(userId).child("favourites").push().setValue(newItem).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, it.exception?.message)
            }
        }
    }

    fun deleteFromFavourites(
        userId: String,
        itemToDelete: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        userRef.child(userId).child("favourites").orderByValue().equalTo(itemToDelete)
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

}