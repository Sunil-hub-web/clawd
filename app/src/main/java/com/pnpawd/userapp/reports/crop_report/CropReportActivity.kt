package com.pnpawd.userapp.reports.crop_report

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
import com.pnpawd.userapp.adapters.Crop_Report_Adapter
import com.pnpawd.userapp.models.CropReportModel
import com.pnpawd.userapp.models.StatusModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class CropReportActivity : AppCompatActivity() {
    var token: String = ""
    var status: String = ""
    private lateinit var bNext: Button
    private lateinit var bBack: Button
    var startIndex = 1
    var endIndex: Int? = null

    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    var cropModel: ArrayList<CropReportModel> = ArrayList<CropReportModel>()
    private final lateinit var cropReportModel: CropReportModel
    private lateinit var cropReportAdapter: Crop_Report_Adapter
    private lateinit var farmer_recyclerView: RecyclerView

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_report)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        status = bundle!!.getString("status").toString()

        back = findViewById(R.id.crop_report_back)
        searchView = findViewById(R.id.crop_report_search)
        progressBar = findViewById(R.id.crop_progressBar)
        farmer_recyclerView = findViewById(R.id.crop_report_recyclerView)

        bNext = findViewById(R.id.crop_list_next)
        bBack = findViewById(R.id.crop_list_back)

        cropReportAdapter = Crop_Report_Adapter(cropModel)
        val layoutManager = LinearLayoutManager(this)
        farmer_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        farmer_recyclerView.adapter = cropReportAdapter


        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

// Search Functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                cropModel.clear()
                cropReportAdapter = Crop_Report_Adapter(cropModel)
                farmer_recyclerView.adapter = cropReportAdapter
                cropReportAdapter.notifyDataSetChanged()

// Making search API call
                searchData(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {

                return true
            }
        })

// Disabling Prev button in start
        bBack.isEnabled = false
        bBack.isClickable = false


        bNext.setOnClickListener(View.OnClickListener {
            val size = cropModel.size
            if(startIndex >= size){

            }
            else{
                startIndex += 10
                endIndex = startIndex + 10

                if(endIndex!! >= size ){
                    endIndex = size

                    Log.e("next_data", "IF")
                    val adapter = Crop_Report_Adapter(cropModel.subList(startIndex, endIndex!!))
                    farmer_recyclerView.adapter = adapter
                    farmer_recyclerView.layoutManager = LinearLayoutManager(this@CropReportActivity)
                    cropReportAdapter.notifyDataSetChanged()

                    bNext.isClickable = false
                    bNext.isEnabled = false

                    bBack.isClickable = true
                    bBack.isEnabled = true

                    Toast.makeText(this@CropReportActivity, "Last Page", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.e("next_data", "else")
                    val adapter = Crop_Report_Adapter(cropModel.subList(startIndex, endIndex!!))
                    farmer_recyclerView.adapter = adapter
                    farmer_recyclerView.layoutManager = LinearLayoutManager(this@CropReportActivity)
                    cropReportAdapter.notifyDataSetChanged()

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

                Toast.makeText(this@CropReportActivity, "First Page", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.e("back_data", "else")
                endIndex = startIndex + 10
                val adapter = Crop_Report_Adapter(cropModel.subList(startIndex, endIndex!!))
                farmer_recyclerView.adapter = adapter
                farmer_recyclerView.layoutManager = LinearLayoutManager(this@CropReportActivity)
                cropReportAdapter.notifyDataSetChanged()
            }
        })

        getData(token)
    }


    private fun searchData(query: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.cropReportQuery("Bearer $token", query).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("CropData")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val plot_no = jsonObject.optString("plot_no").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val area_in_acers = jsonObject.optString("area_in_acers").toString()
                            val season = jsonObject.optString("season").toString()
                            Log.e("uniqueId", uniqueId)

                            val cropReportModel = CropReportModel(plot_no, uniqueId, area_in_acers, season)
                            cropModel.add(cropReportModel)
                            cropReportAdapter.notifyDataSetChanged()
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
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val statusModel = StatusModel(status)
        apiInterface.cropReportList("Bearer $token", statusModel).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val jsonArray = jsonObject.optJSONArray("CropData")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val plot_no = jsonObject.optString("plot_no").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val area_in_acers = jsonObject.optString("area_in_acers").toString()
                            val season = jsonObject.optString("season").toString()

                            Log.e("uniqueId", uniqueId)
                            cropReportModel = CropReportModel(plot_no, uniqueId, area_in_acers, season)
                            cropModel.add(cropReportModel)
                        }

// Checking if reportmodel size is greater than 10 or not
                        if(cropModel.size > 10){
                            val adapter = Crop_Report_Adapter(cropModel.subList(0, 10))
                            farmer_recyclerView.adapter = adapter
                            farmer_recyclerView.layoutManager = LinearLayoutManager(this@CropReportActivity)

                            cropReportAdapter.notifyDataSetChanged()
                        }
                        else{
                            cropReportAdapter.notifyDataSetChanged()
                            bNext.isClickable = false
                            bBack.isClickable = false
                        }

// Dismissing the progress bar

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