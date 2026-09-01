package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardState
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashcardActTest {

    @Before
    fun clearFlashcardPrefs() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Flashcard_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun ratingACard_recordsStudyStreak() {
        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.cardFlashcard)).perform(click())
            Thread.sleep(400) // flip animation
            onView(withId(R.id.btnFlashcardGood)).perform(click())
            Thread.sleep(400) // card-advance slide animation

            val streak = StudyStreakPref(ApplicationProvider.getApplicationContext()).getCurrentStreak()
            assertEquals(1, streak)
        }
    }

    @Test
    fun launch_showsFirstDueCardFront_withHintAndNoRatingsYet() {
        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.tvFlashcardFront)).check(matches(withText("H")))
            onView(withId(R.id.tvFlashcardHint)).check(matches(isDisplayed()))
            onView(withId(R.id.tvFlashcardProgress)).check(matches(withText("1 / 118")))
        }
    }

    @Test
    fun tapCard_flipsToShowBackAndRatingButtons() {
        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.cardFlashcard)).perform(click())
            Thread.sleep(400) // let the flip animation finish before asserting on the back face
            onView(withId(R.id.tvFlashcardBack)).check(matches(isDisplayed()))
            onView(withId(R.id.layoutFlashcardRatings)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun ratingGood_persistsFirstRepetitionState_andAdvancesToNextCard() {
        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.cardFlashcard)).perform(click())
            Thread.sleep(400) // flip animation
            onView(withId(R.id.btnFlashcardGood)).perform(click())
            Thread.sleep(400) // card-advance slide animation

            val state = FlashcardPref(ApplicationProvider.getApplicationContext()).getState("H")
            assertEquals(1, state.repetitions)
            assertEquals(1, state.intervalDays)

            onView(withId(R.id.tvFlashcardFront)).check(matches(withText("He")))
            onView(withId(R.id.tvFlashcardProgress)).check(matches(withText("2 / 118")))
            // New card must reset to front-facing state
            onView(withId(R.id.layoutFlashcardRatings)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        }
    }

    @Test
    fun ratingAgain_setsOneDayIntervalAndResetsRepetitions() {
        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.cardFlashcard)).perform(click())
            Thread.sleep(400) // flip animation
            onView(withId(R.id.btnFlashcardAgain)).perform(click())
            Thread.sleep(400) // card-advance slide animation

            val state = FlashcardPref(ApplicationProvider.getApplicationContext()).getState("H")
            assertEquals(0, state.repetitions)
            assertEquals(1, state.intervalDays)
        }
    }

    @Test
    fun ratingClick_beforeFlip_isIgnored() {
        ActivityScenario.launch(FlashcardAct::class.java).use { scenario ->
            // Rating row is INVISIBLE (not GONE) before flip, so directly invoking the
            // listener simulates a stray tap and must be a no-op — the card must not advance.
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.btnFlashcardGood).performClick()
            }
            onView(withId(R.id.tvFlashcardFront)).check(matches(withText("H")))
            onView(withId(R.id.tvFlashcardProgress)).check(matches(withText("1 / 118")))
        }
    }

    // Edge case: nothing due yet (every card just reviewed with a future date) must fall
    // back to the full 118-element deck instead of showing an empty/complete screen.
    @Test
    fun allCardsNotYetDue_fallsBackToFullDeckPracticeMode() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val pref = FlashcardPref(context)
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val farFuture = System.currentTimeMillis() + 30L * 86_400_000L
        for (element in elements) {
            pref.saveState(element.short, FlashcardState(intervalDays = 30, repetitions = 3), farFuture)
        }

        ActivityScenario.launch(FlashcardAct::class.java).use {
            onView(withId(R.id.tvFlashcardProgress)).check(matches(withText("1 / 118")))
            onView(withId(R.id.cardFlashcard)).check(matches(isDisplayed()))
        }
    }

    // Integration: rate every card GOOD once, verify the session-complete screen appears
    // and the queue size matches the full deck (end-to-end flip -> rate -> advance -> finish).
    @Test
    fun ratingEveryCard_reachesSessionComplete() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val elementCount = elements.size

        ActivityScenario.launch(FlashcardAct::class.java).use {
            repeat(elementCount) {
                onView(withId(R.id.cardFlashcard)).perform(click())
                Thread.sleep(310) // flip animation
                onView(withId(R.id.btnFlashcardGood)).perform(click())
                Thread.sleep(310) // card-advance slide animation
            }
            onView(withId(R.id.tvFlashcardComplete)).check(matches(isDisplayed()))
            assertTrue(elementCount > 0)
        }
    }
}
