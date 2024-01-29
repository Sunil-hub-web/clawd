package com.pnpawd.userapp.reports.farmer_report

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.OnBoardingDetailsAdapter
import com.pnpawd.userapp.models.FarmerIDModel
import com.pnpawd.userapp.models.OnBoardingDetailsModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class OnBoardingDetailsActivity : AppCompatActivity() {
    var model: ArrayList<OnBoardingDetailsModel> = ArrayList<OnBoardingDetailsModel>()
    private lateinit var onBoardingDetailsAdapter: OnBoardingDetailsAdapter
    private lateinit var onBoarding_recyclerView: LinearLayout

    var imageArray: ArrayList<String> = ArrayList()
    private lateinit var carouselView: CarouselView

    private var unique_id: String = ""
    private var token: String = ""
    private var base_vale: Double = 0.0
    private var count: Int = 0

    private lateinit var linearList: LinearLayout
    private lateinit var txtUniqueID: TextView
    private lateinit var txtFarmerName: TextView
    private lateinit var txtPlotNo: TextView
    private lateinit var txtTotalArea: TextView
    private lateinit var txtAccess: TextView
    private lateinit var txtRelationship: TextView
    private lateinit var txtMobile: TextView
    private lateinit var txtState: TextView
    private lateinit var txtDistrict: TextView
    private lateinit var txtTaluka: TextView
    private lateinit var txtPanchayat: TextView
    private lateinit var txtVillage: TextView
    private lateinit var txtLatitude: TextView
    private lateinit var txtLongitude: TextView

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding_details)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("uniqueId")!!
        } else {
            Log.e("unique_id", "Nope")
        }

        linearList = findViewById(R.id.layout_data_list)
        txtUniqueID = findViewById(R.id.onBoarding_details_uniqueId)
        txtFarmerName = findViewById(R.id.onBoarding_details_farmer_name)
        txtPlotNo  = findViewById(R.id.onBoarding_details_plot)
        txtTotalArea = findViewById(R.id.onBoarding_details_total_plots)
        txtAccess = findViewById(R.id.onBoarding_details_mobile_access)
        txtRelationship = findViewById(R.id.onBoarding_details_relationship)
        txtMobile = findViewById(R.id.onBoarding_details_mobile)
        txtState = findViewById(R.id.onBoarding_details_state)
        txtDistrict = findViewById(R.id.onBoarding_details_district)
        txtTaluka = findViewById(R.id.onBoarding_details_taluka)
        txtPanchayat = findViewById(R.id.onBoarding_details_panchayat)
        txtVillage = findViewById(R.id.onBoarding_details_village)
        txtLatitude = findViewById(R.id.onBoarding_details_latitude)
        txtLongitude = findViewById(R.id.onBoarding_details_longitude)

        back = findViewById(R.id.onBoarding_report_back)
        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

        carouselView = findViewById(R.id.carouselView)
        imageArray.add("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_960_720.jpg")
        carouselView.pageCount = imageArray.size
        carouselView.setImageListener(imageListener)


