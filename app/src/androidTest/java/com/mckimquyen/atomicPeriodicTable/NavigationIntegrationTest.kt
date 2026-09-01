package com.mckimquyen.atomicPeriodicTable

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.act.MainAct
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @Test
    fun testNavigateFromMainToQuiz() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Quiz menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuQuizBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Quiz screen is visible
            onView(withId(R.id.quizProgress)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToBalancer() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Balancer menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuBalancerBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Balancer screen is visible
            onView(withId(R.id.balancerInput)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToCalculator() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Calculator menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuMolarMassBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Calculator screen is visible
            onView(withId(R.id.calcInput)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToFlashcard() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Flashcard menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuFlashcardBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Flashcard screen is visible
            onView(withId(R.id.tvFlashcardProgress)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToTrendsChart() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Trends menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuTrendsBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Trends Chart screen is visible
            onView(withId(R.id.cardTrendsChart)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToUnitConverter() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.menuBtn).performClick()
            }
            Thread.sleep(1000)

            // Click Unit Converter menu button programmatically
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.menuUnitConverterBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify Unit Converter screen is visible
            onView(withId(R.id.cardConverter)).check(matches(isDisplayed()))
        }
    }
}
