package com.pnpawd.userapp.reports.farmer_report

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.Farmer_Report_Adapter
import com.pnpawd.userapp.models.FarmerReportModel
import com.pnpawd.userapp.models.StatusModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class OnBoardingReportActivity : AppCompatActivity() {
    var token: String = ""
    var status: String = ""
    var isScrolling: Boolean = true
    private lateinit var bNext: Button
    private lateinit var bBack: Button
    private var next_page_url: String? = null
    var startIndex = 1
    var endIndex: Int? = null

    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    var reportModel: ArrayList<FarmerReportModel> = ArrayList<FarmerReportModel>()
    private final lateinit var farmerReportModel: FarmerReportModel
    private lateinit var farmerReportAdapter: Farmer_Report_Adapter
    private lateinit var farmer_recyclerView: RecyclerView

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding_report)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        status = bundle!!.getString("status").toString()

        bNext = findViewById(R.id.onboarding_list_next)
        bBack = findViewById(R.id.onboarding_list_back)

        back = findViewById(R.id.farmer_report_back)
        searchView = findViewById(R.id.onboarding_report_search)
        progressBar = findViewById(R.id.farmer_progressBar)
        farmer_recyclerView = findViewById(R.id.farmer_report_recyclerView)

        farmerReportAdapter = Farmer_Report_Adapter(reportModel)
        val layoutManager = LinearLayoutManager(this)
        farmer_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        farmer_recyclerView.adapter = farmerReportAdapter


        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

// Disabling Prev button in start
        bBack.isEnabled = false
        bBack.isClickable = false



        bNext.setOnClickListener(View.OnClickListener {
            val size = reportModel.size
            if(startIndex >= size){

            }
            else{
                startIndex += 10
                endIndex = startIndex + 10

                if(endIndex!! >= size ){
                    endIndex = size

                    Log.e("next_data", "IF")
                    val adapter = Farmer_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                    farmer_recyclerView.adapter = adapter
                    farmer_recyclerView.layoutManager = LinearLayoutManager(this@OnBoardingReportActivity)
                    farmerReportAdapter.notifyDataSetChanged()

                    bNext.isClickable = false
                    bNext.isEnabled = false

                    bBack.isClickable = true
                    bBack.isEnabled = true

                    Toast.makeText(this@OnBoardingReportActivity, "Last Page", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.e("next_data", "else")
                    val adapter = Farmer_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                    farmer_recyclerView.adapter = adapter
                    farmer_recyclerView.layoutManager = LinearLayoutManager(this@OnBoardingReportActivity)
                    farmerReportAdapter.notifyDataSetChanged()

                    bBack.isClickable = true
                    bBack.isEnabled = true
                }
            }
        })

        bBack.setOnClickListener(View.OnClickListener {
            bNext.isClickable = true
            bNext.isEnabled = true

            endIndex = startIndex
            startIndex -= 10

            Log.e("index", endIndex.toString())
            Log.e("index", startIndex.toString())

            if(startIndex < 1){
                startIndex = 1
                endIndex = 0

                Log.e("back_data", "if")
                bBack.isClickable = false
                bBack.isEnabled = false

                Toast.makeText(this@OnBoardingReportActivity, "First Page", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.e("back_data", "else")
                endIndex = startIndex + 10
                val adapter = Farmer_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                farmer_recyclerView.adapter = adapter
                farmer_recyclerView.layoutManager = LinearLayoutManager(this@OnBoardingReportActivity)
                farmerReportAdapter.notifyDataSetChanged()
            }
        })


// Search Functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                reportModel.clear()
                farmerReportAdapter = Farmer_Report_Adapter(reportModel)
                farmer_recyclerView.adapter = farmerReportAdapter
                farmerReportAdapter.notifyDataSetChanged()

                searchData(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {

                return true
            }
        })

// Getting list of entries from API
        getData(token)
    }


    private fun searchData(query: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.registrationReportQuery("Bearer $token", query).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("farmers")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val id = jsonObject.optString("id").toString()

                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()

                            val date = jsonObject.optString("date_survey").toString()

                            val time = jsonObject.optString("time_survey").toString()

                            Log.e("uniqueId", uniqueId)

                            val farmerReportModel = FarmerReportModel(id, uniqueId, date, time)
                            reportModel.add(farmerReportModel)
                            farmerReportAdapter.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    Log.e("status_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })

    }

    private fun getData(token: String) {
        val statusModel = StatusModel(status)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.registrationReportList("Bearer $token", statusModel).enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val farmers = jsonObject.optJSONArray("farmers")

                        for (i in 0 until farmers.length()) {
                            val jsonObject = farmers.getJSONObject(i)

                            val id = jsonObject.optString("id").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val date = jsonObject.optString("date_survey").toString()
                            val time = jsonObject.optString("time_survey").toString()
                            Log.e("uniqueId", uniqueId)

                            farmerReportModel = FarmerReportModel(id, uniqueId, date, time)
                            reportModel.add(farmerReportModel)
                        }

// Checking if reportmodel size is greater than 10 or not
                        if(reportModel.size > 10){
                            val adapter = Farmer_Report_Adapter(reportModel.subList(0, 10))
                            farmer_recyclerView.adapter = adapter
                            farmer_recyclerView.layoutManager = LinearLayoutManager(this@OnBoardingReportActivity)

                            farmerReportAdapter.notifyDataSetChanged()
                        }
                        else{
                            farmerReportAdapter.notifyDataSetChanged()
                            bNext.isClickable = false
                            bBack.isClickable = false
                        }

                    }
                }
                else{
                    Log.e("status_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }
}