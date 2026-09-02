package com.mckimquyen.atomicPeriodicTable.feature.trivia

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyTriviaReceiverTest {

    // POST_NOTIFICATIONS only exists as a grantable runtime permission from API 33 — asking
    // UiAutomation to grant it on older API levels throws SecurityException, so skip the rule
    // there (the permission is implicitly available pre-33 anyway).
    @get:Rule
    val notificationPermissionRule: TestRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        TestRule { base, _ -> base }
    }

    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("Daily_Trivia_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        NotificationManagerCompat.from(context).cancel(DailyTriviaReceiver.NOTIFICATION_ID)
        // Some OEM notification trays (observed on ColorOS) don't reflect cancel() in
        // activeNotifications() immediately when tests run back-to-back — give it a beat.
        Thread.sleep(200)
    }

    private fun postedNotificationIds(): List<Int> = NotificationManagerCompat.from(context).activeNotifications.map { it.id }

    @Test
    fun alarmFires_whenEnabled_postsNotification() {
        DailyTriviaPref(context).setEnabled(true)

        DailyTriviaReceiver().onReceive(context, Intent(DailyTriviaReceiver.ACTION_SHOW_TRIVIA))

        assertTrue(DailyTriviaReceiver.NOTIFICATION_ID in postedNotificationIds())
    }

    @Test
    fun alarmFires_whenDisabled_postsNothing() {
        DailyTriviaPref(context).setEnabled(false)

        DailyTriviaReceiver().onReceive(context, Intent(DailyTriviaReceiver.ACTION_SHOW_TRIVIA))

        assertFalse(DailyTriviaReceiver.NOTIFICATION_ID in postedNotificationIds())
    }

    @Test
    fun bootCompleted_whenEnabled_reschedulesAlarm() {
        DailyTriviaPref(context).setEnabled(true)
        DailyTriviaScheduler.cancel(context)
        assertFalse(DailyTriviaScheduler.isScheduled(context))

        DailyTriviaReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertTrue(DailyTriviaScheduler.isScheduled(context))
    }

    @Test
    fun bootCompleted_whenDisabled_doesNotSchedule() {
        DailyTriviaPref(context).setEnabled(false)
        DailyTriviaScheduler.cancel(context)

        DailyTriviaReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertFalse(DailyTriviaScheduler.isScheduled(context))
    }
}
