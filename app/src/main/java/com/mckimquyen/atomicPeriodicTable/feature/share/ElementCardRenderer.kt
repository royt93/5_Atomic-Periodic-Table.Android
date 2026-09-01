package com.mckimquyen.atomicPeriodicTable.feature.share

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.drawToBitmap
import com.mckimquyen.atomicPeriodicTable.databinding.ViewElementShareCardBinding

/**
 * Renders a small "share card" layout (view_element_share_card.xml) to a Bitmap without ever
 * attaching it to the visible UI — measure+layout it manually at a fixed size, then
 * View.drawToBitmap() (androidx-core-ktx, already a dependency; no custom Canvas code needed).
 */
object ElementCardRenderer {
    private const val CARD_SIZE_DP = 360

    fun render(
        context: Context,
        symbol: String,
        name: String,
        number: Int,
        massText: String,
        categoryText: String,
    ): Bitmap {
        val binding = ViewElementShareCardBinding.inflate(LayoutInflater.from(context))
        binding.tvShareSymbol.text = symbol
        binding.tvShareName.text = name
        binding.tvShareNumber.text = "#$number"
        binding.tvShareMass.text = massText
        binding.tvShareCategory.text = categoryText

        val sizePx = (CARD_SIZE_DP * context.resources.displayMetrics.density).toInt()
        val spec = View.MeasureSpec.makeMeasureSpec(sizePx, View.MeasureSpec.EXACTLY)
        binding.root.measure(spec, spec)
        binding.root.layout(0, 0, sizePx, sizePx)
        return binding.root.drawToBitmap()
    }
}
