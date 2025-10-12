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
import com.mckimquyen.atomicPeriodicTable.model.Element
import java.util.Locale

class IsotopeAdt(
    var elementList: ArrayList<Element>,
    var clickListener: OnElementClickListener,
    val context: Context,
) : RecyclerView.Adapter<IsotopeAdt.ViewHolder>() {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.initialize(item = elementList[position], action = clickListener, context = context)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.view_isotope_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return elementList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewElement: TextView = itemView.findViewById(R.id.tvIsoType)
        private val textViewShort: TextView = itemView.findViewById(R.id.tvIcIsoType)
        private val textViewNumb: TextView = itemView.findViewById(R.id.tvIsoNumb)

        fun initialize(
            item: Element,
            action: OnElementClickListener,
            context: Context,
        ) {
            // Fixed: Remove redundant first assignment - only use the capitalized version
            textViewElement.text = item.element.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            textViewShort.text = item.short
            textViewNumb.text = item.number.toString()

            itemView.foreground = ContextCompat.getDrawable(context, R.drawable.shape_toast_card_ripple)
            itemView.isClickable = true
            itemView.isFocusable = true

            itemView.setOnClickListener {
                action.elementClickListener(item, bindingAdapterPosition)
            }
        }
    }

    interface OnElementClickListener {
        fun elementClickListener(item: Element, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: ArrayList<Element>) {
        elementList = filteredList
        notifyDataSetChanged()
    }
}
