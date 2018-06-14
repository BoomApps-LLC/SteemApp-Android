package com.boomapps.steemapp.ui.dialogs

import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import com.boomapps.steemapp.R

import java.util.ArrayList

class ListActionsDialog : DialogFragment(), View.OnClickListener {

    private var title: String? = null
    private var values: ArrayList<String>? = null
    private var listener: OnValuesSelectedListener? = null

    interface OnValuesSelectedListener {
        fun onSelect(position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments

        //read arguments
        if (arg != null) {
            values = arg.getStringArrayList(BUNDLE_ARG_VALUES_LIST)
            title = arg.getString(BUNDLE_ARG_TITLE, "")
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //setup dialog view
        val view = inflater.inflate(R.layout.dialog_list_actions, container, false)
        //setup title
        if (title == null || title?.length == 0) {
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        } else {
//            val tv = dialog.findViewById<TextView>(android.R.id.title)
//            tv.setSingleLine(false)
            dialog.setTitle(title)
        }


        //setup list
        val valuesList = view.findViewById(R.id.dialogListAction_valuesList) as RecyclerView
        valuesList.layoutManager = LinearLayoutManager(activity)
        valuesList.adapter = ValuesAdapter()

        return view
    }

    fun setOnValuesSelectedListener(listener: OnValuesSelectedListener) {
        this.listener = listener
    }

    override fun onClick(view: View) {
        val id = view.id
        when (id) {
            R.id.value_root -> {
                val position = view.tag as Int
                listener!!.onSelect(position)
                dialog.dismiss()
            }
        }

    }

    private inner class ValuesAdapter : RecyclerView.Adapter<ValueViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValueViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_list_action_dialog, parent, false)
            return ValueViewHolder(view)
        }

        override fun onBindViewHolder(holder: ValueViewHolder, position: Int) {
            //setup more list item
            holder.value.text = values!![position]
            holder.root.tag = position
            holder.root.isClickable = true
            holder.root.setOnClickListener(this@ListActionsDialog)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return values!!.size
        }
    }

    private inner class ValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var value: TextView
        internal var root: RelativeLayout

        init {
            value = itemView.findViewById(R.id.value_title) as TextView
            root = itemView.findViewById(R.id.value_root) as RelativeLayout
        }
    }

    companion object {

        val TAG = ListActionsDialog::class.java.simpleName
        val BUNDLE_ARG_TITLE = "title"
        val BUNDLE_ARG_VALUES_LIST = "values_list"

        fun newInstance(title: String, values: ArrayList<String>): ListActionsDialog {
            val dialog = ListActionsDialog()

            //set arguments
            val args = Bundle()
            args.putString(BUNDLE_ARG_TITLE, title)
            args.putStringArrayList(BUNDLE_ARG_VALUES_LIST, values)
            dialog.arguments = args

            return dialog
        }

        fun newInstance(values: ArrayList<String>): ListActionsDialog {
            val dialog = ListActionsDialog()

            //set arguments
            val args = Bundle()
            args.putStringArrayList(BUNDLE_ARG_VALUES_LIST, values)
            dialog.arguments = args

            return dialog
        }
    }
}
