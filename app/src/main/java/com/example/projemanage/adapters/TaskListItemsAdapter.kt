package com.example.projemanage.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.projemanage.R
import com.example.projemanage.activities.TaskListActivity
import com.example.projemanage.models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // A global variable for position dragged FROM.
    private var mPositionDraggedFrom = -1
    // A global variable for position dragged TO.
    private var mPositionDraggedTo = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        // Here the layout params are converted dynamically according to the screen size as width is 70% and height is wrap_content.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position:Int) {
        val model = list[position]
        val tv_add_task_list = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
        val ll_task_item = holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item)
        val tv_task_list_title = holder.itemView.findViewById<TextView>(R.id.tv_task_list_title)
        val cv_add_task_list_name = holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name)
        val ib_close_list_name = holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name)
        val ib_done_list_name = holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name)
        val ib_done_edit_list_name = holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
        val ib_delete_list = holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list)


        if (holder is MyViewHolder) {

            if (position == list.size - 1) {
                tv_add_task_list.visibility = View.VISIBLE
                ll_task_item.visibility = View.GONE
            } else {
                tv_add_task_list.visibility = View.GONE
                ll_task_item.visibility = View.VISIBLE
            }

            tv_task_list_title.text = model.title

            tv_add_task_list.setOnClickListener {

                tv_add_task_list.visibility = View.GONE
                cv_add_task_list_name.visibility = View.VISIBLE
            }

           ib_close_list_name.setOnClickListener {
                tv_add_task_list.visibility = View.VISIBLE
                cv_add_task_list_name.visibility = View.GONE
            }
            val et_task_list_name = holder.itemView.findViewById<EditText>(R.id.et_task_list_name)
            ib_done_list_name.setOnClickListener {
                val listName = et_task_list_name.text.toString()

                if (listName.isNotEmpty()) {
                    // Here we check the context is an instance of the TaskListActivity.
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            val ib_edit_list_name = holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name)
            val et_edit_task_list_name = holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name)
            val ll_title_view = holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view)
            val cv_edit_task_list_name = holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name)

            ib_edit_list_name.setOnClickListener {

                et_edit_task_list_name.setText(model.title) // Set the existing title
                ll_title_view.visibility = View.GONE
                cv_edit_task_list_name.visibility = View.VISIBLE
            }
            val ib_close_editable_view = holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)

            ib_close_editable_view.setOnClickListener {
                ll_title_view.visibility = View.VISIBLE
                cv_edit_task_list_name.visibility = View.GONE
            }


          ib_done_edit_list_name.setOnClickListener {
                val listName = et_edit_task_list_name.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            ib_delete_list.setOnClickListener {

                alertDialogForDeleteList(position, model.title)
            }

            val tv_add_card = holder.itemView.findViewById<TextView>(R.id.tv_add_card)
            val cv_add_card = holder.itemView.findViewById<CardView>(R.id.cv_add_card)
            val ib_close_card_name = holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name)

            tv_add_card.setOnClickListener {

                tv_add_card.visibility = View.GONE
                cv_add_card.visibility = View.VISIBLE

                ib_close_card_name.setOnClickListener {
                    tv_add_card.visibility = View.VISIBLE
                    cv_add_card.visibility = View.GONE
                }

                val ib_done_card_name = holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name)
                val et_card_name = holder.itemView.findViewById<EditText>(R.id.et_card_name)
                ib_done_card_name.setOnClickListener {

                    val cardName = et_card_name.text.toString()

                    if (cardName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.addCardToTaskList(position, cardName)
                        }
                    } else {
                        Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            val rv_card_list = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)
            rv_card_list.layoutManager = LinearLayoutManager(context)
            rv_card_list.setHasFixedSize(true)

            val adapter =
                CardListItemsAdapter(context, model.cards)
                rv_card_list.adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })


            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                rv_card_list.addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                /*Called when ItemTouchHelper wants to move the dragged item from its old position to
                 the new position.*/
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition


                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    // move item in `draggedPosition` to `targetPosition` in adapter.
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false // true if moved, false otherwise
                }

                // Called when a ViewHolder is swiped by the user.
                override fun onSwiped(
                    viewHolder: ViewHolder,
                    direction: Int
                ) { // remove from adapter
                }

                /*Called by the ItemTouchHelper when the user interaction with an element is over and it
                 also completed its animation.*/
                override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                        (context as TaskListActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )
                    }

                    // Reset the global variables
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })

            /*Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
            attached to a RecyclerView, it will first detach from the previous one.*/
            helper.attachToRecyclerView(rv_card_list)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()


    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()


    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}