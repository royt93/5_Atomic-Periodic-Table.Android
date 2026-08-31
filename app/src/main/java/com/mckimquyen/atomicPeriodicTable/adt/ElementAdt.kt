package com.mckimquyen.atomicPeriodicTable.adt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.pref.SearchPref
import java.util.Locale

class ElementAdt(
    var elementList: ArrayList<Element>,
    var clickListener: OnElementClickListener2,
    val con: Context,
) : RecyclerView.Adapter<ElementAdt.ViewHolder>() {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.initialize(item = elementList[position], action = clickListener, con = con)
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
        //        private val cardHolder = itemView.findViewById(R.id.rCard) as FrameLayout
        private val elementCard: FrameLayout = itemView.findViewById(R.id.elemntCard)
        private val textViewElement: TextView = itemView.findViewById(R.id.tvIsoType)
        private val textViewShort: TextView = itemView.findViewById(R.id.tvIcIsoType)
        private val textViewNumb: TextView = itemView.findViewById(R.id.tvIsoNumb)

        fun initialize(
            item: Element,
            action: OnElementClickListener2,
            con: Context,
        ) {
            val searchPreference = SearchPref(con)
            val searchPrefValue = searchPreference.getValue()

            textViewElement.text = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(con, item.element)
            textViewShort.text = item.short

            itemView.foreground =
                ContextCompat.getDrawable(con, R.drawable.shape_toast_card_outline_ripple)
            itemView.isClickable = true
            itemView.isFocusable = true

            if (searchPrefValue == 0) {
                textViewNumb.text = item.number.toString()
            }
            if (searchPrefValue == 1) {
                textViewNumb.text = item.electro.toString()
                textViewShort.setTextColor(Color.argb(255, 18, 18, 18))
                if (item.electro == 0.0) {
                    elementCard.background.setTint(Color.argb(255, 180, 180, 180))
                    textViewNumb.text = "---"
                } else {
                    if (item.electro > 1) {
                        elementCard.background.setTint(Color.argb(255, 255, 225.div(item.electro).toInt(), 0))
                    } else {
                        elementCard.background.setTint(Color.argb(255, 255, 255, 0))
                    }
                }
            }
            if (searchPrefValue == 2) {
                textViewNumb.text = item.number.toString()
            }

            itemView.setOnClickListener {
//                action.elementClickListener2(item, adapterPosition)
                action.elementClickListener2(item, bindingAdapterPosition)
            }
        }
    }

    interface OnElementClickListener2 {
        fun elementClickListener2(item: Element, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: ArrayList<Element>) {
        elementList = filteredList
        notifyDataSetChanged()
    }
}
