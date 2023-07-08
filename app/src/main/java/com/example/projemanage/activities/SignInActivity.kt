package com.example.projemanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.example.projemanage.R
import com.example.projemanage.firebase.FirestoreClass
import com.example.projemanage.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setupActionBar()
        val btn_sign_in = findViewById<Button>(R.id.btn_sign_in)
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }
    fun signInSuccess(user: User){
        hideProgressDialog()
        Toast.makeText(this@SignInActivity,
            "You have successfully signed in.", Toast.LENGTH_LONG).show()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    private fun setupActionBar(){
        val toolbar_sign_in_activity = findViewById<Toolbar>(R.id.toolbar_sign_in_activity)

        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar : ActionBar? = supportActionBar

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }


    private fun signInRegisteredUser() {

        val et_email = findViewById<EditText>(R.id.et_email)
        val et_password = findViewById<EditText>(R.id.et_password)

        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // Show the progress dialog.
            showProgressDialog()

            // Sign-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {

                        FirestoreClass().loadUserData(this@SignInActivity)

                    } else {
                        Toast.makeText(
                            this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }
}