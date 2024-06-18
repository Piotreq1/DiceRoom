package com.example.diceroom.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.diceroom.MainActivity
import com.example.diceroom.R
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
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            Log.d(FIREBASE_MESSAGING, "Message data payload: " + remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Log.d(FIREBASE_MESSAGING, "Message Notification Body: ${it.body}")
            sendNotification(it.body, it.title)
        }
    }

    private fun sendNotification(messageBody: String?, title: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationBuilder =
            NotificationCompat.Builder(this, "CHANNEL_ID").setSmallIcon(R.drawable.logo)
                .setContentTitle(title).setContentText(messageBody).setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "CHANNEL_ID", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT
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