package com.pnpawd.userapp.reports.polygon_report

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.Pipe_Report_Adapter
import com.pnpawd.userapp.models.PipeReportModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class PolygonReportActivity : AppCompatActivity() {
    private var token: String = ""
    private var status: String = ""
    private var isScrolling: Boolean = true
    private var next_page_url: String? = null
    private lateinit var bNext: Button
    private lateinit var bBack: Button
    private var startIndex = 1
    private var endIndex: Int? = null

    private lateinit var back: ImageView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var pipeReportModel: PipeReportModel

    private var reportModel: ArrayList<PipeReportModel> = ArrayList<PipeReportModel>()
    private lateinit var pipeReportAdapter: Pipe_Report_Adapter
    private lateinit var pipe_recyclerView: RecyclerView

    private lateinit var progress: SweetAlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polygon_report)
        progress = SweetAlertDialog(this@PolygonReportActivity, SweetAlertDialog.PROGRESS_TYPE)

// Initializing sharedpreference
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!


        back = findViewById(R.id.pipe_report_back)
        searchView = findViewById(R.id.polygon_report_search)
        progressBar = findViewById(R.id.polygon_progressBar)

        bNext = findViewById(R.id.polygon_list_next)
        bBack = findViewById(R.id.polygon_list_back)

        pipe_recyclerView = findViewById(R.id.polygon_report_recyclerView)

        pipeReportAdapter = Pipe_Report_Adapter(reportModel)
        val layoutManager = LinearLayoutManager(this)
        pipe_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        pipe_recyclerView.adapter = pipeReportAdapter

// Search Functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                reportModel.clear()
                pipeReportAdapter = Pipe_Report_Adapter(reportModel)
                pipe_recyclerView.adapter = pipeReportAdapter
                pipeReportAdapter.notifyDataSetChanged()

// Making search API call
                searchData(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
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
                    val adapter = Pipe_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                    pipe_recyclerView.adapter = adapter
                    pipe_recyclerView.layoutManager = LinearLayoutManager(this@PolygonReportActivity)
                    pipeReportAdapter.notifyDataSetChanged()

                    bNext.isClickable = false
                    bNext.isEnabled = false

                    bBack.isClickable = true
                    bBack.isEnabled = true

                    Toast.makeText(this@PolygonReportActivity, "Last Page", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.e("next_data", "else")
                    val adapter = Pipe_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                    pipe_recyclerView.adapter = adapter
                    pipe_recyclerView.layoutManager = LinearLayoutManager(this@PolygonReportActivity)
                    pipeReportAdapter.notifyDataSetChanged()

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

                Toast.makeText(this@PolygonReportActivity, "First Page", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.e("back_data", "else")
                endIndex = startIndex + 10
                val adapter = Pipe_Report_Adapter(reportModel.subList(startIndex, endIndex!!))
                pipe_recyclerView.adapter = adapter
                pipe_recyclerView.layoutManager = LinearLayoutManager(this@PolygonReportActivity)
                pipeReportAdapter.notifyDataSetChanged()
            }
        })

// Getting list of entries from API
        getData(token)

    }


    private fun getData(token: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = "Checking"
        progress.contentText = " Checking for new version"
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.polygonReportList("Bearer $token").enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("polygon")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val id = jsonObject.optString("id").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid").toString()
                            val plot_no = jsonObject.optString("plot_no").toString()
                            val pipe_no = jsonObject.optString("pipe_no").toString()
                            val lat = jsonObject.optString("lat").toString()
                            val lng = jsonObject.optString("lng").toString()
                            val distance = jsonObject.optString("distance").toString()
                            val area_in_acers = jsonObject.optString("area_in_acers").toString()
                            val state = jsonObject.optString("state").toString()
                            val district = jsonObject.optString("district").toString()
                            val taluka = jsonObject.optString("taluka").toString()
                            val villageName = jsonObject.optString("village").toString()


                            val farmerapproved = jsonObject.getJSONObject("farmerapproved")
                            val farmer_name = farmerapproved.optString("farmer_name").toString()
                            var aadharNum :String?= farmerapproved.optString("aadhaar").toString()
                            var mobileNum :String?= farmerapproved.optString("mobile").toString()

                            val reject_reason = jsonObject.getJSONObject("reject_reason")
                            val reason_id = reject_reason.optString("id").toString()
                            val reasons = reject_reason.optString("reasons").toString()

                            if (aadharNum == null){
                                aadharNum = "N.A"
                            }
                            if (mobileNum == null){
                                mobileNum = "N.A"
                            }

                            Log.e("uniqueId", uniqueId)

                            pipeReportModel = PipeReportModel(id, uniqueId, lat, farmer_plot_uniqueid, lng, plot_no, pipe_no, distance, farmer_name, reasons, reason_id, area_in_acers,state,district,taluka,villageName,aadharNum,mobileNum)
                            reportModel.add(pipeReportModel)
                        }

