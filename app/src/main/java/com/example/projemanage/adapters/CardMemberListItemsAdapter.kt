package com.example.projemanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanage.R
import com.example.projemanage.models.SelectedMembers

open class CardMemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card_selected_member,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            val iv_add_member = holder.itemView.findViewById<ImageView>(R.id.iv_add_member)
            val iv_selected_member_image = holder.itemView.findViewById<ImageView>(R.id.iv_selected_member_image)

            if (position == list.size - 1 && assignMembers) {
                iv_add_member.visibility = View.VISIBLE
                iv_selected_member_image.visibility = View.GONE
            } else {
                iv_add_member.visibility = View.GONE
                iv_selected_member_image.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_selected_member_image)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }


    interface OnClickListener {
        fun onClick()
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}