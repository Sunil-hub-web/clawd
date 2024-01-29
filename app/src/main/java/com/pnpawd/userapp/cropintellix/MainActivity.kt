package com.pnpawd.userapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.pnpawd.userapp.cropintellix.PrivacyActivity
import com.pnpawd.userapp.cropintellix.SignUpActivity
import com.pnpawd.userapp.cropintellix.TNCActivity

class MainActivity : AppCompatActivity() {
    private lateinit var signIn : LinearLayout
    private lateinit var signUp : LinearLayout

    private lateinit var privacy : TextView
    private lateinit var terms : TextView

    var PERMISSION_ALL = 1
    private var Permissions: Array<String> = arrayOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signIn = findViewById(R.id.signIn)
        signUp = findViewById(R.id.signUp)

        privacy = findViewById(R.id.main_privacy_policy)
        terms = findViewById(R.id.main_terms_condition)

        Permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (!hasPermissions(this, *Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, PERMISSION_ALL)
        }

        signIn.setOnClickListener(View.OnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        })

        signUp.setOnClickListener(View.OnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        })


        terms.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, TNCActivity::class.java)
            startActivity(intent)
        })

        privacy.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        })
    }

    private fun hasPermissions(context: Context?, vararg PERMISSIONS: String): Boolean {
        if (context != null) {
            for (permissions in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permissions
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        finishAffinity()
        finish()
    }
}