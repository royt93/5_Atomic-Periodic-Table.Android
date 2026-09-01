package com.mckimquyen.atomicPeriodicTable.widget

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regression guard for FIX-023: ShortCommandWidget.onUpdate() calls
 * RemoteViews.setOnClickPendingIntent(id, ...) with a single hardcoded id. RemoteViews
 * silently no-ops if that id isn't present in whichever layout-<qualifier> variant actually
 * gets inflated on the device's API level — it doesn't throw, so this can't be caught by an
 * on-device instrumented test running on a single API level. Assert directly against the
 * raw layout XML + widget source so a future edit that adds another layout-<qualifier>
 * variant, or renames the click target, can't reintroduce a dead widget on some API level.
 */
class ShortCommandWidgetLayoutTest {

    private val resDir = File("src/main/res")
    private val widgetSourceFile =
        File("src/main/java/com/mckimquyen/atomicPeriodicTable/widget/ShortCommandWidget.kt")

    @Test
    fun everyWidgetLayoutVariant_declaresEveryIdBoundInCode() {
        val boundIds = extractBoundIds()
        assertTrue("expected to find at least one R.id.xxx bound in $widgetSourceFile", boundIds.isNotEmpty())

        val layoutVariants = resDir.listFiles { f -> f.isDirectory && f.name.startsWith("layout") }
            ?.mapNotNull { dir -> File(dir, "view_short_command_widget.xml").takeIf { it.exists() } }
            .orEmpty()

        assertTrue("expected to find at least one view_short_command_widget.xml layout", layoutVariants.isNotEmpty())

        for (layoutFile in layoutVariants) {
            val xml = layoutFile.readText()
            for (boundId in boundIds) {
                assertTrue(
                    "$layoutFile must declare android:id=\"@+id/$boundId\" — ShortCommandWidget.kt " +
                        "binds it via RemoteViews, which silently no-ops (dead widget content) if " +
                        "the id is missing from a layout variant actually inflated on some API level",
                    xml.contains("@+id/$boundId"),
                )
            }
        }
    }

    private fun extractBoundIds(): Set<String> {
        val source = widgetSourceFile.readText()
        val ids = mutableSetOf<String>()
        Regex("""setOnClickPendingIntent\(\s*R\.id\.(\w+)""").findAll(source)
            .forEach { ids += it.groupValues[1] }
        Regex("""setTextViewText\(\s*R\.id\.(\w+)""").findAll(source)
            .forEach { ids += it.groupValues[1] }
        return ids
    }
}
