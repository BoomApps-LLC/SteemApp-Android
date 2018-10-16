/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.help

import android.content.Context
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import com.boomapps.steemapp.R
import kotlinx.android.synthetic.main.activity_onboarding.*

/**
 * Created by vgrechikha on 20.03.2018.
 */
class HelpActivity : AppCompatActivity() {

    private lateinit var adapter: HelpTabsAdapter
    private lateinit var pager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        pager = findViewById(R.id.aOnboarding_vpPager)
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var page = 0

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position != page && positionOffset == 0.0f) {
                    page = position
                    Log.d("onPageChangeLoader", "onPageScrolled >> setShowedPage($page)")
                    setButtonsState(page)
                }
            }

            override fun onPageSelected(position: Int) {

            }
        })
        adapter = HelpTabsAdapter(
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                intArrayOf(R.layout.view_onboarding_1, R.layout.view_onboarding_2, R.layout.view_onboarding_3, R.layout.view_onboarding_4, R.layout.view_onboarding_5))
        pager.adapter = adapter
        aOnboarding_btnNext.setOnClickListener({
            if (pager.currentItem < 4) {
                ++pager.currentItem
            } else {
                finish()
            }
        })
        aOnboarding_btnSkip.setOnClickListener({
            finish()
        })
        (aOnboarding_rgDots.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun setButtonsState(page: Int) {
        val resId = if (page == 4) {
            R.string.onboarding_btn_done
        } else {
            R.string.onboarding_btn_next
        }
        setNextButtonText(resId)
        aOnboarding_btnSkip.visibility = if (page == 4) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        (aOnboarding_rgDots.getChildAt(page) as RadioButton).isChecked = true
    }

    private fun setNextButtonText(resId: Int) {
        aOnboarding_btnNext.setText(resId)
    }


}