package com.pnpawd.userapp.pipeinstallation.Submitted

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.LocationModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.pipeinstallation.LandInfoPreviewActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.pnpawd.userapp.BuildConfig
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

class LandInfoSubmittedActivity : AppCompatActivity(), OnMapReadyCallback{
    private var LATLNG = ArrayList<LatLng>()
    private var Polygon_lat_lng = ArrayList<String>()
    private val latLngslist = ArrayList<LatLng>()
    private var latList = ArrayList<LocationModel>()
    private var lngList = ArrayList<String>()
    var polygon: Polygon? = null

    private lateinit var btnNext: Button
    private lateinit var btnBack : Button

    private lateinit var txtLatitude: TextView
    private lateinit var txtLongitude: TextView
    private lateinit var txtState: TextView
    private lateinit var txtDistrict: TextView
    private lateinit var txtTaluka: TextView
    private lateinit var txtVillage: TextView
    private lateinit var txtUnit: TextView
    private lateinit var txtArea: TextView

    private var LAT = ArrayList<Double>()
    private var LNG = ArrayList<Double>()
    private var area: String = ""
    private var unique_id: String = ""
    private var sub_plot_no: String = ""
    private var farmer_id: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var state: String = ""
    private var district: String = ""
    private var taluka: String = ""
    private var village: String = ""
    private var khasara_no: String = ""
    private var acers_units: String = ""
    private var token: String = ""

    private lateinit var mMap: GoogleMap
    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_info)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        btnBack = findViewById(R.id.pipe_land_back)
        btnNext = findViewById(R.id.pipe_land_next)

        txtLatitude = findViewById(R.id.pipe_land_latitude)
        txtLongitude = findViewById(R.id.pipe_land_longitude)
        txtState = findViewById(R.id.pipe_state)
        txtDistrict = findViewById(R.id.pipe_district)
        txtTaluka = findViewById(R.id.pipe_taluka)
        txtVillage = findViewById(R.id.pipe_village)
        txtUnit = findViewById(R.id.pipe_area_unit)
        txtArea = findViewById(R.id.pipe_area)

        progress = SweetAlertDialog(this@LandInfoSubmittedActivity, SweetAlertDialog.PROGRESS_TYPE)

        val bundle = intent.extras
        if (bundle != null) {
            area = bundle.getString("plot_area")!!
            unique_id = bundle.getString("farmer_uniqueId")!!
            sub_plot_no = bundle.getString("plot_no")!!
            farmer_id = bundle.getString("farmer_id")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!
            state = bundle.getString("state")!!
            district = bundle.getString("district")!!
            taluka = bundle.getString("taluka")!!
            village = bundle.getString("village")!!
            khasara_no = bundle.getString("khasara_no")!!
            acers_units = bundle.getString("acers_units")!!
            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!

            txtLatitude.text = latitude
            txtLongitude.text = longitude
            txtState.text = state
            txtDistrict.text = district
            txtTaluka.text = taluka
            txtVillage.text = village
            txtUnit.text = acers_units
            txtArea.text = area
        } else {
            Log.e("area", "Nope")
        }

        btnBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        // Check if Developer Option is on
        if (BuildConfig.DEBUG) {
            Log.e("CHECKKK", "in if Debug")
        } else {
            Log.e("CHECKKK", "in else Release")
            warnAboutDevOpt()
        }



        btnNext.setOnClickListener(View.OnClickListener {
            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = resources.getString(R.string.loading)
            progress.contentText = resources.getString(R.string.data_send)
            progress.setCancelable(false)
            progress.show()


            checkData()

        })

        for(i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.]".toRegex(), "")
            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()

            LATLNG.add(LatLng(lat, lng))
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.pipe_land_googleMapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val centerLatLng =  computeCentroid(LATLNG)
        Log.e("centerLatLng", centerLatLng.toString())
    }

    private fun warnAboutDevOpt() {
        val adb = Settings.Secure.getInt(this.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if(adb == 1) {
            AlertDialog.Builder(this@LandInfoSubmittedActivity)
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
                        this@LandInfoSubmittedActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    })
                .show()
        }
    }

    private fun checkData() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkPipeStatus("Bearer $token", unique_id, sub_plot_no).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if(response.body() != null){
                        val jsonObj = JSONObject(response.body()!!.string())
                        val status = jsonObj.optInt("status")
                        if (status == 0){
                            nextScreen()
                        }
                        else{
                            submittedScreen()
                        }
                    }
                }
                else {
                    progress.dismiss()
                    nextScreen()
                    Log.e("area", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }

        })
    }


    private fun computeCentroid(points: List<LatLng>): LatLng {
        var latitude = 0.0
        var longitude = 0.0
        val n = points.size
        for (point in points) {
            latitude += point.latitude
            longitude += point.longitude
        }
        return LatLng(latitude / n, longitude / n)
    }

    private fun submittedScreen() {
        progress.dismiss()

        val intent = Intent(this, LandInfoSubmittedPreviewActivity::class.java).apply {
            putStringArrayListExtra("locationList", Polygon_lat_lng)
            putExtra("farmer_id", farmer_id)
            putExtra("unique_id", unique_id)
            putExtra("sub_plot_no", sub_plot_no)
            putExtra("area", area)
        }
        startActivity(intent)
    }

    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, LandInfoPreviewActivity::class.java).apply {
            putStringArrayListExtra("locationList", Polygon_lat_lng)
            putExtra("farmer_id", farmer_id)
            putExtra("unique_id", unique_id)
            putExtra("sub_plot_no", sub_plot_no)
            putExtra("area", area)
        }
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true

        val dfgdg: String =  Polygon_lat_lng[0].replace("[^0-9,.]".toRegex(), "")
        val lat: Double = dfgdg.split(",").first().toDouble()
        val lng: Double = dfgdg.split(",").last().toDouble()
        val currentLocation = LatLng(lat, lng)

        mMap.addMarker(MarkerOptions().position(currentLocation).visible(false))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f), 2000, null)


        for (i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.]".toRegex(), "")

            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()

            Log.e("area", lat.toString())
            Log.e("area", lng.toString())

            latLngslist.add(LatLng(lat, lng))

            LAT.add(lat)
            LNG.add(lng)
        }

        var polygonOptions = PolygonOptions()
        for (i in latLngslist.indices) if (i == 0) {
            polygonOptions = PolygonOptions().add(latLngslist[0])
        } else {
            mMap.clear()
            polygonOptions.add(latLngslist[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until LAT.size){
            mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(LAT[i], LNG[i])))
        }

        mMap.setMinZoomPreference(15f)
        mMap.getUiSettings().setZoomGesturesEnabled(false)
        mMap.uiSettings.isScrollGesturesEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}