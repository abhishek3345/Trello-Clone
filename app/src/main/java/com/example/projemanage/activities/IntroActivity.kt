package com.example.projemanage.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.projemanage.R

class IntroActivity : BaseActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
        )
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon bl.ttf")

        val tv_app_name_intro = findViewById<TextView>(R.id.tv_app_name_intro)
        tv_app_name_intro.typeface = typeface

        // TODO (Step 7: Add a click event for Sign Up btn and launch the Sign Up Screen.)
        // START
        val btn_sign_up_intro :Button = findViewById(R.id.btn_sign_up_intro)
        val btn_sign_in_intro :Button = findViewById(R.id.btn_sign_in_intro)

        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_sign_in_intro.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }
}