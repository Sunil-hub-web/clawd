package com.pnpawd.userapp.farmeronboarding

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.FarmerLocationModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.content.DialogInterface


import android.location.LocationManager
import android.os.CountDownTimer
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.models.DistrictModel
import java.text.DecimalFormat

class LocationActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    lateinit var txtAddress: EditText
    lateinit var txtLatitude: TextView
    lateinit var txtLongitude: TextView

    lateinit var state_spinner: Spinner
    lateinit var district_spinner: Spinner
    lateinit var taluka_spinner: Spinner
    lateinit var village_spinner: Spinner
    lateinit var panchayat_spinner: Spinner

    var plotAreaList = ArrayList<String>()
    var leasedList = ArrayList<String>()
    var plotList = ArrayList<String>()
    private var total_plot: Int = 0
    private var unique_id: String = ""
    private var COUNTRY: String = "India"
    private var statePosition: Int = 0
    private var districtPosition: Int = 0
    private var talukaPosition: Int = 0
    private var panchayatPosition: Int = 0
    private var villagePosition: Int = 0

    var stateIDList = ArrayList<Int>()
    var stateNameList = ArrayList<String>()

    var districtIDList = ArrayList<Int>()
    var districtNameList = ArrayList<String>()

    var talukaIDList = ArrayList<Int>()
    var talukaNameList = ArrayList<String>()

    var panchayatIDList = ArrayList<Int>()
    var panchayatNameList = ArrayList<String>()

    var villageIDList = ArrayList<Int>()
    var villageNameList = ArrayList<String>()

    var token: String = ""
    var userId: String = ""
    var farmerId: String = ""
    var farmerUniqueId: String = ""
    var state_ID: String = ""
    var selectedState: String = ""
    var unit: String = ""
    var areaValue: Double = 0.0
    private lateinit var progress: SweetAlertDialog
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        progress = SweetAlertDialog(this@LocationActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putBoolean("Leased", false)
        editor.apply()

        stateIDList.add(0)
        stateNameList.add("--Select--")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        /**
         * Getting some data's from previous screen.
         */

        val bundle = intent.extras
        if (bundle != null) {
            total_plot = bundle.getInt("total_plot")
            unit = bundle.getString("area_unit")!!
            areaValue = bundle.getDouble("area_value")
            plotList = bundle.getStringArrayList("areaHectare")!!
            plotAreaList = bundle.getStringArrayList("areaAcres")!!
            unique_id = bundle.getString("unique_id")!!
            farmerId = bundle.getString("FarmerId")!!

            state_ID = bundle.getString("state_id")!!
            selectedState = bundle.getString("state")!!

            stateIDList.add(state_ID.toInt())
            stateNameList.add(selectedState)
        }
        else{
            Log.e("total_plot", "Nope")
        }


        state_spinner = findViewById(R.id.state)
        district_spinner = findViewById(R.id.district)
        taluka_spinner = findViewById(R.id.taluka)
        village_spinner = findViewById(R.id.village)
        panchayat_spinner = findViewById(R.id.panchayat)

        val locationIcon = findViewById<LinearLayout>(R.id.location_icon)

        val next = findViewById<Button>(R.id.location_Next)
        val back = findViewById<Button>(R.id.location_back)

        txtAddress = findViewById(R.id.remark)
        txtLatitude = findViewById(R.id.latitude)
        txtLongitude = findViewById(R.id.longitude)
// Remove before giving to client.
//        txtLatitude.text = "29.97689"
//        txtLongitude.text = "31.13420"

        /**
         * Getting token & userId from shared preference.
        */
        token = sharedPreference.getString("token","")!!
        userId = sharedPreference.getString("user_id","")!!

        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })


        // Check if Developer Option is on
        if (BuildConfig.DEBUG) {
            Log.e("CHECKKK", "in if Debug")
        } else {
            Log.e("CHECKKK", "in else Release")
            warnAboutDevOpt()
        }


        /**
         * Assign the adapter to statw spinner.
         * Also if index is not 0 the calling the district api.
        */
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stateNameList)
        state_spinner.adapter = adapter
        state_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                statePosition = position
                Log.e("statePosition", statePosition.toString())

               if (position != 0) {
                   districtAPI()
               }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        /**
         * Checking if all required data are present or not after clicking next button.
        */
        next.setOnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@LocationActivity, SweetAlertDialog.WARNING_TYPE)

            if (txtLatitude.text.isEmpty() && txtLongitude.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.location_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (stateNameList.isEmpty() || statePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.state_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (districtNameList.isEmpty() || districtPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.district_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (talukaNameList.isEmpty() || talukaPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.taluka_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (panchayatNameList.isEmpty() || panchayatPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.panchayat_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (villageNameList.isEmpty() || villagePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.village_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData()
            }
        }

        /**
         *  Getting the current location of the uer.
         */

        locationIcon.setOnClickListener {
            val active = isLocationEnabled(this@LocationActivity)
            Log.e("active", active.toString())

            if (active) {
                loaderTimer()
                getLocation()
            } else {
                AlertDialog.Builder(this@LocationActivity)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.yes,
                        DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                            this@LocationActivity.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        })
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        }
    }

    private fun warnAboutDevOpt() {

        val adb = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if(adb == 1) {
            AlertDialog.Builder(this@LocationActivity)
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
                        this@LocationActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    })
                .show()
        }
    }

    /**
     * Sending all the data through API
     */

    private fun sendData() {

        val remarks = txtAddress.text.toString().trim()
        val latitude = txtLatitude.text.toString().trim()
        val longitude = txtLongitude.text.toString().trim()

        val state = stateIDList[statePosition].toString()
        val district = districtIDList[districtPosition].toString()
        val taluka = talukaIDList[talukaPosition].toString()
        val panchayat = panchayatIDList[panchayatPosition].toString()
        val village = villageIDList[villagePosition].toString()

        val country = COUNTRY

        val farmerLocationModel = FarmerLocationModel(farmerId, unique_id, country, state, district, taluka, panchayat, village, remarks, latitude, longitude)

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.farmerLocation("Bearer $token", farmerLocationModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val jsonObject = JSONObject(response.body()!!.string())
                    farmerId = jsonObject.getString("farmerId")
                    farmerUniqueId = jsonObject.getString("farmerUniqueId")

                    nextScreen()

                } else if (response.code() == 500) {
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun loaderTimer() {
        object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopLoader()
            }
        }.start()
    }

    private fun stopLoader() {
        progress.dismiss()
    }

    /**
     * Getting the device current location
     */


    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.location)
        progress.setCancelable(false)
        progress.show()

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

        getLocation1()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation1() {
        if (isLocationEnabled(this@LocationActivity)) {
            progress.dismiss()
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val list: List<Address> =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)!!

                    txtLatitude.text = list[0].latitude.toString()
                    txtLongitude.text = list[0].longitude.toString()
                }
            }
        } else {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     *
     */
    override fun onLocationChanged(p0: Location) {
        progress.dismiss()

//        try {
//            val df = DecimalFormat("#.#####")
//
//            txtLatitude.text = df.format(p0.latitude)
//            txtLongitude.text = df.format(p0.longitude)
//        }
//        catch (e: Exception){
//            Log.e("location", "catch block")
//        }
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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Sending some data to next screen.
     */

    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, PlotActivity::class.java).apply {
            putStringArrayListExtra("areaHectare", plotList)
            putStringArrayListExtra("areaAcres", plotAreaList)
            putExtra("area_unit", unit)
            putExtra("area_value", areaValue)
            putExtra("unique_id", unique_id)
            putExtra("total_plot", total_plot)
            putExtra("plot_number", 1)
            putExtra("FarmerId", farmerId)
            putExtra("state_id", state_ID)
            putExtra("latitude", txtLatitude.text.toString())
            putExtra("longitude", txtLongitude.text.toString())

        }
        startActivity(intent)
    }


    /**
     * Calling the District API
     */

    private fun districtAPI() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val districtModel = DistrictModel(userId.toInt())

        apiInterface.newDistrict("Bearer $token", districtModel).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                districtIDList.clear()
                districtNameList.clear()

                if (response.body() != null) {
                    districtIDList.add(0)
                    districtNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("district")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("district")

                        Log.e("name", name)

                        districtIDList.add(id)
                        districtNameList.add(name)
                    }
                    districtSpinner()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * Calling the Taluka API
     */

    private fun talukaAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var districtID: String = districtIDList[districtPosition].toString()

        apiInterface.taluka("Bearer $token", districtID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                talukaIDList.clear()
                talukaNameList.clear()

                if (response.body() != null) {
                    talukaIDList.add(0)
                    talukaNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("Taluka")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("taluka")

                        Log.e("name", name)

                        talukaIDList.add(id)
                        talukaNameList.add(name)
                    }
                    talukaSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }

        })
    }

    /**
     * Calling the Panchayat API
     */

    private fun panchayatAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var panchayatID: String = talukaIDList[talukaPosition].toString()

        apiInterface.panchayat("Bearer $token", panchayatID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                panchayatIDList.clear()
                panchayatNameList.clear()

                if (response.body() != null) {
                    panchayatIDList.add(0)
                    panchayatNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("panchayat")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("panchayat")

                        Log.e("name", name)

                        panchayatIDList.add(id)
                        panchayatNameList.add(name)
                    }
                    panchayatSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }

        })
    }

    /**
     * Calling the Village API
     */

    private fun villageAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var villageID: String = panchayatIDList[panchayatPosition].toString()

        apiInterface.village("Bearer $token", villageID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                villageIDList.clear()
                villageNameList.clear()

                if (response.body() != null) {
                    villageIDList.add(0)
                    villageNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("Village")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("village")

                        Log.e("name", name)

                        villageIDList.add(id)
                        villageNameList.add(name)
                    }
                    villageSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }

        })
    }

    /**
     * Assigning adapter to district spinner
     */

    private fun districtSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, districtNameList)
        district_spinner.adapter = adapter
        district_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                districtPosition = position
                Log.e("districtPosition", districtPosition.toString())
                if (position != 0) {
                    talukaAPI()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Assigning adapter to taluka spinner
     */

    private fun talukaSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, talukaNameList)
        taluka_spinner.adapter = adapter
        taluka_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                talukaPosition = position
                Log.e("talukaPosition", talukaPosition.toString())

                if (position != 0) {
                    panchayatAPI()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Assigning adapter to panchayat spinner
     */

    private fun panchayatSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, panchayatNameList)
        panchayat_spinner.adapter = adapter
        panchayat_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                panchayatPosition = position
                Log.e("panchayatPosition", panchayatPosition.toString())
                if (position != 0) {
                    villageAPI()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    /**
     * Assigning adapter to village spinner
     */

    private fun villageSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, villageNameList)
        village_spinner.adapter = adapter
        village_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                villagePosition = position
                Log.e("villagePosition", villagePosition.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

}