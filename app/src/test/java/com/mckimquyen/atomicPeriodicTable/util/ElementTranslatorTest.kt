package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ElementTranslatorTest {

    private lateinit var mockContext: Context
    private lateinit var mockResources: Resources

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockResources = mock(Resources::class.java)
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockContext.packageName).thenReturn("com.mckimquyen.atomicPeriodicTable")
    }

    @Test
    fun testGetCleanName() {
        assertEquals("hydrogen", ElementTranslator.getCleanName("Hydrogen"))
        assertEquals("hydrogen", ElementTranslator.getCleanName("  HYDROGEN  "))
        assertEquals("flerovium", ElementTranslator.getCleanName("flerovium"))
    }

    @Test
    fun testGetLocalizedName_whenKeyExists() {
        val fakeResId = 0x7f123456
        `when`(mockResources.getIdentifier(eq("element_name_hydrogen"), eq("string"), anyString()))
            .thenReturn(fakeResId)
        `when`(mockContext.getString(fakeResId)).thenReturn("Hydro")

        val result = ElementTranslator.getLocalizedName(mockContext, "Hydrogen")
        assertEquals("Hydro", result)
    }

    @Test
    fun testGetLocalizedName_fallbackToCapitalizedWhenNotFound() {
        `when`(mockResources.getIdentifier(anyString(), eq("string"), anyString()))
            .thenReturn(0)

        val result = ElementTranslator.getLocalizedName(mockContext, "unknown_element")
        assertEquals("Unknown_element", result)
    }

    @Test
    fun testAll118ElementsCleanNames() {
        val elementNames = listOf(
            "Hydrogen", "Helium", "Lithium", "Beryllium", "Boron", "Carbon", "Nitrogen", "Oxygen",
            "Fluorine", "Neon", "Sodium", "Magnesium", "Aluminium", "Silicon", "Phosphorus", "Sulfur",
            "Chlorine", "Argon", "Potassium", "Calcium", "Scandium", "Titanium", "Vanadium", "Chromium",
            "Manganese", "Iron", "Cobalt", "Nickel", "Copper", "Zinc", "Gallium", "Germanium",
            "Arsenic", "Selenium", "Bromine", "Krypton", "Rubidium", "Strontium", "Yttrium", "Zirconium",
            "Niobium", "Molybdenum", "Technetium", "Ruthenium", "Rhodium", "Palladium", "Silver", "Cadmium",
            "Indium", "Tin", "Antimony", "Tellurium", "Iodine", "Xenon", "Caesium", "Barium",
            "Lanthanum", "Cerium", "Praseodymium", "Neodymium", "Promethium", "Samarium", "Europium", "Gadolinium",
            "Terbium", "Dysprosium", "Holmium", "Erbium", "Thulium", "Ytterbium", "Lutetium", "Hafnium",
            "Tantalum", "Tungsten", "Rhenium", "Osmium", "Iridium", "Platinum", "Gold", "Mercury",
            "Thallium", "Lead", "Bismuth", "Polonium", "Astatine", "Radon", "Francium", "Radium",
            "Actinium", "Thorium", "Protactinium", "Uranium", "Neptunium", "Plutonium", "Americium", "Curium",
            "Berkelium", "Californium", "Einsteinium", "Fermium", "Mendelevium", "Nobelium", "Lawrencium", "Rutherfordium",
            "Dubnium", "Seaborgium", "Bohrium", "Hassium", "Meitnerium", "Darmstadtium", "Roentgenium", "Copernicium",
            "Nihonium", "Flerovium", "Moscovium", "Livermorium", "Tennessine", "Oganesson"
        )
        assertEquals(118, elementNames.size)
        elementNames.forEach { name ->
            val clean = ElementTranslator.getCleanName(name)
            assertEquals(name.lowercase(), clean)
        }
    }
}
