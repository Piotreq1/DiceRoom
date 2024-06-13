package com.example.diceroom

import android.content.Context
import android.util.Log
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants.Companion.FCM_TOKEN_TAG
import com.google.firebase.messaging.FirebaseMessagingService

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(FCM_TOKEN_TAG, "New token registered: $token")
        refreshUsersToken(token)
    }

    private fun refreshUsersToken(token: String) {
        val userManager = UserManager()
        val sharedPreferences =
            applicationContext.getSharedPreferences(FCM_TOKEN_TAG, Context.MODE_PRIVATE)
        val oldToken = sharedPreferences.getString(FCM_TOKEN_TAG, "")
        if (oldToken != null) {
            userManager.getUsersByToken(oldToken) { users ->
                if (users != null) {
                    for (userId in users) {
                        userManager.saveTokenOnDatabase(userId, token)
                        Log.d(FCM_TOKEN_TAG, "Token refreshed for $userId")
                    }

                    sharedPreferences.edit().putString(FCM_TOKEN_TAG, token).apply()
                } else {
                    Log.w(FCM_TOKEN_TAG, "Failed to retrieve users with token")
                }
            }
        }
    }
}