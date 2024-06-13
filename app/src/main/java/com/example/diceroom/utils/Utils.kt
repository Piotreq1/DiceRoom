package com.example.diceroom.utils

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.diceroom.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, duration).show()
        }
    }

    private fun showSnackbar(context: Context, message: String) {
        val rootView: View =
            (context as? androidx.appcompat.app.AppCompatActivity)?.findViewById(android.R.id.content)
                ?: View(context)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    fun handleFirebaseResult(
        isSuccess: Boolean, message: String?, context: Context, success: String, failure: String
    ) {
        if (isSuccess) {
            showToast(context, success)
        } else {
            if (message != null) {
                showSnackbar(context, message)
            } else {
                showToast(context, failure)
            }
        }
    }

    fun uploadImageToFirebaseStorage(uri: Uri?, onComplete: (Boolean, String?) -> Unit) {
        if (uri == null) {
            onComplete(false, "File URI cannot be null")
            return
        }
        val filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileRef = FirebaseStorage.getInstance().reference.child("images/$filename")
        fileRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val url = downloadUrl.toString()
                    onComplete(true, url)
                }.addOnFailureListener { downloadUrl ->
                    onComplete(false, downloadUrl.message)
                }
            } else {
                onComplete(false, it.exception?.message)
            }
        }
    }

    fun downloadImageFromFirebaseStorage(
        context: Context, imageUrl: String?, onComplete: (Boolean, File?) -> Unit
    ) {
        if (imageUrl.isNullOrEmpty()) {
            onComplete(false, null)
            return
        }
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        val cacheDir = context.cacheDir
        val localFile = File(cacheDir, "tempFile")
        storageReference.getFile(localFile).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(true, localFile)
            } else {
                onComplete(false, null)
            }
        }
    }

    fun showDatePickerDialog(context: Context, place: TextInputEditText, isFuture: Boolean) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val selectedCalendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                        ?: Date()
                }
                val isValidDate = if (isFuture) {
                    selectedCalendar.timeInMillis > System.currentTimeMillis()
                } else {
                    selectedCalendar.timeInMillis < System.currentTimeMillis()
                }

                if (isValidDate) {
                    place.setText(selectedDate)
                } else {
                    showToast(
                        context, "Please select a valid ${if (isFuture) "future" else "past"} date."
                    )
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            show()
        }
    }


    fun loadGlide(context: Context, drawable: Any, place: ImageView) {
        Glide.with(context).load(drawable).apply(RequestOptions().placeholder(R.drawable.loading))
            .into(place)
    }

    fun loadGlide(view: View, drawable: Any, place: ImageView) {
        Glide.with(view).load(drawable).apply(RequestOptions().placeholder(R.drawable.loading))
            .into(place)
    }

    fun createMessagingTopicForMeeting(meetingId: String) {
        Firebase.messaging.subscribeToTopic(meetingId).addOnCompleteListener { task ->
            var msg = "Subscribed"
            if (!task.isSuccessful) {
                msg = "Subscribe failed"
            }
            Log.d(Constants.FIREBASE_MESSAGING, msg)
        }
    }

    fun leaveMessagingMeetingTopic(meetingId: String) {
        Firebase.messaging.unsubscribeFromTopic(meetingId).addOnCompleteListener { task ->
            var msg = "Unsubscribed"
            if (!task.isSuccessful) {
                msg = "Unsubscribe failed"
            }
            Log.d(Constants.FIREBASE_MESSAGING, msg)
        }
    }
}