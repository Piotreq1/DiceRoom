package com.example.diceroom.fcm

import android.content.Context
import android.util.Log
import com.example.diceroom.utils.Constants.Companion.FIREBASE_MESSAGING
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class NotificationHandler {
    fun createMessagingTopicForMeeting(
        context: Context, meetingId: String, notificationBody: NotificationBody
    ) {
        Firebase.messaging.subscribeToTopic(meetingId).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            Log.d(FIREBASE_MESSAGING, "FCM topic subscribed")
            sendMessage(context, meetingId, notificationBody)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMessage(context: Context, topic: String, notificationBody: NotificationBody, senderId: String? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val conn = buildConnection(context)
                val jsonPayload = buildJsonPayload(topic, notificationBody, senderId)

                Log.d(FIREBASE_MESSAGING, "FCM notification sending: $jsonPayload")

                val outputStream = conn.outputStream
                outputStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                val responseMessage = conn.responseMessage
                Log.d(FIREBASE_MESSAGING, "FCM notification sent: $responseCode $responseMessage")

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = conn.errorStream.bufferedReader().use { it.readText() }
                    Log.e(FIREBASE_MESSAGING, "Error response: $errorStream")
                }

            } catch (e: Exception) {
                Log.e(FIREBASE_MESSAGING, "Exception in sending FCM notification", e)
            }
        }
    }

    private fun buildJsonPayload(
        topic: String,
        notificationBody: NotificationBody,
        senderId: String?
    ): String {
        val data = senderId?.let { """"senderId": "$senderId",""" } ?: ""

        return """
            {
                "message": {
                    "topic": "$topic",
                    "notification": {
                        "title": "${notificationBody.title}",
                        "body": "${notificationBody.message}"
                    },
                    "data": {
                        $data
                    }
                }
            }
        """.trimIndent()
    }

    private fun buildConnection(context: Context): HttpURLConnection {
        val url = URL(BASE_URL + FCM_SEND_ENDPOINT)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.doInput = true

        conn.setRequestProperty("Authorization", "Bearer ${getAccessToken(context)}")
        conn.setRequestProperty("Content-Type", "application/json")
        return conn
    }

    private fun getAccessToken(context: Context): String {
        val assetManager = context.assets
        val inputStream = assetManager.open(SERVICE_ACCOUNT_JSON)

        val googleCredentials: GoogleCredentials =
            GoogleCredentials.fromStream(inputStream).createScoped(SCOPES)
        googleCredentials.refreshIfExpired()
        return googleCredentials.accessToken.tokenValue
    }

    companion object {
        private const val SERVICE_ACCOUNT_JSON = "serviceAccountKey.json"
        private const val PROJECT_ID = "diceroom-85beb"
        private const val BASE_URL = "https://fcm.googleapis.com"
        private const val FCM_SEND_ENDPOINT = "/v1/projects/${PROJECT_ID}/messages:send"
        private val SCOPES = listOf("https://www.googleapis.com/auth/firebase.messaging")
    }
}