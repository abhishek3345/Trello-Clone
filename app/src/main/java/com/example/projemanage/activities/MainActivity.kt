package com.example.projemanage.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanage.R
import com.example.projemanage.adapters.BoardItemsAdapter
import com.example.projemanage.firebase.FirestoreClass
import com.example.projemanage.models.Board
import com.example.projemanage.models.User
import com.example.projemanage.utils.Constants
import com.example.projemanage.utils.Constants.FCM_TOKEN_UPDATED
import com.example.projemanage.utils.Constants.PROGEMANAG_PREFERENCES
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpActionBar()

        val nav_view = findViewById<NavigationView>(R.id.nav_view)
        nav_view.setNavigationItemSelectedListener(this)
        mSharedPreferences = this.getSharedPreferences(PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(FCM_TOKEN_UPDATED,false)

        if(tokenUpdated) {
            showProgressDialog()
            FirestoreClass().loadUserData(this,true)
        }else{
            FirebaseInstallations.getInstance().getToken(true)
                .addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
                }
        }

        val fab_create_board = findViewById<FloatingActionButton>(R.id.fab_create_board)

        fab_create_board.setOnClickListener {
            val intent = Intent(this,
                CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Update the data in the database.
        // Show the progress dialog.
        showProgressDialog()
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }


    fun updateNavigationUserDetails(user: User){
        val nav_user_image = findViewById<ImageView>(R.id.nav_user_image)

        mUserName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image);
        val tv_username = findViewById<TextView>(R.id.tv_username)
        tv_username.text = user.name
    }

    private fun setUpActionBar(){
        val toolbar_main_activity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    private fun toggleDrawer(){
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

       override fun onBackPressed(){
           val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK &&
                requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)

            }
            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {

        hideProgressDialog()

        if (boardsList.size > 0) {

            val rv_boards_list = findViewById<RecyclerView>(R.id.rv_boards_list)
            val tv_no_boards_available = findViewById<TextView>(R.id.tv_no_boards_available)
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this@MainActivity)
            rv_boards_list.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            rv_boards_list.adapter = adapter // Attach the adapter to the recyclerView.

            adapter.setOnClickListener(object :
                BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            val rv_boards_list = findViewById<RecyclerView>(R.id.rv_boards_list)
            val tv_no_boards_available = findViewById<TextView>(R.id.tv_no_boards_available)
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()

        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog()
        FirestoreClass().loadUserData(this@MainActivity)
    }
}