package com.pnpawd.userapp.cropintellix

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.pnpawd.userapp.R
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrivacyActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var privacyData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        back = findViewById(R.id.privacy_back)
        privacyData = findViewById(R.id.privacyData)

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })



        getPrivacyData()
    }

    private fun getPrivacyData() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.privacyData().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val terms = stringResponse.getString("app_privacypolicy")

                        privacyData.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            HtmlCompat.fromHtml(terms, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        } else {
                            Html.fromHtml(terms)
                        }

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
}