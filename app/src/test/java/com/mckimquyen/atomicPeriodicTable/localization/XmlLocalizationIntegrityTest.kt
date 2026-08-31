package com.mckimquyen.atomicPeriodicTable.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XmlLocalizationIntegrityTest {

    private val resDir = File("src/main/res")

    private val supportedLanguages = listOf(
        "ar", "de", "es", "fr", "hi", "in", "it", "ja", "ko",
        "pt", "pt-rBR", "ru", "th", "vi", "zh", "zh-rTW"
    )

    private fun extractKeys(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.parse(file)
        val stringNodes = doc.getElementsByTagName("string")
        val map = mutableMapOf<String, String>()
        for (i in 0 until stringNodes.length) {
            val item = stringNodes.item(i) as? Element ?: continue
            val name = item.getAttribute("name")
            if (name.isNotEmpty()) {
                map[name] = item.textContent ?: ""
            }
        }
        return map
    }

    @Test
    fun testBaseStringsCount() {
        val baseStrings = extractKeys(File(resDir, "values/strings.xml"))
        val baseDict = extractKeys(File(resDir, "values/strings_dict.xml"))
        val baseDesc = extractKeys(File(resDir, "values/strings_desc.xml"))

        assertTrue("Base strings.xml must have >= 500 keys", baseStrings.size >= 500)
        assertEquals("Base strings_dict.xml must have 162 keys", 162, baseDict.size)
        assertEquals("Base strings_desc.xml must have 118 keys", 118, baseDesc.size)
    }

    @Test
    fun testNoMissingKeysAcrossAllLanguages() {
        val baseStrings = extractKeys(File(resDir, "values/strings.xml"))
        val baseDict = extractKeys(File(resDir, "values/strings_dict.xml"))
        val baseDesc = extractKeys(File(resDir, "values/strings_desc.xml"))

        val totalBaseCount = baseStrings.size + baseDict.size + baseDesc.size

        for (lang in supportedLanguages) {
            val langDir = File(resDir, "values-$lang")
            assertTrue("Directory values-$lang must exist", langDir.exists())

            val langStrings = extractKeys(File(langDir, "strings.xml"))
            val langDict = extractKeys(File(langDir, "strings_dict.xml"))
            val langDesc = extractKeys(File(langDir, "strings_desc.xml"))

            val missingStrings = baseStrings.keys - langStrings.keys
            assertTrue(
                "Language $lang has missing keys in strings.xml: $missingStrings",
                missingStrings.isEmpty()
            )

            val missingDict = baseDict.keys - langDict.keys
            assertTrue(
                "Language $lang has missing keys in strings_dict.xml: $missingDict",
                missingDict.isEmpty()
            )

            val missingDesc = baseDesc.keys - langDesc.keys
            assertTrue(
                "Language $lang has missing keys in strings_desc.xml: $missingDesc",
                missingDesc.isEmpty()
            )

            val totalLangCount = langStrings.size + langDict.size + langDesc.size
            assertEquals(
                "Language $lang must have exact total keys match with base ($totalBaseCount)",
                totalBaseCount,
                totalLangCount
            )
        }
    }

    @Test
    fun testAll118ElementNamesPresentInAllLanguages() {
        val elementKeys = listOf(
            "hydrogen", "helium", "lithium", "beryllium", "boron", "carbon", "nitrogen", "oxygen",
            "fluorine", "neon", "sodium", "magnesium", "aluminium", "silicon", "phosphorus", "sulfur",
            "chlorine", "argon", "potassium", "calcium", "scandium", "titanium", "vanadium", "chromium",
            "manganese", "iron", "cobalt", "nickel", "copper", "zinc", "gallium", "germanium",
            "arsenic", "selenium", "bromine", "krypton", "rubidium", "strontium", "yttrium", "zirconium",
            "niobium", "molybdenum", "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium",
            "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "caesium", "barium",
            "lanthanum", "cerium", "praseodymium", "neodymium", "promethium", "samarium", "europium", "gadolinium",
            "terbium", "dysprosium", "holmium", "erbium", "thulium", "ytterbium", "lutetium", "hafnium",
            "tantalum", "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury",
            "thallium", "lead", "bismuth", "polonium", "astatine", "radon", "francium", "radium",
            "actinium", "thorium", "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium",
            "berkelium", "californium", "einsteinium", "fermium", "mendelevium", "nobelium", "lawrencium", "rutherfordium",
            "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium", "darmstadtium", "roentgenium", "copernicium",
            "nihonium", "flerovium", "moscovium", "livermorium", "tennessine", "oganesson"
        )

        assertEquals(118, elementKeys.size)

        for (lang in supportedLanguages) {
            val langDir = File(resDir, "values-$lang")
            val langStrings = extractKeys(File(langDir, "strings.xml"))
            for (el in elementKeys) {
                val key = "element_name_$el"
                assertTrue(
                    "Language $lang must define $key",
                    langStrings.containsKey(key) && langStrings[key]?.isNotBlank() == true
                )
            }
        }
    }
}
