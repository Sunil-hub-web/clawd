package com.pnpawd.userapp.reports.benefit_report

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.Benefit_Report_Adapter
import com.pnpawd.userapp.models.BenefitReportModel
import com.pnpawd.userapp.models.StatusModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class BenefitReportActivity : AppCompatActivity() {
    var token: String = ""
    var status: String = ""
    var isScrolling: Boolean = true
    private var next_page_url: String? = null

    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    var benefitModel: ArrayList<BenefitReportModel> = ArrayList<BenefitReportModel>()
    private lateinit var benefitReportAdapter: Benefit_Report_Adapter
    private lateinit var benefit_recyclerView: RecyclerView

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benefit_report)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        status = bundle!!.getString("status").toString()

        back = findViewById(R.id.benefit_report_back)
        searchView = findViewById(R.id.benefit_report_search)
        progressBar = findViewById(R.id.benefit_progressBar)
        benefit_recyclerView = findViewById(R.id.benefit_report_recyclerView)


        benefitReportAdapter = Benefit_Report_Adapter(benefitModel)
        val layoutManager = LinearLayoutManager(this)
        benefit_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        benefit_recyclerView.adapter = benefitReportAdapter


        benefit_recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("scroll", "idle")
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    isScrolling = true
                    Log.d("scroll", "settling")
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Log.d("scroll", "dragging")
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("scroll", "scrolling")

                val currentItems = layoutManager.childCount
                val totalItems = layoutManager.itemCount
                val scrollOutItems = layoutManager.findFirstVisibleItemPosition()

                Log.d("scroll", dx.toString())
                Log.d("scroll", dy.toString())
                Log.d("scroll", currentItems.toString())
                Log.d("scroll", scrollOutItems.toString())
                Log.d("scroll", totalItems.toString())


                if (currentItems + scrollOutItems == totalItems){
//                    isScrolling = false
//                    Handler(Looper.getMainLooper()).postDelayed({
                    if (isScrolling) {
                        progressBar.visibility = View.VISIBLE
                        next_page_url?.let { paginationData(it) }
                    }
//                    }, 3000)
                }
            }
        })


        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                benefitModel.clear()
                benefitReportAdapter = Benefit_Report_Adapter(benefitModel)
                benefit_recyclerView.adapter = benefitReportAdapter
                benefitReportAdapter.notifyDataSetChanged()

                searchData(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {

                return true
            }
        })


        getData(token)
    }

    private fun paginationData(page_url: String) {
        isScrolling = false
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val statusModel = StatusModel(status)
        apiInterface.benefitReportPagination("Bearer $token", page_url, statusModel).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val jsonArray = jsonObject.optJSONArray("Benefits")

                        progressBar.visibility = View.GONE
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val total_plot_area = jsonObject.optString("total_plot_area").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val benefit = jsonObject.optString("benefit").toString()
                            val seasons = jsonObject.optString("seasons").toString()
                            Log.e("uniqueId", uniqueId)

                            val benefitReportModel = BenefitReportModel(total_plot_area, uniqueId, benefit, seasons)
                            benefitModel.add(benefitReportModel)
                            benefitReportAdapter.notifyDataSetChanged()
                        }
                        isScrolling = true
                    }
                }
                else{
                    progressBar.visibility = View.GONE
                    Log.e("status_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun getData(token: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val statusModel = StatusModel(status)
        apiInterface.benefitReportList("Bearer $token", statusModel).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val jsonArray = jsonObject.optJSONArray("Benefits")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val total_plot_area = jsonObject.optString("total_plot_area").toString()
                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()
                            val benefit = jsonObject.optString("benefit").toString()
                            val seasons = jsonObject.optString("seasons").toString()
                            Log.e("uniqueId", uniqueId)

                            val benefitReportModel = BenefitReportModel(total_plot_area, uniqueId, benefit, seasons)
                            benefitModel.add(benefitReportModel)
                            benefitReportAdapter.notifyDataSetChanged()
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


    private fun searchData(uniqueid: String){
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.benefitReportQuery("Bearer $token", uniqueid).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonArray = jsonObject.optJSONArray("Benefit")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val total_plot_area = jsonObject.optString("total_plot_area").toString()

                            val uniqueId = jsonObject.optString("farmer_uniqueId").toString()

                            val benefit = jsonObject.optString("benefit").toString()

                            val seasons = jsonObject.optString("seasons").toString()

                            Log.e("uniqueId", uniqueId)

                            val benefitReportModel = BenefitReportModel(total_plot_area, uniqueId, benefit, seasons)
                            benefitModel.add(benefitReportModel)
                            benefitReportAdapter.notifyDataSetChanged()
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