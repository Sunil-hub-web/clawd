package com.pnpawd.userapp.cropintellix

import WeatherResponseModel
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.ProfileActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.SessionManager
import com.pnpawd.userapp.addmoreplots.FetchFarmerDetailsActivity
import com.pnpawd.userapp.awd.AerationActivity
import com.pnpawd.userapp.cropdata.CropActivity
import com.pnpawd.userapp.farmerbenefit.FarmerBenefitActivity
import com.pnpawd.userapp.farmeronboarding.StateActivity
import com.pnpawd.userapp.models.StateIdModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.network.WeatherApiClient
import com.pnpawd.userapp.pipeinstallation.PipeActivity
import com.pnpawd.userapp.polygon.PolygonActivity
import com.pnpawd.userapp.reports.ReportActivity
import com.pnpawd.userapp.weather.WeatherForecastActivity
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Double
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Throwable
import kotlin.apply
import kotlin.arrayOf
import kotlin.toString

class DashboardActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val appid = "18b90425aa5502f51a695dd903e3d993";
    var PERMISSION_ALL = 1
    private var Permissions: Array<String> = arrayOf<String>()
    var AcresList = ArrayList<String>()
    var update = ArrayList<String>()

    private lateinit var farmer_onboarding : RelativeLayout
    private lateinit var crop_info : RelativeLayout
    private lateinit var benefit : RelativeLayout
    private lateinit var reports: RelativeLayout
    private lateinit var addMorePlots: RelativeLayout
    private lateinit var ivPipeOne: RelativeLayout
    private lateinit var ivPipeTwo: RelativeLayout
    private lateinit var aeration: RelativeLayout
    private lateinit var weather: RelativeLayout
    private lateinit var profile: ImageView
    private lateinit var weatherImg: ImageView
    private lateinit var txtTemperature: TextView


    private var FarmerRegistration: Int = 0
    private var CropData: Int = 0
    private var PipeInstallation: Int = 0
    private var Polygon: Int = 0
    private var CaptureAeration: Int = 0
    private var FarmerBenefit: Int = 0
    private var end_of_date: Int = 0
    private var preparation_date_interval: Int = 0
    private var transplantation_date_interval: Int = 0
    private var current_latitude: kotlin.Double= 0.0
    private var current_longitude: kotlin.Double= 0.0
    private lateinit var language: String
    var token: String = ""
    var state_id: String = ""
    var iconUrl: String? = null
    var humidity: String = "NA"
    var windSpeed: String = "NA"
    var sunset: String = "NA"
    var description: String = ""

    private lateinit var locale: Locale
    private lateinit var progress: SweetAlertDialog

    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this@DashboardActivity)
        sessionManager.logoutUser()

        val bundle = intent.extras
        if (bundle != null) {
            language = bundle.getString("language").toString()
        } else {
            language = "en"
            Log.e("data", "No bundle data")
        }

