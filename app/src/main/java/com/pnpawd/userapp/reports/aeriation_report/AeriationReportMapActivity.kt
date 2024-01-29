package com.pnpawd.userapp.reports.aeriation_report

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.PolyUtil
import com.pnpawd.userapp.R
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class AeriationReportMapActivity: AppCompatActivity() , OnMapReadyCallback, LocationListener {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location
    private lateinit var mapFragment: SupportMapFragment
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99

    private var latlngList = ArrayList<LatLng>()
    private var pipeLatitude = 0.0
    private var pipeLongitude = 0.0
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var xxxxx: Int = 0
    lateinit var polygon: Polygon

    private var token: String = ""


    private lateinit var next: FloatingActionButton
    private lateinit var back: ImageView
    private lateinit var farmer_plot_uniqueid: String
    private lateinit var pipe_installation_id: String
    private lateinit var pipe_no: String
    private lateinit var unique_id: String
    private lateinit var aeration_no: String
    private lateinit var plot_no: String


    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeration_map)
        progress = SweetAlertDialog(this@AeriationReportMapActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid").toString()
            pipe_no = bundle.getString("pipe_no")!!
            plot_no = bundle.getString("plot_no")!!
            unique_id = bundle.getString("unique_id")!!
            aeration_no = bundle.getString("aeration_no")!!
            pipe_installation_id = bundle.getString("pipe_installation_id")!!
        }

        back = findViewById(R.id.aeriation_map_back)
        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        next = findViewById(R.id.fabNext)
        next.setOnClickListener(View.OnClickListener {
            xxxxx = 1

            progress.progressHelper.barColor = Color.parseColor("#06c238")
            progress.titleText = "Checking"
            progress.contentText = " Checking location !!"
            progress.setCancelable(false)
            progress.show()

            locationReq()
        })


        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@AeriationReportMapActivity)
        getLocationList()
    }


    private fun getLocationList() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.aeriationPloygonList("Bearer $token", farmer_plot_uniqueid, pipe_no).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("polygon")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val lat = jsonObject.optString("lat")
                            val lng = jsonObject.optString("lng")

                            var latLng = LatLng(lat.toDouble(), lng.toDouble())
                            latlngList.add(latLng)
                        }

                        val pipeLocationArray =  stringResponse.optJSONObject("PipeLocation")
                        pipeLatitude = pipeLocationArray.getDouble("lat")
                        pipeLongitude = pipeLocationArray.getDouble("lng")
                    }

                    plotPolygons()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AeriationReportMapActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun plotPolygons() {
        var polygonOptions = PolygonOptions()

        for (i in latlngList.indices) if (i == 0) {
            polygonOptions = PolygonOptions().add(latlngList[0])
        } else {
            mMap.clear()
            polygonOptions.add(latlngList[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until latlngList.size){
            mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latlngList[i].latitude, latlngList[i].longitude))
                .icon(bitmapDescriptorFromVector(this@AeriationReportMapActivity, R.drawable.ic_location_marker)))
        }


        mMap.addMarker(
            MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(pipeLatitude, pipeLongitude))
            .icon(bitmapDescriptorFromVector(this@AeriationReportMapActivity, R.drawable.ic_plot_marker)))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return

            askForPermission()
        }
        else {

            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                requestNewLocationData()
            }
        }
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), MY_PERMISSIONS_REQUEST_LOCATION)
    }

    private fun requestNewLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync(OnMapReadyCallback {
                    var latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

                    Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
                })
            }
        }
        locationReq()
    }


    private fun locationReq() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,0)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdateDelayMillis(5000)
            .build()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())

            currentLatitude = mLastLocation!!.latitude
            currentLongitude = mLastLocation!!.longitude
            stop()
        }
    }

    private fun stop() {
        Log.e("Stopped", "Location Update Stopped")
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)

        calculate ()
    }


    private fun calculate () {
        val cond = PolyUtil.containsLocation(LatLng(currentLatitude, currentLongitude), latlngList, false)
        if (xxxxx == 1) {
            if(cond){
            var latlng = ArrayList<String>()

            for (i in 0 until latlngList.size) {
                val lat = latlngList[i].latitude
                val lng = latlngList[i].longitude
                val ll = LatLng(lat, lng).toString()

                latlng.add(ll)
            }

            progress.dismiss()
            val intent = Intent(this, AeriationReportImageActivity::class.java).apply {
                putStringArrayListExtra("latlngList", latlng)
                putExtra("farmer_plot_uniqueid", farmer_plot_uniqueid)
                putExtra("pipe_installation_id", pipe_installation_id)
                putExtra("latitude", pipeLatitude)
                putExtra("longitude", pipeLongitude)
                putExtra("aeration_no", aeration_no)
                putExtra("unique_id", unique_id)
                putExtra("pipe_no", pipe_no)
                putExtra("plot_no", plot_no)
            }
            startActivity(intent)
            }
            else{
                progress.dismiss()

                val WarningDialog = SweetAlertDialog(this@AeriationReportMapActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "You are not inside the marked polygon."
                WarningDialog.confirmText = " OK "
                WarningDialog.showCancelButton(false)
                WarningDialog.setCancelable(false)
                WarningDialog.setConfirmClickListener {
                    WarningDialog.cancel()
                }.show()
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20f))
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}