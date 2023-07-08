package com.example.projemanage.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanage.adapters.LabelColorListItemsAdapter
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanage.R


abstract class LabelColorListDialog (
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }


    private fun setUpRecyclerView(view: View) {


       // view.tvTitle.text = title
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = title
        val rvList = view.findViewById<RecyclerView>(R.id.rvList)
       // view.rvList.layoutManager = LinearLayoutManager(context)
        rvList.layoutManager = LinearLayoutManager(context)

        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)

      //  view.rvList.adapter = adapter
        rvList.adapter = adapter


        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {

            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}
