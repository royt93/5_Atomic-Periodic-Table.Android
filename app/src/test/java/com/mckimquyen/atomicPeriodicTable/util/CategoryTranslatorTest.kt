package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CategoryTranslatorTest {

    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        `when`(mockContext.getString(R.string.cat_alkali_metals)).thenReturn("Alkali Metals")
        `when`(mockContext.getString(R.string.cat_alkaline_earth)).thenReturn("Alkaline Earth Metals")
        `when`(mockContext.getString(R.string.cat_transition_metals)).thenReturn("Transition Metals")
        `when`(mockContext.getString(R.string.cat_lanthanides)).thenReturn("Lanthanides")
        `when`(mockContext.getString(R.string.cat_actinides)).thenReturn("Actinides")
        `when`(mockContext.getString(R.string.cat_post_transition)).thenReturn("Post-transition Metals")
        `when`(mockContext.getString(R.string.cat_metalloids)).thenReturn("Metalloids")
        `when`(mockContext.getString(R.string.cat_reactive_nonmetals)).thenReturn("Reactive Nonmetals")
        `when`(mockContext.getString(R.string.cat_noble_gases)).thenReturn("Noble Gases")
        `when`(mockContext.getString(R.string.cat_halogens)).thenReturn("Halogens")
        `when`(mockContext.getString(R.string.cat_other_nonmetals)).thenReturn("Other Nonmetals")
    }

    @Test
    fun testTranslateAllCategories() {
        assertEquals("Alkali Metals", CategoryTranslator.translate(mockContext, "alkali metal"))
        assertEquals("Alkali Metals", CategoryTranslator.translate(mockContext, "alkali metals"))
        assertEquals("Alkaline Earth Metals", CategoryTranslator.translate(mockContext, "alkaline earth metal"))
        assertEquals("Transition Metals", CategoryTranslator.translate(mockContext, "transition metal"))
        assertEquals("Lanthanides", CategoryTranslator.translate(mockContext, "lanthanide"))
        assertEquals("Actinides", CategoryTranslator.translate(mockContext, "actinide"))
        assertEquals("Post-transition Metals", CategoryTranslator.translate(mockContext, "post-transition metal"))
        assertEquals("Metalloids", CategoryTranslator.translate(mockContext, "metalloid"))
        assertEquals("Reactive Nonmetals", CategoryTranslator.translate(mockContext, "reactive nonmetal"))
        assertEquals("Noble Gases", CategoryTranslator.translate(mockContext, "noble gas"))
        assertEquals("Halogens", CategoryTranslator.translate(mockContext, "halogen"))
        assertEquals("Other Nonmetals", CategoryTranslator.translate(mockContext, "other nonmetal"))
    }

    @Test
    fun testTranslateUnknownFallback() {
        val unknown = "Super Heavy Unknown Element"
        assertEquals(unknown, CategoryTranslator.translate(mockContext, unknown))
    }
}
