package com.boomapps.steemapp.editor.tabs

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.controls.SquareLinearLayout
import com.boomapps.steemapp.editor.EditorViewModel

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
        reward_100.setOnClickListener({setRewardsControlState(0)})
        reward_50 = rewardButtonsContainer.findViewById(R.id.rewardItem3)
        reward_50.setOnClickListener({setRewardsControlState(1)})
        reward_0 = rewardButtonsContainer.findViewById(R.id.rewardItem2)
        reward_0.setOnClickListener({setRewardsControlState(2)})

        postButton = view.findViewById(R.id.postButton)
        postButton.setOnClickListener({
            viewModel.publishStory()
        })

        voteSwitcher = view.findViewById(R.id.upvoteSwitcher)
        voteSwitcher.isChecked = viewModel.upvoteState
        voteSwitcher.setOnCheckedChangeListener({ _, isChecked ->
            viewModel.upvoteState = isChecked
        })
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
}