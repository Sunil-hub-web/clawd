package com.pnpawd.userapp.cropintellix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.pnpawd.userapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

class LanguageSelectActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_select)


        val hindi = findViewById<View>(R.id.hindi_layout) as LinearLayout
        val english = findViewById<View>(R.id.english_layout) as LinearLayout
        val telugu = findViewById<View>(R.id.telugu_layout) as LinearLayout
        val marathi = findViewById<View>(R.id.marathi_layout) as LinearLayout
        val gujrati = findViewById<View>(R.id.gujrati_layout) as LinearLayout
        val odia = findViewById<View>(R.id.odia_layout) as LinearLayout

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForUpdate()

        english.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashboardActivity::class.java)
            intent.putExtra("language", "en")
            startActivity(intent)
        }


        hindi.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashboardActivity::class.java)
            intent.putExtra("language", "hi")
            startActivity(intent)
        }


        telugu.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashboardActivity::class.java)
            intent.putExtra("language", "te")
            startActivity(intent)
        }


        marathi.setOnClickListener { }


        gujrati.setOnClickListener { }


        odia.setOnClickListener { }

    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }


            if (isUpdateAllowed && isUpdateAvailable) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this@LanguageSelectActivity,
                    123
                )
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123){
            if(resultCode != RESULT_OK){
                println("Something went wrong !!")
            }
        }
    }


    override fun onBackPressed() {
        finishAffinity()
        finish()
    }


    override fun onResume() {
        super.onResume()

        checkForUpdate()
    }
}