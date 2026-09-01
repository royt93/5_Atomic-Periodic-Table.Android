package com.mckimquyen.atomicPeriodicTable.act.setting

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.pref.TemperatureUnits
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for FIX-024: FavoritePageAct used to read/write its own DegreePref (Int),
 * a completely separate SharedPreferences from TemperatureUnits (String) that Settings >
 * Units (UnitAct) reads/writes. Picking a unit in one screen silently had no effect on the
 * other. Both screens must now share the same TemperatureUnits pref.
 */
@RunWith(AndroidJUnit4::class)
class FavoritePageTemperatureUnitTest {

    @After
    fun tearDown() {
        TemperatureUnits(ApplicationProvider.getApplicationContext()).setValue("celsius")
    }

    @Test
    fun tappingFahrenheitInFavoritePage_writesToSameTemperatureUnitsPrefAsSettings() {
        ActivityScenario.launch(FavoritePageAct::class.java).use {
            onView(withId(R.id.fahrenheitbtn)).perform(click())
        }

        val actual = TemperatureUnits(ApplicationProvider.getApplicationContext()).getValue()
        assertEquals("kelvinBtn/celsiusBtn/fahrenheitbtn must write the same pref UnitAct reads", "fahrenheit", actual)
    }
}
