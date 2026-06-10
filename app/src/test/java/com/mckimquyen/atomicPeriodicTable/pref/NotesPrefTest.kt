package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

/**
 * Unit tests for [NotesPref].
 *
 * Covers the "notes_" key prefix (per the feat.md spec) and the legacy migration
 * path from the old "note_" prefix so existing user notes are never lost.
 */
class NotesPrefTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var notesPref: NotesPref

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(eq("Element_Notes_Preference"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPreferences)

        `when`(mockPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        `when`(mockEditor.remove(any())).thenReturn(mockEditor)

        notesPref = NotesPref(mockContext)
    }

    @Test
    fun testSaveNote_usesNotesPrefix() {
        notesPref.saveNote("hydrogen", "My note")
        verify(mockEditor).putString(eq("notes_hydrogen"), eq("My note"))
        verify(mockEditor).apply()
    }

    @Test
    fun testSaveNote_emptyNoteStored() {
        notesPref.saveNote("oxygen", "")
        verify(mockEditor).putString(eq("notes_oxygen"), eq(""))
        verify(mockEditor).apply()
    }

    @Test
    fun testGetNote_returnsNewKeyValueWhenPresent() {
        `when`(mockPreferences.getString(eq("notes_hydrogen"), eq(null))).thenReturn("Existing note")

        assertEquals("Existing note", notesPref.getNote("hydrogen"))

        // No migration should occur and the legacy key must not be touched.
        verify(mockPreferences, never()).getString(eq("note_hydrogen"), any())
        verify(mockEditor, never()).apply()
    }

    @Test
    fun testGetNote_returnsEmptyWhenNothingStored() {
        `when`(mockPreferences.getString(eq("notes_carbon"), eq(null))).thenReturn(null)
        `when`(mockPreferences.getString(eq("note_carbon"), eq(""))).thenReturn("")

        assertEquals("", notesPref.getNote("carbon"))

        // Nothing to migrate -> no write.
        verify(mockEditor, never()).apply()
    }

    @Test
    fun testGetNote_migratesLegacyNoteToNewKey() {
        // New key empty, legacy key has a value -> migrate.
        `when`(mockPreferences.getString(eq("notes_iron"), eq(null))).thenReturn(null)
        `when`(mockPreferences.getString(eq("note_iron"), eq(""))).thenReturn("Legacy note")

        val result = notesPref.getNote("iron")

        assertEquals("Legacy note", result)
        verify(mockEditor).putString(eq("notes_iron"), eq("Legacy note"))
        verify(mockEditor).remove(eq("note_iron"))
        verify(mockEditor).apply()
    }

    @Test
    fun testGetNote_noMigrationWhenLegacyEmpty() {
        `when`(mockPreferences.getString(eq("notes_gold"), eq(null))).thenReturn(null)
        `when`(mockPreferences.getString(eq("note_gold"), eq(""))).thenReturn("")

        assertEquals("", notesPref.getNote("gold"))

        verify(mockEditor, never()).putString(any(), any())
        verify(mockEditor, never()).remove(any())
        verify(mockEditor, never()).apply()
    }

    @Test
    fun testGetNote_newKeyTakesPrecedenceOverLegacy() {
        // Once migrated/saved, the new key wins even if a legacy value somehow remains.
        `when`(mockPreferences.getString(eq("notes_neon"), eq(null))).thenReturn("New note")

        assertEquals("New note", notesPref.getNote("neon"))
        verify(mockPreferences, never()).getString(eq("note_neon"), any())
    }
}
