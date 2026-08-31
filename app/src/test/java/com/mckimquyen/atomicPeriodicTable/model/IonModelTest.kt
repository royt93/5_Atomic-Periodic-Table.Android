package com.mckimquyen.atomicPeriodicTable.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IonModelTest {

    @Test
    fun testIonModelHasOneEntryPerElement() {
        val ions = ArrayList<Ion>()
        IonModel.getList(ions)
        assertEquals(118, ions.size)
    }

    // Regression guard for FIX-001/FIX-002: IonModel used to disagree with ElementModel
    // on the symbol for the same element (platinum="Pa", palladium="Ph"). Both models
    // describe the same 118 elements and must always agree on the chemical symbol.
    @Test
    fun testIonModelSymbolsMatchElementModelByName() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val symbolByName = elements.associateBy({ it.element }, { it.short })

        val ions = ArrayList<Ion>()
        IonModel.getList(ions)

        val mismatches = ions.map { ion ->
            val expected = symbolByName[ion.name]
            when {
                expected == null -> "${ion.name}: không tồn tại trong ElementModel"
                expected != ion.short -> "${ion.name}: IonModel=${ion.short} vs ElementModel=$expected"
                else -> null
            }
        }.filterNotNull()

        assertTrue("IonModel/ElementModel symbol mismatch: $mismatches", mismatches.isEmpty())
    }

    @Test
    fun testPlatinumIonSymbolIsPt() {
        val ions = ArrayList<Ion>()
        IonModel.getList(ions)
        val platinum = ions.first { it.name == "platinum" }
        assertEquals("Pt", platinum.short)
    }

    @Test
    fun testPalladiumIonSymbolIsPd() {
        val ions = ArrayList<Ion>()
        IonModel.getList(ions)
        val palladium = ions.first { it.name == "palladium" }
        assertEquals("Pd", palladium.short)
    }
}
