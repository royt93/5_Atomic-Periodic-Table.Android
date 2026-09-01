package com.mckimquyen.atomicPeriodicTable.adt

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.table.IonAct
import com.mckimquyen.atomicPeriodicTable.model.Ion
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class IonAdapter(
    var list: ArrayList<Ion>,
    var clickListener: IonAct,
    val context: Context,
) :
    RecyclerView.Adapter<IonAdapter.ViewHolder>() {

    companion object {
        // FIX-017: onBindViewHolder used to open + parse the per-element JSON asset on
        // every bind/recycle instead of caching. Cache by element name once parsed.
        private val ionizationEnergyCache = mutableMapOf<String, String>()
    }

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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_row_ions_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: FrameLayout = itemView.findViewById(R.id.ionCard)
        private val textViewName: TextView = itemView.findViewById(R.id.tvNameD)
        private val textViewShort: TextView = itemView.findViewById(R.id.tvShortD)
        private val textViewCharge: TextView = itemView.findViewById(R.id.tvEnd)
        private val textViewVoltage: TextView = itemView.findViewById(R.id.tvIonization)

        @SuppressLint("SetTextI18n")
        fun initialize(
            item: Ion,
            action: OnIonClickListener,
            context: Context,
        ) {
            textViewVoltage.text = ionizationEnergyCache.getOrPut(item.name) {
                try {
                    val ext = ".json"
                    val element = item.name
                    val elementJson = "$element$ext"

                    val inputStream: InputStream = context.assets.open(elementJson)
                    val jsonString = inputStream.bufferedReader().use { it.readText() }

                    val jsonArray = JSONArray(jsonString)
                    val jsonObject: JSONObject = jsonArray.getJSONObject(0)

                    jsonObject.optString("element_ionization_energy1", "---")
                } catch (e: Exception) {
                    e.printStackTrace()
                    "---"
                }
            }
            textViewName.text = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(context, item.name)
            textViewShort.text = item.short
            textViewCharge.text = context.getString(R.string.ion_view_all, item.count)

            cardView.foreground = ContextCompat.getDrawable(context, R.drawable.shape_toast_card_ripple)
            cardView.isClickable = true
            cardView.isFocusable = true
            cardView.setOnClickListener {
                action.ionClickListener(item, bindingAdapterPosition)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: ArrayList<Ion>) {
        list = filteredList
        notifyDataSetChanged()

    }

    interface OnIonClickListener {
        fun ionClickListener(item: Ion, position: Int)
    }
}
