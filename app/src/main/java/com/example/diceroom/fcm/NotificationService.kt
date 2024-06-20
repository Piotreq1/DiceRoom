package com.example.diceroom.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.diceroom.R
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants.Companion.FIREBASE_MESSAGING
import com.example.diceroom.utils.Constants.Companion.NOTIFICATION_CHANNEL
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(FIREBASE_MESSAGING, "New token registered: $token")
        refreshUsersToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            // TODO: UNCOMMENT IT LATER MAYBE -> sending to yourself
//            if(remoteMessage.data["senderId"] == AuthManager().getCurrentUser()?.uid!!){
//                return
//            }
        }

        remoteMessage.notification?.let {
            Log.d(FIREBASE_MESSAGING, "Message Notification Body: ${it.body}")
            sendNotification(it.body, it.title)
        }
    }

    private fun sendNotification(messageBody: String?, title: String?) {
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(0, notificationBuilder.build())
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