package com.boomapps.steemapp.main


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ViewState
import com.boomapps.steemapp.repository.Balance
import kotlinx.android.synthetic.main.fragment_wallet.*

class WalletFragment : Fragment() {

    private lateinit var viewModelShared: MainViewModel
    private var balanceValue: Array<String> = arrayOf("0", "00")
    private var balanceSymbol = "$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelShared = ViewModelProviders.of(activity as AppCompatActivity).get(MainViewModel::class.java)
        viewModelShared.state.observe(this, object : Observer<ViewState> {
            override fun onChanged(t: ViewState?) {
                when (t) {
                    ViewState.SUCCESS_RESULT -> {
                        viewModelShared.state.value = ViewState.COMMON
                        viewModelShared.stringError = ""
                        viewModelShared.stringSuccess = ""
                    }
                    ViewState.FAULT_RESULT -> {
                        viewModelShared.state.value = ViewState.COMMON
                        Toast.makeText(activity, "Error: ${viewModelShared.stringError}", Toast.LENGTH_LONG)
                        viewModelShared.stringError = ""
                    }
                }
            }
        })
        viewModelShared.getBalance().observe(this, object : Observer<Balance> {
            override fun onChanged(data: Balance?) {
                if (data != null) {
                    val asString: String = data.fullBalance.toString()
                    var intVal = asString.substringBefore(".")
                    if (intVal.toInt() < 0) {
                        intVal = "0"
                    }
                    val after = asString.substringAfter(".")
                    balanceValue = arrayOf(
                            intVal,
                            if (after.isEmpty()) {
                                "0"
                            } else {
                                after.subSequence(0, Math.min(2, after.length))
                            }.toString()
                    )

                    myBalanceValue.text = getString(R.string.balance_template, balanceSymbol, balanceValue[0], balanceValue[1])
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnWalletFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onWalletFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(): WalletFragment {
            val fragment = WalletFragment()
            return fragment
        }
    }
}// Required empty public constructor
