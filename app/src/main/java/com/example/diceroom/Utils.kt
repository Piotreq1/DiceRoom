package com.example.diceroom

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utils {

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
}