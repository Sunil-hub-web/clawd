package com.pnpawd.userapp.farmerbenefit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.BenefitCheckModel
import com.pnpawd.userapp.models.BenefitModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pnpawd.userapp.SessionManager
import com.pnpawd.userapp.ViewCameraDetail
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class FarmerBenefitActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private val locationPermissionCode = 2
    private lateinit var locationManager: LocationManager

    var IDList = ArrayList<Int>()
    var NameList = ArrayList<String>()
    var SeasonIDList = ArrayList<Int>()
    var SeasonNameList = ArrayList<String>()
    var BenefitIDList = ArrayList<Int>()
    var BenefitNameList = ArrayList<String>()
    var PlotIDList = ArrayList<String>()
    var arrayList = ArrayList<String>()

    val watermark: Common = Common()

    lateinit var edtMobile_number: EditText

    lateinit var plot_ID: Spinner
    lateinit var season_spinner: Spinner
    lateinit var benefit_spinner: Spinner

    lateinit var txtFarmer_name: TextView
    lateinit var sub_plot: TextView
    lateinit var firstLinear: LinearLayout


    lateinit var number_search: ImageView
    lateinit var benefit_image1: ImageView
    lateinit var benefit_image2: ImageView
    lateinit var cancel_image1: ImageView
    lateinit var cancel_image2: ImageView
    lateinit var search: ImageView

    lateinit var submit: Button
    lateinit var home: Button

    var imageFileName: String = ""
    private var image1: String = ""
    private var image2: String = ""
    private var image3: String = ""
    private var FarmerId: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var uniquePlotPosition: Int = 0
    private var seasonPosition: Int = 0
    private var benefitPosition: Int = 0
    private var subPlotPosition: Int = 0
    private var total: Double = 0.0
    var rotate = 0

    var token: String = ""
    private lateinit var progress: SweetAlertDialog

    private lateinit var currentPhotoPath: String
    private lateinit var photoPath: File
    private lateinit var uri: Uri

    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_benefit)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        sessionManager = SessionManager(this@FarmerBenefitActivity)

        progress = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.PROGRESS_TYPE)

        token = sharedPreference.getString("token","")!!

        edtMobile_number = findViewById(R.id.benefit_mobile_number)

        plot_ID = findViewById(R.id.benefit_plot_unique_id)
        sub_plot = findViewById(R.id.benefit_total_plots)
        season_spinner = findViewById(R.id.season)
        benefit_spinner = findViewById(R.id.benefit)
        search = findViewById(R.id.benefit_search)

        submit = findViewById(R.id.benefit_submit)
        home = findViewById(R.id.farmer_benefit_home)

        firstLinear = findViewById(R.id.first)

        txtFarmer_name = findViewById(R.id.benefit_farmer_name)

        number_search = findViewById(R.id.benefit_search)
        benefit_image1 = findViewById(R.id.benefit_camera_capture1)
        benefit_image2 = findViewById(R.id.benefit_camera_capture2)
        cancel_image1 = findViewById(R.id.benefit_cancel_image1)
        cancel_image2 = findViewById(R.id.benefit_cancel_image2)


        benefit_image1.isEnabled = false
        benefit_image2.isEnabled = false


        benefit_image1.setOnClickListener(View.OnClickListener {

            val intent = Intent(this@FarmerBenefitActivity, ViewCameraDetail::class.java)
            intent.putExtra("clickimage", "framerbenefit1")
            startActivity(intent)


//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (intent.resolveActivity(packageManager) != null) {
//                try {
//                    photoPath = createImageFile()
//                } catch (ex: IOException) {
//                }
//                // Continue only if the File was successfully created
//                if (photoPath != null) {
//                    uri = FileProvider.getUriForFile(
//                        this@FarmerBenefitActivity,
//                        BuildConfig.APPLICATION_ID.toString() + ".provider", photoPath
//                    )
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                    resultLauncher1.launch(intent)
//                }
//            }
        })

        benefit_image2.setOnClickListener(View.OnClickListener {

            val intent = Intent(this@FarmerBenefitActivity, ViewCameraDetail::class.java)
            intent.putExtra("clickimage", "framerbenefit2")
            startActivity(intent)

//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (intent.resolveActivity(packageManager) != null) {
//                try {
//                    photoPath = createImageFile()
//                } catch (ex: IOException) {
//                }
//                // Continue only if the File was successfully created
//                if (photoPath != null) {
//                    uri = FileProvider.getUriForFile(
//                        this@FarmerBenefitActivity,
//                        BuildConfig.APPLICATION_ID.toString() + ".provider", photoPath
//                    )
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                    resultLauncher2.launch(intent)
//                }
//            }
        })


        cancel_image1.setOnClickListener(View.OnClickListener {
            if (image1 != null) {
                benefit_image1.setImageBitmap(null)
                image1 = ""
            }
        })

        cancel_image2.setOnClickListener(View.OnClickListener {
            if (image2 != null) {
                benefit_image2.setImageBitmap(null)
                image2 = ""
            }
        })


        submit.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.WARNING_TYPE)

            if (image1.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.image1_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (txtFarmer_name.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.name_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (NameList.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.plot_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (uniquePlotPosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.plot_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (seasonPosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.season_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (benefitPosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.benefits_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else{
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                if (image2.isEmpty() && image3.isEmpty()){
                    arrayList.add(image1)
                }
                else if(image2.isNotEmpty() && image3.isEmpty()){
                    arrayList.add(image1)
                    arrayList.add(image2)
                }
                else if(image2.isEmpty() && image3.isNotEmpty()){
                    arrayList.add(image1)
                    arrayList.add(image3)
                }
                else {
                    arrayList.add(image1)
                    arrayList.add(image2)
                    arrayList.add(image3)
                }
                sendData()
            }
        })

        home.setOnClickListener(View.OnClickListener {
            homeScreen()
        })

//        edtMobile_number.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//                if (edtMobile_number.text.length == 10) {
//                    getPlotUniqueId(edtMobile_number.text.toString())
//                }
//            }
//        })


        search.setOnClickListener{
            if(edtMobile_number.text.isNotEmpty())
            {
                getPlotUniqueId(edtMobile_number.text.toString())
            }
        }


        number_search.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtMobile_number.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if(edtMobile_number.text.length < 10){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {
                getPlotUniqueId(edtMobile_number.text.toString())
            }
        })

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@FarmerBenefitActivity)

        gpsCheck()
        getSeason()
    }

    private fun gpsCheck() {
        val active = isLocationEnabled(this@FarmerBenefitActivity)
        Log.e("active", active.toString())

        if (active) {
//            getLocation()
            fetchLocation()
        }
        else{
            AlertDialog.Builder(this@FarmerBenefitActivity)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        this@FarmerBenefitActivity.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun upLoadSingleImage(arrayList: ArrayList<String>, s: String, FarmerBenefitId: String) {
        if (arrayList.isEmpty()) {
            nextScreen()
        } else {
            val FarmerUniqueID = NameList[uniquePlotPosition]
            val PLOTNO = PlotIDList[subPlotPosition]

            val currentUrl: String = arrayList[0]
            val file = File(currentUrl)

            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val farmer_id = FarmerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer_unique_id = FarmerUniqueID.toRequestBody("text/plain".toMediaTypeOrNull())
            val plot_no = PLOTNO.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer_benefit_id = FarmerBenefitId.toRequestBody("text/plain".toMediaTypeOrNull())

            val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val farmerID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmer_id)
            val farmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, farmer_unique_id)
            val plotNo: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, plot_no)
            val benefit_id: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_benefit_id", null, farmer_benefit_id)

            val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            retIn.benefitImageUpload("Bearer $token", farmerID, farmerUniqueID, plotNo, body, benefit_id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        arrayList.removeAt(0)
                    }
                    upLoadSingleImage(arrayList, token, FarmerBenefitId)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                    Log.e("message", t.message.toString())
                }
            })
        }
    }

    private fun sendData() {
        val FarmerUniqueID: String = NameList[uniquePlotPosition]
        val PLOTNO: String = PlotIDList[subPlotPosition]
        val SEASON: String = SeasonNameList[seasonPosition]
        val BENEFIT: String = BenefitNameList[benefitPosition]
        val TOTAL_PLOT_AREA = total.toString()
        val BENEFIT_ID: String = BenefitIDList[benefitPosition].toString()


        val benefitModel= BenefitModel(FarmerId, FarmerUniqueID, PLOTNO, SEASON, BENEFIT, TOTAL_PLOT_AREA, BENEFIT_ID)
        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.sendBenefitData("Bearer $token", benefitModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val jsonObject = JSONObject(response.body()!!.string())
                    val FarmerBenefitId = jsonObject.getString("farmer_benefit_id")

                    upLoadSingleImage(arrayList, "Bearer $token", FarmerBenefitId)
                }
                else if(response.code() == 422){
                    progress.dismiss()
                }
                else if (response.code() == 500) {
                    progress.dismiss()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getBenefit() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.benefits("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        BenefitIDList.add(0)
                        BenefitNameList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("benefits")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.optString("id").toInt()
                            val benefit = jsonObject.optString("name")

                            BenefitIDList.add(id)
                            BenefitNameList.add(benefit)
                        }
                        BenefitSpinner()
                    } else {
                        Log.e("statusCode", response.code().toString())
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun BenefitSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, BenefitNameList)
        benefit_spinner.adapter = adapter
        benefit_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                benefitPosition = position
                Log.e("benefit_spinner", benefit_spinner.toString())

                checkData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getSeason() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.seasons("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {

                    SeasonNameList.add("--Select--")

                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("seasons")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.optString("id").toInt()
                            val seasons = jsonObject.optString("name")

                            SeasonIDList.add(id)
                            SeasonNameList.add(seasons)
                        }
                        seasonSpinner()
                    } else {
                        Log.e("statusCode", response.code().toString())
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun seasonSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, SeasonNameList)
        season_spinner.adapter = adapter
        season_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                seasonPosition = position
                Log.e("season_spinner", season_spinner.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getPlotUniqueId(mobile: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        BenefitIDList.clear()
        BenefitNameList.clear()

        IDList.clear()
        NameList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotUniqueId("Bearer $token", mobile).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("list")

                    if (jsonArray.length() != 0) {
                        NameList.add("--Select--")
                        IDList.add(0)

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.optString("id").toInt()
                            val farmer_uniqueId = jsonObject.optString("farmer_uniqueId")

                            IDList.add(id)
                            NameList.add(farmer_uniqueId)

                        }
                        plotSpinner()
                        BenefitSpinner()

                        progress.dismiss()
                    } else {
                        IDList.clear()
                        NameList.clear()

                        plotSpinner()
                        sub_plot.text = ""
                        txtFarmer_name.text = ""

                        progress.dismiss()

                        val WarningDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = " No Data Found "
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {

                            WarningDialog.cancel()
                        }.show()
                    }
                }
                else if(response.code() == 403){
                    IDList.clear()
                    NameList.clear()

                    plotSpinner()
                    sub_plot.text = ""
                    txtFarmer_name.text = ""

                    Log.e("statusCode", response.code().toString())
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getPlotId(){
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()


        var plotUniqueID: String = IDList[uniquePlotPosition].toString()
        var plotUniqueIDName: String = NameList[uniquePlotPosition]

        total = 0.0

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.subPlotId("Bearer $token", plotUniqueIDName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                PlotIDList.clear()
                total = 0.0

                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("plotlist")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val plot_no = jsonObject.optString("plot_no")
                            val plot_area = jsonObject.optString("area_in_acers")

                            Log.e("plot_area", plot_area)

                            PlotIDList.add(plot_no)
                            total += plot_area.toDouble()
                        }
                        progress.dismiss()
                        subPlotIdSpinner(total)
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getFarmerName() {
        var plotUniqueID: String = IDList[uniquePlotPosition].toString()
        var plotUniqueIDName: String = NameList[uniquePlotPosition]
        var plotNumber: String = PlotIDList[subPlotPosition]

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.farmerPlotDetails("Bearer $token", plotUniqueID, plotUniqueIDName, plotNumber).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonObject1 = stringResponse.getJSONObject("benefitdetail")
                        FarmerId = jsonObject1.optString("farmer_id")

                        val jsonObject2 = stringResponse.getJSONObject("farmer")
                        val farmer_name = jsonObject2.optString("farmer_name")
                        txtFarmer_name.text = farmer_name

                        getBenefit()
                    }
                }
                else if(response.code() == 422){
                    val stringResponse = JSONObject(response.errorBody()!!.string())
                    val status = stringResponse.optInt("Status")

                    if(status == 1) {
                        val jsonObject = stringResponse.getJSONObject("farmer")

                        FarmerId = jsonObject.optString("id")
                        Log.e("farmer_id", FarmerId)

                        val farmer_name = jsonObject.optString("farmer_name")
                        txtFarmer_name.text = farmer_name

                        getBenefit()
                    }
                    else{
                        val WarningDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "Enter Crop Data First"
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {
                            WarningDialog.cancel()

                            homeScreen()
                        }.show()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()

            }
        })
    }

    private fun homeScreen() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun plotSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, NameList)
        plot_ID.adapter = adapter
        plot_ID.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                uniquePlotPosition = position
                Log.e("plot_ID", plot_ID.toString())

                BenefitIDList.clear()
                BenefitNameList.clear()

                if(position != 0){
                    benefit_image1.isEnabled = true
                    benefit_image2.isEnabled = true

                    getPlotId()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    @SuppressLint("SetTextI18n")
    private fun subPlotIdSpinner(total: Double) {
        sub_plot.text =   " ${Math.round(total * 100.0) / 100.00} Acers"
        getFarmerName()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

// Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        Log.i("imagepath", currentPhotoPath)
        return image
    }


    var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                    val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    val exif = ExifInterface(photoPath.absolutePath)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                    rotate = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        6 -> 90
                        8 -> -90
                        else -> 0
                    }
                    Log.e("rotate", rotate.toString())
                    benefit_image1.setImageBitmap(watermark.addWatermark(application.applicationContext, image, "#${NameList[uniquePlotPosition]} | B1 | $timeStamp | $latitude | $longitude"))
                    benefit_image1.rotation = rotate.toFloat()

                    try {
                        val draw = benefit_image1.drawable
                        val bitmap = draw.toBitmap()

                        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        val outFile = File(storageDir, "$imageFileName.jpg")
                        val outStream = FileOutputStream(outFile)

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                        outStream.flush()
                        outStream.close()

                        image1 = outFile.absolutePath

                    } catch (e: FileNotFoundException) {
                        Log.d("TAG", "Error Occurred" + e.message)
                        e.printStackTrace()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    var resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }
                Log.e("rotate", rotate.toString())
                benefit_image2.setImageBitmap(watermark.addWatermark(application.applicationContext, image, "#${NameList[uniquePlotPosition]} | B1 | $timeStamp | $latitude | $longitude"))
                benefit_image2.rotation = rotate.toFloat()

                try {
                    val draw = benefit_image2.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image2 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data submitted successfully. "
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

    private fun checkData() {
        val FarmerUniqueID: String = NameList[uniquePlotPosition]
        val BENEFIT_ID: String = BenefitIDList[benefitPosition].toString()

        val benefitCheckModel= BenefitCheckModel(FarmerUniqueID, BENEFIT_ID)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkBenefitData("Bearer $token", benefitCheckModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    submit.isEnabled = true
                    submit.isClickable = true
                    submit.setBackgroundDrawable(ContextCompat.getDrawable(this@FarmerBenefitActivity, R.drawable.green_bg))
                    Log.e("statusCode", response.code().toString())
                }
                else if( response.code() == 422){
                    submit.isEnabled = false
                    submit.isClickable = false
                    submit.setBackgroundDrawable(ContextCompat.getDrawable(this@FarmerBenefitActivity, R.drawable.grey_bg))

                    Log.e("benefit_provided", "Benefit_Provided")

                    val WarningDialog = SweetAlertDialog(this@FarmerBenefitActivity, SweetAlertDialog.WARNING_TYPE)

                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = " Benefit Already Provided "
                    WarningDialog.confirmText = " OK "
                    WarningDialog.showCancelButton(false)
                    WarningDialog.setCancelable(false)
                    WarningDialog.setConfirmClickListener {

                        WarningDialog.cancel()
                    }.show()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerBenefitActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }

        })
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        else if(requestCode == permissionCode){
            fetchLocation()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fetchLocation(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return false
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
//                Toast.makeText(applicationContext, currentLocation.latitude.toString() + " , " + currentLocation.longitude, Toast.LENGTH_SHORT).show()
                val supportMapFragment = supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment
                supportMapFragment.getMapAsync(this@FarmerBenefitActivity)
            }
        }
        return true
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

