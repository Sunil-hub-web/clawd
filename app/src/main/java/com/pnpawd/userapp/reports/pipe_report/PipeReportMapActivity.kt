package com.pnpawd.userapp.reports.pipe_report

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.*
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.pnpawd.userapp.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PipeReportMapActivity: AppCompatActivity() , OnMapReadyCallback {
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var plot_no: String
    private lateinit var pipe_no: String
    private lateinit var reasons: String
    private lateinit var uniqueId: String
    private lateinit var distance: String
    private lateinit var reason_id: String
    private lateinit var pipe_img_id: String
    private lateinit var farmer_name: String
    private lateinit var farmer_uniqueId: String
    private lateinit var pipeImageLatitude: String
    private lateinit var pipeImageLongitude: String
    private lateinit var plotArea: String
    private var threshold: String = ""
    private var token: String = ""


    private lateinit var  delete: ImageView
    private lateinit var back: ImageView
    private lateinit var undo: ImageView
    private lateinit var save: ImageView
    private lateinit var edit: ImageView
    private lateinit var txtPolygon: TextView
    private lateinit var txtOld: TextView
    private lateinit var txtAccuracy: TextView
    private lateinit var linearBox: LinearLayout

    var firstLat: Double = 0.0
    var firstLng: Double = 0.0
    var polygon_area: Double = 0.0

    private var latLngArrayListPolygon = ArrayList<LatLng>()
    private var latList = ArrayList<LocationModel>()
    var nearbyPolygonList = ArrayList<LatLng>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var progress: SweetAlertDialog
    private lateinit var mMap: GoogleMap
    var polygon: Polygon? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        progress = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            pipe_img_id = bundle.getString("pipe_img_id").toString()
            farmer_uniqueId = bundle.getString("farmer_uniqueId").toString()
            farmer_name = bundle.getString("farmer_name").toString()
            uniqueId = bundle.getString("uniqueId").toString()
            pipeImageLatitude = bundle.getString("lat").toString()
            pipeImageLongitude = bundle.getString("lng").toString()
            plot_no = bundle.getString("plot_no").toString()
            pipe_no = bundle.getString("pipe_no").toString()
            distance = bundle.getString("distance").toString()
            reasons = bundle.getString("reasons").toString()
            reason_id = bundle.getString("reason_id").toString()

        } else {
            Log.e("bundle", "Nope")
        }

        save = findViewById(R.id.ivSaveLocation)
        delete = findViewById(R.id.bin)
        back = findViewById(R.id.ivBackMap)
        undo = findViewById(R.id.backArrow)
        edit = findViewById(R.id.edit)
        txtPolygon = findViewById(R.id.polygon_area)
        txtOld = findViewById(R.id.area_acres)
        txtAccuracy = findViewById(R.id.polygon_accuracy)
        linearBox = findViewById(R.id.menuLayout)

        edit.visibility = View.GONE
        delete.visibility = View.GONE
        undo.visibility = View.GONE

        txtOld.text = "$distance  acres"

        // Check if Developer Option is on
        if (BuildConfig.DEBUG) {
            Log.e("CHECKKK", "in if Debug")
        } else {
            Log.e("CHECKKK", "in else Release")
            warnAboutDevOpt()
        }



        save.setOnClickListener(View.OnClickListener {
            val minus = distance.toDouble() - threshold.toDouble()
            Log.e("minus", minus.toString())

            val add = distance.toDouble() + threshold.toDouble()
            Log.e("add", add.toString())

            if (polygon_area > minus && polygon_area < add){
                Log.e("polygon_area", polygon_area.toString())

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                postData(latLngArrayListPolygon)
            }
            else if (polygon_area > add){
                val WarningDialog = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Area drawn is more than plot \n area"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }
            else if (polygon_area < minus){
                val WarningDialog = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Area drawn is less than plot \n area"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
        })

        getThreshold()

        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@PipeReportMapActivity)
    }

    private fun warnAboutDevOpt() {
        val adb = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if(adb == 1) {
            android.app.AlertDialog.Builder(this@PipeReportMapActivity)
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
                        this@PipeReportMapActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    })
                .show()


            val WarningDialog = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.WARNING_TYPE)

            WarningDialog.titleText = "Developer options are enabled on phone."
            WarningDialog.contentText = "Disable to continue using the app."
            WarningDialog.confirmText = resources.getString(R.string.disable)
            WarningDialog.cancelText = resources.getString(R.string.close_app)
            WarningDialog.setCancelable(false)
            WarningDialog.setConfirmClickListener {
                val data = this@PipeReportMapActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                Log.e("data", data.equals(true).toString())

            }
            WarningDialog.setCancelClickListener {
                finishAffinity()
                finish()
            }.show()
        }
    }


    private fun getThreshold() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.threshold("Bearer $token").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if(response.body() != null){
                        val stringResponse = JSONObject(response.body()!!.string())
                        threshold = stringResponse.optString("threshold")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PipeReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.uiSettings.isCompassEnabled = true
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragStart(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                pointsOverlap(marker.title, marker)
            }

            override fun onMarkerDrag(marker: Marker) {}
        })

        gpsCheck()
    }


    private fun pointsOverlap(title: String?, marker: Marker) {
        val pos: Int? = title?.toInt()
        marker.position.latitude
        val lat_lng = LatLng(marker.position.latitude, marker.position.longitude)
//        val lat_lng: LatLng = latLngArrayListPolygon[pos!!]
        Log.e("latitude_longitude", lat_lng.toString())


        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val checkPolygonModel = CheckPolygonModel(farmer_uniqueId, lat_lng.latitude, lat_lng.longitude)
        apiInterface.checkCoordinates("Bearer $token", checkPolygonModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
//                    if (response.body() != null){

                        Toast.makeText(this@PipeReportMapActivity, "Point overlapping", Toast.LENGTH_SHORT).show()
                        pointOverlappingMsg()
//                    }
//                    else{
//                    }
                    resetMarkers()
                }
                else if (response.code() == 422){
                    updateMarkerOnDrag(title!!, marker, true)
                }
                else{
                    resetMarkers()
                    Log.e("CheckCoordinates", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PipeReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
                resetMarkers()
            }
        })
    }


    private fun pointOverlappingMsg() {
        val WarningDialog = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.point_overlapping_warning)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }


    private fun gpsCheck() {
        val active = isLocationEnabled(this@PipeReportMapActivity)
//        Log.e("active", active.toString())

        if (!active) {
            android.app.AlertDialog.Builder(this@PipeReportMapActivity)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        this@PipeReportMapActivity.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton(R.string.no,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        gpsCheck()
                    })
                .show()
        }
        else{
            checkLocationPermission()
        }
    }


    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                Log.e("else", "if_Small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK") { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
//                Log.e("else", "else_small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK") { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            }
        } else {
            Log.e("else", "else_big")
            getCurrentLocation()
        }
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        else{
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    mapFragment.getMapAsync(OnMapReadyCallback {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        firstLat = latLng.latitude
                        firstLng = latLng.longitude
//
//                        Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
//                        getRadiusPolygon(firstLat, firstLng)
                    })
                }
            }
            latLngArrayListPolygon.clear()
            getLocationAccuracy()
            getLastPolygons()
        }
    }


    private fun plotPolygons(latLng: ArrayList<LatLng>) {
        val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
        var mark: Marker?

        for (j in 0 until latLng.size){
            val latitude = latLng[j].latitude
            val longitude = latLng[j].longitude
            mark = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth, pickupMarkerDrawable.intrinsicHeight,
                    null))).draggable(true).title(j.toString()))
            mark?.tag = j.toString()
        }

        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(latLng)
        polygonOptions.strokeColor(Color.BLACK)
        polygonOptions.strokeWidth(5f)
        polygonOptions.fillColor(0x33FF0000)
        polygon = mMap.addPolygon(polygonOptions)

        calculateDistance(latLngArrayListPolygon)
        nearbyPolygon()
    }


    private fun updateMarkerOnDrag(latLng: String, marker: Marker, ready: Boolean) {
        val pos: Int = latLng.toInt()
        latLngArrayListPolygon.removeAt(pos)
        latLngArrayListPolygon.add(pos, LatLng(marker.position.latitude, marker.position.longitude))

        if (ready){
            mMap.clear()

            plotPolygons(latLngArrayListPolygon)
        }
    }


    private fun resetMarkers() {
        mMap.clear()

        val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_location_marker,null)
        var mark: Marker?

        for (j in 0 until latLngArrayListPolygon.size){
            val latitude = latLngArrayListPolygon[j].latitude
            val longitude = latLngArrayListPolygon[j].longitude
            mark = mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth, pickupMarkerDrawable.intrinsicHeight, null)))
                .draggable(true).title(j.toString()))
            mark?.tag = j.toString()
        }

        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(latLngArrayListPolygon)
        polygonOptions.strokeColor(Color.BLACK)
        polygonOptions.strokeWidth(5f)
        polygonOptions.fillColor(0x33FF0000)
        polygon = mMap.addPolygon(polygonOptions)

        calculateDistance(latLngArrayListPolygon)
        nearbyPolygon()


