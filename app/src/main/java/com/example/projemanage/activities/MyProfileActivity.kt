package com.example.projemanage.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanage.R
import com.example.projemanage.firebase.FirestoreClass
import com.example.projemanage.models.User
import com.example.projemanage.utils.Constants.IMAGE
import com.example.projemanage.utils.Constants.MOBILE
import com.example.projemanage.utils.Constants.NAME
import com.example.projemanage.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.projemanage.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.projemanage.utils.Constants.getFileExtension
import com.example.projemanage.utils.Constants.showImageChooser
import com.google.android.gms.dynamite.DynamiteModule.VersionPolicy.IVersions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {


    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        val iv_profile_user_image = findViewById<ImageView>(R.id.iv_profile_user_image)

        iv_profile_user_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED){
                    showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE)
            }
        }
        val btn_update = findViewById<Button>(R.id.btn_update)
        btn_update.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog()
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }
        }else{
            Toast.makeText(this,"Oops, you hust denied the permission for storage. You can also allow it from settings."
            ,Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){
            mSelectedImageFileUri = data.data
            val iv_profile_user_image = findViewById<CircleImageView>(R.id.iv_profile_user_image)
            try {
                Glide
                    .with(this)
                    .load(Uri.parse(mSelectedImageFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image);
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar(){
        val toolbar_my_profile_activity = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    fun setUserDataInUI(user: User){
        val iv_profile_user_image = findViewById<CircleImageView>(R.id.iv_profile_user_image)
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_email = findViewById<EditText>(R.id.et_email)
        val et_mobile = findViewById<EditText>(R.id.et_mobile)

        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image);

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()


        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[IMAGE] = mProfileImageURL

        }
        val et_name = findViewById<EditText>(R.id.et_name)

        if(et_name.text.toString() != mUserDetails.name){
            userHashMap[NAME] = et_name.text.toString()

        }
        val et_mobile = findViewById<EditText>(R.id.et_mobile)

        if(et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[MOBILE] = et_mobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    private  fun uploadUserImage(){
        showProgressDialog()

        if(mSelectedImageFileUri != null){
            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE" +
                        System.currentTimeMillis() + "." +
                        getFileExtension(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this,exception.message,
                    Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}