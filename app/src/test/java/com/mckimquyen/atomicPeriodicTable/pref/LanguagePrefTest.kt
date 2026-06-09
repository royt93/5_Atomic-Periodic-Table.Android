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

class LanguagePrefTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var languagePref: LanguagePref

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(eq("Language_Preference"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPreferences)
        
        `when`(mockPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        
        languagePref = LanguagePref(mockContext)
    }

    @Test
    fun testGetValueDefault() {
        `when`(mockPreferences.getString(eq("Language_Value"), eq("en"))).thenReturn("en")
        assertEquals("en", languagePref.getValue())
    }

    @Test
    fun testGetValueCustom() {
        `when`(mockPreferences.getString(eq("Language_Value"), eq("en"))).thenReturn("vi")
        assertEquals("vi", languagePref.getValue())
    }

    @Test
    fun testSetValue() {
        languagePref.setValue("fr")
        verify(mockEditor).putString(eq("Language_Value"), eq("fr"))
        verify(mockEditor).commit()
    }
}
