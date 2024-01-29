package com.pnpawd.userapp.reports.benefit_report

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.FarmerIDModel
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

class BenefitDetailActivity : AppCompatActivity() {
    private var unique_id: String = ""
    private var token: String = ""

    var imageArray: ArrayList<String> = ArrayList()
    private lateinit var carouselView: CarouselView

    private lateinit var txtUniqueID: TextView
    private lateinit var txtTotalArea: TextView
    private lateinit var txtSeason: TextView
    private lateinit var txtBenefit: TextView
    private lateinit var txtSurveyID: TextView
    private lateinit var txtSurveyorName: TextView

    private  lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benefit_detail)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("uniqueId")!!
        } else {
            Log.e("unique_id", "Nope")
        }

        txtUniqueID = findViewById(R.id.benefit_details_uniqueId)
        txtTotalArea = findViewById(R.id.benefit_details_plot_area)
        txtSeason = findViewById(R.id.benefit_details_season)
        txtBenefit = findViewById(R.id.benefit_details_benefits)
        txtSurveyID = findViewById(R.id.benefit_details_survey_number)
        txtSurveyorName = findViewById(R.id.benefit_details_surveyor_name)

        back = findViewById(R.id.benefit_report_back)

        carouselView = findViewById(R.id.benefit_carouselView)
        imageArray.add("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_960_720.jpg")
        carouselView.pageCount = imageArray.size
        carouselView.setImageListener(imageListener)

        txtUniqueID.text = "Unique ID :${unique_id}"

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

        getData(unique_id)
    }

    private fun getData(uniqueId: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val farmerIDModel = FarmerIDModel(uniqueId)
        apiInterface.benefitReportDetail("Bearer $token" ,farmerIDModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val crop_data = jsonObject.getJSONObject("CropData")

                        val total_plot_area = crop_data.optString("total_plot_area").toString()
                        val seasons = crop_data.optString("seasons").toString()
                        val benefit = crop_data.optString("benefit").toString()
                        val surveyor_id = crop_data.optString("surveyor_id").toString()
                        val surveyor_name = crop_data.optString("surveyor_name").toString()

                        txtTotalArea.text = total_plot_area
                        txtSeason.text = seasons
                        txtBenefit.text = benefit
                        txtSurveyID.text = surveyor_id
                        txtSurveyorName.text = surveyor_name

                        imageArray.clear()
                        val plotImgArray = jsonObject.optJSONArray("BenefitImage")
                        for (i in 0 until plotImgArray.length()) {
                            val jsonObject = plotImgArray.getJSONObject(i)

                            val imageList = jsonObject.optString("path").toString()
                            imageArray.add(imageList)
                        }

                        carouselDisplay()
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

    private fun carouselDisplay() {
        carouselView.pageCount = imageArray.size
        carouselView.setImageListener(imageListener)
    }

    var imageListener: ImageListener = ImageListener { position, imageView -> // You can use Glide or Picasso here
        Picasso.get().load(imageArray[position]).into(imageView)
    }
}