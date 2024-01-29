package com.pnpawd.userapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.SignUpActivity
import com.pnpawd.userapp.models.LoginModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.provider.Settings.Secure
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.pnpawd.userapp.cropintellix.LanguageSelectActivity
import java.util.*


class SignInActivity : AppCompatActivity() {
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnLogin: Button

    lateinit var signUp: TextView

    var mobile: String = ""
    var password: String = ""
    var fcmToken: String = ""

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        Log.e("version", versionName + versionCode + release.toString())

        val deviceName = Build.MODEL // returns model name
        val deviceManufacturer = Build.MANUFACTURER // returns manufacturer
        Log.e("version", deviceName + deviceManufacturer)

        btnLogin = findViewById(R.id.login)
        edtEmail = findViewById(R.id.username)
        edtPassword = findViewById(R.id.password)
        signUp = findViewById(R.id.loginSignup)

        signUp.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        })

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

// Get new FCM registration token
            fcmToken = task.result
            Log.e("FCM_TOKEN", fcmToken)
        })

        btnLogin.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@SignInActivity, SweetAlertDialog.WARNING_TYPE)

            progress = SweetAlertDialog(this@SignInActivity, SweetAlertDialog.PROGRESS_TYPE)

            if (edtEmail.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.email_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtPassword.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.password_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtPassword.text.length < 6){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.password_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else{
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.wait)
                progress.contentText = resources.getString(R.string.checking_credentials)
                progress.setCancelable(false)
                progress.show()

                mobile = edtEmail.text.toString()
                password = edtPassword.text.toString()

                login(mobile, password, versionCode, versionName, release, deviceName, deviceManufacturer, fcmToken)
            }
        })
    }

    private fun login(
        mobile: String,
        password: String,
        versionCode: Int,
        versionName: String,
        release: Double,
        deviceName: String,
        deviceManufacturer: String,
        fcmToken: String
    ){
        val m_androidId = Secure.getString(contentResolver, Secure.ANDROID_ID)
        val uniqueID: String = UUID.randomUUID().toString()
        Log.e("device_ID", m_androidId)
        Log.e("uniqueID", uniqueID)

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val registerInfo = LoginModel(mobile, password, versionCode.toString(), versionName, release, deviceName, deviceManufacturer, m_androidId, fcmToken)

        retIn.login(registerInfo).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if(response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())
                        val phoneJsonObject = jsonObject.getJSONObject("user")
                        val token = phoneJsonObject.getString("token")
                        val state_id = phoneJsonObject.getString("state_id")
                        val user_id = phoneJsonObject.getString("id")
                        val company_id = phoneJsonObject.getString("company_id")

                        Log.e("stata_id", state_id)

                        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putBoolean("isLoggedin", true)
                        editor.putString("token",token)
                        editor.putString("state_id",state_id)
                        editor.putString("user_id",user_id)
                        editor.putString("company_id", company_id)
                        editor.commit()

                    }
                    Toast.makeText(this@SignInActivity, "Login Successfully!", Toast.LENGTH_SHORT).show()
                    nextScreen()
                }
                else if (response.code() == 422){
                    progress.dismiss()

                    if (response.errorBody() != null) {
                        val stringResponse = JSONObject(response.errorBody()!!.string())
                        val error_message = stringResponse.optString("error")

                        val WarningDialog = SweetAlertDialog(this@SignInActivity, SweetAlertDialog.WARNING_TYPE)
                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = error_message
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                    }

                }
                else{
                    progress.dismiss()
                    Toast.makeText(this@SignInActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
                progress.dismiss()
            }
        })
    }

    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, LanguageSelectActivity::class.java)
        startActivity(intent)
        finish()
    }
}