// Checking if reportmodel size is greater than 10 or not
                        if(reportModel.size > 10){
                            val adapter = Pipe_Report_Adapter(reportModel.subList(0, 10))
                            pipe_recyclerView.adapter = adapter
                            pipe_recyclerView.layoutManager = LinearLayoutManager(this@PolygonReportActivity)

                            pipeReportAdapter.notifyDataSetChanged()
                        }
                        else{
                            pipeReportAdapter.notifyDataSetChanged()
                            bNext.isClickable = false
                            bBack.isClickable = false
                        }

// Dismissing the progress bar
                        progress.dismiss()
                    }
                }

// Checking status code
                else{
                    Log.e("status_code", response.code().toString())
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }

// Search API Function
    private fun searchData(query: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.searchPipeReport("Bearer $token", query).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("pipe")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val id = jsonObject.optString("id").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid").toString()
                            val plot_no = jsonObject.optString("plot_no").toString()
                            val pipe_no = jsonObject.optString("pipe_no").toString()
                            val lat = jsonObject.optString("lat").toString()
                            val lng = jsonObject.optString("lng").toString()
                            val distance = jsonObject.optString("distance").toString()
                            val state = jsonObject.optString("state").toString()
                            val district = jsonObject.optString("district").toString()
                            val taluka = jsonObject.optString("taluka").toString()
                            val villageName = jsonObject.optString("village").toString()

                            Log.e("uniqueId", uniqueId)

                            val farmerapproved = jsonObject.getJSONObject("farmerapproved")
                            val farmer_name = farmerapproved.optString("farmer_name").toString()
                            var aadharNum :String?= farmerapproved.optString("aadhaar").toString()
                            var mobileNum :String?= farmerapproved.optString("mobile").toString()

                            if (aadharNum == null){
                                aadharNum = "N.A"
                            }
                            if (mobileNum == null){
                                mobileNum = "N.A"
                            }
                            val reject_reason = jsonObject.getJSONObject("reject_reason")
                            val reason_id = reject_reason.optString("id").toString()
                            val reasons = reject_reason.optString("reasons").toString()

                            val pipeinstallation = jsonObject.getJSONObject("pipeinstallation")
                            val area_in_acers = pipeinstallation.optString("area_in_acers").toString()

                            val pipeReportActivity = PipeReportModel(id, uniqueId, lat, farmer_plot_uniqueid, lng, plot_no, pipe_no, distance, farmer_name, reasons, reason_id, area_in_acers,state,district, taluka,villageName,aadharNum,mobileNum)
                            reportModel.add(pipeReportActivity)
                        }

// Checking if reportmodel size is greater than 10 or not
                        if(reportModel.size > 10){
                            val adapter = Pipe_Report_Adapter(reportModel.subList(0, 10))
                            pipe_recyclerView.adapter = adapter
                            pipe_recyclerView.layoutManager = LinearLayoutManager(this@PolygonReportActivity)

                            pipeReportAdapter.notifyDataSetChanged()
                        }
                        else{
                            pipeReportAdapter.notifyDataSetChanged()
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