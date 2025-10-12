package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.OrderAdt
import com.mckimquyen.atomicPeriodicTable.databinding.AOrderSettingsPageBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class OrderAct : BaseAct() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: AOrderSettingsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    setTheme(R.style.AppTheme)
                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    setTheme(R.style.AppThemeDark)
                }
            }
        }
        if (themePrefValue == 0) {
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            setTheme(R.style.AppThemeDark)
        }

        // Khởi tạo ViewBinding
        binding = AOrderSettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root) //REMEMBER: Never move any function calls above this

        val dataSet = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        val mAdapter = OrderAdt(dataSet)
        val mList = binding.ordRecycler
        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = mAdapter
        mList.dragListener = onItemDragListener

        binding.viewOrd.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        binding.backBtnOrd.setOnClickListener {
            this.onBackPressed()
        }
    }

    private val onItemDragListener = object : OnItemDragListener<String> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
            // Handle action of item being dragged from one position to another
        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {

        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        binding.ordRecycler.setPadding(
            /* left = */ 0,
            /* top = */
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            /* right = */
            0,
            /* bottom = */
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        val params2 = binding.commonTitleBackOrd.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackOrd.layoutParams = params2
    }

}
