package com.mckimquyen.atomicPeriodicTable.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.mckimquyen.atomicPeriodicTable.R
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@Suppress("unused")
fun Activity.setSoftInputMode(
    mode: Int,
) {
    this.window.setSoftInputMode(mode)
}

fun Activity.rateAppInApp(forceRateInApp: Boolean = false) {
    //import gradle app
//    implementation("com.google.android.play:review:2.0.2")
//    implementation("com.google.android.play:review-ktx:2.0.2")

    val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val lastReviewTime = sharedPreferences.getLong("last_review_time", 0L)
//    Logger.i("requestReview lastReviewTime $lastReviewTime")
    val currentTime = Calendar.getInstance().timeInMillis
    val daysSinceLastReview = (currentTime - lastReviewTime) / (1000 * 60 * 60 * 24)
//    Logger.i("requestReview forceRateInApp $forceRateInApp")
//    Logger.i("requestReview daysSinceLastReview $daysSinceLastReview")
    if (daysSinceLastReview >= 30 || forceRateInApp) {
//    if (daysSinceLastReview >= 30) {
        val reviewManager = ReviewManagerFactory.create(this)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    val reviewInfo: ReviewInfo = task.result
                    reviewManager.launchReviewFlow(this, reviewInfo)
                    sharedPreferences.edit().putLong("last_review_time", currentTime).apply()
//                    Logger.i("requestReview result ${task.result}")
//                    Logger.i("requestReview isSuccessful ${task.isSuccessful}")
//                    Logger.i("requestReview isCanceled ${task.isCanceled}")
//                    Logger.i("requestReview isComplete ${task.isComplete}")
//                    Logger.i("requestReview exception ${task.exception}")
                } else {
                    // Optimized: Remove unused variable - only needed if logging is enabled
                    // @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException?)?.errorCode
//                    Logger.e("requestReview error $reviewErrorCode")
                }
            } catch (e: Exception) {
//                Logger.e("catch e $e")
            }
        }
    }
}

fun Activity.rateApp(
    packageName: String? = null,
) {
    if (packageName.isNullOrEmpty()) {
        return
    }
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()
            )
        )
    } catch (e: android.content.ActivityNotFoundException) {
        e.printStackTrace()
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "http://play.google.com/store/apps/details?id=$packageName".toUri()
            )
        )
    }
}

fun Activity.moreApp(
    nameOfDeveloper: String = "SAIGON PHANTOM LABS",
) {
    val uri = "https://play.google.com/store/apps/developer?id=$nameOfDeveloper"
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    this.startActivity(intent)
}

fun Activity.shareApp(
) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
        // Optimized: Use string template instead of concatenation
        val sAux =
            "\n${this.getString(R.string.share_msg)}\n\nhttps://play.google.com/store/apps/details?id=${this.packageName}"
        intent.putExtra(Intent.EXTRA_TEXT, sAux)
        this.startActivity(Intent.createChooser(intent, this.getString(R.string.chooser_title)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/** Saves [bitmap] to cacheDir and shares it via a FileProvider content:// Uri (image/png). */
fun Activity.shareImage(bitmap: Bitmap, fileName: String) {
    try {
        val file = File(this.cacheDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(this, "${this.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        this.startActivity(Intent.createChooser(intent, this.getString(R.string.chooser_title)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


