package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class ThemePrefTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var themePref: ThemePref

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(eq("Theme_Preference"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPreferences)
        
        `when`(mockPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt(any(), any())).thenReturn(mockEditor)
        
        themePref = ThemePref(mockContext)
    }

    @Test
    fun testGetValueDefault() {
        `when`(mockPreferences.getInt(eq("Theme_Value"), eq(100))).thenReturn(100)
        assertEquals(100, themePref.getValue())
    }

    @Test
    fun testGetValueCustom() {
        `when`(mockPreferences.getInt(eq("Theme_Value"), eq(100))).thenReturn(1)
        assertEquals(1, themePref.getValue())
    }

    @Test
    fun testSetValue() {
        themePref.setValue(0)
        verify(mockEditor).putInt(eq("Theme_Value"), eq(0))
        verify(mockEditor).apply()
    }
}
