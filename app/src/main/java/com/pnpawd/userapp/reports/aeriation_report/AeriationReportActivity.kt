package com.pnpawd.userapp.reports.aeriation_report

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
import com.pnpawd.userapp.adapters.Aeriation_Report_Adapter
import com.pnpawd.userapp.models.AeriationReportModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class AeriationReportActivity : AppCompatActivity() {
    var token: String = ""
    var status: String = ""
    private var next_page_url: String? = null
    private lateinit var bNext: Button
    private lateinit var bBack: Button
    var startIndex = 1
    var endIndex: Int? = null

    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    private lateinit var back: ImageView

    var reportModel: ArrayList<AeriationReportModel> = ArrayList<AeriationReportModel>()
    private lateinit var aeriationReportModel: AeriationReportModel
    private lateinit var aeriationReportAdapter: Aeriation_Report_Adapter
    private lateinit var aeriation_recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeriation_report)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        status = bundle!!.getString("status").toString()

        back = findViewById(R.id.aeriation_report_back)
        searchView = findViewById(R.id.aeriation_report_search)
        progressBar = findViewById(R.id.aeriation_progressBar)
        aeriation_recyclerView = findViewById(R.id.aeriation_report_recyclerView)

        bNext = findViewById(R.id.aeriation_list_next)
        bBack = findViewById(R.id.aeriation_list_back)

        aeriationReportAdapter = Aeriation_Report_Adapter(reportModel)
        val layoutManager = LinearLayoutManager(this)
        aeriation_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        aeriation_recyclerView.adapter = aeriationReportAdapter


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                reportModel.clear()
                aeriationReportAdapter =
                     Aeriation_Report_Adapter(
                        reportModel
                    )
                aeriation_recyclerView.adapter = aeriationReportAdapter
                aeriationReportAdapter.notifyDataSetChanged()

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

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })


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
                    val adapter =
                         Aeriation_Report_Adapter(
                            reportModel.subList(startIndex, endIndex!!)
                        )
                    aeriation_recyclerView.adapter = adapter
                    aeriation_recyclerView.layoutManager = LinearLayoutManager(this@AeriationReportActivity)
                    aeriationReportAdapter.notifyDataSetChanged()

                    bNext.isClickable = false
                    bNext.isEnabled = false

                    bBack.isClickable = true
                    bBack.isEnabled = true

                    Toast.makeText(this@AeriationReportActivity, "Last Page", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.e("next_data", "else")
                    val adapter =
                         Aeriation_Report_Adapter(
                            reportModel.subList(startIndex, endIndex!!)
                        )
                    aeriation_recyclerView.adapter = adapter
                    aeriation_recyclerView.layoutManager = LinearLayoutManager(this@AeriationReportActivity)
                    aeriationReportAdapter.notifyDataSetChanged()

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

                Toast.makeText(this@AeriationReportActivity, "First Page", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.e("back_data", "else")
                endIndex = startIndex + 10
                val adapter =
                     Aeriation_Report_Adapter(
                        reportModel.subList(startIndex, endIndex!!)
                    )
                aeriation_recyclerView.adapter = adapter
                aeriation_recyclerView.layoutManager = LinearLayoutManager(this@AeriationReportActivity)
                aeriationReportAdapter.notifyDataSetChanged()
            }
        })

// Getting list of entries from API
        getData(token)
    }

    private fun getData(token: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.aeriationReportList("Bearer $token").enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val jsonArray = jsonObject.optJSONArray("aeration")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val id = jsonObject.optString("aeration_no").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid").toString()
                            val pipe_no = jsonObject.optString("pipe_no").toString()
                            val pipe_installation_id = jsonObject.optString("pipe_installation_id").toString()
                            val aeration_no = jsonObject.optString("aeration_no").toString()
                            val plot_no = jsonObject.optString("plot_no").toString()

                            val farmerapproved = jsonObject.getJSONObject("farmerapproved")
                            val farmer_name = farmerapproved.optString("farmer_name").toString()

                            val rejectReason = jsonObject.getJSONObject("reject_reason")
                            val reasons = rejectReason.optString("reasons").toString()

                            Log.e("uniqueId", uniqueId)

                            aeriationReportModel = AeriationReportModel(id, uniqueId, pipe_no, farmer_plot_uniqueid, pipe_installation_id, aeration_no, plot_no, farmer_name, reasons)
                            reportModel.add(aeriationReportModel)
                        }

// Checking if reportmodel size is greater than 10 or not
                        if(reportModel.size > 10){
                            val adapter =
                                 Aeriation_Report_Adapter(
                                    reportModel.subList(0, 10)
                                )
                            aeriation_recyclerView.adapter = adapter
                            aeriation_recyclerView.layoutManager = LinearLayoutManager(this@AeriationReportActivity)

                            aeriationReportAdapter.notifyDataSetChanged()
                        }
                        else{
                            aeriationReportAdapter.notifyDataSetChanged()
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

    private fun searchData(query: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.registrationReportQuery("Bearer $token", query).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("aeration")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val id = jsonObject.optString("aeration_no").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid").toString()
                            val pipe_no = jsonObject.optString("pipe_no").toString()
                            val pipe_installation_id = jsonObject.optString("pipe_installation_id").toString()
                            val aeration_no = jsonObject.optString("aeration_no").toString()
                            val plot_no = jsonObject.optString("plot_no").toString()

                            val farmerapproved = jsonObject.getJSONObject("farmerapproved")
                            val farmer_name = farmerapproved.optString("farmer_name").toString()

                            val rejectReason = jsonObject.getJSONObject("reject_reason")
                            val reasons = rejectReason.optString("reasons").toString()

                            Log.e("uniqueId", uniqueId)

                            val aeriationReportModel = AeriationReportModel(id, uniqueId, pipe_no, farmer_plot_uniqueid, pipe_installation_id, aeration_no, plot_no, farmer_name, reasons)
                            reportModel.add(aeriationReportModel)
                            aeriationReportAdapter.notifyDataSetChanged()
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