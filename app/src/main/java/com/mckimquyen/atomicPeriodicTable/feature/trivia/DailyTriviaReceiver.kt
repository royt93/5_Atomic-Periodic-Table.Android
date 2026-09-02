package com.mckimquyen.atomicPeriodicTable.feature.trivia

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.ElementInfoAct
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.util.ElementOfDay
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache

/**
 * Fires the daily reminder (from the AlarmManager set in DailyTriviaScheduler) and reschedules
 * on device reboot (alarms don't survive a reboot). Same element-of-the-day source of truth as
 * ShortCommandWidget (ElementOfDay.indexForDay + ElementWeightCache.getFact) so the widget and
 * the notification never disagree about which element "today" is.
 */
class DailyTriviaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (DailyTriviaPref(context).isEnabled()) {
                    DailyTriviaScheduler.schedule(context)
                }
            }
            ACTION_SHOW_TRIVIA -> {
                if (DailyTriviaPref(context).isEnabled()) {
                    showNotification(context)
                }
            }
        }
    }

    private fun showNotification(context: Context) {
        DailyTriviaScheduler.ensureChannel(context)
        ElementWeightCache.init(context)

        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val today = elements[ElementOfDay.indexForDay(System.currentTimeMillis() / 86_400_000L, elements.size)]
        ElementSendAndLoad(context).setValue(today.element)

        val title = "${today.short} — ${ElementTranslator.getLocalizedName(context, today.element)}"
        val body = ElementWeightCache.getFact(today.short) ?: ""

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, ElementInfoAct::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, DailyTriviaScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val ACTION_SHOW_TRIVIA = "com.mckimquyen.atomicPeriodicTable.ACTION_DAILY_TRIVIA"
        const val NOTIFICATION_ID = 4201
    }
}