//        onBoarding_recyclerView = findViewById(R.id.onBoarding_details_recyclerView)
//        onBoardingDetailsAdapter = OnBoardingDetailsAdapter(model)
//        val layoutManager = LinearLayoutManager(this)
//        onBoarding_recyclerView.layoutManager = layoutManager
//        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        onBoarding_recyclerView.adapter = onBoardingDetailsAdapter

        txtUniqueID.text = "Unique ID :${unique_id}"

        getData(unique_id)
    }

    private fun getData(uniqueId: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val farmerIDModel = FarmerIDModel(uniqueId)
        apiInterface.onBoardingReportDetail("Bearer $token" ,farmerIDModel).enqueue(object :
            Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val onBoarding_data = jsonObject.getJSONObject("Farmers")

                        val farmer_name = onBoarding_data.optString("farmer_name").toString()
                        val no_of_plots = onBoarding_data.optString("no_of_plots").toString()
                        val total_plot_area = onBoarding_data.optString("total_plot_area").toString()
                        val mobile_access = onBoarding_data.optString("mobile_access").toString()
                        val mobile_reln_owner = onBoarding_data.optString("mobile_reln_owner").toString()
                        val mobile = onBoarding_data.optString("mobile").toString()
                        val state = onBoarding_data.optString("state").toString()
                        val district = onBoarding_data.optString("district").toString()
                        val taluka = onBoarding_data.optString("taluka").toString()
                        val panchayat = onBoarding_data.optString("panchayat").toString()
                        val village = onBoarding_data.optString("village").toString()
                        val latitude = onBoarding_data.optString("latitude").toString()
                        val longitude = onBoarding_data.optString("longitude").toString()

                        val baseValueObj = jsonObject.getJSONObject("Basevalue")
                        base_vale = baseValueObj.optDouble("value")

                        val jsonArray = onBoarding_data.optJSONArray("farmer_plot")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val plot_no = jsonObject.optString("plot_no").toString()
                            val area_in_acers = jsonObject.optString("area_in_acers").toString()
                            val land_ownership = jsonObject.optString("land_ownership").toString()
                            val actual_owner_name = jsonObject.optString("actual_owner_name").toString()
                            val survey_no = jsonObject.optString("survey_no").toString()
                            val status = jsonObject.optString("status").toString()

                            Log.e("uniqueId", uniqueId)

                            val onBoardingDetailsModel = OnBoardingDetailsModel(plot_no, area_in_acers, land_ownership, actual_owner_name, survey_no, status, unique_id, base_vale)
                            model.add(onBoardingDetailsModel)
//                            onBoardingDetailsAdapter.notifyDataSetChanged()
                        }

                        imageArray.clear()
                        val plotImgArray = jsonObject.optJSONArray("FarmerPlotImg")
                        for (i in 0 until plotImgArray.length()) {
                            val jsonObject = plotImgArray.getJSONObject(i)

                            val imageList = jsonObject.optString("path").toString()
                            imageArray.add(imageList)
                        }

                        txtFarmerName.text = farmer_name
                        txtPlotNo.text = no_of_plots
                        txtTotalArea.text = total_plot_area
                        txtAccess.text = mobile_access
                        txtRelationship.text = mobile_reln_owner
                        txtMobile.text = mobile
                        txtState.text = state
                        txtDistrict.text = district
                        txtTaluka.text = taluka
                        txtPanchayat.text = panchayat
                        txtVillage.text = village
                        txtLatitude.text = latitude
                        txtLongitude.text = longitude

                        displayData(model, model.size)
                        carouselDisplay(model)
                    }
                }
                else{
                    Log.e("status_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    private fun carouselDisplay(model: ArrayList<OnBoardingDetailsModel>) {
        carouselView.pageCount = imageArray.size
        carouselView.setImageListener(imageListener)
    }

    private fun displayData(model: ArrayList<OnBoardingDetailsModel>, size: Int) {
        if (size > 0){
            Log.e("status_code", size.toString())
            val detailsView = layoutInflater.inflate(R.layout.onboarding_details_row, null, false)

            val layoutLinear = detailsView.findViewById<View>(R.id.layoutLinear) as LinearLayout
            val edit = detailsView.findViewById<View>(R.id.ivEdit) as ImageView
            val plotNumber = detailsView.findViewById<View>(R.id.details_plot_number) as TextView
            val areaHector = detailsView.findViewById<View>(R.id.details_area_hector) as TextView
            val ownership = detailsView.findViewById<View>(R.id.details_ownership) as TextView
            val ownerName = detailsView.findViewById<View>(R.id.details_owner_name) as TextView
            val SurveyNumber = detailsView.findViewById<View>(R.id.details_survey_number) as TextView

            if (model[count].getStatus() == "Rejected"){
                edit.isVisible = true
                layoutLinear.setBackgroundColor(Color.RED)
            }
            else if (model[count].getStatus() == "Pending"){
                layoutLinear.setBackgroundColor(Color.BLUE)
            }
            else if (model[count].getStatus() == "Approved"){
                layoutLinear.setBackgroundColor(Color.GREEN)
            }

            plotNumber.text = model[count].getPlotNo()
            areaHector.text = model[count].getArea()
            ownership.text = model[count].getOwnership()
            ownerName.text = model[count].getOwnerName()
            SurveyNumber.text = model[count].getSurveyNo()

            edit.setOnClickListener(View.OnClickListener {
                val intent = Intent(this@OnBoardingDetailsActivity,OnBoardingDetailsStatusActivity::class.java).apply {
                    putExtra("unique_id", model[plotNumber.text.toString().toInt()-1].getUniqueID())
                    putExtra("plot_no", plotNumber.text.toString())
                    putExtra("base_value", model[plotNumber.text.toString().toInt()-1].getBaseValue())
                }
                startActivity(intent)
            })

            linearList.addView(detailsView)
            count += 1
            displayData(model, size - 1)
        }
        else{

        }
    }

    var imageListener: ImageListener = ImageListener { position, imageView -> // You can use Glide or Picasso here
        Picasso.get().load(imageArray[position]).into(imageView)
    }
}