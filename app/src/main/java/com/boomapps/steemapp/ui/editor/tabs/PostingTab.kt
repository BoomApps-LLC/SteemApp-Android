/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.editor.tabs

import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.controls.SquareLinearLayout
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import com.boomapps.steemapp.ui.editor.EditorViewModel

/**
 * Created by vgrechikha on 22.02.2018.
 */
class PostingTab(view: View, tabListener: TabListener, viewModel: EditorViewModel) : BaseTabView(view, tabListener, viewModel) {

    lateinit var title: TextView
    lateinit var rewardConstraintLayout: ConstraintLayout
    lateinit var rewardButtonsContainer: LinearLayout
    lateinit var postButton: TextView
    lateinit var voteSwitcher: SwitchCompat
    lateinit var reward_0: SquareLinearLayout
    lateinit var reward_50: SquareLinearLayout
    lateinit var reward_100: SquareLinearLayout

    override fun initComponents() {
        title = view.findViewById(R.id.rewardTitle)
        rewardConstraintLayout = view.findViewById(R.id.rewardConstraintLayout)
//        val lp = rewardConstraintLayout.layoutParams as FrameLayout.LayoutParams
//        lp.height = view.height
//        rewardConstraintLayout.layoutParams = lp
        rewardButtonsContainer = view.findViewById<LinearLayout>(R.id.rewardButtonsContainer)
        setRewardsControlState(viewModel.rewardPosition)
        reward_100 = rewardButtonsContainer.findViewById(R.id.rewardItem1)
        reward_100.setOnClickListener({ setRewardsControlState(0) })
        reward_50 = rewardButtonsContainer.findViewById(R.id.rewardItem3)
        reward_50.setOnClickListener({ setRewardsControlState(1) })
        reward_0 = rewardButtonsContainer.findViewById(R.id.rewardItem2)
        reward_0.setOnClickListener({ setRewardsControlState(2) })

        postButton = view.findViewById(R.id.postButton)
        postButton.setOnClickListener({
            if (viewModel.postingDelay <= 0) {
                viewModel.publishStory(null)
            } else {
                showPostingDelayDialog()
            }
        })


        voteSwitcher = view.findViewById(R.id.upvoteSwitcher)
        voteSwitcher.isChecked = viewModel.upvoteState
        voteSwitcher.setOnCheckedChangeListener({ _, isChecked ->
            viewModel.upvoteState = isChecked
        })
    }

    override fun onShow() {
        Log.d("PostingTab", "onShow")
        if (viewModel.getDelay() > 0) {
//            postButton.postDelayed({
                startTimer(viewModel.postingDelay)
//            }, 500)
        }
    }

    override fun onHide() {
        Log.d("PostingTab", "onHide")
        if (timer != null) {
            stopTimer()
        }
    }

    private fun setRewardsControlState(selectedPosition: Int) {
        viewModel.rewardPosition = selectedPosition
        if (rewardButtonsContainer.childCount == 3) {
            for (pos in 0..2) {
                val rewardControl = rewardButtonsContainer.getChildAt(pos) as LinearLayout
                if (pos == selectedPosition) {
                    setRewardActive(rewardControl)
                } else {
                    setRewardInactive(rewardControl)
                }
            }
        }
    }

    private fun setRewardActive(layout: LinearLayout) {
        layout.setBackgroundResource(R.drawable.reward_drawable_active)
        for (pos in 0..layout.childCount) {
            if (layout.getChildAt(pos) is TextView) {
                (layout.getChildAt(pos) as TextView).setTextColor(ContextCompat.getColor(view.context, R.color.white))
            }
        }
    }

    private fun setRewardInactive(layout: LinearLayout) {
        layout.setBackgroundResource(R.drawable.reward_selector_inactive)
        for (pos in 0..layout.childCount) {
            if (layout.getChildAt(pos) is TextView) {
                (layout.getChildAt(pos) as TextView).setTextColor(ContextCompat.getColor(view.context, R.color.selector_violet_white))
            }
        }
    }


    private fun showPostingDelayDialog() {
        WarningDialog.getInstance().showSpecial(
                postButton.context,
                title = null,
                message = "User can publish only one story in 5 minutes",
                positive = "OK",
                negative = null,
                listener = null)
    }


    private fun startTimer(startDelay: Long) {
        Log.d("PostingTab", "startTimer >> $startDelay")
        timer = object : CountDownTimer(startDelay, 1000L) {

            override fun onFinish() {
                setPostButtonReady()
                viewModel.postingDelay = -1
                timer = null
            }

            override fun onTick(millisUntilFinished: Long) {
                val secs = (millisUntilFinished / 1000).toInt()
                val mins: Int = secs / 60
                val restSecs = secs - mins * 60
                setPostButtonUnready(
                        postButton.context.getString(R.string.btn_posting_timer,
                                mins,
                                if (restSecs < 10) {
                                    "0$restSecs"
                                } else {
                                    "$restSecs"
                                }))
            }
        }
        timer?.start()
    }

    private fun stopTimer() {
        Log.d("PostingTab", "stopTimer")
        timer?.cancel()
        timer = null
    }

    private fun setPostButtonReady() {
        postButton.setText(R.string.btn_posting_default)
    }

    private fun setPostButtonUnready(timerValue: String) {
        postButton.setText(timerValue)
    }

    var timer: CountDownTimer? = null
}