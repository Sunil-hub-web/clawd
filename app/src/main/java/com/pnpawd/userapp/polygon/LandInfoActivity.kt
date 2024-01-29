package com.pnpawd.userapp.polygon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.R
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.models.LocationModel
import com.pnpawd.userapp.models.PipeLocationModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LandInfoActivity : AppCompatActivity(), OnMapReadyCallback {
    private var LATLNG = ArrayList<LatLng>()
    private var Polygon_lat_lng = ArrayList<String>()
    private var LocationList = ArrayList<LatLng>()
    private val latLngslist = ArrayList<LatLng>()
    private var latList = ArrayList<LocationModel>()
    private var lngList = ArrayList<String>()
    var polygon: Polygon? = null

    var token: String = ""
    private var village: String = ""
    private var state: String = ""
    private var district: String = ""
    private var country: String = ""
    private var area: String = ""
    private var unique_id: String = ""
    private var sub_plot_no: String = ""
    private var farmer_id: String = ""
    private var farmer_name: String = ""
    private var farmer_plot_uniqueid: String = ""
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var polygon_area: Double = 0.0

    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    private lateinit var txtLatitude: TextView
    private lateinit var txtLongitude: TextView
    private lateinit var txtState: TextView
    private lateinit var txtDistrict: TextView
    private lateinit var txtTaluka: TextView
    private lateinit var txtVillage: TextView
    private lateinit var txtUnit: TextView
    private lateinit var txtArea: TextView

    private var polygon_date_time: String = ""

    private lateinit var mMap: GoogleMap
    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_info)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

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

        progress = SweetAlertDialog(this@LandInfoActivity, SweetAlertDialog.PROGRESS_TYPE)

        val bundle = intent.extras
        if (bundle != null) {
            Polygon_lat_lng = bundle.getStringArrayList("locationList")!!
            LocationList = convertStringListToLatLngList(Polygon_lat_lng)
            Log.e("stringList", LocationList.toString())

            area = bundle.getString("area").toString()
            unique_id = bundle.getString("unique_id")!!
            sub_plot_no = bundle.getString("sub_plot_no")!!
            polygon_area = bundle.getDouble("polygon_area")
            farmer_id = bundle.getString("farmer_id")!!
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid")!!
            polygon_date_time = bundle.getString("polygon_date_time")!!
            farmer_name = bundle.getString("farmer_name")!!
            txtLatitude.text = LocationList[0].latitude.toString()
            txtLongitude.text = LocationList[0].longitude.toString()
            txtArea.text = polygon_area.toString()
            getAddress(txtLatitude.text.toString(), txtLongitude.text.toString())
        } else {
            Log.e("total_plot", "Nope")
        }

        txtUnit.text = "Acers"

        btnBack.setOnClickListener {
            super.onBackPressed()
            finish()
        }

        btnNext.setOnClickListener {
            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = resources.getString(R.string.loading)
            progress.contentText = resources.getString(R.string.data_send)
            progress.setCancelable(false)
            progress.show()

            sendData()
        }


        for (i in 0 until LocationList.size) {
            val lat = LocationList[i].latitude
            val lng = LocationList[i].longitude
            LATLNG.add(LatLng(lat, lng))
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.pipe_land_googleMapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val centerLatLng = computeCentroid(LATLNG)
        Log.e("centerLatLng", centerLatLng.toString())
    }

    private fun convertStringListToLatLngList(stringList: ArrayList<String>): ArrayList<LatLng> {
        val latLngList = ArrayList<LatLng>()

// Map each string to a LatLng object
        for (str in stringList) {
            val latLngParts = str.split(",") // Assuming the string format is "latitude,longitude"
            if (latLngParts.size == 2) {
                val lat = latLngParts[0].toDouble()
                val lng = latLngParts[1].toDouble()
                val latLng = LatLng(lat, lng)
                latLngList.add(latLng)
            }
        }
        Log.e("stringList", latLngList.toString())
        return latLngList
    }

    private fun sendData() {

        for (i in Polygon_lat_lng.indices) {
            val locationModel = LocationModel(
                LocationList[i].latitude.toString(),
                LocationList[i].longitude.toString()
            )
            latList.add(locationModel)
        }

        val pipeLocationModel = PipeLocationModel(
            farmer_id,
            unique_id,
            sub_plot_no,
            txtLatitude.text.toString(),
            txtLongitude.text.toString(),
            txtState.text.toString(),
            txtDistrict.text.toString(),
            txtTaluka.text.toString(),
            txtVillage.text.toString(),
            txtUnit.text.toString(),
            polygon_area.toString(),
            latList,
            farmer_plot_uniqueid,
            polygon_date_time
        )


        Log.e("DATA", pipeLocationModel.toString())


        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.sendPipeData("Bearer $token", pipeLocationModel)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        progress.dismiss()
                        nextScreen()
                    } else if (response.code() == 422) {
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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

    private fun nextScreen() {
        progress.dismiss()
        val SuccessDialog = SweetAlertDialog(this@LandInfoActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data Submitted Successfully. "
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


    private fun getAddress(latitude: String, longitude: String) {
// Initializing Geocoder
        var addresses: List<Address>? = null
        val geocoder: Geocoder = Geocoder(this@LandInfoActivity)

        try {
// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addresses = geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        try {
            village = if (addresses!![0].locality != null) addresses[0].locality else " "
            state = if (addresses[0].adminArea != null) addresses[0].adminArea else " "
            district = if (addresses[0].subAdminArea != null) addresses[0].subAdminArea else " "
            country = if (addresses[0].countryName != null) addresses[0].countryName else " "

            Log.e("village", village)
            Log.e("state", state)
            Log.e("district", district)
            Log.e("country", country)

            txtVillage.text = village
            txtState.text = state
            txtDistrict.text = district
            txtTaluka.text = district

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false

        mMap.clear()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true

        lat = LocationList[0].latitude
        lng = LocationList[0].longitude
        val currentLocation = LatLng(lat, lng)

        mMap.addMarker(MarkerOptions().position(currentLocation).visible(false))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f), 2000, null)

        val pointX: ArrayList<Double> = ArrayList()
        val pointY: ArrayList<Double> = ArrayList()

        for (i in LocationList.indices) {
            pointX.add(LocationList[i].latitude)
            pointY.add(LocationList[i].longitude)
        }

        for (i in pointX.indices) {
            latLngslist.add(LatLng(pointX[i], pointY[i]))
            mMap.addMarker(
                MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(pointX[i], pointY[i]))
            )
        }

        val polygonOptions = PolygonOptions()
        for (i in latLngslist.indices) {
            polygonOptions.add(latLngslist[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.TRANSPARENT)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        mMap.uiSettings.setAllGesturesEnabled(false)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}