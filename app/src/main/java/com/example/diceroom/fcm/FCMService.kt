package com.example.diceroom.fcm

import android.content.Context
import android.util.Log
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants.Companion.FIREBASE_MESSAGING
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(FIREBASE_MESSAGING, "New token registered: $token")
        refreshUsersToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Log.d(FIREBASE_MESSAGING, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(FIREBASE_MESSAGING, "Message data payload: ${remoteMessage.data}")

//            // Check if data needs to be processed by long running job
//            if (needsToBeScheduled()) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow()
//            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(FIREBASE_MESSAGING, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun refreshUsersToken(token: String) {
        val userManager = UserManager()
        val sharedPreferences =
            applicationContext.getSharedPreferences(FIREBASE_MESSAGING, Context.MODE_PRIVATE)
        val oldToken = sharedPreferences.getString(FIREBASE_MESSAGING, "")
        if (oldToken != null) {
            userManager.getUsersByToken(oldToken) { users ->
                if (users != null) {
                    for (userId in users) {
                        userManager.saveTokenOnDatabase(userId, token)
                        Log.d(FIREBASE_MESSAGING, "Token refreshed for $userId")
                    }

                    sharedPreferences.edit().putString(FIREBASE_MESSAGING, token).apply()
                } else {
                    Log.w(FIREBASE_MESSAGING, "Failed to retrieve users with token")
                }
            }
        }
    }


}