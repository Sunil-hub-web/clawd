package com.pnpawd.userapp.awd

import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AerationActivity : AppCompatActivity() {
    val aeratioNumber = arrayOf("--Select--", "1", "2")

    private lateinit var unique_Id_spinner: Spinner
    private lateinit var sub_plot_spinner: Spinner
    private lateinit var event_number_spinner: Spinner
    private lateinit var pipe_number: Spinner

    private lateinit var edtMobile: EditText
    private lateinit var txtName: TextView
    private lateinit var search: ImageView

    private lateinit var back: Button
    private lateinit var submit: Button

    private var token: String = ""
    private var uniqueID: String = ""
    private var subPlot: String = ""
    private var farmerId: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""
    private var farmerUniquePosition: Int = 0
    private var subPlotUniquePosition: Int = 0
    private var aerationPosition: Int = 0
    private var PipeNoPosition: Int = 0
    private var pipe_installation_id: Int = 0
    private var plot_no: Int = 0

    private var IDList = ArrayList<Int>()
    private var FarmerUniqueList = ArrayList<String>()
    private var SubPlotList = ArrayList<String>()
    private var FarmerNameList = ArrayList<String>()
    private var PipeList = ArrayList<String>()

    private lateinit var locationManager: LocationManager

    var arrayList = ArrayList<String>()

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeration)
        progress = SweetAlertDialog(this@AerationActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        unique_Id_spinner = findViewById(R.id.aeration_unique_id)
        sub_plot_spinner = findViewById(R.id.aeration_sub_plot)
        event_number_spinner = findViewById(R.id.aeration_event_number)

        edtMobile = findViewById(R.id.aeration_mobile)
        txtName = findViewById(R.id.aeration_farmer_name)
        pipe_number = findViewById(R.id.pipe_number)
        search = findViewById(R.id.aeration_search)


        back = findViewById(R.id.aeration_back)

        submit = findViewById(R.id.aeration_Submit)

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        submit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@AerationActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtMobile.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {

                    WarningDialog.cancel()
                }.show()
            }
            else if(farmerUniquePosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()
            }
            else if(subPlotUniquePosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.sub_plot_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()
            }
            else if(aerationPosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.aeration_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()
            }
            else if(PipeNoPosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.aeration_pipe_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()
            }
            else {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                checkData()
            }
        })

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, aeratioNumber)
        event_number_spinner.adapter = adapter
        event_number_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position !=0){
                aerationPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        search.setOnClickListener{
            if (edtMobile.text.isNotEmpty()) {
                getPlotUniqueId(edtMobile.text.toString())
            }
        }

//        edtMobile.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun afterTextChanged(p0: Editable?) {
//                if (edtMobile.text.length == 10) {
//                    getPlotUniqueId(edtMobile.text.toString())
//                }
//            }
//        })
    }

    private fun checkData() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.aeriationPloygonList("Bearer $token", SubPlotList[subPlotUniquePosition], PipeList[PipeNoPosition], aeratioNumber[aerationPosition])
            .enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    progress.dismiss()
                    val WarningDialog = SweetAlertDialog(this@AerationActivity, SweetAlertDialog.WARNING_TYPE)

                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.already_submitted)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener {
                        WarningDialog.cancel()
                    }.show()
                }
                else if(response.code() == 422){
                    nextScreen()
                }
                else{
                    Log.e("response.code", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, AerationMapActivity::class.java).apply {
            putExtra("farmer_plot_uniqueid", SubPlotList[subPlotUniquePosition])
            putExtra("unique_id", FarmerUniqueList[farmerUniquePosition])
            putExtra("aeriation", aeratioNumber[aerationPosition])
            putExtra("farmer_name", txtName.text.toString())
            putExtra("mobile_number", edtMobile.text.toString())
            putExtra("pipe_no", PipeList[PipeNoPosition])
            putExtra("pipe_installation_id", pipe_installation_id)
            putExtra("plot_no", plot_no)
        }
        startActivity(intent)
    }


    private fun getPlotUniqueId(mobileNumber: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        IDList.clear()
        FarmerNameList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotUniqueId("Bearer $token", mobileNumber).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        IDList.add(0)
                        FarmerUniqueList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("list")

                        if (jsonArray.length() == 0) {
                            Log.e("length", jsonArray.length().toString())

                            IDList.clear()
                            FarmerUniqueList.clear()

                            progress.dismiss()
                            val WarningDialog = SweetAlertDialog(this@AerationActivity, SweetAlertDialog.WARNING_TYPE)

                            WarningDialog.titleText = resources.getString(R.string.warning)
                            WarningDialog.contentText = "Mobile number\nhas no data."
                            WarningDialog.confirmText = " OK "
                            WarningDialog.showCancelButton(false)
                            WarningDialog.setCancelable(false)
                            WarningDialog.setConfirmClickListener {
                                WarningDialog.cancel()
                            }.show()
                        }
                        else if (jsonArray.length() > 0){
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val id = jsonObject.optString("id").toInt()
                                val farmer_uniqueId = jsonObject.optString("farmer_uniqueId")

                                IDList.add(id)
                                FarmerUniqueList.add(farmer_uniqueId)
                            }

                            farmerUniqueIdSpinner()
                            progress.dismiss()
                        }
                    }
                    else {
                        progress.dismiss()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getPlots() {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val plotUniqueIDName: String = FarmerUniqueList[farmerUniquePosition]

        SubPlotList.clear()
        FarmerNameList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.aeriationPltList("Bearer $token", plotUniqueIDName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("plot_unique_id")

                        SubPlotList.add("--Select--")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid")
                            val farmer_name = jsonObject.optString("farmer_name")

                            SubPlotList.add(farmer_plot_uniqueid.toString())
                            FarmerNameList.add(farmer_name.toString())
                        }
                        progress.dismiss()
                        plotSpinner()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    private fun farmerUniqueIdSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerUniqueList)
        unique_Id_spinner.adapter = adapter
        unique_Id_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    farmerUniquePosition = position
                    uniqueID = FarmerUniqueList[farmerUniquePosition]

                    getPlots()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun plotSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, SubPlotList)
        sub_plot_spinner.adapter = adapter
        sub_plot_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                subPlotUniquePosition = position
                subPlot = SubPlotList[subPlotUniquePosition]

                txtName.text = ""
                if(position != 0){
                    displayFarmerName(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displayFarmerName(position: Int) {
        txtName.text = FarmerNameList[position-1]

        PipeList.clear()
        if(position != 0){
            Log.e("position", "I'm here......")
            getPipeNumbers()
        }
    }

    private fun getPipeNumbers() {
        val farmer_plot_uniqueid: String = SubPlotList[subPlotUniquePosition]

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.aeriationPipeNumber("Bearer $token", farmer_plot_uniqueid).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        pipe_installation_id = stringResponse.optInt("pipe_installation_id")
                        plot_no = stringResponse.optInt("plot_no")


                        PipeList.add("--Select--")
                        val jsonArray = stringResponse.optJSONArray("PipeList")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val pipe_no = jsonObject.optString("pipe_no")

                            PipeList.add(pipe_no.toString())
                        }
                        progress.dismiss()
                        pipeNoSpinner()
                    }
                }
                else if(response.code() == 422){
                    backScreen()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun pipeNoSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, PipeList)
        pipe_number.adapter = adapter
        pipe_number.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                PipeNoPosition = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun backScreen() {
        val WarningDialog = SweetAlertDialog(this@AerationActivity, SweetAlertDialog.WARNING_TYPE)
        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.fill_pipe)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener {
            super.onBackPressed()
            finish()
        }.show()
    }
}