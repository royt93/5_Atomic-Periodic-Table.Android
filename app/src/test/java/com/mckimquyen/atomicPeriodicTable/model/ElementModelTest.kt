package com.mckimquyen.atomicPeriodicTable.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementModelTest {

    @Test
    fun testElementModelLoadList() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        
        // Ensure that the list is populated and contains all 118 elements
        assertEquals("Bảng tuần hoàn phải có 118 nguyên tố", 118, elements.size)
    }

    @Test
    fun testElementProperties() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)

        // Test Hydrogen (First element)
        val hydrogen = elements.firstOrNull { it.number == 1 }
        assertNotNull("Không tìm thấy Hydro", hydrogen)
        assertEquals("hydrogen", hydrogen?.element)
        assertEquals("H", hydrogen?.short)
        assertEquals(2.20, hydrogen?.electro ?: 0.0, 0.001)

        // Test Helium (Second element)
        val helium = elements.firstOrNull { it.number == 2 }
        assertNotNull("Không tìm thấy Heli", helium)
        assertEquals("helium", helium?.element)
        assertEquals("He", helium?.short)

        // Test Oganesson (Last element)
        val oganesson = elements.firstOrNull { it.number == 118 }
        assertNotNull("Không tìm thấy Oganesson", oganesson)
        assertEquals("oganesson", oganesson?.element)
        assertEquals("Og", oganesson?.short)
    }

    @Test
    fun testElementsOrdering() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)

        // Check if atomic numbers are strictly increasing from 1 to 118
        for (i in 0 until 118) {
            assertEquals("Thứ tự nguyên tố bị sai ở chỉ mục $i", i + 1, elements[i].number)
        }
    }

    // Regression guard for FIX-001: Platinum (78) was assigned "Pa", colliding with
    // Protactinium (91). A symbol must map back to exactly one element.
    @Test
    fun testAllElementSymbolsAreUnique() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)

        val bySymbol = elements.groupBy { it.short }
        val duplicates = bySymbol.filterValues { it.size > 1 }
            .mapValues { (_, dupes) -> dupes.map { it.element } }

        assertTrue(
            "Symbol trùng lặp giữa các nguyên tố (mỗi symbol chỉ được gán cho đúng 1 nguyên tố): $duplicates",
            duplicates.isEmpty()
        )
        assertEquals(118, bySymbol.size)
    }

    @Test
    fun testPlatinumSymbolIsPt() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val platinum = elements.first { it.number == 78 }
        assertEquals("platinum", platinum.element)
        assertEquals("Pt", platinum.short)
    }

    @Test
    fun testProtactiniumSymbolIsPa() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val protactinium = elements.first { it.number == 91 }
        assertEquals("protactinium", protactinium.element)
        assertEquals("Pa", protactinium.short)
    }
}