//        Toast.makeText(applicationContext,currentLocation.latitude.toString(),Toast.LENGTH_SHORT).show()
//        Toast.makeText(applicationContext,currentLocation.longitude.toString(),Toast.LENGTH_SHORT).show()

        val df = DecimalFormat("#.#####")

        latitude = df.format(currentLocation.latitude).toString()
        longitude = df.format(currentLocation.longitude).toString()

        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        googleMap.addMarker(markerOptions)
    }

//    private fun getLocation() {
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//
//        progress.progressHelper.barColor = Color.parseColor("#06c238")
//        progress.titleText = resources.getString(R.string.loading)
//        progress.contentText = resources.getString(R.string.location)
//        progress.setCancelable(false)
//        progress.show()
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
//    }

//    override fun onLocationChanged(p0: Location) {
//        progress.dismiss()
//
//        try {
//            val df = DecimalFormat("#.#####")
//
//            latitude = df.format(p0.latitude)
//            longitude = df.format(p0.longitude)
//        }
//        catch (e: Exception){
//            Log.e("location", "catch block")
//        }
//    }

    override fun onStart() {
        super.onStart()

        if (!sessionManager.getFARMERBENEFIT1().equals("DEFAULT")){
            image1 = sessionManager.getFARMERBENEFIT1().toString()
            val imgBitmap = BitmapFactory.decodeFile(sessionManager.getFARMERBENEFIT1())
            benefit_image1.setImageBitmap(imgBitmap)
        }

        if (!sessionManager.getFARMERBENEFIT2().equals("DEFAULT")){
            image2 = sessionManager.getFARMERBENEFIT2().toString()
            val imgBitmap = BitmapFactory.decodeFile(sessionManager.getFARMERBENEFIT2())
            benefit_image2.setImageBitmap(imgBitmap)
        }

    }

}