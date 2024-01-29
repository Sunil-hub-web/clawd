package com.pnpawd.userapp.reports.crop_report

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
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CropDetailsActivity : AppCompatActivity() {
    private var unique_id: String = ""
    private var token: String = ""

    private lateinit var txtUniqueID: TextView
    private lateinit var txtPlotNO: TextView
    private lateinit var txtArea: TextView
    private lateinit var txtSeason: TextView
    private lateinit var txtVariety: TextView
    private lateinit var txtIrrigationDate: TextView
    private lateinit var txtTransplantationDate: TextView
    private lateinit var txtPreparationDate: TextView
    private lateinit var txtSurveyID: TextView
    private lateinit var txtSurveyorName: TextView

    private  lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_details)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("uniqueId")!!
        } else {
            Log.e("unique_id", "Nope")
        }

        txtUniqueID = findViewById(R.id.crop_details_uniqueId)
        txtPlotNO = findViewById(R.id.crop_details_plot_no)
        txtArea = findViewById(R.id.crop_details_area_hector)
        txtSeason = findViewById(R.id.crop_details_season)
        txtVariety = findViewById(R.id.crop_details_survey_crop_variety)
        txtIrrigationDate = findViewById(R.id.crop_details_surveyor_date_irrigation)
        txtTransplantationDate = findViewById(R.id.crop_details_survey_transplanting_date)
        txtPreparationDate = findViewById(R.id.crop_details_surveyor_preparation_date)
        txtSurveyID = findViewById(R.id.crop_details_survey_number)
        txtSurveyorName = findViewById(R.id.crop_details_surveyor_name)

        back = findViewById(R.id.benefit_report_back)

        txtUniqueID.text = "Unique ID :${unique_id}"

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

        getData(unique_id)
    }

    private fun getData(uniqueId: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val farmerIDModel = FarmerIDModel(uniqueId)
        apiInterface.cropReportDetail("Bearer $token" ,farmerIDModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val crop_data = jsonObject.getJSONObject("CropData")

                        val plot_no = crop_data.optString("plot_no").toString()
                        val area_in_acers = crop_data.optString("area_in_acers").toString()
                        val seasons = crop_data.optString("season").toString()
                        val crop_variety = crop_data.optString("crop_variety").toString()
                        val dt_irrigation_last = crop_data.optString("dt_irrigation_last").toString()
                        val dt_transplanting = crop_data.optString("dt_transplanting").toString()
                        val dt_ploughing = crop_data.optString("dt_ploughing").toString()
                        val surveyor_id = crop_data.optString("surveyor_id").toString()
                        val surveyor_name = crop_data.optString("surveyor_name").toString()


                        txtPlotNO.text = plot_no
                        txtArea.text = area_in_acers
                        txtSeason.text = seasons
                        txtVariety.text = crop_variety
                        txtIrrigationDate.text = dt_irrigation_last
                        txtTransplantationDate.text = dt_transplanting
                        txtPreparationDate.text = dt_ploughing
                        txtSurveyID.text = surveyor_id
                        txtSurveyorName.text = surveyor_name
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
}