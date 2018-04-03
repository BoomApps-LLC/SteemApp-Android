package com.boomapps.steemapp.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import com.boomapps.steemapp.BaseActivity
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ViewState
import com.boomapps.steemapp.editor.EditorActivity
import com.boomapps.steemapp.main.MainViewModel.Companion.TAB_EDIT
import com.boomapps.steemapp.main.MainViewModel.Companion.TAB_PROFILE
import com.boomapps.steemapp.main.MainViewModel.Companion.TAB_WALLET
import com.boomapps.steemapp.signin.SignInActivity
import com.boomapps.steemapp.votedialog.VoteDialog
import kotlinx.android.synthetic.main.activity_main_bn.*

class MainActivity : BaseActivity() {

    private val FRAGMENT_WALLET_TAG = "wallet"
    private val FRAGMENT_EDIT_TAG = "edit"
    private val FRAGMENT_PROFILE_TAG = "profile"

    companion object {
        val EXTRA_UPDATE_DATA = "update_data"
        val TAG = MainActivity::class.java.simpleName
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_wallet -> {
                Log.d(TAG, "mOnNavigationItemSelectedListener(R.id.navigation_home) >> showFragment(FRAGMENT_WALLET_TAG)")
                val animArray = getAnim(viewModel.currentTab, TAB_WALLET)
                viewModel.currentTab = TAB_WALLET
                showFragment(FRAGMENT_WALLET_TAG, animArray)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_edit -> {
                Log.d(TAG, "mOnNavigationItemSelectedListener(R.id.navigation_dashboard) >> showFragment(FRAGMENT_EDIT_TAG)")
                val intent = Intent(this, EditorActivity::class.java)
                startActivity(intent)
                viewModel.currentTab = TAB_WALLET
                return@OnNavigationItemSelectedListener false
            }
            R.id.navigation_profile -> {
                Log.d(TAG, "mOnNavigationItemSelectedListener(R.id.navigation_notifications) >> showFragment(FRAGMENT_PROFILE_TAG)")
                val animArray = getAnim(viewModel.currentTab, TAB_PROFILE)
                viewModel.currentTab = TAB_PROFILE
                showFragment(FRAGMENT_PROFILE_TAG, animArray)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    lateinit var viewModel: MainViewModel

    var updateData: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_bn)
        updateData = intent.getBooleanExtra(EXTRA_UPDATE_DATA, false)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.state.observe(this, object : Observer<ViewState> {
            override fun onChanged(t: ViewState?) {
                when (t) {
                    ViewState.COMMON -> {
                        dismissProgress()
                    }
                    ViewState.PROGRESS -> {
                        showProgress("Processing ...")
                    }
                }
            }
        })
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        when (viewModel.currentTab) {
            TAB_WALLET -> {
                navigation.selectedItemId = R.id.navigation_wallet
            }
            TAB_EDIT -> {
                navigation.selectedItemId = R.id.navigation_edit
            }
            TAB_PROFILE -> {
                navigation.selectedItemId = R.id.navigation_profile
            }
        }

    }

    private fun getAnim(from: Int, to: Int): Array<Int> {
        if (from == to) {
            return arrayOf()
        } else {
            if (from < to) {
                return arrayOf(R.animator.right_in, R.animator.left_out)
            } else {
                return arrayOf(R.animator.left_in, R.animator.right_out)
            }
        }
    }


    private fun showFragment(tagCurrent: String, animArray: Array<Int>) {
        Log.d(TAG, "showFragment(${tagCurrent})")
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag(tagCurrent)
        if (fragment == null) {
            Log.d(TAG, "fragment(${tagCurrent} is null)")
            fragment = getFragmentNewInstance(tagCurrent)
        }
        if (fragment.isAdded) {
            Log.d(TAG, "fragment(${tagCurrent} is added >> return)")
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        if (mainActivityFragmentsContainer.childCount == 0) {
            Log.d(TAG, "mainActivityFragmentsContainer doesn't have any child >> add fragment(${tagCurrent})")
            transaction.add(R.id.mainActivityFragmentsContainer, fragment, tagCurrent)
        } else {
            Log.d(TAG, "mainActivityFragmentsContainer has child >> replace with fragment(${tagCurrent})")
            if (animArray.size == 2) {
                transaction.setCustomAnimations(animArray[0], animArray[1])
            }
            transaction.replace(R.id.mainActivityFragmentsContainer, fragment, tagCurrent)
        }
        transaction.commit()
    }

    private fun getFragmentNewInstance(tag: String): Fragment {
        Log.d(TAG, "getFragmentNewInstance(${tag})")
        return when (tag) {
            FRAGMENT_WALLET_TAG -> WalletFragment.newInstance()
            FRAGMENT_PROFILE_TAG -> ProfileFragment.newInstance()
            else -> WalletFragment.newInstance()
        }
    }

    fun goToSignInScreen() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TOP;
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (updateData) {
            updateData = false
            viewModel.updateData()
        }
        Handler().postDelayed({ showVoteDialog() }, 1000)
    }

    private fun showVoteDialog() {
        if (!viewModel.shouldShowVoteDialog()) {
            return
        }
        val voteDialog: android.app.DialogFragment = VoteDialog.newInstance(object : VoteDialog.OnVoteInteractListener {

            override fun onCloseClick() {
                viewModel.updateVotingState(true)
            }

            override fun onVoteClick() {
                // TODO increment voting counter
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://steemconnect.com/sign/account-witness-vote?witness=yuriks2000&approve=1"))
                startActivity(intent)
                viewModel.updateVotingState(false)
            }
        })
        voteDialog.show(fragmentManager, VoteDialog.TAG)
    }
}
