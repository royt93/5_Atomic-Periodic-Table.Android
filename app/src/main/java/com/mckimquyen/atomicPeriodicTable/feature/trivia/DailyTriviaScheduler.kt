package com.mckimquyen.atomicPeriodicTable.feature.trivia

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mckimquyen.atomicPeriodicTable.R
import java.util.Calendar

/**
 * No WorkManager per project rule — uses AlarmManager.setRepeating(), which the platform is
 * free to batch/delay under Doze. Accepted trade-off (documented in doc/task/feat_new.md mục 10):
 * this is a best-effort daily reminder, not a precision timer.
 */
object DailyTriviaScheduler {
    const val CHANNEL_ID = "daily_trivia_channel"
    const val TRIGGER_HOUR = 9
    const val TRIGGER_MINUTE = 0
    private const val REQUEST_CODE = 4200

    /** Pure: the epoch-millis of the next TRIGGER_HOUR:TRIGGER_MINUTE strictly after [nowMillis]. */
    fun nextTriggerAtMillis(nowMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nowMillis
        calendar.set(Calendar.HOUR_OF_DAY, TRIGGER_HOUR)
        calendar.set(Calendar.MINUTE, TRIGGER_MINUTE)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    fun schedule(context: Context) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerAtMillis(System.currentTimeMillis()),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)!!, // never null without FLAG_NO_CREATE
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context)!! // never null without FLAG_NO_CREATE
        alarmManager.cancel(pending)
        // AlarmManager.cancel() only stops the alarm from firing — the PendingIntent itself
        // stays registered with the system until explicitly cancelled, so isScheduled() would
        // otherwise keep reporting "scheduled" after this call.
        pending.cancel()
    }

    /** Whether an alarm is currently registered — for tests; production code never needs to ask. */
    fun isScheduled(context: Context): Boolean =
        pendingIntent(context, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null

    private fun pendingIntent(context: Context, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE): PendingIntent? {
        val intent = Intent(context, DailyTriviaReceiver::class.java).apply {
            action = DailyTriviaReceiver.ACTION_SHOW_TRIVIA
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.daily_trivia_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
                manager.createNotificationChannel(channel)
            }
        }
    }
}
