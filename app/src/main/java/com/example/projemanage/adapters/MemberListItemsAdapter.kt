package com.example.projemanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanage.R
import com.example.projemanage.models.User
import com.example.projemanage.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]


        if (holder is MyViewHolder) {
            val iv_member_image = holder.itemView.findViewById<CircleImageView>(R.id.iv_member_image)
            val tv_member_name = holder.itemView.findViewById<TextView>(R.id.tv_member_name)
            val tv_member_email = holder.itemView.findViewById<TextView>(R.id.tv_member_email)
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(iv_member_image)

            tv_member_name.text = model.name
            tv_member_email.text = model.email

            val iv_selected_member = holder.itemView.findViewById<ImageView>(R.id.iv_selected_member)

            if (model.selected) {
                iv_selected_member.visibility = View.VISIBLE
            } else {
                iv_selected_member.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
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
        fun onClick(position: Int, user: User, action: String)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}