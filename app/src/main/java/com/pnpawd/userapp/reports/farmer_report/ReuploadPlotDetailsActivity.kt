package com.pnpawd.userapp.reports.farmer_report

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.AreaReUploadModel
import com.pnpawd.userapp.models.NameReUploadModel
import com.pnpawd.userapp.models.SurveyNumberReUploadModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReuploadPlotDetailsActivity : AppCompatActivity() {
    private lateinit var unique_id: String
    private lateinit var plot_no: String
    private lateinit var reason_id: String
    private var token: String = ""
    private var base_value: Double = 0.0

    private lateinit var progress: SweetAlertDialog

    private lateinit var txtUniqueID: TextView
    private lateinit var txtPlot_No: TextView
    private lateinit var edtName: EditText
    private lateinit var edtArea: EditText
    private lateinit var edtSurveyNumber: EditText

    private lateinit var btnSubmit: Button

    private lateinit var backBtn : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reupload_plot_details)
        progress = SweetAlertDialog(this@ReuploadPlotDetailsActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("unique_id")!!
            plot_no = bundle.getString("plot_no")!!
            reason_id = bundle.getString("reason_id")!!
            base_value = bundle.getDouble("base_value")
        } else {
            Log.e("unique_id", "Nope")
        }


        txtUniqueID = findViewById(R.id.re_upload_unique_id)
        edtName = findViewById(R.id.owner_name_reupload)
        txtPlot_No = findViewById(R.id.re_upload_plot_number)
        edtArea = findViewById(R.id.area_hector_reupload)
        edtSurveyNumber = findViewById(R.id.survey_number_reupload)

        if(reason_id == "3"){
            edtArea.isEnabled = false
            edtName.isEnabled = false
        }
        else if(reason_id == "4"){
            edtSurveyNumber.isEnabled = false
            edtName.isEnabled = false
        }
        else if(reason_id == "5"){
            edtSurveyNumber.isEnabled = false
            edtArea.isEnabled = false
        }

        btnSubmit = findViewById(R.id.re_upload_submit)

        backBtn = findViewById(R.id.reupload_plotDetails_back)
        backBtn.setOnClickListener { onBackPressed() }

        edtArea.filters = arrayOf(
            com.pnpawd.userapp.utils.DecimalDigitsInputFilter(
                2,
                2,
                99.99
            )
        )

        txtPlot_No.text = plot_no
        getRejectedReason(unique_id, plot_no)


        btnSubmit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@ReuploadPlotDetailsActivity, SweetAlertDialog.WARNING_TYPE)

            if(edtSurveyNumber.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.survey_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if(edtName.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.owner_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if(edtArea.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.plot_area_Ha)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()


                if(reason_id == "3"){
                    sendNumberData(unique_id, plot_no, reason_id, edtSurveyNumber.text.toString())
                }
                else if(reason_id == "4"){
                    sendAreaData(unique_id, plot_no, reason_id, edtArea.text.toString())
                }
                else if(reason_id == "5"){
                    sendNameData(unique_id, plot_no, reason_id, edtName.text.toString())
                }
            }
        })
    }

    private fun sendNameData(uniqueId: String, plotNo: String, reasonId: String, text: String) {
        val nameReUploadModel= NameReUploadModel(uniqueId, plotNo, reasonId, text)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.submitNameRejectReason("Bearer $token", nameReUploadModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null){
                        nextScreen()
                    }
                }
                else{
                    Log.e("response_code", response.code().toString())
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }

    private fun sendAreaData(uniqueId: String, plotNo: String, reasonId: String, text: String) {
        val WarningDialog = SweetAlertDialog(this@ReuploadPlotDetailsActivity, SweetAlertDialog.WARNING_TYPE)

        if (edtArea.text.toString().toDouble() < base_value){
            progress.dismiss()

            WarningDialog.titleText = resources.getString(R.string.warning)
            WarningDialog.contentText = "Area in Acres cannot be less than ${base_value.toString()}"
            WarningDialog.confirmText = resources.getString(R.string.ok)
            WarningDialog.setCancelClickListener {

                WarningDialog.cancel()
            }.show()
        }
        else{
            val areaReUploadModel= AreaReUploadModel(uniqueId, plotNo, reasonId, text)
            val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

            apiInterface.submitAreaRejectReason("Bearer $token", areaReUploadModel).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.code() == 200){
                        if (response.body() != null){
                            nextScreen()
                        }
                    }
                    else{
                        Log.e("response_code", response.code().toString())
                        progress.dismiss()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progress.dismiss()
                }
            })
        }
    }

    private fun sendNumberData(uniqueId: String, plotNo: String, reasonId: String, text: String) {
        val reuploadModel = SurveyNumberReUploadModel(uniqueId, plotNo, reasonId, text)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.submitNumberRejectReason("Bearer $token", reuploadModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null){
                        nextScreen()
                    }
                }
                else{
                    Log.e("response_code", response.code().toString())
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }

    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@ReuploadPlotDetailsActivity, SweetAlertDialog.SUCCESS_TYPE)
        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Plot Details Updated Successfully. "
        SuccessDialog.confirmText = " OK "
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener {

            SuccessDialog.cancel()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }.show()
    }


    private fun getRejectedReason(unique_id: String, plot_no: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.rejectReason("Bearer $token" , unique_id, plot_no).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonObject1 = jsonObject.optJSONObject("plot")
                        val farmer_uniqueId = jsonObject1.optString("farmer_uniqueId")
                        val actual_owner_name = jsonObject1.optString("actual_owner_name")
                        val area_in_acers = jsonObject1.optString("area_in_acers")
                        val survey_no = jsonObject1.optString("survey_no")

                        txtUniqueID.text = farmer_uniqueId
                        edtName.setText(actual_owner_name)
                        edtArea.setText(area_in_acers)
                        edtSurveyNumber.setText(survey_no)

                        val jsonObject2 = jsonObject1.optJSONObject("reasons")
                        reason_id=  jsonObject2.optString("id")

                        if(reason_id == "3"){
                            edtArea.isEnabled = false
                            edtName.isEnabled = false
                        }
                        else if(reason_id == "4"){
                            edtSurveyNumber.isEnabled = false
                            edtName.isEnabled = false
                        }
                        else if(reason_id == "5"){
                            edtSurveyNumber.isEnabled = false
                            edtArea.isEnabled = false
                        }
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