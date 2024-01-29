package com.pnpawd.userapp.reports.farmer_report

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.RejectedOnBoardingList_Adapter
import com.pnpawd.userapp.models.RejectedListModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OnBoardingDetailsStatusActivity : AppCompatActivity() {
    private lateinit var unique_id: String
    private lateinit var plot_no: String
    private var token: String = ""
    private var base_value: Double = 0.0

    private lateinit var myAdapter: RejectedOnBoardingList_Adapter
    private var rejectedList: ArrayList<RejectedListModel> = ArrayList()
    private lateinit var  back : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding_details_status)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("unique_id")!!
            plot_no = bundle.getString("plot_no")!!
            base_value = bundle.getDouble("base_value")
        } else {
            Log.e("unique_id", "Nope")
        }

        back = findViewById(R.id.ivBackFromReject)
        back.setOnClickListener { onBackPressed() }

        val recyclerView : RecyclerView = findViewById(R.id.rvRejectedList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter = RejectedOnBoardingList_Adapter(rejectedList)
        recyclerView.adapter = myAdapter

        getRejectedReason(unique_id, plot_no)
    }

    private fun getRejectedReason(unique_id: String, plot_no: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.rejectReason("Bearer $token" , unique_id, plot_no).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonObject1 = jsonObject.optJSONObject("plot")
                        val reject_comment = jsonObject1.optString("reject_comment")

                        val jsonObject2 = jsonObject1.optJSONObject("reasons")

                        val id = jsonObject2.optString("id")
                        val reasons = jsonObject2.optString("reasons")

                        rejectedList.add(RejectedListModel(reasons, reject_comment, id, unique_id, plot_no, base_value))
                        myAdapter.notifyDataSetChanged()
                    }
                }
                else{
                    Log.e("response_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}