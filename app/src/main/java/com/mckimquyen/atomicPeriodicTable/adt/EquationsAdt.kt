package com.mckimquyen.atomicPeriodicTable.adt

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.table.EquationsAct
import com.mckimquyen.atomicPeriodicTable.model.Equation
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class EquationsAdt(
    var list: ArrayList<Equation>,
    var clickListener: EquationsAct,
    val context: Context,
) : RecyclerView.Adapter<EquationsAdt.ViewHolder>() {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.initialize(item = list[position], action = clickListener, context = context)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_row_equations_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val equTitle: TextView = itemView.findViewById(R.id.tvEqu)
        private val equCategory: TextView = itemView.findViewById(R.id.tvIcEqu)
        private val equImg: ImageView = itemView.findViewById(R.id.ivIcEqView)

        fun initialize(item: Equation, action: OnEquationClickListener, context: Context) {
            equTitle.text = item.equationTitle

            // Optimized: Replace multiple if statements with when expression
            equCategory.text = when (item.category) {
                "Mechanics" -> "Me"
                "General" -> "Ge"
                "Theory of Relativity" -> "TR"
                "Thermodynamics" -> "Th"
                "Wavelengths" -> "Wv"
                "Electricity" -> "El"
                "Magnetism and Induction" -> "MI"
                "Atomic Physics" -> "AP"
                "Nuclear Physics" -> "NP"
                else -> ""
            }

            equImg.setImageResource(item.equation)
            val themePref = ThemePref(context)
            val themePrefValue = themePref.getValue()
            if (themePrefValue == 1) {
                equImg.colorFilter = ColorMatrixColorFilter(NEGATIVE)
            }
            if (themePrefValue == 100) {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        equImg.colorFilter = ColorMatrixColorFilter(NEGATIVE)
                    }
                }
            }
            itemView.foreground = ContextCompat.getDrawable(context, R.drawable.shape_toast_card_ripple)
            itemView.isClickable = true
            itemView.isFocusable = true
            itemView.setOnClickListener {
//                action.equationClickListener(item, adapterPosition)
                action.equationClickListener(item, bindingAdapterPosition)
            }
        }

        companion object {
            // Moved constant to companion object for proper scope and reusability
            private val NEGATIVE = floatArrayOf(
                -1.0f, 0f, 0f, 0f, 255f,
                0f, -1.0f, 0f, 0f, 255f,
                0f, 0f, -1.0f, 0f, 255f,
                0f, 0f, 0f, 1.0f, 0f
            )
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: ArrayList<Equation>) {
        list = filteredList
        notifyDataSetChanged()
    }

    interface OnEquationClickListener {
        fun equationClickListener(item: Equation, position: Int)
    }

}
