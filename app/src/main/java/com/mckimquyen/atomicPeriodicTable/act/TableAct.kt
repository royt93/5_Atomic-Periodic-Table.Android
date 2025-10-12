package com.mckimquyen.atomicPeriodicTable.act

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.table.ElectrodeAct
import com.mckimquyen.atomicPeriodicTable.act.table.EquationsAct
import com.mckimquyen.atomicPeriodicTable.act.table.IonAct
import com.mckimquyen.atomicPeriodicTable.act.table.NuclideAct
import com.mckimquyen.atomicPeriodicTable.act.table.PHAct
import com.mckimquyen.atomicPeriodicTable.databinding.ATablesBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class TableAct : BaseAct() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: ATablesBinding

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
        binding = ATablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewSub.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        //Title Controller
        binding.commonTitleTableColor.visibility = View.INVISIBLE
        binding.tablesTitle.visibility = View.INVISIBLE
        binding.commonTitleBackTab.elevation = (resources.getDimension(R.dimen.zero_elevation))
        binding.tableScroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (binding.tableScroll.scrollY > 150) {
                        binding.commonTitleTableColor.visibility = View.VISIBLE
                        binding.tablesTitle.visibility = View.VISIBLE
                        binding.tablesTitleDownstate.visibility = View.INVISIBLE
                        binding.commonTitleBackTab.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        binding.commonTitleTableColor.visibility = View.INVISIBLE
                        binding.tablesTitle.visibility = View.INVISIBLE
                        binding.tablesTitleDownstate.visibility = View.VISIBLE
                        binding.commonTitleBackTab.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = binding.tableScroll.scrollY.toFloat()
                }
            })

        tableListeners()

        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val params = binding.commonTitleBackTab.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackTab.layoutParams = params

        val params2 = binding.tablesTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        binding.tablesTitleDownstate.layoutParams = params2

    }

    private fun tableListeners() {
        binding.solTable.setOnClickListener {
            val intent = Intent(this, SolubilityAct::class.java)
            startActivity(intent)
        }
        binding.solButton.setOnClickListener {
            val intent = Intent(this, SolubilityAct::class.java)
            startActivity(intent)
        }
        binding.eleTable.setOnClickListener {
            val intent = Intent(this, ElectrodeAct::class.java)
            startActivity(intent)
        }
        binding.eleButton.setOnClickListener {
            val intent = Intent(this, ElectrodeAct::class.java)
            startActivity(intent)
        }
        binding.equTable.setOnClickListener {
            val intent = Intent(this, EquationsAct::class.java)
            startActivity(intent)
        }
        binding.equButton.setOnClickListener {
            val intent = Intent(this, EquationsAct::class.java)
            startActivity(intent)
        }
        binding.ionTable.setOnClickListener {
            val intent = Intent(this, IonAct::class.java)
            startActivity(intent)
        }
        binding.ionButton.setOnClickListener {
            val intent = Intent(this, IonAct::class.java)
            startActivity(intent)
        }
        binding.nucTable.setOnClickListener {
            val intent = Intent(this, NuclideAct::class.java)
            startActivity(intent)
        }
        binding.nucButton.setOnClickListener {
            val intent = Intent(this, NuclideAct::class.java)
            startActivity(intent)
        }
        binding.phTable.setOnClickListener {
            val intent = Intent(this, PHAct::class.java)
            startActivity(intent)
        }
        binding.phButton.setOnClickListener {
            val intent = Intent(this, PHAct::class.java)
            startActivity(intent)
        }
    }

}
