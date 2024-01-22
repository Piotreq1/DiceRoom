package com.example.diceroom.managers

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(true, auth.currentUser?.uid)
                } else {
                    onComplete(false, it.exception?.message)
                }
            }
    }

    fun login(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(true, auth.currentUser?.uid)
                } else {
                    onComplete(false, it.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user: FirebaseUser? = auth.currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)

        user?.reauthenticate(credential)?.addOnCompleteListener { reauthorize ->
            if (reauthorize.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener {
                    if (it.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, it.exception?.message)
                    }
                }
            } else {
                onComplete(false, reauthorize.exception?.message)
            }
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, it.exception?.message)
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}