//        plotPolygons(latLngArrayListPolygon)
    }


    @SuppressLint("SetTextI18n")
    private fun calculateDistance(latLngArrayListPolygon: ArrayList<LatLng>) {
// Calculating meters from polygon list
        val m = SphericalUtil.computeArea(latLngArrayListPolygon)

// converting meters to acers
        val df = DecimalFormat("#.#####")
        polygon_area = df.format(m * 0.000247105).toDouble()
        txtPolygon.text = "$polygon_area  acres"
    }

    @SuppressLint("SetTextI18n")
    private fun calculateDistance2(latLngArrayListPolygon: ArrayList<LatLng>) {
// Calculating meters from polygon list
        val m = SphericalUtil.computeArea(latLngArrayListPolygon)

// converting meters to acers
        val df = DecimalFormat("#.#####")
        polygon_area = df.format(m * 0.000247105).toDouble()
        txtAccuracy.text = polygon_area.toString() + "  acers"
    }


    private fun getLastPolygons() {
        val farmerUniqueIdModel = FarmerUniqueIdModel(farmer_uniqueId)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.pipeGetPolygon("Bearer $token", farmerUniqueIdModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){

                    if (response.body() != null) {
                        val jsonArray = JSONArray(response.body()!!.string())

                        for (i in 0 until jsonArray.length()){
                            val jsonObject = jsonArray.getJSONObject(i)
                            val latitude = jsonObject.getDouble("lat")
                            val longitude = jsonObject.getDouble("lng")

                            val latlng = LatLng(latitude, longitude)
                            latLngArrayListPolygon.add(latlng)
                        }
                        plotPolygons(latLngArrayListPolygon)
                        calculateDistance2(latLngArrayListPolygon)
                    }
                    else if(response.body() == null){
                        progress.dismiss()
                    }
                }
                else{
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@PipeReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun nearbyPolygon() {
        if (firstLat == 0.0 && firstLat == 0.0){
            getCurrentLocation()
        }
        else{
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.polygonNearby("Bearer $token", farmer_uniqueId, 18.185524019727975, 78.95679194480181).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null) {
                        progress.dismiss()

                        val jsonArray = JSONArray(response.body()!!.string())
                        for (i in 0 until jsonArray.length()){

                            var innerArray = jsonArray.getJSONArray(i)
                            for (k in 0 until innerArray.length()){

                                val innerObject = innerArray.getJSONObject(k)
                                val rangeArray = innerObject.getJSONArray("ranges")

                                for (j in 0 until rangeArray.length()){
                                    val jsonObject1 = rangeArray.getJSONObject(j)

                                    val latitude = jsonObject1.optString("lat").toDouble()
                                    val longitude = jsonObject1.optString("lng").toDouble()

                                    val latLng = LatLng(latitude, longitude)
                                    nearbyPolygonList.add(latLng)
                                }
                                Log.e("nearbyPolygon", nearbyPolygonList.toString())

                                val polygonOptions = PolygonOptions()

                                for (j in 0 until nearbyPolygonList.size){
                                    val latitude = nearbyPolygonList[j].latitude
                                    val longitude = nearbyPolygonList[j].longitude

                                    polygonOptions.add(LatLng(latitude,longitude))
                                    polygonOptions.strokeColor(Color.CYAN)
                                    polygonOptions.strokeWidth(4f)
                                    polygonOptions.fillColor(Color.CYAN)
                                    val polygon: Polygon = mMap.addPolygon(polygonOptions)
                                }
                                nearbyPolygonList.clear()
                            }
                        }
                    }
                    else if(response.body() == null){
                        progress.dismiss()
                    }
                }
                else{
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@PipeReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData(latLngArrayListPolygon: java.util.ArrayList<LatLng>) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val currentTime = current.format(formatter)

        val area: Double = polygon_area

        for (i in latLngArrayListPolygon.indices){
            var locationModel = LocationModel(latLngArrayListPolygon[i].latitude.toString(), latLngArrayListPolygon[i].longitude.toString())
            latList.add(locationModel)
        }

        val updatePolygonModel = UpdatePolygonModel(farmer_uniqueId, area, currentTime.toString(), latList)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.updatePipePolygon("Bearer $token", updatePolygonModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null) {
                        progress.dismiss()
                        nextScreen()
                    }
                    else if(response.body() == null){
                        progress.dismiss()
                    }
                }
                else if(response.code() == 422) {
                    Log.e("bundle", response.errorBody().toString())
                    Toast.makeText(this@PipeReportMapActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                }
                else{
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PipeReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@PipeReportMapActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = "Entry updated successfully."
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



    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationAccuracy() {
        val startingNumber = 20
        var currentNumber = startingNumber

        lifecycleScope.launch {
            while (currentNumber >= 3) {
                println(currentNumber)
                currentNumber--

                delay(2000) // Pause for 2 seconds
                txtAccuracy.text = "Accuracy : $currentNumber meters"
            }
        }

    }
}