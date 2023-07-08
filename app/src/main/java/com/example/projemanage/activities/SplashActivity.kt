package com.example.projemanage.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.projemanage.R
import com.example.projemanage.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
        )

        val typeFace: Typeface = Typeface.createFromAsset(assets,"carbon bl.ttf")
        val textview = findViewById<TextView>(R.id.tv_app_name)
        textview.typeface = typeFace

        Handler(Looper.getMainLooper()).postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserId()

            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this,MainActivity::class.java))
            }else{
                startActivity(Intent(this,IntroActivity::class.java))
            }
            finish()
        }, 2500)
    }
}