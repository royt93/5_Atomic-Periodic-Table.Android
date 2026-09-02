package com.mckimquyen.atomicPeriodicTable.act

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.ScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.feature.trivia.DailyTriviaPref
import com.mckimquyen.atomicPeriodicTable.feature.trivia.DailyTriviaScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.File

/**
 * Regression guard for FIX-029: the "clear cache" action used to call
 * cacheDir.deleteRecursively(), which deletes the cache directory itself (not just its
 * contents) — a directory Android guarantees exists for the app's lifetime. Clearing cache
 * must empty it, not remove it.
 */
@RunWith(AndroidJUnit4::class)
class SettingsActTest {

    // POST_NOTIFICATIONS is a runtime permission on API 33+ only — without this rule, toggling
    // the Daily Trivia switch on in a test would block on the real system permission dialog;
    // asking UiAutomation to grant it pre-33 throws SecurityException (unknown permission there).
    @get:Rule
    val notificationPermissionRule: TestRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        TestRule { base, _ -> base }
    }

    @Before
    fun resetDailyTriviaState() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Daily_Trivia_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        DailyTriviaScheduler.cancel(context)
    }

    @Test
    fun clearCache_emptiesCacheDir_withoutDeletingTheDirectoryItself() {
        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            var cacheDir: File? = null
            var dummyFile: File? = null

            scenario.onActivity { activity ->
                cacheDir = activity.cacheDir
                dummyFile = File(activity.cacheDir, "fix029_test_file.tmp").apply { writeText("x") }
                // cacheLay sits below the fold in scrollSettings — scroll to it programmatically
                // (same FIX-038 reasoning: a synthesized swipe gesture risks hitting the OS
                // gesture-navigation zone near the screen edge on real devices).
                val scrollView = activity.findViewById<ScrollView>(R.id.scrollSettings)
                val row = activity.findViewById<android.view.View>(R.id.clearCacheSettings)
                val rowLoc = IntArray(2).also { row.getLocationInWindow(it) }
                val scrollViewLoc = IntArray(2).also { scrollView.getLocationInWindow(it) }
                scrollView.scrollTo(0, scrollView.scrollY + (rowLoc[1] - scrollViewLoc[1]))
            }
            assertTrue("setup: dummy file must exist before clearing cache", dummyFile!!.exists())

            onView(withId(R.id.clearCacheSettings)).perform(click())

            assertTrue("cacheDir itself must still exist after clearing cache", cacheDir!!.exists())
            assertTrue("cacheDir must still be a directory after clearing cache", cacheDir!!.isDirectory)
            assertFalse("cache contents must be gone after clearing cache", dummyFile!!.exists())
        }
    }

    @Test
    fun togglingDailyTriviaOn_persistsPrefAndSchedulesAlarm() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.dailyTriviaSwitch).performClick()
            }
            Thread.sleep(300) // permission-check + scheduler round trip

            assertTrue(DailyTriviaPref(context).isEnabled())
            assertTrue(DailyTriviaScheduler.isScheduled(context))
        }
    }

    @Test
    fun togglingDailyTriviaOff_clearsPrefAndCancelsAlarm() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        DailyTriviaPref(context).setEnabled(true)
        DailyTriviaScheduler.schedule(context)

        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.dailyTriviaSwitch).performClick()
            }
            Thread.sleep(300)

            assertFalse(DailyTriviaPref(context).isEnabled())
            assertFalse(DailyTriviaScheduler.isScheduled(context))
        }
    }

    // Export/Import buttons launch the system file picker (ACTION_CREATE_DOCUMENT /
    // ACTION_OPEN_DOCUMENT), which Espresso cannot drive — instead this exercises the actual
    // private write/read glue functions via reflection against a real temp-file Uri, covering
    // the full export -> import round trip through SettingsAct's own code path.
    @Test
    fun exportThenImport_throughSettingsActGlue_roundTripsStreak() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        StudyStreakPref(context).recordStudyToday(todayEpochDay = 20003L)

        val tempFile = File(context.cacheDir, "settings_act_backup_test.json")
        val uri = Uri.fromFile(tempFile)

        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val writeMethod = SettingsAct::class.java.getDeclaredMethod("writeBackupToUri", Uri::class.java)
                writeMethod.isAccessible = true
                writeMethod.invoke(activity, uri)
            }
            assertTrue("backup file must be written", tempFile.exists() && tempFile.length() > 0)

            // Clear local state, then restore from the file we just wrote.
            context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()

            scenario.onActivity { activity ->
                val readMethod = SettingsAct::class.java.getDeclaredMethod("readBackupFromUri", Uri::class.java)
                readMethod.isAccessible = true
                readMethod.invoke(activity, uri)
            }

            assertEquals(1, StudyStreakPref(context).getCurrentStreak())
            assertEquals(20003L, StudyStreakPref(context).getLastEpochDay())
        }
        tempFile.delete()
    }
}
