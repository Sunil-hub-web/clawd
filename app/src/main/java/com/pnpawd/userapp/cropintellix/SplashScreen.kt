package com.pnpawd.userapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pnpawd.userapp.R
import com.pnpawd.userapp.cropintellix.LanguageSelectActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val logged = sharedPreference.getBoolean("isLoggedin", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (logged){
                val intent = Intent(this@SplashScreen, LanguageSelectActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val editor = sharedPreference.edit()
                editor.putBoolean("isLoggedin", false)
                editor.commit()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)

    }
}