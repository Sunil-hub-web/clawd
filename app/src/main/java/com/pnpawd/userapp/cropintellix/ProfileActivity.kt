package com.pnpawd.userapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.PrivacyActivity
import com.pnpawd.userapp.cropintellix.TNCActivity
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var logout: LinearLayout
    private lateinit var back: ImageView
    private lateinit var profile: ImageView

    private lateinit var edtName: TextView
    private lateinit var edtMobile: TextView
//    private lateinit var edtEmail: TextView
    private lateinit var tnc: TextView
    private lateinit var privacy: TextView

//    private lateinit var submit: Button

    var token: String = ""

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        progress = SweetAlertDialog(this@ProfileActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        logout = findViewById(R.id.logout)
        back = findViewById(R.id.profile_back)
        profile = findViewById(R.id.user_profile_pic)

        edtName = findViewById(R.id.profile_name)
        edtMobile = findViewById(R.id.profile_mobile_number)
//        edtEmail = findViewById(R.id.profile_email)

        tnc = findViewById(R.id.terms_condition)
        privacy = findViewById(R.id.privacy_policy)

//        submit = findViewById(R.id.profile_submit)

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        logout.setOnClickListener(View.OnClickListener {
            AlertDialog.Builder(this@ProfileActivity)
                .setMessage(R.string.logout_enabled)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        var editor = sharedPreference.edit()
                        editor.putBoolean("isLoggedin", false)
                        editor.commit()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
                .setNegativeButton(R.string.no, null)
                .show()
        })

        tnc.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, TNCActivity::class.java)
            startActivity(intent)
        })

        privacy.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        })

        getUserDetails()
    }

    private fun getUserDetails() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.userDetails("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())

                        val name = stringResponse.getString("name")
//                        val email = stringResponse.getString("email")
                        val mobile = stringResponse.getString("mobile")

                        edtName.setText(name)
//                        edtEmail.text = email
                        edtMobile.text = mobile
                    } else {
                        Log.e("statusCode", response.code().toString())
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }
        })
    }

//    private fun nextScreen() {
//        progress.dismiss()
//
//        super.onBackPressed()
//        finish()
//    }
}