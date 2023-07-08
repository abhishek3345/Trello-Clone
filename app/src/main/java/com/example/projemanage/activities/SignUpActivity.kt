package com.example.projemanage.activities


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
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // FirebaseApp.initializeApp(this@SignUpActivity)
        setContentView(R.layout.activity_sign_up)
        setupActionBar()
        val btn_sign_up = findViewById<Button>(R.id.btn_sign_up)
        btn_sign_up.setOnClickListener {

            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this,"you have successfully registered.",
            Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar() {
        val toolbar_sign_up_activity = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun registerUser(){

        val et_email = findViewById<EditText>(R.id.et_email)
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_password = findViewById<EditText>(R.id.et_password)

        val name: String = et_name.text.toString().trim { it <= ' ' }
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            // Show the progress dialog.

            showProgressDialog()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->

                        if (task.isSuccessful) {

                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid,name,registeredEmail)

                            FirestoreClass().registerUser(this, user)

                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideProgressDialog()
                        }
                    }
        }
    }


     private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
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


