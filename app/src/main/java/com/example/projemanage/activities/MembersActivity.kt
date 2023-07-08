package com.example.projemanage.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanage.R
import com.example.projemanage.adapters.MemberListItemsAdapter
import com.example.projemanage.firebase.FirestoreClass
import com.example.projemanage.models.Board
import com.example.projemanage.models.User
import com.example.projemanage.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board

    // A global variable for Assigned Members List.
    private lateinit var mAssignedMembersList: ArrayList<User>

    // A global variable for notifying any changes done or not in the assigned members list.
    private var anyChangesDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

        // Show the progress dialog.
        showProgressDialog()
        FirestoreClass().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )
    }

    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {


        val toolbar_members_activity = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbar_members_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_add_member -> {

                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A function to setup assigned members list into recyclerview.
     */
    fun setupMembersList(list: ArrayList<User>) {

        mAssignedMembersList = list

        hideProgressDialog()

        val rv_members_list  = findViewById<RecyclerView>(R.id.rv_members_list)
        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this@MembersActivity, list)
        rv_members_list.adapter = adapter
    }

    /**
     * Method is used to show the Custom Dialog.
     */
    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        /*Set the screen content from a layout resource.
    The resource will be inflated, adding all top-level views to the screen.*/
        dialog.setContentView(R.layout.dialog_search_member)

        val tv_add = dialog.findViewById<TextView>(R.id.tv_add)
        tv_add.setOnClickListener(View.OnClickListener {

            val et_email_search_member = dialog.findViewById<EditText>(R.id.et_email_search_member)
            val email = et_email_search_member.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()

                // Show the progress dialog.
                showProgressDialog()
                FirestoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        })
        val tv_cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        tv_cancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        //Start the dialog and display it on screen.
        dialog.show()
    }

    fun memberDetails(user: User) {

        mBoardDetails.assignedTo.add(user.id)

        FirestoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
    }


    fun memberAssignSuccess(user: User) {

        hideProgressDialog()

        mAssignedMembersList.add(user)

        anyChangesDone = true

        setupMembersList(mAssignedMembersList)


        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()

    }


    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {


        override fun onPreExecute() {
            super.onPreExecute()

            // Show the progress dialog.
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {
            var result: String

            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL) // Base Url
                connection = url.openConnection() as HttpURLConnection


                connection.doOutput = true
                connection.doInput = true


                connection.instanceFollowRedirects = false


                connection.requestMethod = "POST"


                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")


                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false


                val wr = DataOutputStream(connection.outputStream)


                val jsonRequest = JSONObject()

                // Create a data object
                val dataObject = JSONObject()
                // Here you can pass the title as per requirement as here we have added some text and board name.
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")
                // Here you can pass the message as per requirement as here we have added some text and appended the name of the Board Admin.
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                )


                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)


                wr.writeBytes(jsonRequest.toString())
                wr.flush() // Flushes this data output stream.
                wr.close() // Closes this output stream and releases any system resources associated with the stream

                val httpResult: Int =
                    connection.responseCode // Gets the status code from an HTTP response message.

                if (httpResult == HttpURLConnection.HTTP_OK) {


                    val inputStream = connection.inputStream


                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {

                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {

                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {

                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            // You can notify with your result to onPostExecute.
            return result
        }


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            hideProgressDialog()

            // JSON result is printed in the log.
            Log.e("JSON Response Result", result)
        }
    }
    // END

}