// Check if Developer Option is on
        if (BuildConfig.DEBUG) {
            Log.e("CHECKKK", "in if Debug")
        } else {
            Log.e("CHECKKK", "in else Release")
            warnAboutDevOpt()
        }

        locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        setContentView(R.layout.activity_dashboard)

        progress = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        state_id = sharedPreference.getString("state_id","")!!



        farmer_onboarding = findViewById(R.id.farmer)
        benefit = findViewById(R.id.farmer_benefit)
        crop_info = findViewById(R.id.crop)
        profile = findViewById(R.id.profile)
        reports = findViewById(R.id.reports)
        addMorePlots = findViewById(R.id.addPlots)
        ivPipeOne = findViewById(R.id.ivPipe1)
        ivPipeTwo = findViewById(R.id.ivPipe2)
        aeration = findViewById(R.id.aeration)
        weather = findViewById(R.id.weather)
        weatherImg = findViewById(R.id.weather_img)
        txtTemperature = findViewById(R.id.current_temp)


        Permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (!hasPermissions(this, *Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, PERMISSION_ALL)
        }



        farmer_onboarding.setOnClickListener {
            if (FarmerRegistration == 1) {
                val intent = Intent(this, StateActivity::class.java)
                startActivity(intent)
            } else {
                val WarningDialog =
                    SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Farmern registration \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        crop_info.setOnClickListener {
            if (CropData == 1) {
                val intent = Intent(this, CropActivity::class.java).apply {
                    putExtra("total_sub_plots", 0)
                    putExtra("total_number", 0)
                    putStringArrayListExtra("plot_area", AcresList)
                    putStringArrayListExtra("farmer_plot_uniqueid", AcresList)
                    putStringArrayListExtra("plot_no", AcresList)
                    putStringArrayListExtra("plot_id", AcresList)
                    putStringArrayListExtra("awd_plot_area", AcresList)
                    putStringArrayListExtra("awd_acres_area", AcresList)
                    putStringArrayListExtra("update", update)
                    putExtra("farmer_name", "")
                    putExtra("mobile_number", "")
                    putExtra("unique_id", "")
                    putExtra("state", " ")
                    putExtra("state_id", " ")
                    putExtra("cropdata_end_days", end_of_date)
                    putExtra("preparation_date_interval", preparation_date_interval)
                    putExtra("transplantation_date_interval", transplantation_date_interval)
                    putExtra("transplantation_day", 0)
                    putExtra("transplantation_month", 0)
                    putExtra("transplantation_year", 0)
                }
                startActivity(intent)
            } else {
                val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Crop Data \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        benefit.setOnClickListener {
            if (FarmerBenefit == 1) {
                val intent = Intent(this, FarmerBenefitActivity::class.java).apply { }
                startActivity(intent)
            } else {
                val WarningDialog =
                    SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = " Farmer Benefits \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        profile.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        })

        reports.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            finish()
        })


        addMorePlots.setOnClickListener(View.OnClickListener{
            val intent = Intent(this,FetchFarmerDetailsActivity::class.java)
            startActivity(intent)
        })

        ivPipeOne.setOnClickListener{
            if (Polygon == 1) {
                val intent = Intent(this, PolygonActivity::class.java)
                startActivity(intent)
            }
            else{
                val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Polygon \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        ivPipeTwo.setOnClickListener{
            if (PipeInstallation == 1) {
                val intent = Intent(this, PipeActivity::class.java)
                startActivity(intent)
            }
            else{
                val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Pipe Installation \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        aeration.setOnClickListener {
            if (CaptureAeration == 1) {
                val intent = Intent(this, AerationActivity::class.java)
                startActivity(intent)
            } else {
                val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Capture Aeration Events \n Access Not Allowed"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        }

        weather.setOnClickListener{
            if(current_longitude == 0.0 && current_latitude == 0.0){
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = "Loading"
                progress.contentText = "Getting current location"
                progress.setCancelable(false)
                progress.show()

                getCurrentLocation()
            }
            else {
                val intent = Intent(this, WeatherForecastActivity::class.java).apply {
                    putExtra("latitude", current_latitude.toString())
                    putExtra("longitude", current_longitude.toString())
                    putExtra("current_temperature", txtTemperature.text.toString())
                    putExtra("description", description)
                    putExtra("icon", iconUrl)
                    putExtra("appId", appid)
                    putExtra("humidity", humidity)
                    putExtra("wind_speed", windSpeed)
                    putExtra("sunset", sunset)
                    putExtra("description", description)
                }
                startActivity(intent)
            }
        }


        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        Log.e("version", versionName + versionCode + release.toString())

        val deviceName = Build.MODEL // returns model name
        val deviceManufacturer = Build.MANUFACTURER // returns manufacturer
        Log.e("version", deviceName + deviceManufacturer)

        checkVersion(versionName, versionCode)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val active = isLocationEnabled(this@DashboardActivity)
        Log.e("active", active.toString())

        if (!active) {
            AlertDialog.Builder(this@DashboardActivity)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(
                    R.string.yes
                ) { paramDialogInterface, paramInt ->
                    this@DashboardActivity.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
        else{
            getCurrentLocation()
        }
    }

    //        Check if Developers Option is on
    private fun warnAboutDevOpt() {
        val adb = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if(adb == 1){
            AlertDialog.Builder(this@DashboardActivity)

                .setTitle("Developer Option Detected")
                .setMessage("Developer options are enabled on phone. Disable to continue using the app.")
                .setCancelable(false)
                .setNegativeButton(
                    R.string.close_app,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        finishAffinity()
                        finish()
                    })
                .setPositiveButton(R.string.clcik_disable,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        this@DashboardActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    })
                .show()


            val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

            WarningDialog.titleText = "Developer options are enabled on phone."
            WarningDialog.contentText = "Disable to continue using the app."
            WarningDialog.confirmText = resources.getString(R.string.disable)
            WarningDialog.cancelText = resources.getString(R.string.close_app)
            WarningDialog.setCancelable(false)
            WarningDialog.setConfirmClickListener {
                val data = this@DashboardActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                Log.e("data", data.equals(true).toString())

            }
            WarningDialog.setCancelClickListener {
                finishAffinity()
                finish()
            }.show()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    current_latitude = location.latitude
                    current_longitude = location.longitude
// Use latitude and longitude values
                    println("Latitude: $current_latitude, Longitude: $current_longitude")

                    getWeather(current_latitude, current_longitude)
                } else {
                    // Handle the case where location is null
                }
            }
            .addOnFailureListener { exception: Exception ->
                Log.e("Dashboard Location Error", exception.message.toString())
// Handle any errors that occurred while retrieving the location
            }
    }

    private fun getWeather(latitude: kotlin.Double, longitude: kotlin.Double) {
        val apiInterface = WeatherApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.weatherResponse(latitude.toString(), longitude.toString(), appid).enqueue(object : Callback<WeatherResponseModel> {
            @SuppressLint("SimpleDateFormat")
            override fun onResponse(call: Call<WeatherResponseModel>, response: Response<WeatherResponseModel>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val weatherData : WeatherResponseModel = response.body()!!

                        try {
                            val kelvin = weatherData.main.temp
                            val temperature = kelvin - 273.15
                            txtTemperature.text = DecimalFormat("##").format(temperature)

                            val icon = weatherData.weather[0].icon
                            description = weatherData.weather[0].description
                            icon.let {
                                iconUrl = "http://openweathermap.org/img/wn/$icon@4x.png"
                                Picasso.get().load(iconUrl).into(weatherImg)
                            }

                            humidity = "${weatherData.main.humidity} %"
                            windSpeed = "${weatherData.wind.speed} meter/sec"

                            val sun = weatherData.sys.sunset
                            val timestamp = sun * 1000 // Convert to milliseconds
                            val sdf = SimpleDateFormat("hh:mm a")
                            val date = Date(timestamp)
                            val formattedDate = sdf.format(date)
                            println("Readable date and time: $formattedDate")
                            sunset = formattedDate


                            progress.dismiss()
                        }
                        catch (e: Exception){
                            Log.e("weather_exception", e.toString())
                        }

                    }
                }
            }
            override fun onFailure(call: Call<WeatherResponseModel>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkVersion(versionName: String, versionCode: Int) {
        println("Checking app Version Current is $versionCode");
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = "Checking"
        progress.contentText = " Checking for new version"
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkVersion(versionName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        modulesAvailable()
                    }
                } else if (response.code() == 500) {
                    progress.dismiss()
                    println("Checking app Version Current is in 500 ${response.body()}");
                    if (response.errorBody() != null) {
                        val stringResponse = JSONObject(response.errorBody()!!.string())
                        val url = stringResponse.optString("url")
                        message(url)
                    }

//                    modulesAvailable()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun modulesAvailable() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val stateIdModel = StateIdModel(state_id)
        Log.e("state_id", state_id)

        apiInterface.moduleAccess(stateIdModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null) {

                        val jsonObject = JSONObject(response.body()!!.string())
                        FarmerRegistration = jsonObject.getInt("farmer_registration")
                        CropData = jsonObject.getInt("crop_data")
                        Polygon = jsonObject.getInt("polygon")
                        PipeInstallation = jsonObject.getInt("pipe_installation")
                        CaptureAeration = jsonObject.getInt("capture_aeration")
                        FarmerBenefit = jsonObject.getInt("farmer_benefit")

                        getDateIntervals()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    private fun hasPermissions(context: Context?, vararg PERMISSIONS: String): Boolean {
        if (context != null) {
            for (permissions in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permissions) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun getDateIntervals() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.dateInterval("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        val setting = jsonObject.optJSONObject("setting")

                        if (setting != null) {
                            preparation_date_interval = setting.optInt("preparation_date_interval")
                            transplantation_date_interval = setting.optInt("transplantation_date_interval")
                            end_of_date = setting.optInt("cropdata_end_days")
                        }

                        getFormCount()
                    }
                }
                else{
                    progress.dismiss()
                }

            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getFormCount() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.formCount("Bearer $token").enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())

                        val FarmersReject = stringResponse.optInt("FarmersReject")

                        val CropdataRejected = stringResponse.optInt("CropdataRejected")

                        val PipeRejected = stringResponse.optInt("reject_pipes")

                        val BenefitRejected = stringResponse.optInt("BenefitRejected")

                        val AerationRejected = stringResponse.optInt("reject_awd")

//                        if(FarmersReject != 0 || CropdataRejected != 0 || PipeRejected != 0 || BenefitRejected != 0 || AerationRejected != 0){
//                        if(FarmersReject != 0 || CropdataRejected != 0 || PipeRejected != 0 || BenefitRejected != 0 || AerationRejected != 0){
//                            reports.background = ResourcesCompat.getDrawable(resources,
//                                R.drawable.reports_error, null)
//                            marker.background = ResourcesCompat.getDrawable(resources,
//                                R.drawable.ic_warning, null)
//                        }
//                        else{
//                            reports.background = ResourcesCompat.getDrawable(resources,
//                                R.drawable.reports, null)
//                            marker.visibility = View.GONE
//                        }

                        progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }


    private fun message(url: String) {
        val WarningDialog = SweetAlertDialog(this@DashboardActivity, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = " Warning "
        WarningDialog.contentText = " Please download new app. "
        WarningDialog.confirmText = " Download Now "
        WarningDialog.showCancelButton(false)
        WarningDialog.setCancelable(false)
        WarningDialog.setConfirmClickListener {
            WarningDialog.cancel()

            val newAppUrl = Uri.parse(url)
            val browserIntent = Intent(Intent.ACTION_VIEW, newAppUrl)
            ContextCompat.startActivity(this, browserIntent, null)
        }.show()
    }

    override fun onBackPressed() {
        finishAffinity()
        finish()
    }
}