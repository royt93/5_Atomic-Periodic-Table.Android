package com.mckimquyen.atomicPeriodicTable.adt

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.table.ElectrodeAct
import com.mckimquyen.atomicPeriodicTable.model.Series
import java.util.Locale

class ElectrodeAdt(
    var list: ArrayList<Series>,
    var clickListener: ElectrodeAct,
    val context: Context,
) : RecyclerView.Adapter<ElectrodeAdt.ViewHolder>() {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.initialize(list[position], context)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_electrode_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(R.id.tvName)
        private val textViewShort: TextView = itemView.findViewById(R.id.tvShort)
        private val textViewCharge: TextView = itemView.findViewById(R.id.tvCharge)
        private val textViewVoltage: TextView = itemView.findViewById(R.id.tvVoltage)

        fun initialize(
            item: Series,
            context: Context,
        ) {
            textViewName.text = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(context, item.name)
            textViewShort.text = item.short
            // Optimized: Use string template instead of concatenation
            textViewVoltage.text = "${item.voltage} (Volt)"
            textViewCharge.text = item.charge

            itemView.foreground = ContextCompat.getDrawable(context, R.drawable.shape_toast_card_ripple)
            itemView.isClickable = true
            itemView.isFocusable = true

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: ArrayList<Series>) {
        list = filteredList
        notifyDataSetChanged()
    }

}
